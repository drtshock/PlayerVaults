package me.shock.playervaults.commands;

import me.shock.playervaults.Main;
import me.shock.playervaults.util.VaultManager;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OwnVault {

	private static Main plugin;
	private static VaultManager vm = new VaultManager(plugin);
	static Feedback feedback = new Feedback();

	static String pv = ChatColor.DARK_RED + "[" + ChatColor.WHITE + "PlayerVaults" + 
			ChatColor.DARK_RED + "]" + ChatColor.WHITE + ": ";

	public static boolean checkPerms(CommandSender cs, int number) {
		if(number <= 0) {
			return false;
		}
		if(cs.hasPermission("playervaults.amount." + String.valueOf(number))) {
			return true;
		}
		else if(checkPerms(cs, number-1)) {
			return true;
		}
		return false;
	}
	
	public static boolean openOwnVault(CommandSender sender, String arg0) {
		if(arg0.matches("^[0-9]{1,2}$")) {
			System.out.println("yay regex!");
			int number = 0;
			try {
				number = Integer.parseInt(arg0);
			}
			catch(NumberFormatException nfe) {
				//Yell at the player
				//We should probably check perms first though
			}
			if(checkPerms(sender, number)) {
				vm.loadVault(sender, sender.getName(), number);
				sender.sendMessage(pv + "Opening vault " + ChatColor.GREEN + number);
				return true;
			} else {
				feedback.noPerms(sender);
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	private static boolean allowedWorld(Player player) {
		World world = player.getWorld();
		if(plugin.disabledWorlds().contains(world))
			return false;
		return true;
	}
}
