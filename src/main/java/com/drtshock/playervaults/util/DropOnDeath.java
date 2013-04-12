package com.drtshock.playervaults.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.drtshock.playervaults.Main;

public class DropOnDeath {

    public static Main plugin;

    public DropOnDeath(Main instance) {
        DropOnDeath.plugin = instance;
    }

    static VaultManager vm = new VaultManager(plugin);

    /**
     * Drops all items when a player dies.
     * @param player
     */
    public static void drop(Player player) {
        Location loc = player.getLocation();

        for(int count = 1; count <= Main.inventoriesToDrop; count++) {
            Inventory inv = vm.getVault(player, count);
            ItemStack[] stack = inv.getContents();
            for(ItemStack is:stack) {
                loc.getWorld().dropItemNaturally(loc, is);
            }
        }
    }

}
