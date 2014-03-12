/* 
 * Copyright (C) 2013 drtshock
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
package com.drtshock.playervaults.listeners;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.util.Lang;
import com.drtshock.playervaults.vaultmanagement.VaultManager;
import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import com.drtshock.playervaults.vaultmanagement.VaultViewInfo;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;

import java.io.IOException;

public class Listeners implements Listener {

    public PlayerVaults plugin;

    public Listeners(PlayerVaults playerVaults) {
        this.plugin = playerVaults;
    }

    VaultManager vm = new VaultManager(plugin);

    public void saveVault(Player player) {
        if (PlayerVaults.IN_VAULT.containsKey(player.getName())) {
            Inventory inv = player.getOpenInventory().getTopInventory();
            player.closeInventory(); // close the player's inventory just in case.
            if (inv.getViewers().size() == 1) {
                VaultViewInfo info = PlayerVaults.IN_VAULT.get(player.getName());
                try {
                    vm.saveVault(inv, info.getHolder(), info.getNumber());
                } catch (IOException e) {
                }
                PlayerVaults.OPENINVENTORIES.remove(info.toString());
            }
            PlayerVaults.IN_VAULT.remove(player.getName());
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        saveVault(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        saveVault(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (PlayerVaults.UPDATE && (player.isOp() || player.hasPermission("playervaults.notify"))) {
            player.sendMessage(ChatColor.GREEN + "Version " + PlayerVaults.NEWVERSION + " of PlayerVaults is up for download!");
            player.sendMessage(ChatColor.GREEN + PlayerVaults.LINK + " to view the changelog and download!");
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        saveVault(player);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        HumanEntity he = event.getPlayer();
        if (he instanceof Player) {
            Player player = (Player) he;
            saveVault(player);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (PlayerVaults.IN_VAULT.containsKey(player.getName())) {
                Block block = event.getClickedBlock();
                // Different inventories that we don't want the player to open.
                if (block.getType() == Material.CHEST
                        || block.getType() == Material.TRAPPED_CHEST
                        || block.getType() == Material.ENDER_CHEST
                        || block.getType() == Material.FURNACE
                        || block.getType() == Material.BURNING_FURNACE
                        || block.getType() == Material.BREWING_STAND
                        || block.getType() == Material.ENCHANTMENT_TABLE
                        || block.getType() == Material.BEACON) {
                    event.setCancelled(true);
                }
            }
        }
        if (PlayerVaults.SET_SIGN.containsKey(player.getName())) {
            int i = PlayerVaults.SET_SIGN.get(player.getName()).getChest();
            boolean self = PlayerVaults.SET_SIGN.get(player.getName()).isSelf();
            String owner = null;
            if (!self) {
                owner = PlayerVaults.SET_SIGN.get(player.getName()).getOwner();
            }
            PlayerVaults.SET_SIGN.remove(player.getName());
            event.setCancelled(true);
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (event.getClickedBlock().getType() == Material.WALL_SIGN || event.getClickedBlock().getType() == Material.SIGN_POST) {
                    Sign s = (Sign) event.getClickedBlock().getState();
                    Location l = s.getLocation();
                    String world = l.getWorld().getName();
                    int x = l.getBlockX();
                    int y = l.getBlockY();
                    int z = l.getBlockZ();
                    if (self) {
                        plugin.getSigns().set(world + ";;" + x + ";;" + y + ";;" + z + ".self", self);
                    } else {
                        plugin.getSigns().set(world + ";;" + x + ";;" + y + ";;" + z + ".owner", owner);
                    }
                    plugin.getSigns().set(world + ";;" + x + ";;" + y + ";;" + z + ".chest", i);
                    plugin.saveSigns();
                    player.sendMessage(Lang.TITLE.toString() + Lang.SET_SIGN);
                } else {
                    player.sendMessage(Lang.TITLE.toString() + Lang.NOT_A_SIGN);
                }
            } else {
                player.sendMessage(Lang.TITLE.toString() + Lang.NOT_A_SIGN);
            }
            return;
        }
        Block b = event.getClickedBlock();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST) {
                Location l = b.getLocation();
                String world = l.getWorld().getName();
                int x = l.getBlockX();
                int y = l.getBlockY();
                int z = l.getBlockZ();
                if (plugin.getSigns().getKeys(false).contains(world + ";;" + x + ";;" + y + ";;" + z)) {
                    int num = PlayerVaults.SIGNS.getInt(world + ";;" + x + ";;" + y + ";;" + z + ".chest");
                    if ((player.hasPermission("playervaults.signs.use") && (player.hasPermission("playervaults.signs.bypass") || VaultOperations.checkPerms(player, 99)))) {
                        boolean self = PlayerVaults.SIGNS.getBoolean(world + ";;" + x + ";;" + y + ";;" + z + ".self", false);
                        String owner = null;
                        if (!self) {
                            owner = PlayerVaults.SIGNS.getString(world + ";;" + x + ";;" + y + ";;" + z + ".owner");
                        }
                        Inventory inv = PlayerVaults.VM.loadOwnVault((self) ? player.getName() : owner, num, VaultOperations.getMaxVaultSize(player));
                        player.openInventory(inv);
                        PlayerVaults.IN_VAULT.put(player.getName(), new VaultViewInfo((self) ? player.getName() : owner, num));
                        event.setCancelled(true);
                        player.sendMessage(Lang.TITLE.toString() + Lang.OPEN_WITH_SIGN.toString().replace("%v", String.valueOf(num)).replace("%p", (self) ? player.getName() : owner));
                    } else {
                        player.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        blockChangeCheck(event.getBlock().getLocation());
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        blockChangeCheck(event.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        blockChangeCheck(event.getBlock().getLocation());
    }

    /**
     * Check if the location given is a sign, and if so, remove it from the signs.yml file
     *
     * @param location The location to check
     */
    public void blockChangeCheck(Location location) {
        String world = location.getWorld().getName();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        if (plugin.getSigns().getKeys(false).contains(world + ";;" + x + ";;" + y + ";;" + z)) {
            plugin.getSigns().set(world + ";;" + x + ";;" + y + ";;" + z, null);
            plugin.saveSigns();
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        EntityType type = event.getRightClicked().getType();
        if ((type == EntityType.VILLAGER || type == EntityType.MINECART) && PlayerVaults.IN_VAULT.containsKey(player.getName())) {
            event.setCancelled(true);
        }
    }
}
