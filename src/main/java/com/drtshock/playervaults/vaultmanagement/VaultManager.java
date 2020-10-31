/*
 * PlayerVaultsX
 * Copyright (C) 2013 Trent Hensler
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.drtshock.playervaults.vaultmanagement;

import com.drtshock.playervaults.PlayerVaults;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class VaultManager {

    private static final String VAULTKEY = "vault%d";
    private static VaultManager instance;
    private final File directory = PlayerVaults.getInstance().getVaultData();
    private final Map<String, YamlConfiguration> cachedVaultFiles = new ConcurrentHashMap<>();
    private final PlayerVaults plugin;

    public VaultManager(PlayerVaults plugin) {
        this.plugin = plugin;
        instance = this;
    }

    /**
     * Get the instance of this class.
     *
     * @return - instance of this class.
     */
    public static VaultManager getInstance() {
        return instance;
    }

    /**
     * Saves the inventory to the specified player and vault number.
     *
     * @param inventory The inventory to be saved.
     * @param target The player of whose file to save to.
     * @param number The vault number.
     */
    public void saveVault(Inventory inventory, String target, int number) {
        YamlConfiguration yaml = getPlayerVaultFile(target, true);
        int size = VaultOperations.getMaxVaultSize(target);
        String serialized = Base64Serialization.toBase64(inventory, size, target);
        yaml.set(String.format(VAULTKEY, number), serialized);
        saveFileSync(target, yaml);
    }

    /**
     * Load the player's vault and return it.
     *
     * @param player The holder of the vault.
     * @param number The vault number.
     */
    public Inventory loadOwnVault(Player player, int number, int size) {
        if (size % 9 != 0) {
            size = PlayerVaults.getInstance().getDefaultVaultSize();
        }

        PlayerVaults.debug("Loading self vault for " + player.getName() + " (" + player.getUniqueId() + ')');

        String title = PlayerVaults.getInstance().getVaultTitle(String.valueOf(number));
        VaultViewInfo info = new VaultViewInfo(player.getUniqueId().toString(), number);
        if (PlayerVaults.getInstance().getOpenInventories().containsKey(info.toString())) {
            PlayerVaults.debug("Already open");
            return PlayerVaults.getInstance().getOpenInventories().get(info.toString());
        }

        YamlConfiguration playerFile = getPlayerVaultFile(player.getUniqueId().toString(), true);
        VaultHolder vaultHolder = new VaultHolder(number);
        if (playerFile.getString(String.format(VAULTKEY, number)) == null) {
            PlayerVaults.debug("No vault matching number");
            Inventory inv = Bukkit.createInventory(vaultHolder, size, title);
            vaultHolder.setInventory(inv);
            return inv;
        } else {
            return getInventory(vaultHolder, player.getUniqueId().toString(), playerFile, size, number, title);
        }
    }

    /**
     * Load the player's vault and return it.
     *
     * @param name The holder of the vault.
     * @param number The vault number.
     */
    public Inventory loadOtherVault(String name, int number, int size) {
        if (size % 9 != 0) {
            size = PlayerVaults.getInstance().getDefaultVaultSize();
        }

        PlayerVaults.debug("Loading other vault for " + name);

        String holder = name;

        try {
            UUID uuid = UUID.fromString(name);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            holder = offlinePlayer.getUniqueId().toString();
        } catch (Exception e) {
            // Not a player
        }

        String title = PlayerVaults.getInstance().getVaultTitle(String.valueOf(number));
        VaultViewInfo info = new VaultViewInfo(name, number);
        Inventory inv;
        VaultHolder vaultHolder = new VaultHolder(number);
        if (PlayerVaults.getInstance().getOpenInventories().containsKey(info.toString())) {
            PlayerVaults.debug("Already open");
            inv = PlayerVaults.getInstance().getOpenInventories().get(info.toString());
        } else {
            YamlConfiguration playerFile = getPlayerVaultFile(holder, true);
            Inventory i = getInventory(vaultHolder, holder, playerFile, size, number, title);
            if (i == null) {
                return null;
            } else {
                inv = i;
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
     * @return inventory if exists, otherwise null.
     */
    private Inventory getInventory(InventoryHolder owner, String ownerName, YamlConfiguration playerFile, int size, int number, String title) {
        Inventory inventory = Bukkit.createInventory(owner, size, title);

        String data = playerFile.getString(String.format(VAULTKEY, number));
        Inventory deserialized = Base64Serialization.fromBase64(data, ownerName);
        if (deserialized == null) {
            PlayerVaults.debug("Loaded vault as null");
            return inventory;
        }

        // Check if deserialized has more used slots than the limit here.
        // Happens on change of permission or if people used the broken version.
        // In this case, players will lose items.
        if (deserialized.getContents().length > size) {
            for (ItemStack stack : deserialized.getContents()) {
                if (stack != null) {
                    inventory.addItem(stack);
                }
            }
        } else {
            inventory.setContents(deserialized.getContents());
        }

        PlayerVaults.debug("Loaded vault");
        return inventory;
    }

    /**
     * Gets an inventory without storing references to it. Used for dropping a players inventories on death.
     *
     * @param holder The holder of the vault.
     * @param number The vault number.
     * @return The inventory of the specified holder and vault number. Can be null.
     */
    public Inventory getVault(String holder, int number) {
        YamlConfiguration playerFile = getPlayerVaultFile(holder, true);
        String serialized = playerFile.getString(String.format(VAULTKEY, number));
        return Base64Serialization.fromBase64(serialized, holder);
    }

    /**
     * Checks if a vault exists.
     *
     * @param holder holder of the vault.
     * @param number vault number.
     * @return true if the vault file and vault number exist in that file, otherwise false.
     */
    public boolean vaultExists(String holder, int number) {
        File file = new File(directory, holder + ".yml");
        if (!file.exists()) {
            return false;
        }

        return getPlayerVaultFile(holder, true).contains(String.format(VAULTKEY, number));
    }

    /**
     * Gets the numbers belonging to all their vaults.
     *
     * @param holder
     * @return a set of Integers, which are player's vaults' numbers (fuck grammar).
     */
    public Set<Integer> getVaultNumbers(String holder) {
        Set<Integer> vaults = new HashSet<>();
        YamlConfiguration file = getPlayerVaultFile(holder, true);
        if (file == null) {
            return vaults;
        }

        for (String s : file.getKeys(false)) {
            try {
                // vault%
                int number = Integer.parseInt(s.substring(4));
                vaults.add(number);
            } catch (NumberFormatException e) {
                // silent
            }
        }


        return vaults;
    }

    public void deleteAllVaults(String holder) {
        removeCachedPlayerVaultFile(holder);
        deletePlayerVaultFile(holder);
    }

    /**
     * Deletes a players vault.
     *
     * @param sender The sender of whom to send messages to.
     * @param holder The vault holder.
     * @param number The vault number.
     * @throws IOException Uh oh!
     */
    public void deleteVault(CommandSender sender, final String holder, final int number) {
        new BukkitRunnable() {
            @Override
            public void run() {
                File file = new File(directory, holder + ".yml");
                if (!file.exists()) {
                    return;
                }

                YamlConfiguration playerFile = YamlConfiguration.loadConfiguration(file);
                if (file.exists()) {
                    playerFile.set(String.format(VAULTKEY, number), null);
                    if (cachedVaultFiles.containsKey(holder)) {
                        cachedVaultFiles.put(holder, playerFile);
                    }
                    try {
                        playerFile.save(file);
                    } catch (IOException ignored) {
                    }
                }
            }
        }.runTaskAsynchronously(PlayerVaults.getInstance());

        OfflinePlayer player = Bukkit.getPlayer(holder);
        if (player != null) {
            if (sender.getName().equalsIgnoreCase(player.getName())) {
                this.plugin.getTL().deleteVault().title().with("vault", String.valueOf(number)).send(sender);
            } else {
                this.plugin.getTL().deleteOtherVault().title().with("vault", String.valueOf(number)).with("player", player.getName()).send(sender);
            }
        }

        String vaultName = sender instanceof Player ? ((Player) sender).getUniqueId().toString() : holder;
        PlayerVaults.getInstance().getOpenInventories().remove(new VaultViewInfo(vaultName, number).toString());
    }

    // Should only be run asynchronously
    public void cachePlayerVaultFile(String holder) {
        YamlConfiguration config = this.loadPlayerVaultFile(holder, false);
        if (config != null) {
            this.cachedVaultFiles.put(holder, config);
        }
    }

    public void removeCachedPlayerVaultFile(String holder) {
        cachedVaultFiles.remove(holder);
    }

    /**
     * Get the holder's vault file. Create if doesn't exist.
     *
     * @param holder The vault holder.
     * @return The holder's vault config file.
     */
    public YamlConfiguration getPlayerVaultFile(String holder, boolean createIfNotFound) {
        if (cachedVaultFiles.containsKey(holder)) {
            return cachedVaultFiles.get(holder);
        }
        return loadPlayerVaultFile(holder, createIfNotFound);
    }

    public YamlConfiguration loadPlayerVaultFile(String holder) {
        return this.loadPlayerVaultFile(holder, true);
    }

    /**
     * Attempt to delete a vault file.
     *
     * @param holder UUID of the holder.
     * @return true if successful, otherwise false.
     */
    public void deletePlayerVaultFile(String holder) {
        File file = new File(this.directory, holder + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }

    public YamlConfiguration loadPlayerVaultFile(String uniqueId, boolean createIfNotFound) {
        if (!this.directory.exists()) {
            this.directory.mkdir();
        }

        File file = new File(this.directory, uniqueId + ".yml");
        if (!file.exists()) {
            if (createIfNotFound) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                return null;
            }
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    public void saveFileSync(final String holder, final YamlConfiguration yaml) {
        if (cachedVaultFiles.containsKey(holder)) {
            cachedVaultFiles.put(holder, yaml);
        }

        final boolean backups = PlayerVaults.getInstance().isBackupsEnabled();
        final File backupsFolder = PlayerVaults.getInstance().getBackupsFolder();
        final File file = new File(directory, holder + ".yml");
        if (file.exists() && backups) {
            file.renameTo(new File(backupsFolder, holder + ".yml"));
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            PlayerVaults.getInstance().getLogger().log(Level.SEVERE, "Failed to save vault file for: " + holder, e);
        }
        PlayerVaults.debug("Saved vault for " + holder);
    }
}
