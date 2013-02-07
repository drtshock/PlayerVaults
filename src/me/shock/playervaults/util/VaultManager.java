package me.shock.playervaults.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import me.shock.playervaults.Main;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class VaultManager 
{

	public Main plugin;
	public VaultManager(Main instance) {
		this.plugin = instance;
	}
	String title;

	/**
	 * Method to save player's vault.
	 * Serialize his inventory.
	 * Save the vaults.yml
	 * @param player
	 * @throws IOException 
	 */
	public void saveVault(Inventory inv, Player player, int number) throws IOException
	{
		if(plugin.inVault().containsKey(player.getName()))
		{
			// Get the player's file and serialize the inventory.
			String ser = Serialization.toBase64(inv);
			YamlConfiguration file = plugin.playerVaultFile(player.getName());

			// Prepare to save D:
			file.set("vault" + number + "", ser);
			if(plugin.debugMode())
			{
				plugin.getLogger().log(Level.INFO, "[PlayerVaults] Saved " + " " + number + " for " + player.getName());
			}
		}
	}

	/**
	 * Method to load player's vault.
	 * Deserialize his inventory
	 * 
	 * TODO: Check to see if the path exists before we get it!
	 */
	public void loadVault(CommandSender sender, String target, int number)
	{
		YamlConfiguration playerFile = plugin.playerVaultFile(target);
		String data = playerFile.getString("vault" + "" + number + "");
		Inventory inv = Serialization.fromBase64(data);
		Player player = (Player) sender;
		player.openInventory(inv);
		player.sendMessage(title + " Opening " + ChatColor.GREEN + " " + number);
		return;

	}

	public void deleteVault(CommandSender sender, String target, int number) throws IOException
	{
		String name = target.toLowerCase();
		File file = new File(plugin.getDataFolder() + File.separator + "vaults" + name + ".yml");
		FileConfiguration playerFile = YamlConfiguration.loadConfiguration(file);
		if(file.exists())
		{
			ConfigurationSection section = playerFile.getConfigurationSection("vault" + number);
			section.set(null, null);
			sender.sendMessage(title + "Deleting " + ChatColor.GREEN + " " + number);
			playerFile.save(file);
			return;

		}
		else
		{
			sender.sendMessage(title + " That doesn't exist!");
			return;
		}
	}
}
