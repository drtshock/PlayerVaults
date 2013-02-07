package me.shock.playervaults;

import java.io.IOException;
import java.util.HashMap;

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
	String pv = ChatColor.DARK_RED + "[" + ChatColor.WHITE + "PlayerVaults" + 
			ChatColor.DARK_RED + "]" + ChatColor.WHITE + ": ";

	public HashMap<String, String> inVault = new HashMap<String, String>();


	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase("pv"))
		{
			if (!(sender instanceof Player))
			{
				sender.sendMessage("[PlayerVaults] Sorry but the console can't have a vault :(");
				return true;
			}

			if (args.length == 1)
			{
				if (args[0].matches("[1-9]"))
				{
					int number = Integer.parseInt(args[0]);
					if ((number <= 9) && (sender.hasPermission("playervaults.amount.9")))
					{
						vm.loadVault(sender, sender.getName(), number);
						sender.sendMessage(pv + "Opening vault " + ChatColor.GREEN + args[0]);
						return true;
					}
					if ((number <= 8) && (sender.hasPermission("playervaults.amount.8")))
					{
						vm.loadVault(sender, sender.getName(), number);
						sender.sendMessage(pv + "Opening vault " + ChatColor.GREEN + args[0]);
						return true;
					}
					if ((number <= 7) && (sender.hasPermission("playervaults.amount.7")))
					{
						vm.loadVault(sender, sender.getName(), number);
						sender.sendMessage(pv + "Opening vault " + ChatColor.GREEN + args[0]);
						return true;
					}
					if ((number <= 6) && (sender.hasPermission("playervaults.amount.6")))
					{
						vm.loadVault(sender, sender.getName(), number);
						sender.sendMessage(pv + "Opening vault " + ChatColor.GREEN + args[0]);
						return true;
					}
					if ((number <= 5) && (sender.hasPermission("playervaults.amount.5")))
					{
						vm.loadVault(sender, sender.getName(), number);
						sender.sendMessage(pv + "Opening vault " + ChatColor.GREEN + args[0]);
						return true;
					}
					if ((number <= 4) && (sender.hasPermission("playervaults.amount.4")))
					{
						vm.loadVault(sender, sender.getName(), number);
						sender.sendMessage(pv + "Opening vault " + ChatColor.GREEN + args[0]);
						return true;
					}
					if ((number <= 3) && (sender.hasPermission("playervaults.amount.3")))
					{
						vm.loadVault(sender, sender.getName(), number);
						sender.sendMessage(pv + "Opening vault " + ChatColor.GREEN + args[0]);
						return true;
					}
					if ((number <= 2) && (sender.hasPermission("playervaults.amount.2")))
					{
						vm.loadVault(sender, sender.getName(), number);
						sender.sendMessage(pv + "Opening vault " + ChatColor.GREEN + args[0]);
						return true;
					}
					if ((number == 1) && (sender.hasPermission("playervaults.amount.1")))
					{
						vm.loadVault(sender, sender.getName(), number);
						sender.sendMessage(pv + "Opening vault " + ChatColor.GREEN + args[0]);
						return true;
					}

					sender.sendMessage(pv + "You don't have permission for that many vaults!");
					return true;
				}

				showHelp(sender);
				return true;
			}

			if (args.length == 2)
			{
				if (args[0].equals("delete"))
				{
					if (sender.hasPermission("playervaults.delete"))
					{
						if (args[1].matches("[1-9]"))
						{
							int number = Integer.parseInt(args[1]);
							try {
								vm.deleteVault(sender, sender.getName(), number);
							} catch (IOException e) {
								e.printStackTrace();
							}
							sender.sendMessage(pv + "Deleted vault " + ChatColor.GREEN + args[1]);
							return true;
						}
					}
					else
					{
						sender.sendMessage(pv + "You don't have permission for ");
						return true;
					}

				}

				else
				{
					if (!sender.hasPermission("playervaults.admin"))
					{
						sender.sendMessage(pv + "You don't have permission for ");
						return true;
					}
					if (args[1].matches("[1-9]"))
					{
						int number = Integer.parseInt(args[2]);
						vm.loadVault(sender, args[1].toLowerCase(), number);
						sender.sendMessage(pv + "Opened vault " + ChatColor.GREEN + args[1] + ChatColor.WHITE + " for " + 
								ChatColor.GREEN + args[0]);
						return true;
					}

					sender.sendMessage(pv + "Chest number must be 1-9.");
					return true;
				}

				sender.sendMessage(pv + "We have no record of that vault.");
				return true;
			}

			if (args.length > 1)
			{
				if (args[0].equalsIgnoreCase("delete"))
				{
					if (sender.hasPermission("playervaults.admin"))
					{
						if (args[2].matches("[1-9]"))
						{
							Integer number = Integer.parseInt(args[2]);
							try {
								vm.deleteVault(sender, sender.getName(), number);
							} catch (IOException e) {
								e.printStackTrace();
							}
							sender.sendMessage(pv + "Deleted vault " + ChatColor.RED + args[2] + ChatColor.WHITE + 
									" for " + ChatColor.RED + args[1]);
							return true;
						}
					}
					else
					{
						sender.sendMessage(pv + "You don't have permission for ");
						return true;
					}
				}
			}
			else
			{
				showHelp(sender);
				return true;
			}

		}

		return true;
	}

	public void showHelp(CommandSender sender)
	{
		sender.sendMessage(pv + "/vault <number>");
		sender.sendMessage(pv + "/vault delete <number>");
	}
}