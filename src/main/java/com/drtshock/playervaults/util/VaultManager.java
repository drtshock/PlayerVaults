package com.drtshock.playervaults.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.commands.VaultViewInfo;

/**
 * A class for managing actual IO to the files, loading inventories, and saving them.
 */
public class VaultManager {

    public PlayerVaults plugin;

    public VaultManager(PlayerVaults instance) {
        this.plugin = instance;
    }

    private final String directory = "plugins" + File.separator + "PlayerVaults" + File.separator + "vaults";

    /**
     * Saves the inventory to the specified player and vault number.
     * @param inventory The inventory to be saved.
     * @param player The player of whose file to save to.
     * @param number The vault number.
     * @throws IOException Uh oh!
     */
    public void saveVault(Inventory inventory, String player, int number) throws IOException {
        YamlConfiguration yaml = getPlayerVaultFile(player);
        yaml.set("vault" + number, null);
        List<String> list = Serialization.toString(inventory);
        String[] ser = list.toArray(new String[list.size()]);
        for(int x = 0; x < ser.length; x++) {
            if (!ser[x].equalsIgnoreCase("null")) {
                yaml.set("vault" + number + "." + x, ser[x]);
            }
        }
        saveFile(player, yaml);
    }

    /**
     * Load the player's vault and return it.
     * @param holder The holder of the vault.
     * @param number The vault number.
     */
    public Inventory loadVault(String holder, int number) {
        VaultViewInfo info = new VaultViewInfo(holder, number);
        Inventory inv = null;
        if (PlayerVaults.OPENINVENTORIES.containsKey(info.toString())) {
            inv = PlayerVaults.OPENINVENTORIES.get(info.toString());
        } else {
            YamlConfiguration playerFile = getPlayerVaultFile(holder);
            if (playerFile.getConfigurationSection("vault" + number) == null) {
                inv = Bukkit.createInventory(null, 54, ChatColor.DARK_RED + "Vault #" + String.valueOf(number));
            } else {
                List<String> data = new ArrayList<String>();
                for(int x = 0; x < 54; x++) {
                    String line = playerFile.getString("vault" + number + "." + x);
                    if (line != null) {
                        data.add(line);
                    } else {
                        data.add("null");
                    }
                }
                inv = Serialization.toInventory(data, number);
            }
            PlayerVaults.OPENINVENTORIES.put(info.toString(), inv);
        }
        return inv;
    }

    /**
     * Gets an inventory without storing references to it. Used for dropping a players inventories on death.
     * @param holder The holder of the vault.
     * @param number The vault number.
     * @return The inventory of the specified holder and vault number.
     */
    public Inventory getVault(String holder, int number) {
        YamlConfiguration playerFile = getPlayerVaultFile(holder);
        List<String> data = playerFile.getStringList("vault" + number);
        if (data == null) {
            Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GREEN + "Vault #" + String.valueOf(number));
            return inv;
        } else {
            Inventory inv = Serialization.toInventory(data, number);
            return inv;
        }
    }

    /**
     * Deletes a players vault.
     * @param sender The sender of whom to send messages to.
     * @param holder The vault holder.
     * @param number The vault number.
     * @throws IOException Uh oh!
     */
    public void deleteVault(CommandSender sender, String holder, int number) throws IOException {
        String name = holder.toLowerCase();
        File file = new File(directory + File.separator + name.toLowerCase() + ".yml");
        FileConfiguration playerFile = YamlConfiguration.loadConfiguration(file);
        if (file.exists()) {
            playerFile.set("vault" + number, null);
            playerFile.save(file);
        }
        if (sender.getName().equalsIgnoreCase(holder)) {
            sender.sendMessage(Lang.TITLE.toString() + Lang.DELETE_VAULT.toString().replace("%v", String.valueOf(number)));
        } else {
            sender.sendMessage(Lang.TITLE.toString() + Lang.DELETE_OTHER_VAULT.toString().replace("%v", String.valueOf(number)).replace("%p", holder));
        }
    }

    /**
     * Get the holder's vault file. Create if doesn't exist.
     * @param holder The vault holder.
     * @return The holder's vault config file.
     */
    public YamlConfiguration getPlayerVaultFile(String holder) {
        File folder = new File(directory);
        if (!folder.exists()) {
            folder.mkdir();
        }
        File file = new File(directory + File.separator + holder.toLowerCase() + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch(IOException e) {
                // Who cares?
            }
        }
        YamlConfiguration playerFile = YamlConfiguration.loadConfiguration(file);
        return playerFile;
    }

    /**
     * Save the players vault file.
     * @param holder The vault holder of whose file to save.
     * @param yaml The config to save.
     * @throws IOException Uh oh!
     */
    public void saveFile(String holder, YamlConfiguration yaml) throws IOException {
        File file = new File(directory + File.separator + holder.toLowerCase() + ".yml");
        if (file.exists()) {
            file.renameTo(new File(directory + File.separator + "backups" + File.separator + holder.toLowerCase() + ".yml"));
        }
        yaml.save(file);
    }
}
