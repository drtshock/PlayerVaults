package com.drtshock.playervaults;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.drtshock.playervaults.commands.Commands;
import com.drtshock.playervaults.util.BackwardsCompatibility;
import com.drtshock.playervaults.util.Lang;
import com.drtshock.playervaults.util.Metrics;
import com.drtshock.playervaults.util.Serialization;
import com.drtshock.playervaults.util.Updater;


public class Main extends JavaPlugin {

	public static Main plugin;
	public Logger log;
	public static boolean update = false;
	public static String name = "";
	Commands commands;
	public static Economy econ = null;
	public static boolean dropOnDeath = false;
	public static int inventoriesToDrop = 0;
	public static boolean useVault = false;
	public static YamlConfiguration lang;
	public static File langFile;
	public static String directory = "plugins" + File.separator + "PlayerVaults" + File.separator + "vaults";


	@Override
	public void onEnable() {
		try {
			transferVaults();
		} catch (IOException e) {
			log.log(Level.SEVERE, "PlayerVaults: Failed to check to transfer vaults.");
			log.log(Level.SEVERE, "PlayerVaults: Report this stack trace to drtshock and gomeow.");
			e.printStackTrace();
		}
		loadLang();
		log = getServer().getLogger();
		getServer().getPluginManager().registerEvents(new Listeners(this), this);
		loadConfig();
		startMetrics();
		Updater u = new Updater(getDescription().getVersion());
		if(getConfig().getBoolean("check-update")) {
			try {
				if(u.getUpdate()) {
					update = true;
					name = u.getNewVersion();
				}
			} catch (Exception e) {
				log.log(Level.WARNING, "PlayerVaults: Failed to check for updates.");
				log.log(Level.WARNING, "PlayerVaults: Report this stack trace to drtshock and gomeow.");
				e.printStackTrace();
			}
		}

		commands = new Commands();
		getCommand("pv").setExecutor(commands);
		getCommand("pvdel").setExecutor(commands);
		setupEconomy();

		if(getConfig().getBoolean("drop-on-death.enabled")) {
			dropOnDeath = true;
			inventoriesToDrop = getConfig().getInt("drop-on-death.inventories");
		}

		new File(directory + File.separator + "backups").mkdirs();

	}

	public void transferVaults() throws IOException {
		File f = new File(getDataFolder() + File.separator + "vaults.yml");
		if(f.exists() && !new File(getDataFolder() + File.separator + "vaults").exists()) {
			YamlConfiguration vaults = YamlConfiguration.loadConfiguration(f);
			for(String person:vaults.getKeys(false)) {
				YamlConfiguration yaml = new YamlConfiguration();
				for(String vault:vaults.getConfigurationSection(person).getKeys(false)) {
					String data = vaults.getString(person + "." + vault);
					Inventory inv = BackwardsCompatibility.pre2_0_0ToCurrent(data);
					List<String> list = Serialization.toString(inv);
					String[] ser = list.toArray(new String[list.size()]);
					for(int x = 0; x < ser.length; x++) {
						if(!ser[x].equalsIgnoreCase("null"))
							yaml.set(vault + "." + x, ser[x]);
					}
				}
				yaml.save(new File(directory + File.separator + person + ".yml"));
			}
			getLogger().warning("Found old storage format used! Converting to new format!");
		}
	}

	public void startMetrics() {
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		useVault = true;
		return econ != null;
	}

	public void loadConfig() {
		File config = new File(getDataFolder() + File.separator + "config.yml");
		if(!config.exists()) {
			saveDefaultConfig();
		} else {
			updateConfig();
		}
	}

	public void updateConfig() {
		boolean checkUpdate = getConfig().getBoolean("check-update", true);
		boolean ecoEnabled = getConfig().getBoolean("economy.enabled", false);
		int ecoCreate = getConfig().getInt("economy.cost-to-create", 100);
		int ecoOpen = getConfig().getInt("economy.cost-to-open", 10);
		int ecoDelete = getConfig().getInt("economy.refund-on-delete", 50);
		boolean dropEnabled = getConfig().getBoolean("drop-on-death.enabled", false);
		int dropInvs = getConfig().getInt("drop-on-death.inventories", 50);
		new File("plugins" + File.separator + "PlayerVaults" + File.separator + "config.yml").delete();
		saveDefaultConfig();
		setInConfig("check-update", checkUpdate);
		setInConfig("economy.enabled", ecoEnabled);
		setInConfig("economy.cost-to-create", ecoCreate);
		setInConfig("economy.cost-to-open", ecoOpen);
		setInConfig("economy.refund-on-delete", ecoDelete);
		setInConfig("drop-on-death.enabled", dropEnabled);
		setInConfig("drop-on-death.inventories", dropInvs);
		saveConfig();
	}
	
	public <T> void setInConfig(String path, T object) {
		getConfig().set(path, object);
	}

	public YamlConfiguration loadLang() {
		File lang = new File(getDataFolder(), "lang.yml");
		if(!lang.exists()) {
			try{
				getDataFolder().mkdir();
				lang.createNewFile();
				InputStream defConfigStream = this.getResource("lang.yml");
				if (defConfigStream != null) {
					YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
					defConfig.save(lang);
					Lang.setFile(defConfig);
					return defConfig;
				}
			} catch (IOException e) {
				e.printStackTrace(); //So they notice
				log.severe("[PlayerVaults] Couldn't create language file.");
				log.severe("[PlayerVaults] This is a fatal error. Now disabling");
				this.setEnabled(false); //Without it loaded, we can't send them messages
			}	
		}
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
		Lang.setFile(conf);
		Main.lang = conf;
		Main.langFile = lang;
		return conf;
	}

	public YamlConfiguration getLang() {
		return lang;
	}

	public File getLangFile() {
		return langFile;
	}
}
