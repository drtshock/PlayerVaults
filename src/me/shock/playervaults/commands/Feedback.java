package me.shock.playervaults.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Feedback {
		
	String pv = ChatColor.DARK_RED + "[" + ChatColor.WHITE + "PlayerVaults" + 
			ChatColor.DARK_RED + "]" + ChatColor.WHITE + ": ";
	
	public void noPerms(CommandSender sender) {
		sender.sendMessage(pv + "You don't have permission for that!");
		return;
	}
	

	public void showHelp(CommandSender sender) {
		sender.sendMessage(pv + "/vault <number>");
		sender.sendMessage(pv + "/vault delete <number>");
	}
}
