package com.drtshock.playervaults.commands;

import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.drtshock.playervaults.Main;
import com.drtshock.playervaults.util.EconomyOperations;
import com.drtshock.playervaults.util.Lang;
import com.drtshock.playervaults.util.VaultManager;

public class VaultOperations {

	private static Main plugin;
	private static VaultManager vm = new VaultManager(plugin);


	public static boolean checkPerms(CommandSender cs, int number) {
		if(cs.hasPermission("playervaults.amount." + String.valueOf(number))) return true;
		for(int x = number; x <= 99;x++) {
			if(cs.hasPermission("playervaults.amount." + String.valueOf(x))) return true;
		}
		return false;
	}
	
	/*
	 * TODO: Change how permissions are checked here.
	 */
	public static boolean openOwnVault(Player sender, String arg) {
		if(arg.matches("^[0-9]{1,2}$")) {
			int number = 0;
			try {
				number = Integer.parseInt(arg);
			}
			catch(NumberFormatException nfe) {
				sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
				return false;
			}
			if(checkPerms(sender, number)) {
				if(EconomyOperations.payToOpen(sender)) {
					vm.loadVault(sender, sender.getName(), number);
					sender.sendMessage(Lang.TITLE.toString() + Lang.OPEN_VAULT.toString().replace("%v", arg));
					return true;
				} else {
					sender.sendMessage(Lang.TITLE.toString() + Lang.INSUFFICIENT_FUNDS);
					return false;
				}
			} else {
				Feedback.noPerms(sender);
			}
		} else {
			sender.sendMessage(Lang.TITLE.toString() + Lang.MUST_BE_NUMBER);
		}
		return false;
	}

	public static boolean openOtherVault(Player sender, String user, String arg) {
		if(sender.hasPermission("playervaults.admin")) {
			if(arg.matches("^[0-9]{1,2}$")) {
				int number = 0;
				try {
					number = Integer.parseInt(arg);
				}
				catch(NumberFormatException nfe) {
					sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
				}
				vm.loadVault(sender, user, number);
				sender.sendMessage(Lang.TITLE.toString() + Lang.OPEN_OTHER_VAULT.toString().replace("%v", arg).replace("%p", user));
				return true;
			} else {
				sender.sendMessage(Lang.TITLE.toString() + Lang.MUST_BE_NUMBER);
			}
		}
		else {
			Feedback.noPerms(sender);
		}
		return false;
	}
	
	public static void deleteOwnVault(Player sender, String arg) {
		if(arg.matches("^[0-9]{1,2}$")) {
			int number = 0;
			try {
				number = Integer.parseInt(arg);
			}
			catch(NumberFormatException nfe) {
				sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
			}
			try {
				if(EconomyOperations.refundOnDelete(sender)) {
					vm.deleteVault(sender, sender.getName(), number);
					return;
				}
			} catch (IOException e) {
				sender.sendMessage(Lang.TITLE.toString() + Lang.DELETE_VAULT_ERROR);
			}
		} else {
			sender.sendMessage(Lang.TITLE.toString()+ Lang.MUST_BE_NUMBER);
		}
	}
	
	public static void deleteOtherVault(CommandSender sender, String user, String arg) {
		if(sender.hasPermission("playervaults.delete")) {
			if(arg.matches("^[0-9]{1,2}$")) {
				int number = 0;
				try {
					number = Integer.parseInt(arg);
				}
				catch(NumberFormatException nfe) {
					sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
				}
				try {
					vm.deleteVault(sender, user, number);
				} catch (IOException e) {
					sender.sendMessage(Lang.TITLE.toString() + Lang.DELETE_VAULT_ERROR);
				}
			} else {
				sender.sendMessage(Lang.TITLE.toString() + Lang.MUST_BE_NUMBER);
			}
		}
		else Feedback.noPerms(sender);
	}

}
