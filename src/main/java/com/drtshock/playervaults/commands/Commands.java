package com.drtshock.playervaults.commands;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.util.Lang;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("pv")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                switch (args.length) {
                    case 1:
                        if (VaultOperations.openOwnVault(p, args[0]))
                            PlayerVaults.IN_VAULT.put(sender.getName(), new VaultViewInfo(sender.getName(), Integer.parseInt(args[0])));
                        break;
                    case 2:
                        if (VaultOperations.openOtherVault(p, args[0], args[1]))
                            PlayerVaults.IN_VAULT.put(sender.getName(), new VaultViewInfo(args[0], Integer.parseInt(args[1])));
                        break;
                    default:
                        sender.sendMessage(Lang.TITLE + "/pv <number>");
                        sender.sendMessage(Lang.TITLE + "/pv <player> <number>");
                }
            } else sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.PLAYER_ONLY);
        } else if (cmd.getName().equalsIgnoreCase("pvdel")) {
            switch (args.length) {
                case 1:
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        VaultOperations.deleteOwnVault(p, args[0]);
                    } else {
                        sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.PLAYER_ONLY);
                    }
                    break;
                case 2:
                    VaultOperations.deleteOtherVault(sender, args[0], args[1]);
                    break;
                default:
                    sender.sendMessage(Lang.TITLE + "/pvdel <number>");
                    sender.sendMessage(Lang.TITLE + "/pvdel <player> <number>");
            }
        } else if (cmd.getName().equalsIgnoreCase("workbench")) {
            if (sender.hasPermission("playervaults.workbench")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.openWorkbench(null, true);
                    player.sendMessage(Lang.TITLE.toString() + Lang.OPEN_WORKBENCH);
                } else {
                    sender.sendMessage(Lang.TITLE.toString() + Lang.PLAYER_ONLY);
                }
            } else {
                sender.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS);
            }
        } else if (cmd.getName().equalsIgnoreCase("pvsign")) {
            if (sender.hasPermission("playervaults.signs.set")) {
                if (sender instanceof Player) {
                    if (args.length == 1) {
                        int i = 0;
                        try {
                            i = Integer.parseInt(args[0]);
                        } catch (NumberFormatException nfe) {
                            sender.sendMessage(Lang.TITLE.toString() + Lang.MUST_BE_NUMBER);
                            sender.sendMessage(Lang.TITLE.toString() + "Usage: /" + label + " <owner> <#>");
                            return true;
                        }
                        PlayerVaults.SET_SIGN.put(sender.getName(), new SignSetInfo(i));
                        sender.sendMessage(Lang.TITLE.toString() + Lang.CLICK_A_SIGN);
                    } else if (args.length >= 2) {
                        int i = 0;
                        try {
                            i = Integer.parseInt(args[1]);
                        } catch (NumberFormatException nfe) {
                            sender.sendMessage(Lang.TITLE.toString() + Lang.MUST_BE_NUMBER);
                            sender.sendMessage(Lang.TITLE.toString() + "Usage: /" + label + " <owner> <#>");
                            return true;
                        }
                        PlayerVaults.SET_SIGN.put(sender.getName(), new SignSetInfo(args[0].toLowerCase(), i));
                        sender.sendMessage(Lang.TITLE.toString() + Lang.CLICK_A_SIGN);
                    } else {
                        sender.sendMessage(Lang.TITLE.toString() + Lang.INVALID_ARGS);
                    }
                } else {
                    sender.sendMessage(Lang.TITLE.toString() + Lang.PLAYER_ONLY);
                }
            } else {
                sender.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS);
            }
        }
        return true;
    }
}
