package com.drtshock.playervaults.vaultmanagement;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.translations.Lang;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * A class that handles all economy operations.
 */
public class EconomyOperations {

    private static final PlayerVaults PLUGIN = PlayerVaults.getInstance();
    private static final FileConfiguration BUKKIT_CONFIG = PLUGIN.getConfig();

    /**
     * Have a player pay to open a vault.
     *
     * @param player The player to pay.
     * @param number The vault number to open.
     * @return The transaction success.
     */
    public static boolean payToOpen(Player player, int number) {
        if (!PLUGIN.isEconomyEnabled() || player.hasPermission("playervaults.free")) {
            return true;
        }

        if (!VaultManager.getInstance().vaultExists(player.getUniqueId().toString(), number)) {
            return payToCreate(player);
        } else {
            double cost = BUKKIT_CONFIG.getDouble("economy.cost-to-open", 10);
            EconomyResponse resp = PlayerVaults.getInstance().getEconomy().withdrawPlayer(player, cost);
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
        if (!PLUGIN.isEconomyEnabled() || player.hasPermission("playervaults.free")) {
            return true;
        }

        double cost = BUKKIT_CONFIG.getDouble("economy.cost-to-create", 100);
        EconomyResponse resp = PlayerVaults.getInstance().getEconomy().withdrawPlayer(player, cost);
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
        if (!PLUGIN.isEconomyEnabled() || player.hasPermission("playervaults.free")) {
            return true;
        }

        File playerFile = new File(PLUGIN.getVaultData(), player.getUniqueId().toString() + ".yml");
        if (playerFile.exists()) {
            YamlConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);
            if (playerData.getString("vault" + number) == null) {
                player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.VAULT_DOES_NOT_EXIST);
                return false;
            }
        } else {
            player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.VAULT_DOES_NOT_EXIST);
            return false;
        }

        double cost = BUKKIT_CONFIG.getDouble("economy.refund-on-delete");
        EconomyResponse resp = PlayerVaults.getInstance().getEconomy().depositPlayer(player, cost);
        if (resp.transactionSuccess()) {
            player.sendMessage(Lang.TITLE.toString() + Lang.REFUND_AMOUNT.toString().replaceAll("%price", String.valueOf(cost)));
            return true;
        }

        return false;
    }
}
