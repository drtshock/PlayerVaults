package com.drtshock.playervaults.commands;

import com.drtshock.playervaults.translations.Lang;
import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (VaultOperations.isLocked()) {
            sender.sendMessage(Lang.TITLE + Lang.LOCKED.toString());
            return true;
        }

        switch (args.length) {
            case 1:
                if (sender instanceof Player) {
                    VaultOperations.deleteOwnVault((Player) sender, args[0]);
                } else {
                    sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.PLAYER_ONLY);
                }
                break;
            case 2:
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
                if (player == null || !player.hasPlayedBefore()) {
                    sender.sendMessage(Lang.TITLE.toString() + Lang.NO_PLAYER_FOUND.toString().replaceAll("%p", args[0]));
                    break;
                }

                // TODO: fix the stupid message inconsistencies where sometimes this class sends, sometimes vaultops does.
                if (args[1].equalsIgnoreCase("all")) {
                    VaultOperations.deleteOtherAllVaults(sender, player.getUniqueId());
                    sender.sendMessage(Lang.TITLE.toString() + Lang.DELETE_OTHER_VAULT_ALL.toString().replaceAll("%p", player.getName()));
                    break;
                }

                VaultOperations.deleteOtherVault(sender, player, args[1]);
                break;
            default:
                sender.sendMessage(Lang.TITLE + "/pvdel <number>");
                sender.sendMessage(Lang.TITLE + "/pvdel <player> <number>");
                sender.sendMessage(Lang.TITLE + "/pvdel <player> all");
        }

        return true;
    }
}