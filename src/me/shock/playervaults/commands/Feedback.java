package me.shock.playervaults.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;


public class Feedback {
	public enum Type {
		OPEN,
		DELETE,
		WORKBENCH, //For later versions
		FURNACE;
	}
	static String pv = ChatColor.DARK_RED + "[" + ChatColor.WHITE + "PlayerVaults" + 
			ChatColor.DARK_RED + "]" + ChatColor.WHITE + ": ";
	
	public static void noPerms(CommandSender sender) {
		sender.sendMessage(pv + "You don't have permission for that!");
		return;
	}
	

	public static void showHelp(CommandSender sender, Type t) {
		if(t == Type.OPEN) {
			sender.sendMessage(pv + "/pv <number>");
			sender.sendMessage(pv + "/pv <player> <number>");
		}
		else if(t == Type.DELETE) {
			sender.sendMessage(pv + "/pvdel <number>");
			sender.sendMessage(pv + "/pvdel <player> <number>");
		}
	}
	
	public static void badWorld(CommandSender sender) {
		sender.sendMessage(pv + "You can't use this in that world!");
		return;
	}
}
