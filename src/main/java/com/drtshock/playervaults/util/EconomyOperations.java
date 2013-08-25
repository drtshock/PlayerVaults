package com.drtshock.playervaults.util;

import com.drtshock.playervaults.PlayerVaults;

import java.io.File;
import java.io.FileNotFoundException;
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

    private static File CONFIG_FILE;
    private static YamlConfiguration BUKKIT_CONFIG = new YamlConfiguration();

    public static PlayerVaults PLUGIN;

    public EconomyOperations(PlayerVaults instance) throws FileNotFoundException, IOException, InvalidConfigurationException {
        PLUGIN = instance;
        CONFIG_FILE = new File(PLUGIN.getDataFolder(), "config.yml");
        BUKKIT_CONFIG.load(CONFIG_FILE);
    }

    /**
     * Have a player pay to open a vault.
     * @param player The player to pay.
     * @return The transaction success.
     */
    public static boolean payToOpen(Player player) {
        if (!BUKKIT_CONFIG.getBoolean("economy.enabled") || player.hasPermission("playervaults.free") || !PlayerVaults.USE_VAULT)
            return true;

        double cost = BUKKIT_CONFIG.getDouble("economy.cost-to-open", 10);
        EconomyResponse resp = PlayerVaults.ECON.withdrawPlayer(player.getName(), cost);
        if (resp.transactionSuccess()) {
            player.sendMessage(Lang.TITLE.toString() + Lang.COST_TO_OPEN.toString().replaceAll("%price", "" + cost));
            return true;
        }

        return false;
    }

    /**
     * Have a player pay to create a vault.
     * @param player The player to pay.
     * @return The transaction success
     */
    public static boolean payToCreate(Player player) {
        if (!BUKKIT_CONFIG.getBoolean("economy.enabled") || player.hasPermission("playervaults.free") || !PlayerVaults.USE_VAULT)
            return true;

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
     * @param player The player to receive the money.
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
