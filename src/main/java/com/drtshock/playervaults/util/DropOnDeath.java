package com.drtshock.playervaults.util;

import com.drtshock.playervaults.PlayerVaults;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * A class that contains a method to drop the contents of a player's vault when
 * they die.
 */
public class DropOnDeath {

    /**
     * Drops all items when a player dies.
     *
     * @param player The player to drop the inventory of.
     */
    public static void drop(Player player) {
        Location loc = player.getLocation();
        for (int count = 1; count <= PlayerVaults.INVENTORIES_TO_DROP; count++) {
            Inventory inv = PlayerVaults.VM.getVault(player.getName(), count);
            ItemStack[] stack = inv.getContents();
            for (ItemStack is : stack) {
                loc.getWorld().dropItemNaturally(loc, is);
            }
        }
    }
}
