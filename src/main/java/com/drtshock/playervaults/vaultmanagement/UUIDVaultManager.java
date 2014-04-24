package com.drtshock.playervaults.vaultmanagement;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class to handle vault operations with new UUIDs.
 */
public class UUIDVaultManager {

    private static UUIDVaultManager instance;

    public UUIDVaultManager() {
        instance = this;
    }

    private final String directory = "plugins" + File.separator + "PlayerVaults" + File.separator + "uuidvaults";

    /**
     * Saves the inventory to the specified player and vault number.
     *
     * @param inventory The inventory to be saved.
     * @param player The player of whose file to save to.
     * @param number The vault number.
     *
     * @throws java.io.IOException Uh oh!
     */
    public void saveVault(Inventory inventory, UUID player, int number) throws IOException {
        int size = inventory.getSize();
        YamlConfiguration yaml = getPlayerVaultFile(player);
        if (size == 54) {
            yaml.set("vault" + number, null);
        } else {
            for (int x = 0; x < size; x++) {
                yaml.set("vault" + number + "." + x, null);
            }
        }
        List<String> list = Serialization.toString(inventory);
        String[] ser = list.toArray(new String[list.size()]);
        for (int x = 0; x < ser.length; x++) {
            if (!ser[x].equalsIgnoreCase("null")) {
                yaml.set("vault" + number + "." + x, ser[x]);
            }
        }
        saveFile(player, yaml);
    }

    /**
     * Load the player's vault and return it.
     *
     * @param player The holder of the vault.
     * @param number The vault number.
     */
    public Inventory loadOwnVault(Player player, int number, int size) {
        if (size % 9 != 0) {
            size = 54;
        }
        VaultViewInfo info = new VaultViewInfo(player.getUniqueId().toString(), number);
        Inventory inv = null;
        if (PlayerVaults.getInstance().getOpenInventories().containsKey(info.toString())) {
            inv = PlayerVaults.getInstance().getOpenInventories().get(info.toString());
        } else {
            YamlConfiguration playerFile = getPlayerVaultFile(player.getUniqueId());
            if (playerFile.getConfigurationSection("vault" + number) == null) {
                VaultHolder vaultHolder = new VaultHolder(number);
                if (EconomyOperations.payToCreate(player)) {
                    inv = Bukkit.createInventory(vaultHolder, size, Lang.VAULT_TITLE.toString().replace("%number", String.valueOf(number)).replace("%p", player.getName()));
                    vaultHolder.setInventory(inv);
                } else {
                    player.sendMessage(Lang.TITLE.toString() + Lang.INSUFFICIENT_FUNDS.toString());
                    return null;
                }
            } else {
                if (getInventory(playerFile, size, number) == null) {
                    return null;
                } else {
                    inv = getInventory(playerFile, size, number);
                }
            }
            PlayerVaults.getInstance().getOpenInventories().put(info.toString(), inv);
        }
        return inv;
    }

    /**
     * Load the player's vault and return it.
     *
     * @param holder The holder of the vault.
     * @param number The vault number.
     */
    public Inventory loadOtherVault(UUID holder, int number, int size) {
        if (size % 9 != 0) {
            size = 54;
        }
        VaultViewInfo info = new VaultViewInfo(holder.toString(), number);
        Inventory inv = null;
        if (PlayerVaults.getInstance().getOpenInventories().containsKey(info.toString())) {
            inv = PlayerVaults.getInstance().getOpenInventories().get(info.toString());
        } else {
            YamlConfiguration playerFile = getPlayerVaultFile(holder);
            if (playerFile.getConfigurationSection("vault" + number) == null) {
                return null;
            } else {
                if (getInventory(playerFile, size, number) == null) {
                    return null;
                } else {
                    inv = getInventory(playerFile, size, number);
                }
            }
            PlayerVaults.getInstance().getOpenInventories().put(info.toString(), inv);
        }
        return inv;
    }

    /**
     * Get an inventory from file. Returns null if the inventory doesn't exist. SHOULD ONLY BE USED INTERNALLY
     *
     * @param playerFile the YamlConfiguration file.
     * @param size the size of the vault.
     * @param number the vault number.
     *
     * @return inventory if exists, otherwise null.
     */
    private Inventory getInventory(YamlConfiguration playerFile, int size, int number) {
        List<String> data = new ArrayList<>();
        for (int x = 0; x < size; x++) {
            String line = playerFile.getString("vault" + number + "." + x);
            if (line != null) {
                data.add(line);
            } else {
                data.add("null");
            }
        }
        return Serialization.toInventory(data, number, size);
    }

    /**
     * Gets an inventory without storing references to it. Used for dropping a players inventories on death.
     *
     * @param holder The holder of the vault.
     * @param number The vault number.
     *
     * @return The inventory of the specified holder and vault number.
     */
    public Inventory getVault(UUID holder, int number) {
        YamlConfiguration playerFile = getPlayerVaultFile(holder);
        List<String> data = playerFile.getStringList("vault" + number);
        OfflinePlayer player = Bukkit.getPlayer(holder);
        if (player == null) return null;
        if (data == null) {
            VaultHolder vaultHolder = new VaultHolder(number);
            Inventory inv = Bukkit.createInventory(vaultHolder, VaultOperations.getMaxVaultSize(player), Lang.VAULT_TITLE.toString().replace("%number", String.valueOf(number)).replace("%p", player.getName()));
            vaultHolder.setInventory(inv);
            return inv;
        } else {
            Inventory inv = Serialization.toInventory(data, number, VaultOperations.getMaxVaultSize(player));
            return inv;
        }
    }

    public boolean vaultExists(UUID holder, int number) {
        YamlConfiguration playerFile = getPlayerVaultFile(holder);
        return playerFile.contains("vault" + number);
    }

    /**
     * Deletes a players vault.
     *
     * @param sender The sender of whom to send messages to.
     * @param holder The vault holder.
     * @param number The vault number.
     *
     * @throws IOException Uh oh!
     */
    public void deleteVault(CommandSender sender, UUID holder, int number) throws IOException {
        String name = holder.toString();
        File file = new File(directory + File.separator + name.toLowerCase() + ".yml");
        if (!file.exists()) {
            return;
        }
        FileConfiguration playerFile = YamlConfiguration.loadConfiguration(file);
        if (file.exists()) {
            playerFile.set("vault" + number, null);
            playerFile.save(file);
        }
        OfflinePlayer player = Bukkit.getPlayer(holder);
        if (player != null && sender.getName().equalsIgnoreCase(player.getName())) {
            sender.sendMessage(Lang.TITLE.toString() + Lang.DELETE_VAULT.toString().replace("%v", String.valueOf(number)));
        } else {
            sender.sendMessage(Lang.TITLE.toString() + Lang.DELETE_OTHER_VAULT.toString().replace("%v", String.valueOf(number)).replaceAll("%p", player.getName()));
        }
    }

    /**
     * Get the holder's vault file. Create if doesn't exist.
     *
     * @param holder The vault holder.
     *
     * @return The holder's vault config file.
     */
    public YamlConfiguration getPlayerVaultFile(UUID holder) {
        File folder = new File(directory);
        if (!folder.exists()) {
            folder.mkdir();
        }
        File file = new File(directory + File.separator + holder.toString() + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // Who cares?
            }
        }
        YamlConfiguration playerFile = YamlConfiguration.loadConfiguration(file);
        return playerFile;
    }

    /**
     * Save the players vault file.
     *
     * @param holder The vault holder of whose file to save.
     * @param yaml The config to save.
     *
     * @throws IOException Uh oh!
     */
    public void saveFile(UUID holder, YamlConfiguration yaml) throws IOException {
        File file = new File(directory + File.separator + holder.toString() + ".yml");
        if (file.exists()) {
            file.renameTo(new File(directory + File.separator + "backups" + File.separator + holder.toString() + ".yml"));
        }
        yaml.save(file);
    }

    /**
     * Get the instance of this class.
     *
     * @return - instance of this class.
     */
    public static UUIDVaultManager getInstance() {
        return instance;
    }
}
