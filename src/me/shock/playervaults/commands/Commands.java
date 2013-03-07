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
			if(sender instanceof Player) {
				switch(args.length) {
				case 1:
					if(VaultOperations.openOwnVault(sender, args[0]))
						inVault.put(sender.getName(), new VaultViewInfo(sender.getName(), Integer.parseInt(args[0])));
					break;
				case 2:
					if(VaultOperations.openOtherVault(sender,args[0], args[1])) {
						inVault.put(sender.getName(), new VaultViewInfo(args[0], Integer.parseInt(args[1])));
					}
					break;
				default:
					Feedback.showHelp(sender, Feedback.Type.OPEN);
				}
			}
			else sender.sendMessage(pv + "Sorry but that can only be run by a player!");
		}
		else if(cmd.getName().equalsIgnoreCase("pvdel")) {
			if(sender instanceof Player) {
				switch(args.length) {
				case 1:
					VaultOperations.deleteOwnVault(sender, args[0]);
					break;
				default:
					Feedback.showHelp(sender, Feedback.Type.DELETE);
				}
			}
			else sender.sendMessage(pv + "Sorry but that can only be run by a player!");
		}
		return true;
	}
}
