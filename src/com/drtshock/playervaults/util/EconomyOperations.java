package com.drtshock.playervaults.util;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.drtshock.playervaults.Main;

public class EconomyOperations {

	public Main plugin;
	private static FileConfiguration config;

	public EconomyOperations(Main instance) {
		this.plugin = instance;
		EconomyOperations.config = plugin.getConfig();
	}


	public static boolean payToOpen(Player player) {
		if(!config.getBoolean("economy.enabled") || player.hasPermission("playervaults.free"))
			return true;

		double cost = config.getDouble("economy.cost-to-open");
		EconomyResponse resp = Main.econ.withdrawPlayer(player.getName(), cost);
		if(resp.transactionSuccess()) {
			player.sendMessage(Lang.TITLE.toString() + Lang.COST_TO_OPEN.toString());
			return true;
		}

		return false;
	}
	
	public static boolean payToMake(Player player) {
		if(!config.getBoolean("economy.enabled") || player.hasPermission("playervaults.free"))
			return true;

		double cost = config.getDouble("economy.cost-to-create");
		EconomyResponse resp = Main.econ.withdrawPlayer(player.getName(), cost);
		if(resp.transactionSuccess()) {
			player.sendMessage(Lang.TITLE.toString() + Lang.COST_TO_CREATE.toString());
			return true;
		}

		return false;
	}
	
	public static boolean refundOnDelete(Player player) {
		if(!config.getBoolean("economy.enabled") || player.hasPermission("playervaults.free"))
			return true;

		double cost = config.getDouble("economy.refund-on-delete");
		EconomyResponse resp = Main.econ.depositPlayer(player.getName(), cost);
		if(resp.transactionSuccess()) {
			player.sendMessage(Lang.TITLE.toString() + Lang.REFUND_AMOUNT.toString());
			return true;
		}

		return false;
	}

}
