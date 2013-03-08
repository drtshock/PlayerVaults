package com.drtshock.playervaults;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;
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

	@Override
	public void onEnable() {
		loadLang();
		log = getServer().getLogger();
		getServer().getPluginManager().registerEvents(new Listeners(this), this);
		loadConfig();
		startMetrics();

		Updater u = new Updater();
		try {
			if(u.getUpdate()) {
				update = true;
				name = u.getNewVersion();
			}
		} catch (Exception e) {}
		commands = new Commands();
		getCommand("pv").setExecutor(commands);
		getCommand("pvdel").setExecutor(commands);
	}

	public void startMetrics() {
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	
	/**
	 * Methods to get values from the config.
	 * public so any class / plugin can get them.
	 */

	public boolean updateCheck() {
		return getConfig().getBoolean("check-update");
	}

}
