package com.drtshock.playervaults.vaults;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Handles files for Vaults.
 */
public class VaultFiles {

    public static YamlConfiguration getVaultFile(OfflinePlayer player) {
        return getVaultFile(player.getUniqueId());
    }

    public static YamlConfiguration getVaultFile(UUID uuid) {
        File file = new File(PlayerVaults.getInstance().getDataFolder(), "files" + File.separator + uuid.toString() + ".yml");
        if (!file.exists()) file.mkdirs();
        return YamlConfiguration.loadConfiguration(file);
    }

    public static boolean serializeVault(Inventory inventory, UUID owner, int vaultNumber) {
        YamlConfiguration yaml = getVaultFile(owner);
        List<ConfigurationSerializable> items = new ArrayList<>();
        for (ItemStack is : inventory.getContents()) {
            items.add(is);
        }
        for (int i = 0; i <= items.size(); i++) {
            yaml.set("vault" + vaultNumber + "." + i, items.get(i));
        }
        saveFile(owner);
        return true;
    }

    public static Inventory deserializeVault(UUID owner, int vaultNumber) {
        YamlConfiguration yaml = getVaultFile(owner);
        Inventory inv = Bukkit.createInventory(null, 54, Lang.VAULT_TITLE.toString()); // TODO get correct size.
        for (int i = 0; i <= 54; i++) {
            ItemStack is = yaml.getItemStack("vault" + vaultNumber + "." + i);
            if (is != null) inv.addItem(is);
        }
        return null;
    }

    public static void saveFile(UUID uuid) {
        File file = new File(PlayerVaults.getInstance().getDataFolder(), "files" + File.separator + uuid.toString() + ".yml");
        if (!file.exists()) file.mkdirs();
        saveFile(file, YamlConfiguration.loadConfiguration(file));
    }

    public static void saveFile(File file, YamlConfiguration yaml) {
        try {
            yaml.save(file);
        } catch (IOException e) {
            PlayerVaults.getInstance().getLogger().log(Level.SEVERE, "Failed to save file: " + file.toString());
            e.printStackTrace();
        }
    }
}
