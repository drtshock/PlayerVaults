package com.drtshock.playervaults.commands;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import com.drtshock.playervaults.util.Lang;

public class Commands implements CommandExecutor {

    public static HashMap<String, VaultViewInfo> IN_VAULT = new HashMap<String, VaultViewInfo>();
    public static HashMap<String, SignSetInfo> SET_SIGN = new HashMap<String, SignSetInfo>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("pv")) {
            if(sender instanceof Player) {
                Player p = (Player) sender;
                switch (args.length) {
                case 1:
                    if(VaultOperations.openOwnVault(p, args[0]))
                        IN_VAULT.put(sender.getName(), new VaultViewInfo(sender.getName(), Integer.parseInt(args[0])));
                    break;
                case 2:
                    if(VaultOperations.openOtherVault(p, args[0], args[1]))
                        IN_VAULT.put(sender.getName(), new VaultViewInfo(args[0], Integer.parseInt(args[1])));
                    break;
                default:
                    Feedback.showHelp(sender, Feedback.Type.OPEN);
                }
            }
            else sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.PLAYER_ONLY);
        } else if(cmd.getName().equalsIgnoreCase("pvdel")) {
            switch (args.length) {
            case 1:
                if(sender instanceof Player) {
                    Player p = (Player) sender;
                    VaultOperations.deleteOwnVault(p, args[0]);
                }
                else {
                    sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.PLAYER_ONLY);
                }
                break;
            case 2:
                VaultOperations.deleteOtherVault(sender, args[0], args[1]);
                break;
            default:
                Feedback.showHelp(sender, Feedback.Type.DELETE);
            }
        } else if(cmd.getName().equalsIgnoreCase("workbench")) {
            if(sender.hasPermission("playervaults.workbench")) {
                if(sender instanceof Player) {
                    Inventory workbench = Bukkit.createInventory(null, InventoryType.WORKBENCH);
                    ((Player) sender).openInventory(workbench);
                    sender.sendMessage(Lang.TITLE.toString() + Lang.OPEN_WORKBENCH);
                } else {
                    sender.sendMessage(Lang.TITLE.toString() + Lang.PLAYER_ONLY);
                }
            } else {
                sender.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS);
            }
        } else if(cmd.getName().equalsIgnoreCase("pvsign")) {
            if(sender.hasPermission("playervaults.setsign")) {
                if(sender instanceof Player) {
                    if(args.length >= 2) {
                        int i = 0;
                        try {
                            i = Integer.parseInt(args[1]);
                        } catch(NumberFormatException nfe) {
                            sender.sendMessage(Lang.TITLE.toString() + Lang.MUST_BE_NUMBER);
                            sender.sendMessage(Lang.TITLE.toString() + "Usage: /" + label + " <owner> <#>");
                            return true;
                        }
                        SET_SIGN.put(sender.getName(), new SignSetInfo(args[0].toLowerCase(), i));
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
