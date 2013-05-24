package com.drtshock.playervaults.commands;

import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.util.EconomyOperations;
import com.drtshock.playervaults.util.Lang;

public class VaultOperations {

    /**
     * Check whether or not the player has permission to open the requested vault.
     * @param sender The person to check.
     * @param number The vault number.
     * @return Whether or not they have permission.
     */
    public static boolean checkPerms(CommandSender sender, int number) {
        if (sender.hasPermission("playervaults.amount." + String.valueOf(number))) return true;
        for(int x = number; x <= 99; x++) {
            if (sender.hasPermission("playervaults.amount." + String.valueOf(x))) return true;
        }
        return false;
    }

    /**
     * Open a player's own vault.
     * @param player The player to open to.
     * @param arg The vault number to open.
     * @return Whether or not the player was allowed to open it.
     */
    public static boolean openOwnVault(Player player, String arg) {
        if (arg.matches("^[0-9]{1,2}$")) {
            int number = 0;
            try {
                number = Integer.parseInt(arg);
                if (number == 0)
                    return false;
            } catch(NumberFormatException nfe) {
                player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
                return false;
            }
            if (checkPerms(player, number)) {
                if (EconomyOperations.payToOpen(player)) {
                    Inventory inv = PlayerVaults.VM.loadVault(player.getName(), number);
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
                    if (number == 0)
                        return false;
                } catch(NumberFormatException nfe) {
                    player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
                }
                Inventory inv = PlayerVaults.VM.loadVault(holder, number);
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
     * @param player The player to delete.
     * @param user The user to whom the deleted vault belongs.
     * @param arg The vault number to delete.
     */
    public static void deleteOwnVault(Player player, String arg) {
        if (arg.matches("^[0-9]{1,2}$")) {
            int number = 0;
            try {
                number = Integer.parseInt(arg);
                if (number == 0)
                    player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
                return;
            } catch(NumberFormatException nfe) {
                player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
            }
            try {
                if (EconomyOperations.refundOnDelete(player, number)) {
                    PlayerVaults.VM.deleteVault(player, player.getName(), number);
                    return;
                }
            } catch(IOException e) {
                player.sendMessage(Lang.TITLE.toString() + Lang.DELETE_VAULT_ERROR);
            }
        } else {
            player.sendMessage(Lang.TITLE.toString() + Lang.MUST_BE_NUMBER);
        }
    }

    /**
     * Delete a player's own vault.
     * @param player The player to delete.
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
                } catch(NumberFormatException nfe) {
                    sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
                }
                try {
                    PlayerVaults.VM.deleteVault(sender, holder, number);
                } catch(IOException e) {
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
