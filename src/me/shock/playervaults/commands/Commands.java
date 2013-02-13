package me.shock.playervaults.commands;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

	public static ConcurrentHashMap<String, Integer> inVault = new ConcurrentHashMap<String, Integer>();

	private String pv = ChatColor.DARK_RED + "[" + ChatColor.WHITE + "PlayerVaults" + 
			ChatColor.DARK_RED + "]" + ChatColor.WHITE + ": ";

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("pv")) {
			int length = args.length;
			if(length == 1) {
				if(notConsole(sender)) {
					if(OwnVault.openOwnVault(sender, args[0]))
						inVault.put(sender.getName(), Integer.parseInt(args[0]));
				}
			}

		}
		return true;
	}

	public boolean notConsole(CommandSender sender) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(pv + "Sorry but that can only be run by a player!");
			return false;
		}
		return true;
	}
}