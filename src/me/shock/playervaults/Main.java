package me.shock.playervaults;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import me.shock.playervaults.Listeners;
import me.shock.playervaults.util.Metrics;
import me.shock.playervaults.util.Updater;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {

	public Main plugin;
	public Logger log;
	public static boolean update = false;
	public static String name = "";
	Commands commands = new Commands();

	public void onEnable() 
	{
		log = getServer().getLogger();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new Listeners(this), this);
		getCommand("pv").setExecutor(new Commands());
		loadConfig();
		loadLang();
		startMetrics();

		if(updateCheck())
		{
			Updater updater = new Updater(this, "playervaults", this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
			update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE; 
			name = updater.getLatestVersionString();
		}
	}

	public void onDisable() 
	{
		//saveData();
	}


	public void startMetrics()
	{
		try
		{
			Metrics metrics = new Metrics(this);
			metrics.start();
		}
		catch (IOException localIOException)
		{
			localIOException.printStackTrace();
		}
	}
	
	public void loadConfig()
	{
		/**
		 * Check to see if there's a config.
		 * If not then create a new one.
		 */
		File config = new File(getDataFolder() + File.separator + "config.yml");
		if(!config.exists())
		{
			try{
				getDataFolder().mkdir();
				config.createNewFile();
			} catch (IOException e) {
				Log.error("[PlayerVaults] Couldn't create config");
			}
			/**
			 * Write the config file here.
			 * New, genius way to write it :)
			 */
			try {
				FileOutputStream fos = new FileOutputStream(new File(getDataFolder() + File.separator + "config.yml"));
				InputStream is = getResource("config.yml");
				byte[] linebuffer = new byte[4096];
				int lineLength = 0;
				while((lineLength = is.read(linebuffer)) > 0)
				{
					fos.write(linebuffer, 0, lineLength);
				}
				fos.close();
			} catch (IOException e) {
				Log.error("[PlayerVaults] Couldn't write config: " + e);
			}	
		}
	}

	public void loadLang()
	{
		/**
		 * Check to see if there's a config.
		 * If not then create a new one.
		 */
		File lang = new File(getDataFolder() + File.separator + "lang.yml");
		if(!lang.exists())
		{
			try{
				getDataFolder().mkdir();
				lang.createNewFile();
			} catch (IOException e) {
				Log.error("[PlayerVaults] Couldn't create language file.");
			}
			/**
			 * Write the config file here.
			 * New, genius way to write it :)
			 */
			try {
				FileOutputStream fos = new FileOutputStream(new File(getDataFolder() + File.separator + "lang.yml"));
				InputStream is = getResource("lang.yml");
				byte[] linebuffer = new byte[4096];
				int lineLength = 0;
				while((lineLength = is.read(linebuffer)) > 0)
				{
					fos.write(linebuffer, 0, lineLength);
				}
				fos.close();
			} catch (IOException e) {
				Log.error("[PlayerVaults] Couldn't write Language file: " + e);
			}	
		}
	}
	
	private YamlConfiguration lang() {
		File file = new File(getDataFolder() + "/lang.yml");
		YamlConfiguration lang = YamlConfiguration.loadConfiguration(file);
		return lang;
	}
	
	/**
	 * Methods to get values from the config.
	 * public so any class / plugin can get them.
	 */

	/**
	 * 
	 * @return updateCheck
	 */
	public boolean updateCheck() {
		return getConfig().getBoolean("check-update");
	}

	public boolean debugMode() {
		return getConfig().getBoolean("debug-mode");
	}

	/**
	 * 
	 * @return disabled worlds.
	 */
	public List<?> disabledWorlds() {
		return getConfig().getList("disabled-worlds");
	}

	/**
	 * Values for the lang.yml
	 */

	/**
	 * 
	 * @return title used in all messages.
	 */
	public String title() {
		return lang().getString("title-name");
	}

	/**
	 * 
	 * @return string for opening vault.
	 */
	public String openVault() {
		return lang().getString("open-vault");
	}

	/**
	 * 
	 * @return string for opening someone else's vault.
	 */
	public String openOtherVault() {
		return lang().getString("open-other-vault");
	}

	/**
	 * 
	 * @return string for invalid args.
	 */
	public String invalidArgs() {
		return lang().getString("invalid-args");
	}

	/**
	 * 
	 * @return string for deleting a vault.
	 */
	public String deleteVault() {
		return lang().getString("delete-vault");
	}

	/**
	 * 
	 * @return string for deleting someone else's vault.
	 */
	public String deleteOtherVault() {
		return lang().getString("delete-other-vault");
	}

	
	public HashMap<?, ?> inVault() {
		return commands.inVault;
	}
	
	public Logger getLog() {
		return getServer().getLogger();
	}

}
