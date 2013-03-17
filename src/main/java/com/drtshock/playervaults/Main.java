package com.drtshock.playervaults;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.drtshock.playervaults.commands.Commands;
import com.drtshock.playervaults.util.Lang;
import com.drtshock.playervaults.util.Metrics;
import com.drtshock.playervaults.util.Updater;


public class Main extends JavaPlugin {

	public Main plugin;
	public Logger log;
	public static boolean update = false;
	public static String name = "";
	Commands commands;
	public static Economy econ = null;

	@Override
	public void onEnable() {
		loadLang();
		log = getServer().getLogger();
		getServer().getPluginManager().registerEvents(new Listeners(this), this);
		loadConfig();
		startMetrics();

		if(getConfig().getBoolean("check-update")) {
			Updater u = new Updater();
			try {
				if(u.getUpdate()) {
					update = true;
					name = u.getNewVersion();
				}
			} catch (Exception e) {}
		}

		commands = new Commands();
		getCommand("pv").setExecutor(commands);
		getCommand("pvdel").setExecutor(commands);
		setupEconomy();
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
		return econ != null;
	}

	public void loadConfig() {
		File config = new File(getDataFolder() + File.separator + "config.yml");
		if(!config.exists()) {
			saveDefaultConfig();
		}
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
		return conf;
	}
}
