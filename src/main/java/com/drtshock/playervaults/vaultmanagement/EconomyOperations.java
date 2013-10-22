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

import java.io.File;
import java.io.IOException;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * A class that handles all economy operations.
 */
public class EconomyOperations {

    private static YamlConfiguration BUKKIT_CONFIG = new YamlConfiguration();
    public static PlayerVaults PLUGIN;

    public EconomyOperations(PlayerVaults instance) throws IOException, InvalidConfigurationException {
        PLUGIN = instance;
        File config = new File(PLUGIN.getDataFolder(), "config.yml");
        BUKKIT_CONFIG.load(config);
    }

    /**
     * Have a player pay to open a vault.
     *
     * @param player The player to pay.
     * @param number The vault number to open.
     * @return The transaction success.
     */
    public static boolean payToOpen(Player player, int number) {
        if (!BUKKIT_CONFIG.getBoolean("economy.enabled") || player.hasPermission("playervaults.free") || !PlayerVaults.USE_VAULT) {
            return true;
        }
        if (PlayerVaults.VM.vaultExists(player.getName(), number)) {
            return payToCreate(player);
        } else {
            double cost = BUKKIT_CONFIG.getDouble("economy.cost-to-create", 100);
            EconomyResponse resp = PlayerVaults.ECON.withdrawPlayer(player.getName(), cost);
            if (resp.transactionSuccess()) {
                player.sendMessage(Lang.TITLE.toString() + Lang.COST_TO_OPEN.toString().replaceAll("%price", "" + cost));
                return true;
            }
        }
        return false;
    }

    /**
     * Have a player pay to create a vault.
     *
     * @param player The player to pay.
     * @return The transaction success
     */
    public static boolean payToCreate(Player player) {
        if (!BUKKIT_CONFIG.getBoolean("economy.enabled") || player.hasPermission("playervaults.free") || !PlayerVaults.USE_VAULT) {
            return true;
        }

        double cost = BUKKIT_CONFIG.getDouble("economy.cost-to-create", 100);
        EconomyResponse resp = PlayerVaults.ECON.withdrawPlayer(player.getName(), cost);
        if (resp.transactionSuccess()) {
            player.sendMessage(Lang.TITLE.toString() + Lang.COST_TO_CREATE.toString().replaceAll("%price", "" + cost));
            return true;
        }
        return false;
    }

    /**
     * Have a player get his money back when vault is deleted.
     *
     * @param player The player to receive the money.
     * @param number The vault number to delete.
     * @return The transaction success.
     */
    public static boolean refundOnDelete(Player player, int number) {
        String directory = "plugins" + File.separator + "PlayerVaults" + File.separator + "vaults";
        if (!BUKKIT_CONFIG.getBoolean("economy.enabled") || player.hasPermission("playervaults.free") || !PlayerVaults.USE_VAULT) {
            return true;
        }
        String name = player.getName().toLowerCase();
        File file = new File(directory + File.separator + name.toLowerCase() + ".yml");
        YamlConfiguration playerFile = YamlConfiguration.loadConfiguration(file);
        if (file.exists()) {
            if (playerFile.getString("vault" + number) == null) {
                player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.VAULT_DOES_NOT_EXIST);
                return false;
            }
        } else {
            player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.VAULT_DOES_NOT_EXIST);
            return false;
        }
        double cost = BUKKIT_CONFIG.getDouble("economy.refund-on-delete");
        EconomyResponse resp = PlayerVaults.ECON.depositPlayer(player.getName(), cost);
        if (resp.transactionSuccess()) {
            player.sendMessage(Lang.TITLE.toString() + Lang.REFUND_AMOUNT.toString().replaceAll("%price", String.valueOf(cost)));
            return true;
        }
        return false;
    }
}
