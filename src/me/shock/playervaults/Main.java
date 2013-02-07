package me.shock.playervaults;


import java.io.IOException;
import java.util.logging.Logger;

import me.shock.playervaults.Listeners;
import me.shock.playervaults.util.Config;
import me.shock.playervaults.util.Metrics;
import me.shock.playervaults.util.Updater;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {

	public Main plugin;
	public Logger log;
	Config config = new Config();
	public static boolean update = false;
	public static String name = "";
	
	public void onEnable() 
	{
		log = getServer().getLogger();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new Listeners(this), this);
		getCommand("pv").setExecutor(new Commands());
		config.loadConfig();
		config.loadLang();
		startMetrics();
		
		if(config.updateCheck())
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

}
