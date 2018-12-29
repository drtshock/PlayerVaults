/*
 * PlayerVaultsX
 * Copyright (C) 2013 Trent Hensler
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.drtshock.playervaults.commands;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.translations.Lang;
import com.drtshock.playervaults.vaultmanagement.VaultManager;
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
                OfflinePlayer searchPlayer = Bukkit.getOfflinePlayer(args[0]);
                String target = args[0];
                if (searchPlayer != null && searchPlayer.hasPlayedBefore()) {
                    target = searchPlayer.getUniqueId().toString();
                }

                // TODO: fix the stupid message inconsistencies where sometimes this class sends, sometimes vaultops does.
                if (args[1].equalsIgnoreCase("all")) {
                    if (sender.hasPermission("playervaults.delete.all")) {
                        VaultManager.getInstance().deleteAllVaults(target);
                        sender.sendMessage(Lang.TITLE.toString() + Lang.DELETE_OTHER_VAULT_ALL.toString().replaceAll("%p", target));
                        PlayerVaults.getInstance().getLogger().info(String.format("%s deleted ALL vaults belonging to %s", sender.getName(), target));
                    } else {
                        sender.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS);
                    }

                }
                VaultOperations.deleteOtherVault(sender, target, args[1]);
                break;
            default:
                sender.sendMessage(Lang.TITLE + "/" + label + " <number>");
                sender.sendMessage(Lang.TITLE + "/" + label + " <player> <number>");
                sender.sendMessage(Lang.TITLE + "/" + label + " <player> all");
        }
        return true;
    }
}