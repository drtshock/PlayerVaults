package com.drtshock.playervaults.util;

import org.bukkit.entity.Player;

public class PermissionChecks {

    /**
     * Check if a player can open a request vault.
     *
     * @param player - player in question.
     * @param number - vault number in question
     *
     * @return - true if permitted, otherwise false.
     */
    public static boolean canOpenVault(Player player, int number) {
        if (player.isOp() && player.hasPermission("playervaults.admin") && player.hasPermission("playervaults.amount." + String.valueOf(number))) {
            return true;
        }
        for (int x = number; x <= 99; x++) {
            if (player.hasPermission("playervaults.amount." + String.valueOf(x))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the max size vault a player is allowed to have.
     *
     * @param player that is having his permissions checked.
     *
     * @return max size as int. If no max size is set then it will default to 54.
     */
    public static int getMaxVaultSize(Player player) {
        if (player == null) {
            return 54;
        }
        for (int i = 6; i != 0; i--) {
            if (player.hasPermission("playervaults.size." + i)) {
                return i * 9;
            }
        }
        return 54;
    }

}
