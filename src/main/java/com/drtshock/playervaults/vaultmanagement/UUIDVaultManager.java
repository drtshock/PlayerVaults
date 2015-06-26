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
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to handle vault operations with new UUIDs.
 */
public class UUIDVaultManager {

    private static UUIDVaultManager instance;

    public UUIDVaultManager() {
        instance = this;
    }

    private final File directory = PlayerVaults.getInstance().getVaultData();
    private final Map<UUID, YamlConfiguration> cachedVaultFiles = new ConcurrentHashMap<>();

    /**
     * Saves the inventory to the specified player and vault number.
     *
     * @param inventory The inventory to be saved.
     * @param player    The player of whose file to save to.
     * @param number    The vault number.
     * @throws java.io.IOException Uh oh!
     */
    public void saveVault(Inventory inventory, UUID player, int number) throws IOException {
        saveVault(inventory, player, number, true);
    }

    public void saveVault(Inventory inventory, UUID player, int number, boolean async) {
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
        if (async) {
            saveFile(player, yaml);
        } else {
            saveFileSync(player, yaml);
        }
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
        Inventory inv;
        if (PlayerVaults.getInstance().getOpenInventories().containsKey(info.toString())) {
            inv = PlayerVaults.getInstance().getOpenInventories().get(info.toString());
        } else {
            YamlConfiguration playerFile = getPlayerVaultFile(player.getUniqueId());
            if (playerFile.getConfigurationSection("vault" + number) == null) {
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
        String title = Lang.VAULT_TITLE.toString().replace("%number", String.valueOf(number)).replace("%p", Bukkit.getOfflinePlayer(holder).getName());
        VaultViewInfo info = new VaultViewInfo(holder, number);
        Inventory inv;
        if (PlayerVaults.getInstance().getOpenInventories().containsKey(info.toString())) {
            inv = PlayerVaults.getInstance().getOpenInventories().get(info.toString());
        } else {
            YamlConfiguration playerFile = getPlayerVaultFile(holder);
            if (playerFile.getConfigurationSection("vault" + number) == null) {
                return null;
            } else {
                Inventory i = getInventory(playerFile, size, number, title);
                if (i == null) {
                    return null;
                } else {
                    inv = i;
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
     * @param size       the size of the vault.
     * @param number     the vault number.
     * @return inventory if exists, otherwise null.
     */
    private Inventory getInventory(YamlConfiguration playerFile, int size, int number, String title) {
        List<String> data = new ArrayList<>();
        for (int x = 0; x < size; x++) {
            String line = playerFile.getString("vault" + number + "." + x);
            if (line != null) {
                data.add(line);
            } else {
                data.add("null");
            }
        }
        return Serialization.toInventory(data, number, size, title);
    }

    /**
     * Gets an inventory without storing references to it. Used for dropping a players inventories on death.
     *
     * @param holder The holder of the vault.
     * @param number The vault number.
     * @return The inventory of the specified holder and vault number.
     */
    public Inventory getVault(UUID holder, int number) {
        YamlConfiguration playerFile = getPlayerVaultFile(holder);
        List<String> data = playerFile.getStringList("vault" + number);
        OfflinePlayer player = Bukkit.getOfflinePlayer(holder);
        if (player == null || !player.hasPlayedBefore()) {
            return null;
        }
        String title = Lang.VAULT_TITLE.toString().replace("%number", String.valueOf(number)).replace("%p", Bukkit.getOfflinePlayer(holder).getName());
        if (data == null) {
            VaultHolder vaultHolder = new VaultHolder(number);
            Inventory inv = Bukkit.createInventory(vaultHolder, VaultOperations.getMaxVaultSize(player), Lang.VAULT_TITLE.toString().replace("%number", String.valueOf(number)).replace("%p", player.getName()));
            vaultHolder.setInventory(inv);
            return inv;
        } else {
            return Serialization.toInventory(data, number, VaultOperations.getMaxVaultSize(player), title);
        }
    }

    public boolean vaultExists(UUID holder, int number) {
        return getPlayerVaultFile(holder).contains("vault" + number);
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
                File file = new File(directory, holder.toString() + ".yml");
                if (!file.exists()) {
                    return;
                }

                FileConfiguration playerFile = YamlConfiguration.loadConfiguration(file);
                if (file.exists()) {
                    playerFile.set("vault" + number, null);
                    try {
                        playerFile.save(file);
                    } catch (IOException ignored) {
                    }
                }
            }
        }.runTaskAsynchronously(PlayerVaults.getInstance());

        OfflinePlayer player = Bukkit.getPlayer(holder);
        if (player != null && sender.getName().equalsIgnoreCase(player.getName())) {
            sender.sendMessage(Lang.TITLE.toString() + Lang.DELETE_VAULT.toString().replace("%v", String.valueOf(number)));
        } else {
            sender.sendMessage(Lang.TITLE.toString() + Lang.DELETE_OTHER_VAULT.toString().replace("%v", String.valueOf(number)).replaceAll("%p", player.getName()));
        }

        PlayerVaults.getInstance().getOpenInventories().remove(new VaultViewInfo(holder.toString(), number).toString());
    }

    // Should only be run asynchronously
    public void cachePlayerVaultFile(UUID holder) {
        cachedVaultFiles.put(holder, loadPlayerVaultFile(holder));
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
        if (!directory.exists()) {
            directory.mkdir();
        }
        File file = new File(directory, holder.toString() + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // Who cares?
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Save the players vault file.
     *
     * @param holder The vault holder of whose file to save.
     * @param yaml   The config to save.
     * @throws IOException Uh oh!
     */
    public void saveFile(final UUID holder, final YamlConfiguration yaml) {
        if (cachedVaultFiles.containsKey(holder)) {
            cachedVaultFiles.put(holder, yaml);
        }
        final boolean backups = PlayerVaults.getInstance().isBackupsEnabled();
        final File backupsFolder = PlayerVaults.getInstance().getBackupsFolder();
        final File file = new File(directory, holder.toString() + ".yml");
        new BukkitRunnable() {
            @Override
            public void run() {
                if (file.exists() && backups) {
                    file.renameTo(new File(backupsFolder, holder.toString() + ".yml"));
                }
                try {
                    yaml.save(file);
                } catch (IOException ignored) {
                }
            }
        }.runTaskAsynchronously(PlayerVaults.getInstance());
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
        } catch (IOException ignored) {
        }
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
