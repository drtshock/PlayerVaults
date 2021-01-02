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
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;

/**
 * A class that handles all economy operations.
 */
public class EconomyOperations {

    private static Economy economy;

    public static boolean setup() {
        economy = null;
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> provider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (provider != null) {
                economy = provider.getProvider();
                return true;
            }
        }
        return false;
    }

    public static String getName() {
        return economy == null ? "NONE" : economy.getName();
    }

    public static String getPermsName() {
        RegisteredServiceProvider<Permission> provider = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        if (provider != null) {
            Permission perm = provider.getProvider();
            return perm.getName();
        }
        return null;
    }

    /**
     * Have a player pay to open a vault.
     *
     * @param player The player to pay.
     * @param number The vault number to open.
     * @return The transaction success.
     */
    public static boolean payToOpen(Player player, int number) {
        if (!PlayerVaults.getInstance().isEconomyEnabled() || player.hasPermission("playervaults.free")) {
            return true;
        }

        if (!VaultManager.getInstance().vaultExists(player.getUniqueId().toString(), number)) {
            return payToCreate(player);
        } else {
            if (PlayerVaults.getInstance().getConf().getEconomy().getFeeToOpen() == 0) {
                return true;
            }
            double cost = PlayerVaults.getInstance().getConf().getEconomy().getFeeToOpen();
            EconomyResponse resp = economy.withdrawPlayer(player, cost);
            if (resp.transactionSuccess()) {
                PlayerVaults.getInstance().getTL().costToOpen().title().with("price", cost + "").send(player);
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
        if (!PlayerVaults.getInstance().isEconomyEnabled() || PlayerVaults.getInstance().getConf().getEconomy().getFeeToCreate() == 0 || player.hasPermission("playervaults.free")) {
            return true;
        }

        double cost = PlayerVaults.getInstance().getConf().getEconomy().getFeeToCreate();
        EconomyResponse resp = economy.withdrawPlayer(player, cost);
        if (resp.transactionSuccess()) {
            PlayerVaults.getInstance().getTL().costToCreate().title().with("price", cost + "").send(player);
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
        if (!PlayerVaults.getInstance().isEconomyEnabled() || PlayerVaults.getInstance().getConf().getEconomy().getRefundOnDelete() == 0 || player.hasPermission("playervaults.free")) {
            return true;
        }

        File playerFile = new File(PlayerVaults.getInstance().getVaultData(), player.getUniqueId().toString() + ".yml");
        if (playerFile.exists()) {
            YamlConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);
            if (playerData.getString("vault" + number) == null) {
                PlayerVaults.getInstance().getTL().vaultDoesNotExist().title().send(player);
                return false;
            }
        } else {
            PlayerVaults.getInstance().getTL().vaultDoesNotExist().title().send(player);
            return false;
        }

        double cost = PlayerVaults.getInstance().getConf().getEconomy().getRefundOnDelete();
        EconomyResponse resp = economy.depositPlayer(player, cost);
        if (resp.transactionSuccess()) {
            PlayerVaults.getInstance().getTL().refundAmount().title().with("price", cost + "").send(player);
            return true;
        }

        return false;
    }
}
