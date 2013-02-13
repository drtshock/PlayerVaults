package me.shock.playervaults.commands;

import me.shock.playervaults.Main;
import me.shock.playervaults.util.VaultManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class OwnVault {

	private static Main main;
	private static VaultManager vm = new VaultManager(main);
	static Feedback feedback = new Feedback();

	static String pv = ChatColor.DARK_RED + "[" + ChatColor.WHITE + "PlayerVaults" + 
			ChatColor.DARK_RED + "]" + ChatColor.WHITE + ": ";

	public static boolean openOwnVault(CommandSender sender, String arg0) {
		if(arg0.matches("^[0-9]{1,2}$")) {
			System.out.println("yay regex!");
			if(sender.hasPermission("playervaults.amount." + arg0)) {
				int number = Integer.parseInt(arg0);
				vm.loadVault(sender, sender.getName(), number);
				sender.sendMessage(pv + "Opening vault " + ChatColor.GREEN + number);
				return true;
			} else {
				feedback.noPerms(sender);
			}
		}
		return false;
	}
}
