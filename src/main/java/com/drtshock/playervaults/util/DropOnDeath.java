package com.drtshock.playervaults.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.drtshock.playervaults.PlayerVaults;

public class DropOnDeath {

    public static PlayerVaults PLUGIN;

    public DropOnDeath(PlayerVaults instance) {
        DropOnDeath.PLUGIN = instance;
    }

    static VaultManager VAULT_MANAGER = new VaultManager(PLUGIN);

    /**
     * Drops all items when a player dies.
     * @param player
     */
    public static void drop(Player player) {
        Location loc = player.getLocation();

        for(int count = 1; count <= PlayerVaults.INVENTORIES_TO_DROP; count++) {
            Inventory inv = VAULT_MANAGER.getVault(player, count);
            ItemStack[] stack = inv.getContents();
            for(ItemStack is:stack) {
                loc.getWorld().dropItemNaturally(loc, is);
            }
        }
    }

}
