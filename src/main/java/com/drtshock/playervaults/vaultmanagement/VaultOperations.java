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
import com.drtshock.playervaults.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class VaultOperations {

    private static final AtomicBoolean LOCKED = new AtomicBoolean(false);

    /**
     * Gets whether or not player vaults are locked
     *
     * @return true if locked, false otherwise
     */
    public static boolean isLocked() {
        return LOCKED.get();
    }

    /**
     * Sets whether or not player vaults are locked. If set to true, this will kick anyone who is currently using their
     * vaults out.
     *
     * @param locked true for locked, false otherwise
     */
    public static void setLocked(boolean locked) {
        LOCKED.set(locked);

        if (locked) {
            for (Player player : PlayerVaults.getInstance().getServer().getOnlinePlayers()) {
                if (player.getOpenInventory() != null) {
                    InventoryView view = player.getOpenInventory();
                    if (view.getTopInventory().getHolder() instanceof VaultHolder) {
                        player.closeInventory();
                        player.sendMessage(Lang.TITLE + Lang.LOCKED.toString());
                    }
                }
            }
        }
    }

    /**
     * Check whether or not the player has permission to open the requested vault.
     *
     * @param sender The person to check.
     * @param number The vault number.
     *
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
     * @param target that is having his permissions checked.
     *
     * @return max size as integer. If no max size is set then it will default to 54.
     */
    public static int getMaxVaultSize(String target) {
        UUID uuid;
        try {
            uuid = UUID.fromString(target);
        } catch (Exception e) {
            return 54;
        }

        return getMaxVaultSize(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Get the max size vault a player is allowed to have.
     *
     * @param player that is having his permissions checked.
     *
     * @return max size as integer. If no max size is set then it will default to 54.
     */
    public static int getMaxVaultSize(OfflinePlayer player) {
        if (player == null || !player.hasPlayedBefore() || !player.isOnline()) {
            return 54;
        }
        for (int i = 6; i != 0; i--) {
            if (player.getPlayer().hasPermission("playervaults.size." + i)) {
                return i * 9;
            }
        }
        return 54;
    }

    /**
     * Open a player's own vault.
     *
     * @param player The player to open to.
     * @param arg    The vault number to open.
     *
     * @return Whether or not the player was allowed to open it.
     */
    public static boolean openOwnVault(Player player, String arg) {
        if (isLocked()) {
            return false;
        }
        int number;
        try {
            number = Integer.parseInt(arg);
            if (number < 1) {
                return false;
            }
        } catch (NumberFormatException nfe) {
            player.sendMessage(Lang.TITLE.toString() + Lang.MUST_BE_NUMBER.toString());
            return false;
        }

        if (checkPerms(player, number)) {
            if (EconomyOperations.payToOpen(player, number)) {
                Inventory inv = UUIDVaultManager.getInstance().loadOwnVault(player, number, getMaxVaultSize(player));
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
        return false;
    }

    /**
     * Open a player's own vault. If player is using a command, they'll need the required permission.
     *
     * @param player    The player to open to.
     * @param arg       The vault number to open.
     * @param isCommand - if player is opening via a command or not.
     *
     * @return Whether or not the player was allowed to open it.
     */
    public static boolean openOwnVault(Player player, String arg, boolean isCommand) {
        if (isCommand && player.hasPermission("playervaults.commands.use")) {
            return openOwnVault(player, arg);
        }
        player.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS.toString());
        return false;
    }

    /**
     * Open another player's vault.
     *
     * @param player The player to open to.
     * @param holder The user to whom the requested vault belongs.
     * @param arg    The vault number to open.
     *
     * @return Whether or not the player was allowed to open it.
     */
    public static boolean openOtherVault(Player player, String holder, String arg) {
        if (isLocked()) {
            return false;
        }

        long time = System.currentTimeMillis();

        String nicename = holder;
        int number = 0;
        try {
            number = Integer.parseInt(arg);
            if (number < 1) {
                player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
                return false;
            }
        } catch (NumberFormatException nfe) {
            player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
        }

        // Try to get the vault of an OfflinePlayer if we don't find one with the name first.
        if (!UUIDVaultManager.getInstance().vaultExists(holder, number)) {
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(holder);
            if (targetPlayer.hasPlayedBefore()) {
                holder = targetPlayer.getUniqueId().toString();
            }
        }


        Inventory inv = UUIDVaultManager.getInstance().loadOtherVault(holder, number, getMaxVaultSize(holder));
        if (inv == null) {
            player.sendMessage(Lang.TITLE.toString() + Lang.VAULT_DOES_NOT_EXIST.toString());
        } else {
            player.openInventory(inv);
            player.sendMessage(Lang.TITLE.toString() + Lang.OPEN_OTHER_VAULT.toString().replace("%v", arg).replace("%p", nicename));
            PlayerVaults.debug("opening other vault", time);
            return true;
        }

        PlayerVaults.debug("opening other vault returning false", time);
        return false;
    }

    /**
     * Delete a player's own vault.
     *
     * @param player The player to delete.
     * @param arg    The vault number to delete.
     */
    public static void deleteOwnVault(Player player, String arg) {
        if (isLocked()) {
            return;
        }
        if (arg.matches("^[0-9]{1,2}$")) {
            int number = 0;
            try {
                number = Integer.parseInt(arg);
                if (number == 0) {
                    player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
                    return;
                }
            } catch (NumberFormatException nfe) {
                player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
            }

            try {
                if (EconomyOperations.refundOnDelete(player, number)) {
                    UUIDVaultManager.getInstance().deleteVault(player, player.getUniqueId().toString(), number);
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
     * @param arg    The vault number to delete.
     */
    public static void deleteOtherVault(CommandSender sender, OfflinePlayer holder, String arg) {
        if (isLocked()) {
            return;
        }
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
                    UUIDVaultManager.getInstance().deleteVault(sender, holder.getUniqueId().toString(), number);
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