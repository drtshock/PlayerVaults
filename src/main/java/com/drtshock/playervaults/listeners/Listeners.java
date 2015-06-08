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
import com.drtshock.playervaults.vaultmanagement.UUIDVaultManager;
import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import com.drtshock.playervaults.vaultmanagement.VaultViewInfo;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;

import java.io.IOException;

public class Listeners implements Listener {

    public PlayerVaults plugin;
    UUIDVaultManager vm = UUIDVaultManager.getInstance();

    public Listeners(PlayerVaults playerVaults) {
        this.plugin = playerVaults;
    }

    public void saveVault(Player player) {
        if (PlayerVaults.getInstance().getInVault().containsKey(player.getUniqueId().toString())) {
            Inventory inv = player.getOpenInventory().getTopInventory();
            if (inv.getViewers().size() == 1) {
                VaultViewInfo info = PlayerVaults.getInstance().getInVault().get(player.getUniqueId().toString());
                if (Bukkit.isPrimaryThread() && player.getUniqueId().equals(info.getHolderUUID())) {
                    // Running in main thread, and it's the player's own vault. So we can just cache this until logout.
                    UUIDVaultManager.getInstance().getCachedVaults().setCachedVault(info.getHolderUUID(), info.getNumber(), inv);
                } else {
                    try {
                        // Cache and save.
                        UUIDVaultManager.getInstance().getCachedVaults().setCachedVault(info.getHolderUUID(), info.getNumber(), inv);
                        vm.saveVault(inv, info.getHolderUUID(), info.getNumber());
                    } catch (IOException e) {
                        // ignore
                    }
                }

                PlayerVaults.getInstance().getOpenInventories().remove(info.toString());
            }

            PlayerVaults.getInstance().getInVault().remove(player.getUniqueId().toString());
        }
    }

    public void flushVaultCache(Player player) {
        UUIDVaultManager.getInstance().getCachedVaults().flushVaultCacheToFile(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        saveVault(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        saveVault(event.getPlayer());
        flushVaultCache(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (PlayerVaults.getInstance().needsUpdate() && (player.isOp() || player.hasPermission("playervaults.notify"))) {
            player.sendMessage(ChatColor.GREEN + "Version " + ChatColor.RED + PlayerVaults.getInstance().getNewVersion() + ChatColor.GREEN + " of PlayerVaults is available for download!");
            player.sendMessage(ChatColor.GOLD + "http://dev.bukkit.org/pancakes/playervaults" + ChatColor.GREEN + " to view the changelog and download!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        saveVault(event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClose(InventoryCloseEvent event) {
        HumanEntity he = event.getPlayer();
        if (he instanceof Player) {
            saveVault((Player) he);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (PlayerVaults.getInstance().getInVault().containsKey(player.getName())) {
                Block block = event.getClickedBlock();
                // Different inventories that we don't want the player to open.
                if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST || block.getType() == Material.ENDER_CHEST || block.getType() == Material.FURNACE || block.getType() == Material.BURNING_FURNACE || block.getType() == Material.BREWING_STAND || block.getType() == Material.ENCHANTMENT_TABLE || block.getType() == Material.BEACON) {
                    event.setCancelled(true);
                }
            }
        }
        if (PlayerVaults.getInstance().getSetSign().containsKey(player.getName())) {
            int i = PlayerVaults.getInstance().getSetSign().get(player.getName()).getChest();
            boolean self = PlayerVaults.getInstance().getSetSign().get(player.getName()).isSelf();
            String owner = self ? null : PlayerVaults.getInstance().getSetSign().get(player.getName()).getOwner();
            PlayerVaults.getInstance().getSetSign().remove(player.getName());
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
                        plugin.getSigns().set(world + ";;" + x + ";;" + y + ";;" + z + ".self", true);
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
                    int num = PlayerVaults.getInstance().getSigns().getInt(world + ";;" + x + ";;" + y + ";;" + z + ".chest", 1);
                    if (player.hasPermission("playervaults.signs.use") || player.hasPermission("playervaults.signs.bypass")) {
                        boolean self = PlayerVaults.getInstance().getSigns().getBoolean(world + ";;" + x + ";;" + y + ";;" + z + ".self", false);
                        String owner = self ? player.getName() : PlayerVaults.getInstance().getSigns().getString(world + ";;" + x + ";;" + y + ";;" + z + ".owner");
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner != null ? owner : event.getPlayer().getName()); // Not best way but :\
                        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
                            player.sendMessage(Lang.TITLE.toString() + Lang.VAULT_DOES_NOT_EXIST.toString());
                            return;
                        }
                        if (self) {
                            // We already checked that they can use signs, now lets check if they have this many vaults.
                            if (VaultOperations.checkPerms(player, num)) {
                                Inventory inv = UUIDVaultManager.getInstance().loadOwnVault(player, num, VaultOperations.getMaxVaultSize(player));
                                player.openInventory(inv);
                            } else {
                                player.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS.toString());
                                return; // Otherwise it would try to add vault view info down there.
                            }
                        } else {
                            Inventory inv = UUIDVaultManager.getInstance().loadOtherVault(offlinePlayer.getUniqueId(), num, VaultOperations.getMaxVaultSize(offlinePlayer));
                            if (inv == null) {
                                player.sendMessage(Lang.TITLE.toString() + Lang.VAULT_DOES_NOT_EXIST.toString());
                            } else {
                                player.openInventory(inv);
                            }
                        }
                        PlayerVaults.getInstance().getInVault().put(player.getUniqueId().toString(), new VaultViewInfo((self) ? player.getName() : owner, num));
                        event.setCancelled(true);
                        player.sendMessage(Lang.TITLE.toString() + Lang.OPEN_WITH_SIGN.toString().replace("%v", String.valueOf(num)).replace("%p", (self) ? player.getName() : owner));
                    } else {
                        player.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        blockChangeCheck(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        blockChangeCheck(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        EntityType type = event.getRightClicked().getType();
        if ((type == EntityType.VILLAGER || type == EntityType.MINECART) && PlayerVaults.getInstance().getInVault().containsKey(player.getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (player.hasPermission("playervaults.bypassblockeditems")) {
            return;
        }

        VaultViewInfo info = PlayerVaults.getInstance().getInVault().get(player.getUniqueId().toString());
        if (info != null) {
            int num = info.getNumber();
            String title = Lang.VAULT_TITLE.toString().replace("%number", String.valueOf(num)).replace("%p", info.getHolder());
            if ((event.getClickedInventory().getTitle() != null && event.getClickedInventory().getTitle().equalsIgnoreCase(title) || event.isShiftClick()) && event.getCurrentItem() != null) {
                if (PlayerVaults.getInstance().isBlockedMaterial(event.getCurrentItem().getType())) {
                    event.setCancelled(true);
                    player.sendMessage(Lang.TITLE.toString() + Lang.BLOCKED_ITEM.toString().replace("%m", event.getCurrentItem().getType().name()));
                }
            }
        }

    }
}
