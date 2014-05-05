package com.drtshock.playervaults.vaults;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

/**
 * Handles opening and getting Vaults.
 */
public class VaultManagement {

    public static Inventory getVault(OfflinePlayer player, int num) {
        UUID uuid = player.getUniqueId();
        return null;
    }

    public static void saveVault(Inventory inv, OfflinePlayer player, int num) {
        saveVault(inv, player.getUniqueId(), num);
    }

    public static void saveVault(Inventory inv, UUID owner, int num) {


    }
}
