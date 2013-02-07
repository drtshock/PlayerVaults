package me.shock.playervaults.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;

import me.shock.playervaults.Main;

public class Config 
{

	private Main plugin;
	
	public void loadConfig()
	{
	  Logger log = plugin.getServer().getLogger();

		/**
		 * Check to see if there's a config.
		 * If not then create a new one.
		 */
		File config = new File(plugin.getDataFolder() + "/config.yml");
		if(!config.exists())
		{
			try{
				plugin.getDataFolder().mkdir();
				config.createNewFile();
			} catch (IOException e) {
				log.log(Level.SEVERE, "[PlayerVaults] Couldn't create config");
			}
			/**
			 * Write the config file here.
			 * New, genius way to write it :)
			 */
			try {
				FileOutputStream fos = new FileOutputStream(new File(plugin.getDataFolder() + File.separator + "config.yml"));
				InputStream is = plugin.getResource("config.yml");
				byte[] linebuffer = new byte[4096];
				int lineLength = 0;
				while((lineLength = is.read(linebuffer)) > 0)
				{
					fos.write(linebuffer, 0, lineLength);
				}
				fos.close();
				
				log.log(Level.INFO, "[PlayerVaults] Wrote new config");
				
			} catch (IOException e) {
				log.log(Level.SEVERE, "[PlayerVaults] Couldn't write config: " + e);
			}	
		}
		else
		{
			log.log(Level.INFO, "[PlayerVaults] Config found.");
		}
	}
	
	public void loadLang()
	{
	  Logger log = plugin.getServer().getLogger();

		/**
		 * Check to see if there's a config.
		 * If not then create a new one.
		 */
		File config = new File(plugin.getDataFolder() + "/lang.yml");
		if(!config.exists())
		{
			try{
				plugin.getDataFolder().mkdir();
				config.createNewFile();
			} catch (IOException e) {
				log.log(Level.SEVERE, "[PlayerVaults] Couldn't create language file.");
			}
			/**
			 * Write the config file here.
			 * New, genius way to write it :)
			 */
			try {
				FileOutputStream fos = new FileOutputStream(new File(plugin.getDataFolder() + File.separator + "config.yml"));
				InputStream is = plugin.getResource("lang.yml");
				byte[] linebuffer = new byte[4096];
				int lineLength = 0;
				while((lineLength = is.read(linebuffer)) > 0)
				{
					fos.write(linebuffer, 0, lineLength);
				}
				fos.close();
				
				log.log(Level.INFO, "[PlayerVaults] Wrote new language file");
				
			} catch (IOException e) {
				log.log(Level.SEVERE, "[PlayerVaults] Couldn't write Language file: " + e);
			}	
		}
		else
		{
			log.log(Level.INFO, "[PlayerVaults] Language file found.");
		}
	}
	
	private YamlConfiguration lang() {
		File file = new File(plugin.getDataFolder() + "lang.yml");
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
		return plugin.getConfig().getBoolean("check-update");
	}
	
	public boolean debugMode() {
		return plugin.getConfig().getBoolean("debug-mode");
	}
	
	/**
	 * 
	 * @return disabled worlds.
	 */
	public List<?> disabledWorlds() {
		return plugin.getConfig().getList("disabled-worlds");
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
	
}