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
import com.drtshock.playervaults.vaultmanagement.VaultViewInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class VaultCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (VaultOperations.isLocked()) {
            sender.sendMessage(Lang.TITLE + Lang.LOCKED.toString());
            return true;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (PlayerVaults.getInstance().getInVault().containsKey(player.getUniqueId().toString())) {
                // don't let them open another vault.
                return true;
            }

            switch (args.length) {
                case 1:
                    if (VaultOperations.openOwnVault(player, args[0], true)) {
                        PlayerVaults.getInstance().getInVault().put(player.getUniqueId().toString(), new VaultViewInfo(player.getUniqueId().toString(), Integer.parseInt(args[0])));
                    } else if (sender.hasPermission("playervaults.admin")) {
                        OfflinePlayer searchPlayer = Bukkit.getOfflinePlayer(args[0]);
                        String target = args[0];
                        if (searchPlayer != null && searchPlayer.hasPlayedBefore()) {
                            target = searchPlayer.getUniqueId().toString();
                        }

                        YamlConfiguration file = VaultManager.getInstance().getPlayerVaultFile(target, false);
                        if (file == null) {
                            sender.sendMessage(Lang.TITLE.toString() + Lang.VAULT_DOES_NOT_EXIST.toString());
                        } else {
                            StringBuilder sb = new StringBuilder();
                            for (String key : file.getKeys(false)) {
                                sb.append(key.replace("vault", "")).append(" ");
                            }

                            sender.sendMessage(Lang.TITLE.toString() + Lang.EXISTING_VAULTS.toString().replaceAll("%p", args[0]).replaceAll("%v", sb.toString().trim()));
                        }
                    }
                    break;
                case 2:
                    if (!player.hasPermission("playervaults.admin")) {
                        player.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS.toString());
                        break;
                    }

                    int number;
                    try {
                        number = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.MUST_BE_NUMBER);
                        return true;
                    }

                    String target = args[0];
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                    if (offlinePlayer != null && offlinePlayer.hasPlayedBefore()) {
                        target = offlinePlayer.getUniqueId().toString();
                    }
                    if (VaultOperations.openOtherVault(player, target, args[1])) {
                        PlayerVaults.getInstance().getInVault().put(player.getUniqueId().toString(), new VaultViewInfo(target, number));
                    } else {
                        sender.sendMessage(Lang.TITLE.toString() + Lang.NO_OWNER_FOUND.toString().replaceAll("%p", args[0]));
                    }
                    break;
                default:
                    sender.sendMessage(Lang.TITLE.toString() + Lang.HELP.toString());
            }
        } else {
            sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.PLAYER_ONLY.toString());
        }

        return true;
    }
}
