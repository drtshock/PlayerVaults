package me.shock.playervaults.commands;

import me.shock.playervaults.Main;
import me.shock.playervaults.util.VaultManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class OwnVault {

	private Main main;
	VaultManager vm = new VaultManager(main);
	Feedback feedback = new Feedback();

	String pv = ChatColor.DARK_RED + "[" + ChatColor.WHITE + "PlayerVaults" + 
			ChatColor.DARK_RED + "]" + ChatColor.WHITE + ": ";

	public boolean openOwnVault(CommandSender sender, String arg0) {
		System.out.println("Passed to ownvault class.");
		if(arg0.matches("^[0-9]{1,2}$")) {
			System.out.println("yay regex!");
			if(sender.hasPermission("playervaults.amount." + arg0)) {
				int number = Integer.parseInt(arg0);
				vm.loadVault(sender, sender.getName(), number);
				feedback.putInHash(sender.getName(), number);
				sender.sendMessage(pv + "Opening vault " + ChatColor.GREEN + number);
			} else {
				feedback.noPerms(sender);
			}
		}
		return true;
	}
}
