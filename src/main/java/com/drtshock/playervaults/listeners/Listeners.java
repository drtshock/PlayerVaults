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
import com.drtshock.playervaults.vaultmanagement.VaultViewInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
public class Listeners implements Listener {

    public final PlayerVaults plugin;
    private final UUIDVaultManager vaultManager = UUIDVaultManager.getInstance();

    public Listeners(PlayerVaults playerVaults) {
        this.plugin = playerVaults;
    }

    public void saveVault(Player player, Inventory inventory) {
        if (plugin.getInVault().containsKey(player.getUniqueId().toString())) {

            VaultViewInfo info = plugin.getInVault().get(player.getUniqueId().toString());
            Inventory inv = Bukkit.createInventory(null, 6 * 9);
            inv.setContents(inventory.getContents().clone());

            if (inventory.getViewers().size() == 1 || info.getHolderUUID() == null) {
                // DON'T save when others are viewing. Caused saving as username.yml
                // TODO: verify this doesn't break faction / group vaults.
                return;
            }

            String target = info.getHolderUUID().toString();
            vaultManager.saveVault(inv, target, info.getNumber());

            plugin.getOpenInventories().remove(info.toString());
            plugin.getInVault().remove(player.getUniqueId().toString());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN) {
            return;
        }
        Player p = event.getPlayer();
        // The player will either quit, die, or close the inventory at some point
        if (plugin.getInVault().containsKey(p.getUniqueId().toString())) {
            return;
        }
        saveVault(p, p.getOpenInventory().getTopInventory());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        saveVault(event.getPlayer(), event.getPlayer().getOpenInventory().getTopInventory());
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
        saveVault(event.getEntity(), event.getEntity().getOpenInventory().getTopInventory());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClose(InventoryCloseEvent event) {
        HumanEntity he = event.getPlayer();
        if (he instanceof Player) {
            saveVault((Player) he, event.getInventory());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        EntityType type = event.getRightClicked().getType();
        if ((type == EntityType.VILLAGER || type == EntityType.MINECART) && PlayerVaults.getInstance().getInVault().containsKey(player.getUniqueId().toString())) {
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

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory != null) {
            VaultViewInfo info = PlayerVaults.getInstance().getInVault().get(player.getUniqueId().toString());
            if (info != null) {
                int num = info.getNumber();
                String inventoryTitle = clickedInventory.getTitle();
                String title = Lang.VAULT_TITLE.toString().replace("%number", String.valueOf(num)).replace("%p", info.getHolder());
                if (((inventoryTitle != null && inventoryTitle.equalsIgnoreCase(title)) || event.isShiftClick()) && event.getCurrentItem() != null) {
                    if (PlayerVaults.getInstance().isBlockedMaterial(event.getCurrentItem().getType())) {
                        event.setCancelled(true);
                        player.sendMessage(Lang.TITLE.toString() + Lang.BLOCKED_ITEM.toString().replace("%m", event.getCurrentItem().getType().name()));
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (player.hasPermission("playervaults.bypassblockeditems")) {
            return;
        }

        Inventory clickedInventory = event.getInventory();
        if (clickedInventory != null) {
            VaultViewInfo info = PlayerVaults.getInstance().getInVault().get(player.getUniqueId().toString());
            if (info != null) {
                int num = info.getNumber();
                String inventoryTitle = clickedInventory.getTitle();
                String title = Lang.VAULT_TITLE.toString().replace("%number", String.valueOf(num)).replace("%p", info.getHolder());
                if ((inventoryTitle != null && inventoryTitle.equalsIgnoreCase(title)) && event.getNewItems() != null) {
                    for (ItemStack item : event.getNewItems().values()) {
                        if (PlayerVaults.getInstance().isBlockedMaterial(item.getType())) {
                            event.setCancelled(true);
                            player.sendMessage(Lang.TITLE.toString() + Lang.BLOCKED_ITEM.toString().replace("%m", item.getType().name()));
                            return;
                        }
                    }
                }
            }
        }
    }
}