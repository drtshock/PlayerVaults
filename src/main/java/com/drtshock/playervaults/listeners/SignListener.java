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
import com.drtshock.playervaults.translations.Lang;
import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import com.drtshock.playervaults.vaultmanagement.VaultViewInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

public class SignListener implements Listener {
    private PlayerVaults plugin;

    /**
     * TODO: Some of these events can be lag inducing (specifically: interactions & block breaking),
     * perhaps we should try to optimize these listeners at some point?
     */

    public SignListener(PlayerVaults plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!PlayerVaults.getInstance().getConf().isSigns()) {
            return;
        }
        Player player = event.getPlayer();
        if (player.isSleeping() || player.isDead() || !player.isOnline()) {
            return;
        }
        Block block = event.getClickedBlock();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (PlayerVaults.getInstance().getInVault().containsKey(player.getUniqueId().toString())) {
                // Different inventories that we don't want the player to open.
                if (isInvalidBlock(block)) {
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
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (block != null && plugin.isSign(block.getType())) {
                    Sign s = (Sign) block.getState();
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
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (block != null && plugin.isSign(block.getType())) {
                Location l = block.getLocation();
                String world = l.getWorld().getName();
                int x = l.getBlockX();
                int y = l.getBlockY();
                int z = l.getBlockZ();
                if (plugin.getSigns().getKeys(false).contains(world + ";;" + x + ";;" + y + ";;" + z)) {
                    PlayerVaults.debug("Player " + player.getName() + " clicked sign at world(" + x + "," + y + "," + z + ")");
                    if (PlayerVaults.getInstance().getInVault().containsKey(player.getUniqueId().toString())) {
                        // don't let them open another vault.
                        PlayerVaults.debug("Player " + player.getName() + " denied sign vault because already in a vault!");
                        return;
                    }
                    int num = PlayerVaults.getInstance().getSigns().getInt(world + ";;" + x + ";;" + y + ";;" + z + ".chest", 1);
                    String numS = String.valueOf(num);
                    if (player.hasPermission("playervaults.signs.use") || player.hasPermission("playervaults.signs.bypass")) {
                        boolean self = PlayerVaults.getInstance().getSigns().getBoolean(world + ";;" + x + ";;" + y + ";;" + z + ".self", false);
                        String owner = self ? player.getName() : PlayerVaults.getInstance().getSigns().getString(world + ";;" + x + ";;" + y + ";;" + z + ".owner");
                        PlayerVaults.debug("Player " + player.getName() + " wants to open a " + (self ? "self" : "non-self (" + owner + ")") + " sign vault");
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner != null ? owner : event.getPlayer().getName()); // Not best way but :\
                        if (offlinePlayer == null || (!offlinePlayer.isOnline() && !offlinePlayer.hasPlayedBefore())) {
                            PlayerVaults.debug("Denied sign vault for never-seen-before owner " + owner);
                            player.sendMessage(Lang.TITLE.toString() + Lang.VAULT_DOES_NOT_EXIST.toString());
                            return;
                        }
                        if (self) {
                            // We already checked that they can use signs, now lets check if they have this many vaults.
                            if (VaultOperations.openOwnVault(player, numS, true)) {
                                PlayerVaults.getInstance().getInVault().put(player.getUniqueId().toString(), new VaultViewInfo(player.getUniqueId().toString(), num));
                            } else {
                                PlayerVaults.debug("Player " + player.getName() + " failed to open sign vault!");
                                return;
                            }
                        } else {
                            if (!VaultOperations.openOtherVault(player, owner, numS, false)) {
                                PlayerVaults.debug("Player " + player.getName() + " failed to open sign vault!");
                                return;
                            }
                        }
                        PlayerVaults.debug("Player " + player.getName() + " succeeded in opening sign vault");
                        event.setCancelled(true);
                        player.sendMessage(Lang.TITLE.toString() + Lang.OPEN_WITH_SIGN.toString().replace("%v", String.valueOf(num)).replace("%p", owner));
                    } else {
                        player.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS);
                        PlayerVaults.debug("Player " + player.getName() + " no sign perms!");
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!PlayerVaults.getInstance().getConf().isSigns()) {
            return;
        }
        blockChangeCheck(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!PlayerVaults.getInstance().getConf().isSigns()) {
            return;
        }
        blockChangeCheck(event.getBlock().getLocation());
    }

    /**
     * Check if the location given is a sign, and if so, remove it from the signs.yml file
     *
     * @param location The location to check
     */
    public void blockChangeCheck(Location location) {
        if (plugin.getSigns().getKeys(false).isEmpty()) {
            return; // Save us a check.
        }

        String world = location.getWorld().getName();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        if (plugin.getSigns().getKeys(false).contains(world + ";;" + x + ";;" + y + ";;" + z)) {
            plugin.getSigns().set(world + ";;" + x + ";;" + y + ";;" + z, null);
            plugin.saveSigns();
        }
    }

    private boolean isInvalidBlock(Block block) {
        String type = block.getType().name();
        return block.getState() instanceof InventoryHolder || type.contains("ENCHANT") || type.equals("ENDER_CHEST");
    }
}