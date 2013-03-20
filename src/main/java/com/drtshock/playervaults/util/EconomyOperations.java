package com.drtshock.playervaults.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.drtshock.playervaults.Main;

public class EconomyOperations {

	private static String directory = "plugins" + File.separator + "PlayerVaults" + File.separator;
	private static File configFile = new File(directory + "config.yml");
	private static YamlConfiguration bukkitConfig = new YamlConfiguration();

	public Main plugin;
	public EconomyOperations(Main instance) throws FileNotFoundException, IOException, InvalidConfigurationException {
		this.plugin = instance;
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

		double cost = bukkitConfig.getDouble("economy.cost-to-open");
		EconomyResponse resp = Main.econ.withdrawPlayer(player.getName(), cost);
		if(resp.transactionSuccess()) {
			player.sendMessage(Lang.TITLE.toString() + Lang.COST_TO_OPEN.toString().replaceAll("%price", "" + cost));
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
	public static boolean refundOnDelete(Player player) {
		if(!bukkitConfig.getBoolean("economy.enabled") || player.hasPermission("playervaults.free") || !Main.useVault)
			return true;

		double cost = bukkitConfig.getDouble("economy.refund-on-delete");
		EconomyResponse resp = Main.econ.depositPlayer(player.getName(), cost);
		if(resp.transactionSuccess()) {
			player.sendMessage(Lang.TITLE.toString() + Lang.REFUND_AMOUNT.toString().replaceAll("%price", "" + cost));
			return true;
		}

		return false;
	}

}
