package me.shock.playervaults.commands;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Feedback {
	
	public HashMap<String, Integer> inVault = new HashMap<String, Integer>();
	
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
	
	public void putInHash(String name, int number) {
		inVault.put(name, number);
		return;
	}
	
	public int getNumber(String name) {
		int number = inVault.get(name);
		return number;
	}
	
	public boolean hasKey(String name) {
		if(inVault.containsKey(name))
			return true;
		return false;
	}

}
