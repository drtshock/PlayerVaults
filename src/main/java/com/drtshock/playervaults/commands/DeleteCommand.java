package com.drtshock.playervaults.commands;

import com.drtshock.playervaults.util.Lang;
import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (args.length) {
            case 1:
                if (sender instanceof Player) {
                    VaultOperations.deleteOwnVault((Player) sender, args[0]);
                } else {
                    sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.PLAYER_ONLY);
                }
                break;
            case 2:
                Player player = Bukkit.getPlayer(args[0]);
                if (player == null) {
                    sender.sendMessage(Lang.TITLE.toString() + Lang.NO_PLAYER_FOUND.toString().replaceAll("%p", args[0]));
                    break;
                }

                VaultOperations.deleteOtherVault(sender, player, args[1]);
                break;
            default:
                sender.sendMessage(Lang.TITLE + "/pvdel <number>");
                sender.sendMessage(Lang.TITLE + "/pvdel <player> <number>");
        }

        return true;
    }
}