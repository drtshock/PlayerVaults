package com.drtshock.playervaults.commands;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.drtshock.playervaults.util.Lang;


public class Commands implements CommandExecutor {

	public static HashMap<String, VaultViewInfo> inVault = new HashMap<String, VaultViewInfo>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("pv")) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				switch(args.length) {
				case 1:
					if(VaultOperations.openOwnVault(p, args[0]))
						inVault.put(sender.getName(), new VaultViewInfo(sender.getName(), Integer.parseInt(args[0])));
					break;
				case 2:
					if(VaultOperations.openOtherVault(p,args[0], args[1]))
						inVault.put(sender.getName(), new VaultViewInfo(args[0], Integer.parseInt(args[1])));
					break;
				default:
					Feedback.showHelp(sender, Feedback.Type.OPEN);
				}
			}
			else sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.PLAYER_ONLY);
		}

		else if(cmd.getName().equalsIgnoreCase("pvdel")) {
			switch(args.length) {
			case 1:
				if(sender instanceof Player) {
					Player p = (Player) sender;
					VaultOperations.deleteOwnVault(p, args[0]);
				}
				else 
					sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.PLAYER_ONLY);
				break;
			case 2:
				VaultOperations.deleteOtherVault(sender, args[0], args[1]);
				break;
			default:
				Feedback.showHelp(sender, Feedback.Type.DELETE);
			}
		}
		return true;
	}
}
