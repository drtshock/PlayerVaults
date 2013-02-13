package me.shock.playervaults.commands;

import me.shock.playervaults.Main;
import me.shock.playervaults.util.VaultManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor
{

	private Main plugin;
	VaultManager vm = new VaultManager(plugin);
	OwnVault ownvault = new OwnVault();
	String pv = ChatColor.DARK_RED + "[" + ChatColor.WHITE + "PlayerVaults" + 
			ChatColor.DARK_RED + "]" + ChatColor.WHITE + ": ";

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("pv")) {
			int length = args.length;
			if(length == 1) {
				if(checkConsole(sender)) {
					ownvault.openOwnVault(sender, args[0]);
				}
			}
				
		}
		return true;
	}
	
	public boolean checkConsole(CommandSender sender) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(pv + "Sorry but that can only be run by a player!");
			return false;
		}
		return true;
	}
}