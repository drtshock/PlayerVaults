/*
 * PlayerVaultsX
 * Copyright (C) 2013 Trent Hensler
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
import com.drtshock.playervaults.vaultmanagement.VaultManager;
import com.drtshock.playervaults.vaultmanagement.VaultViewInfo;
import org.bukkit.Bukkit;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Collectors;

public class Listeners implements Listener {

    public final PlayerVaults plugin;
    private final VaultManager vaultManager = VaultManager.getInstance();

    public Listeners(PlayerVaults playerVaults) {
        this.plugin = playerVaults;
    }

    public void saveVault(Player player, Inventory inventory) {
        if (plugin.getInVault().containsKey(player.getUniqueId().toString())) {

            Inventory inv = Bukkit.createInventory(null, 6 * 9);
            inv.setContents(inventory.getContents().clone());

            PlayerVaults.debug(inventory.getType() + " " + inventory.getClass().getSimpleName());
            if (inventory.getViewers().size() <= 1) {
                PlayerVaults.debug("Saving!");
                VaultViewInfo info = plugin.getInVault().get(player.getUniqueId().toString());
                vaultManager.saveVault(inv, info.getVaultName(), info.getNumber());
                plugin.getOpenInventories().remove(info.toString());
            } else {
                PlayerVaults.debug("Other viewers found, not saving! " + inventory.getViewers().stream().map(HumanEntity::getName).collect(Collectors.joining(" ")));
            }

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
    public void onDeath(PlayerDeathEvent event) {
        saveVault(event.getEntity(), event.getEntity().getOpenInventory().getTopInventory());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClose(InventoryCloseEvent event) {
        saveVault((Player) event.getPlayer(), event.getInventory());
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

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory != null) {
            VaultViewInfo info = PlayerVaults.getInstance().getInVault().get(player.getUniqueId().toString());
            if (info != null) {
                int num = info.getNumber();
                String inventoryTitle = event.getView().getTitle();
                String title = this.plugin.getVaultTitle(String.valueOf(num));
                if (inventoryTitle.equalsIgnoreCase(title)) {
                    ItemStack[] items = new ItemStack[2];
                    items[0] = event.getCurrentItem();
                    if (event.getHotbarButton() > -1 && event.getWhoClicked().getInventory().getItem(event.getHotbarButton()) != null) {
                        items[1] = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                    }

                    for (ItemStack item : items) {
                        if (item == null) {
                            continue;
                        }
                        if (!player.hasPermission("playervaults.bypassblockeditems") && PlayerVaults.getInstance().isBlockedMaterial(item.getType())) {
                            event.setCancelled(true);
                            this.plugin.getTL().blockedItem().title().with("item", item.getType().name()).send(player);
                            return;
                        }
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

        Inventory clickedInventory = event.getInventory();
        if (clickedInventory != null) {
            VaultViewInfo info = PlayerVaults.getInstance().getInVault().get(player.getUniqueId().toString());
            if (info != null) {
                int num = info.getNumber();
                String inventoryTitle = event.getView().getTitle();
                String title = this.plugin.getVaultTitle(String.valueOf(num));
                if ((inventoryTitle != null && inventoryTitle.equalsIgnoreCase(title)) && event.getNewItems() != null) {
                    for (ItemStack item : event.getNewItems().values()) {
                        if (!player.hasPermission("playervaults.bypassblockeditems") && PlayerVaults.getInstance().isBlockedMaterial(item.getType())) {
                            event.setCancelled(true);
                            this.plugin.getTL().blockedItem().title().with("item", item.getType().name()).send(player);
                            return;
                        }
                    }
                }
            }
        }
    }
}