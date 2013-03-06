package me.shock.playervaults.commands;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class Commands implements CommandExecutor {

	public static HashMap<String, VaultViewInfo> inVault = new HashMap<String, VaultViewInfo>();
	private final String pv = ChatColor.DARK_RED + "[" + ChatColor.WHITE + "PlayerVaults" + 
			ChatColor.DARK_RED + "]" + ChatColor.WHITE + ": ";

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("pv")) {
			switch(args.length) {
			case 1:
				if(sender instanceof Player) {
					if(VaultOperations.openOwnVault(sender, args[0]))
						inVault.put(sender.getName(), new VaultViewInfo(sender.getName(), Integer.parseInt(args[0])));
				}
				else sender.sendMessage(pv + "Sorry but that can only be run by a player!");
				break;
			case 2:
				if(sender instanceof Player) {
					if(VaultOperations.openOtherVault(sender,args[0], args[1])) {
						inVault.put(sender.getName(), new VaultViewInfo(args[0], Integer.parseInt(args[1])));
					}
				}
				else sender.sendMessage(pv + "Sorry but that can only be run by a player!");
				break;
			default:
				Feedback.showHelp(sender);
			}
		}
		return true;
	}
}
