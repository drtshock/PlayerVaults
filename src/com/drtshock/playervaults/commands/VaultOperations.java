package com.drtshock.playervaults.commands;

import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.drtshock.playervaults.Main;
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
				vm.loadVault(sender, sender.getName(), number);
				sender.sendMessage(Lang.TITLE.toString() + Lang.OPEN_VAULT.toString().replace("%v", arg));
				sender.sendMessage(Lang.TITLE.toString() + "Opening vault " + ChatColor.GREEN + number);
				return true;
			} else {
				Feedback.noPerms(sender);
			}
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
				sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + "You need to specify a number!");
			}
			try {
				vm.deleteVault(sender, sender.getName(), number);
			} catch (IOException e) {
				sender.sendMessage(Lang.TITLE.toString() + "There was an error deleting that vault!");
			}
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
					sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + "You need to specify a number!");
				}
				try {
					vm.deleteVault(sender, user, number);
				} catch (IOException e) {
					sender.sendMessage(Lang.TITLE.toString() + "There was an error deleting that vault!");
				}
			}
		}
		else Feedback.noPerms(sender);
	}

}
