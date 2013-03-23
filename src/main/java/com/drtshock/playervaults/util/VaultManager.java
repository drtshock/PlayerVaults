package com.drtshock.playervaults.util;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.drtshock.playervaults.Main;

public class VaultManager {

	public Main plugin;
	public VaultManager(Main instance) {
		this.plugin = instance;
	}

	private final String directory = "plugins" + File.separator + "PlayerVaults" + File.separator + "vaults";

	/**
	 * Method to save player's vault.
	 * Serialize his inventory.
	 * Save the vaults.yml
	 * @param player
	 * @throws IOException 
	 */
	public void saveVault(Inventory inv, String player, int number) throws IOException {
		String ser = Serialization.toBase64(inv);
		YamlConfiguration yaml = playerVaultFile(player);
		yaml.set("vault" + number + "", ser);
		saveFile(player, yaml);
	}

	/**
	 * Method to load player's vault.
	 * Deserialize his inventory
	 * 
	 * TODO: Check to see if the path exists before we get it!
	 */
	public void loadVault(Player player, String holder, int number) {
		YamlConfiguration playerFile = playerVaultFile(holder);
		String data = playerFile.getString("vault" + number);
		if(data == null) {
			Inventory inv = Bukkit.createInventory(player, 54, ChatColor.DARK_RED + "Vault #" + String.valueOf(number));
			player.openInventory(inv);
		} else {
			Inventory inv = Serialization.fromBase64(data, number);
			player.openInventory(inv);
		}
	}

	/**
	 * Gets an inventory without opening it.
	 * Used for dropping a players inventories on death.
	 * @param player
	 * @param number
	 * @return the inventory
	 */
	public Inventory getVault(Player player, int number) {
		YamlConfiguration playerFile = playerVaultFile(player.getName());
		String data = playerFile.getString("vault" + number);
		if(data == null) {
			Inventory inv = Bukkit.createInventory(player, 54, ChatColor.GREEN + "Vault #" + String.valueOf(number));
			return inv;
		} else {
			Inventory inv = Serialization.fromBase64(data, number);
			return inv;
		}
	}

	/**
	 * Deletes a players vault.
	 * @param sender
	 * @param target
	 * @param number
	 * @throws IOException
	 */
	public void deleteVault(CommandSender sender, String target, int number) throws IOException {
		String name = target.toLowerCase();
		File file = new File(directory + File.separator + name.toLowerCase() + ".yml");
		FileConfiguration playerFile = YamlConfiguration.loadConfiguration(file);
		if(file.exists()) {
			playerFile.set("vault" + number, null);
			playerFile.save(file);
		}
		if(sender.getName().equalsIgnoreCase(target)) {
			sender.sendMessage(Lang.TITLE.toString() + Lang.DELETE_VAULT.toString().replace("%v", String.valueOf(number)));
		}
		else {
			sender.sendMessage(Lang.TITLE.toString() + Lang.DELETE_OTHER_VAULT.toString().replace("%v", String.valueOf(number)).replace("%p", target));
		}
	}

	/**
	 * Get the player's vault file.
	 * Create if doesn't exist.
	 * @param player
	 * @return playerVaultFile file.
	 */
	public YamlConfiguration playerVaultFile(String player) {
		File folder = new File(directory);
		if(!folder.exists()) {
			folder.mkdir();
		}
		File file = new File(directory + File.separator + player.toLowerCase() + ".yml");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		YamlConfiguration playerFile = YamlConfiguration.loadConfiguration(file);
		return playerFile;
	}

	/**
	 * Save the players vault file.
	 * @param name
	 * @param yaml
	 * @throws IOException
	 */
	public void saveFile(String name, YamlConfiguration yaml) throws IOException {
		File file = new File(directory + File.separator + name.toLowerCase() + ".yml");
		if (file.exists()) {
			file.renameTo(new File(directory + File.separator + name.toLowerCase() + ".yml.bak"));
		}
		yaml.save(file);
	}
}
