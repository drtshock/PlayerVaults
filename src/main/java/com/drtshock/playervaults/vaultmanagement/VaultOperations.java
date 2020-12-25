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

package com.drtshock.playervaults.vaultmanagement;

import com.drtshock.playervaults.PlayerVaults;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

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
                        PlayerVaults.getInstance().getTL().locked().title().send(player);
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
     * @return Whether or not they have permission.
     */
    public static boolean checkPerms(CommandSender sender, int number) {
        if (sender.hasPermission("playervaults.amount." + number)) {
            return true;
        }
        for (int x = number; x <= PlayerVaults.getInstance().getMaxVaultAmountPermTest(); x++) {
            if (sender.hasPermission("playervaults.amount." + x)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the max size vault a player is allowed to have.
     *
     * @param name that is having his permissions checked.
     * @return max size as integer. If no max size is set then it will default to the configured default.
     */
    public static int getMaxVaultSize(String name) {
        try {
            UUID uuid = UUID.fromString(name);
            return getMaxVaultSize(Bukkit.getOfflinePlayer(uuid));
        } catch (Exception e) {
            // Not a UUID
        }

        return PlayerVaults.getInstance().getDefaultVaultSize();
    }

    /**
     * Get the max size vault a player is allowed to have.
     *
     * @param player that is having his permissions checked.
     * @return max size as integer. If no max size is set then it will default to the configured default.
     */
    public static int getMaxVaultSize(OfflinePlayer player) {
        if (player == null || !player.isOnline()) {
            return 6 * 9;
        }
        for (int i = 6; i != 0; i--) {
            if (player.getPlayer().hasPermission("playervaults.size." + i)) {
                return i * 9;
            }
        }
        return PlayerVaults.getInstance().getDefaultVaultSize();
    }

    /**
     * Open a player's own vault.
     *
     * @param player The player to open to.
     * @param arg The vault number to open.
     * @return Whether or not the player was allowed to open it.
     */
    public static boolean openOwnVault(Player player, String arg) {
        return openOwnVaultE(player, arg, false, true);
    }

    public static boolean openOwnVaultSign(Player player, String arg) {
        return openOwnVaultE(player, arg, true, false);
    }

    private static boolean openOwnVaultE(Player player, String arg, boolean free, boolean send) {
        if (isLocked()) {
            return false;
        }
        if (player.isSleeping() || player.isDead() || !player.isOnline()) {
            return false;
        }
        int number;
        try {
            number = Integer.parseInt(arg);
            if (number < 1) {
                return false;
            }
        } catch (NumberFormatException nfe) {
            PlayerVaults.getInstance().getTL().mustBeNumber().title().send(player);
            return false;
        }

        if (checkPerms(player, number)) {
            if (free || EconomyOperations.payToOpen(player, number)) {
                Inventory inv = VaultManager.getInstance().loadOwnVault(player, number, getMaxVaultSize(player));
                if (inv == null) {
                    PlayerVaults.debug(String.format("Failed to open null vault %d for %s. This is weird.", number, player.getName()));
                    return false;
                }

                player.openInventory(inv);

                // Check if the inventory was actually opened
                if (player.getOpenInventory().getTopInventory() instanceof CraftingInventory || player.getOpenInventory().getTopInventory() == null) {
                    PlayerVaults.debug(String.format("Cancelled opening vault %s for %s from an outside source.", arg, player.getName()));
                    return false; // inventory open event was cancelled.
                }

                VaultViewInfo info = new VaultViewInfo(player.getUniqueId().toString(), number);
                PlayerVaults.getInstance().getOpenInventories().put(info.toString(), inv);

                if (send) {
                    PlayerVaults.getInstance().getTL().openVault().title().with("vault", arg).send(player);
                }
                return true;
            } else {
                PlayerVaults.getInstance().getTL().insufficientFunds().title().send(player);
                return false;
            }
        } else {
            PlayerVaults.getInstance().getTL().noPerms().title().send(player);
        }
        return false;
    }

    /**
     * Open a player's own vault. If player is using a command, they'll need the required permission.
     *
     * @param player The player to open to.
     * @param arg The vault number to open.
     * @param isCommand - if player is opening via a command or not.
     * @return Whether or not the player was allowed to open it.
     */
    public static boolean openOwnVault(Player player, String arg, boolean isCommand) {
        if (isCommand && player.hasPermission("playervaults.commands.use")) {
            return openOwnVault(player, arg);
        }
        PlayerVaults.getInstance().getTL().noPerms().title().send(player);
        return false;
    }

    /**
     * Open another player's vault.
     *
     * @param player The player to open to.
     * @param vaultOwner The name of the vault owner.
     * @param arg The vault number to open.
     * @return Whether or not the player was allowed to open it.
     */
    public static boolean openOtherVault(Player player, String vaultOwner, String arg) {
        return openOtherVault(player, vaultOwner, arg, true);
    }

    public static boolean openOtherVault(Player player, String vaultOwner, String arg, boolean send) {
        if (isLocked()) {
            return false;
        }

        if (player.isSleeping() || player.isDead() || !player.isOnline()) {
            return false;
        }

        long time = System.currentTimeMillis();

        int number = 0;
        try {
            number = Integer.parseInt(arg);
            if (number < 1) {
                PlayerVaults.getInstance().getTL().mustBeNumber().title().send(player);
                return false;
            }
        } catch (NumberFormatException nfe) {
            PlayerVaults.getInstance().getTL().mustBeNumber().title().send(player);
        }

        Inventory inv = VaultManager.getInstance().loadOtherVault(vaultOwner, number, getMaxVaultSize(vaultOwner));
        String name = vaultOwner;
        try {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(vaultOwner));
            name = offlinePlayer.getName();
        } catch (Exception e) {
            // not a player
        }

        if (inv == null) {
            PlayerVaults.getInstance().getTL().vaultDoesNotExist().title().send(player);
        } else {
            player.openInventory(inv);

            // Check if the inventory was actually opened
            if (player.getOpenInventory().getTopInventory() instanceof CraftingInventory || player.getOpenInventory().getTopInventory() == null) {
                PlayerVaults.debug(String.format("Cancelled opening vault %s for %s from an outside source.", arg, player.getName()));
                return false; // inventory open event was cancelled.
            }
            if (send) {
                PlayerVaults.getInstance().getTL().openOtherVault().title().with("vault", arg).with("player", name).send(player);
            }
            PlayerVaults.debug("opening other vault", time);

            // Need to set ViewInfo for a third party vault for the opening player.
            VaultViewInfo info = new VaultViewInfo(vaultOwner, number);
            PlayerVaults.getInstance().getInVault().put(player.getUniqueId().toString(), info);
            PlayerVaults.getInstance().getOpenInventories().put(player.getUniqueId().toString(), inv);
            return true;
        }

        PlayerVaults.debug("opening other vault returning false", time);
        return false;
    }

    /**
     * Delete a player's own vault.
     *
     * @param player The player to delete.
     * @param arg The vault number to delete.
     */
    public static void deleteOwnVault(Player player, String arg) {
        if (isLocked()) {
            return;
        }
        if (isNumber(arg)) {
            int number = 0;
            try {
                number = Integer.parseInt(arg);
                if (number == 0) {
                    PlayerVaults.getInstance().getTL().mustBeNumber().title().send(player);
                    return;
                }
            } catch (NumberFormatException nfe) {
                PlayerVaults.getInstance().getTL().mustBeNumber().title().send(player);
            }

            if (EconomyOperations.refundOnDelete(player, number)) {
                VaultManager.getInstance().deleteVault(player, player.getUniqueId().toString(), number);
                PlayerVaults.getInstance().getTL().deleteVault().title().with("vault", arg).send(player);
            }

        } else {
            PlayerVaults.getInstance().getTL().mustBeNumber().title().send(player);
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
        if (isLocked()) {
            return;
        }
        if (sender.hasPermission("playervaults.delete")) {
            if (isNumber(arg)) {
                int number = 0;
                try {
                    number = Integer.parseInt(arg);
                    if (number == 0) {
                        PlayerVaults.getInstance().getTL().mustBeNumber().title().send(sender);
                        return;
                    }
                } catch (NumberFormatException nfe) {
                    PlayerVaults.getInstance().getTL().mustBeNumber().title().send(sender);
                }

                VaultManager.getInstance().deleteVault(sender, holder, number);
                PlayerVaults.getInstance().getTL().deleteOtherVault().title().with("vault", arg).with("player", holder).send(sender);
            } else {
                PlayerVaults.getInstance().getTL().mustBeNumber().title().send(sender);
            }
        } else {
            PlayerVaults.getInstance().getTL().noPerms().title().send(sender);
        }
    }

    /**
     * Delete all of a player's vaults
     *
     * @param sender The sender executing the deletion.
     * @param holder The user to whom the deleted vault belongs.
     */
    public static void deleteOtherAllVaults(CommandSender sender, String holder) {
        if (isLocked() || holder == null) {
            return;
        }

        if (sender.hasPermission("playervaults.delete.all")) {
            VaultManager.getInstance().deleteAllVaults(holder);
            PlayerVaults.getInstance().getLogger().info(String.format("%s deleted ALL vaults belonging to %s", sender.getName(), holder));
        } else {
            PlayerVaults.getInstance().getTL().noPerms().title().send(sender);
        }
    }

    private static boolean isNumber(String check) {
        try {
            Integer.parseInt(check);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
