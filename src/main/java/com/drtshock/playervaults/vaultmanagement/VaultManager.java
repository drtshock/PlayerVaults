package com.drtshock.playervaults.vaultmanagement;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class VaultManager {

    private static VaultManager instance;
    private static final String VAULTKEY = "vault%d";

    public VaultManager() {
        instance = this;
    }

    private final File directory = PlayerVaults.getInstance().getVaultData();
    private final Map<UUID, YamlConfiguration> cachedVaultFiles = new ConcurrentHashMap<>();

    /**
     * Saves the inventory to the specified player and vault number.
     *
     * @param inventory The inventory to be saved.
     * @param target    The player of whose file to save to.
     * @param number    The vault number.
     */
    public void saveVault(Inventory inventory, UUID target, int number) {
        YamlConfiguration yaml = getPlayerVaultFile(target);
        String serialized = Base64Serialization.toBase64(inventory);
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
            size = 54;
        }

        String title = Lang.VAULT_TITLE.toString().replace("%number", String.valueOf(number)).replace("%p", player.getName());
        VaultViewInfo info = new VaultViewInfo(player.getUniqueId(), number);
        if (PlayerVaults.getInstance().getOpenInventories().containsKey(info.toString())) {
            return PlayerVaults.getInstance().getOpenInventories().get(info.toString());
        }

        Inventory inv;
        YamlConfiguration playerFile = getPlayerVaultFile(player.getUniqueId());
        if (playerFile.getString(String.format(VAULTKEY, number)) == null) {
            VaultHolder vaultHolder = new VaultHolder(number);
            if (EconomyOperations.payToCreate(player)) {
                inv = Bukkit.createInventory(vaultHolder, size, title);
                vaultHolder.setInventory(inv);
            } else {
                player.sendMessage(Lang.TITLE.toString() + Lang.INSUFFICIENT_FUNDS.toString());
                return null;
            }
        } else {
            Inventory i = getInventory(playerFile, size, number, title);
            if (i == null) {
                return null;
            } else {
                inv = i;
            }
        }
        PlayerVaults.getInstance().getOpenInventories().put(info.toString(), inv);

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

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(holder);
        String name = "null";
        if (offlinePlayer != null) {
            name = offlinePlayer.getName();
        }

        String title = Lang.VAULT_TITLE.toString().replace("%number", String.valueOf(number)).replace("%p", name);
        VaultViewInfo info = new VaultViewInfo(holder, number);
        Inventory inv;
        if (PlayerVaults.getInstance().getOpenInventories().containsKey(info.toString())) {
            inv = PlayerVaults.getInstance().getOpenInventories().get(info.toString());
        } else {
            YamlConfiguration playerFile = getPlayerVaultFile(holder);
            Inventory i = getInventory(playerFile, size, number, title);
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
     * @param size       the size of the vault.
     * @param number     the vault number.
     * @return inventory if exists, otherwise null.
     */
    private Inventory getInventory(YamlConfiguration playerFile, int size, int number, String title) {
        String data = playerFile.getString(String.format(VAULTKEY, number));
        Inventory deserializedData = Base64Serialization.fromBase64(data);
        if (deserializedData == null) {
            return null;
        }
        Inventory inventory = Bukkit.createInventory(null, size, title);

        for (ItemStack itemStack : deserializedData.getContents()) {
            if (itemStack != null) {
                //inventory.addItem(itemStack).values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i)); //Will drop any items that can't fit. Requires java 8 and a player/location argument.
                inventory.addItem(itemStack); //Will delete any items that can't fit.
            }
        }
        return inventory;
    }

    /**
     * Gets an inventory without storing references to it. Used for dropping a players inventories on death.
     *
     * @param holder The holder of the vault.
     * @param number The vault number.
     * @return The inventory of the specified holder and vault number. Can be null.
     */
    public Inventory getVault(UUID holder, int number) {
        YamlConfiguration playerFile = getPlayerVaultFile(holder);
        String serialized = playerFile.getString(String.format(VAULTKEY, number));
        return Base64Serialization.fromBase64(serialized);
    }

    /**
     * Checks if a vault exists.
     *
     * @param holder holder of the vault.
     * @param number vault number.
     * @return true if the vault file and vault number exist in that file, otherwise false.
     */
    public boolean vaultExists(UUID holder, int number) {
        File file = new File(directory, holder + ".yml");
        if (!file.exists()) {
            return false;
        }

        return getPlayerVaultFile(holder).contains(String.format(VAULTKEY, number));
    }

    /**
     * Deletes a players vault.
     *
     * @param sender The sender of whom to send messages to.
     * @param holder The vault holder.
     * @param number The vault number.
     * @throws IOException Uh oh!
     */
    public void deleteVault(CommandSender sender, final UUID holder, final int number) throws IOException {
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
                sender.sendMessage(Lang.TITLE.toString() + Lang.DELETE_VAULT.toString().replace("%v", String.valueOf(number)));
            } else {
                sender.sendMessage(Lang.TITLE.toString() + Lang.DELETE_OTHER_VAULT.toString().replace("%v", String.valueOf(number)).replaceAll("%p", player.getName()));
            }
        }

        PlayerVaults.getInstance().getOpenInventories().remove(new VaultViewInfo(holder, number).toString());
    }

    // Should only be run asynchronously
    public void cachePlayerVaultFile(UUID holder) {
        YamlConfiguration config = this.loadPlayerVaultFile(holder, false);
        if (config != null) {
            this.cachedVaultFiles.put(holder, config);
        } else {
        }
    }

    public void removeCachedPlayerVaultFile(UUID holder) {
        if (cachedVaultFiles.containsKey(holder)) {
            cachedVaultFiles.remove(holder);
        }
    }

    /**
     * Get the holder's vault file. Create if doesn't exist.
     *
     * @param holder The vault holder.
     * @return The holder's vault config file.
     */
    public YamlConfiguration getPlayerVaultFile(UUID holder) {
        if (cachedVaultFiles.containsKey(holder)) {
            return cachedVaultFiles.get(holder);
        }
        return loadPlayerVaultFile(holder);
    }

    public YamlConfiguration loadPlayerVaultFile(UUID holder) {
        return this.loadPlayerVaultFile(holder, true);
    }

    public YamlConfiguration loadPlayerVaultFile(UUID uniqueId, boolean createIfNotFound) {
        if (!this.directory.exists()) {
            this.directory.mkdir();
        }

        File file = new File(this.directory, uniqueId.toString() + ".yml");
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

    public void saveFileSync(final UUID holder, final YamlConfiguration yaml) {
        if (cachedVaultFiles.containsKey(holder)) {
            cachedVaultFiles.put(holder, yaml);
        }

        final boolean backups = PlayerVaults.getInstance().isBackupsEnabled();
        final File backupsFolder = PlayerVaults.getInstance().getBackupsFolder();
        final File file = new File(directory, holder.toString() + ".yml");
        if (file.exists() && backups) {
            file.renameTo(new File(backupsFolder, holder.toString() + ".yml"));
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            PlayerVaults.getInstance().getLogger().log(Level.SEVERE, "Failed to save vault file for: " + holder.toString(), e);
        }
    }

    /**
     * Get the instance of this class.
     *
     * @return - instance of this class.
     */
    public static VaultManager getInstance() {
        return instance;
    }
}
