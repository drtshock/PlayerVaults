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
package com.drtshock.playervaults.vaultmanagement;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.util.EconomyOperations;
import com.drtshock.playervaults.util.Lang;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class VaultOperations {

    /**
     * Check whether or not the player has permission to open the requested
     * vault.
     *
     * @param sender The person to check.
     * @param number The vault number.
     * @return Whether or not they have permission.
     */
    public static boolean checkPerms(CommandSender sender, int number) {
        if (sender.hasPermission("playervaults.amount." + String.valueOf(number))) {
            return true;
        }
        for (int x = number; x <= 99; x++) {
            if (sender.hasPermission("playervaults.amount." + String.valueOf(x))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the max size vault a player is allowed to have.
     *
     * @param player that is having his permissions checked.
     * @return max size as integer. If no max size is set then it will default
     * to 54.
     */
    public static int getMaxVaultSize(Player player) {
        for (int i = 6; i != 0; i--) {
            if (player.hasPermission("playervaults.size." + i)) {
                return i * 9;
            }
        }
        return 54;
    }

    /**
     * Open a player's own vault.
     *
     * @param player The player to open to.
     * @param arg The vault number to open.
     * @return Whether or not the player was allowed to open it.
     */
    public static boolean openOwnVault(Player player, String arg) {
        if (arg.matches("^[0-9]{1,2}$")) {
            int number;
            try {
                number = Integer.parseInt(arg);
                if (number == 0) {
                    return false;
                }
            } catch (NumberFormatException nfe) {
                player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
                return false;
            }
            if (checkPerms(player, number)) {
                if (EconomyOperations.payToOpen(player, number)) {
                    PlayerVaults.LOG.info(String.valueOf(player.hasPermission("playervaults.small")));
                    Inventory inv = PlayerVaults.VM.loadVault(player.getName(), number, getMaxVaultSize(player));
                    player.openInventory(inv);
                    player.sendMessage(Lang.TITLE.toString() + Lang.OPEN_VAULT.toString().replace("%v", arg));
                    return true;
                } else {
                    player.sendMessage(Lang.TITLE.toString() + Lang.INSUFFICIENT_FUNDS);
                    return false;
                }
            } else {
                player.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS);
            }
        } else {
            player.sendMessage(Lang.TITLE.toString() + Lang.MUST_BE_NUMBER);
        }
        return false;
    }

    /**
     * Open another player's vault.
     *
     * @param player The player to open to.
     * @param holder The user to whom the requested vault belongs.
     * @param arg The vault number to open.
     * @return Whether or not the player was allowed to open it.
     */
    public static boolean openOtherVault(Player player, String holder, String arg) {
        if (player.hasPermission("playervaults.admin")) {
            if (arg.matches("^[0-9]{1,2}$")) {
                int number = 0;
                try {
                    number = Integer.parseInt(arg);
                    if (number == 0) {
                        return false;
                    }
                } catch (NumberFormatException nfe) {
                    player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
                }
                Inventory inv = PlayerVaults.VM.loadVault(holder, number, getMaxVaultSize(Bukkit.getPlayerExact(holder)));
                player.openInventory(inv);
                player.sendMessage(Lang.TITLE.toString() + Lang.OPEN_OTHER_VAULT.toString().replace("%v", arg).replace("%p", holder));
                return true;
            } else {
                player.sendMessage(Lang.TITLE.toString() + Lang.MUST_BE_NUMBER);
            }
        } else {
            player.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS);
        }
        return false;
    }

    /**
     * Delete a player's own vault.
     *
     * @param player The player to delete.
     * @param arg The vault number to delete.
     */
    public static void deleteOwnVault(Player player, String arg) {
        if (arg.matches("^[0-9]{1,2}$")) {
            int number = 0;
            try {
                number = Integer.parseInt(arg);
                if (number == 0) {
                    player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
                }
                return;
            } catch (NumberFormatException nfe) {
                player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
            }
            try {
                if (EconomyOperations.refundOnDelete(player, number)) {
                    PlayerVaults.VM.deleteVault(player, player.getName(), number);
                }
            } catch (IOException e) {
                player.sendMessage(Lang.TITLE.toString() + Lang.DELETE_VAULT_ERROR);
            }
        } else {
            player.sendMessage(Lang.TITLE.toString() + Lang.MUST_BE_NUMBER);
        }
    }

    /**
     * Delete a player's vault.
     *
     * @param sender The sender executing the deletion.
     * @param holder The user to whom the deleted vault belongs.
     * @param arg The vault number to delete.
     */
    public static void deleteOtherVault(CommandSender sender, String holder, String arg) {
        if (sender.hasPermission("playervaults.delete")) {
            if (arg.matches("^[0-9]{1,2}$")) {
                int number = 0;
                try {
                    number = Integer.parseInt(arg);
                    if (number == 0) {
                        sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
                        return;
                    }
                } catch (NumberFormatException nfe) {
                    sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
                }
                try {
                    PlayerVaults.VM.deleteVault(sender, holder, number);
                } catch (IOException e) {
                    sender.sendMessage(Lang.TITLE.toString() + Lang.DELETE_VAULT_ERROR);
                }
            } else {
                sender.sendMessage(Lang.TITLE.toString() + Lang.MUST_BE_NUMBER);
            }
        } else {
            sender.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS);
        }
    }
}