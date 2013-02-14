package me.shock.playervaults.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Feedback {
	
	static String pv = ChatColor.DARK_RED + "[" + ChatColor.WHITE + "PlayerVaults" + 
			ChatColor.DARK_RED + "]" + ChatColor.WHITE + ": ";
	
	public void noPerms(CommandSender sender) {
		sender.sendMessage(pv + "You don't have permission for that!");
		return;
	}
	

	public static void showHelp(CommandSender sender) {
		sender.sendMessage(pv + "/vault <number>");
		sender.sendMessage(pv + "/vault delete <number>");
	}
	
	public static void badWorld(CommandSender sender) {
		sender.sendMessage(pv + "You can't use this in that world!");
		return;
	}
}
