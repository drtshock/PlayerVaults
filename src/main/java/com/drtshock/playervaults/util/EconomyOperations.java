package com.drtshock.playervaults.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.drtshock.playervaults.Main;

public class EconomyOperations {

    private static File configFile;
    private static YamlConfiguration bukkitConfig = new YamlConfiguration();

    public static Main plugin;

    public EconomyOperations(Main instance) throws FileNotFoundException, IOException, InvalidConfigurationException {
        plugin = instance;
        configFile = new File(plugin.getDataFolder(), "config.yml");
        bukkitConfig.load(configFile);
    }

    /**
     * Have a player pay to open a vault.
     * Returns true if successful. Otherwise false.
     * @param player
     * @return transaction success
     */
    public static boolean payToOpen(Player player) {
        if(!bukkitConfig.getBoolean("economy.enabled") || player.hasPermission("playervaults.free") || !Main.useVault)
            return true;

        double cost = bukkitConfig.getDouble("economy.cost-to-open", 10);
        EconomyResponse resp = Main.econ.withdrawPlayer(player.getName(), cost);
        if(resp.transactionSuccess()) {
            player.sendMessage(Lang.TITLE.toString() + Lang.COST_TO_OPEN.toString().replaceAll("%price", "" + cost));
            return true;
        }

        return false;
    }

    /**
     * Have a player pay to create a vault.
     * Returns true if successful. Otherwise false.
     * @param player
     * @return transaction success
     */
    public static boolean payToCreate(Player player) {
        if(!bukkitConfig.getBoolean("economy.enabled") || player.hasPermission("playervaults.free") || !Main.useVault)
            return true;

        double cost = bukkitConfig.getDouble("economy.cost-to-create", 100);
        EconomyResponse resp = Main.econ.withdrawPlayer(player.getName(), cost);
        if(resp.transactionSuccess()) {
            player.sendMessage(Lang.TITLE.toString() + Lang.COST_TO_CREATE.toString().replaceAll("%price", "" + cost));
            return true;
        }

        return false;
    }

    /**
     * Have a player get his money back when vault is deleted.
     * Returns true if successful. Otherwise false.
     * @param player
     * @return transaction success.
     */
    public static boolean refundOnDelete(Player player, int number) {
        String directory = "plugins" + File.separator + "PlayerVaults" + File.separator + "vaults";

        if(!bukkitConfig.getBoolean("economy.enabled") || player.hasPermission("playervaults.free") || !Main.useVault)
            return true;
        String name = player.getName().toLowerCase();
        File file = new File(directory + File.separator + name.toLowerCase() + ".yml");
        YamlConfiguration playerFile = YamlConfiguration.loadConfiguration(file);
        if(file.exists()) {
            if(playerFile.getString("vault" + number) == null) {
                player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.VAULT_DOES_NOT_EXIST);
                return false;
            }
        }
        else {
            player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.VAULT_DOES_NOT_EXIST);
            return false;
        }
        double cost = bukkitConfig.getDouble("economy.refund-on-delete");
        EconomyResponse resp = Main.econ.depositPlayer(player.getName(), cost);
        if(resp.transactionSuccess()) {
            player.sendMessage(Lang.TITLE.toString() + Lang.REFUND_AMOUNT.toString().replaceAll("%price", String.valueOf(cost)));
            return true;
        }
        return false;
    }

}
