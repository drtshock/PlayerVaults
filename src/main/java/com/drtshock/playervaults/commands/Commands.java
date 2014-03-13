/* 
 * Copyright (C) 2013 drtshock
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
import com.drtshock.playervaults.util.Lang;
import com.drtshock.playervaults.vaultmanagement.UUIDVaultManager;
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

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("pv")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (PlayerVaults.IN_VAULT.containsKey(p.getName())) return true; // don't let them open another vault.
                switch (args.length) {
                    case 1:
                        if (VaultOperations.openOwnVault(p, args[0])) {
                            PlayerVaults.IN_VAULT.put(sender.getName(), new VaultViewInfo(sender.getName(), Integer.parseInt(args[0])));
                        } else if (sender.hasPermission("playervaults.admin")) {
                            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
                            if(player == null) {
                                sender.sendMessage(Lang.TITLE.toString() + "Cannot find player " + args[0]);
                                break;
                            }
                            YamlConfiguration file = UUIDVaultManager.getInstance().getPlayerVaultFile(player.getUniqueId());
                            if (file == null) {
                                sender.sendMessage(Lang.TITLE.toString() + Lang.VAULT_DOES_NOT_EXIST.toString());
                            } else {
                                StringBuilder sb = new StringBuilder();
                                for (String key : file.getKeys(false)) {
                                    sb.append(key.replace("vault", "")).append(" ");
                                }
                                String vaults = sb.toString().trim();
                                sender.sendMessage(Lang.TITLE.toString() + Lang.EXISTING_VAULTS.toString().replaceAll("%p", args[0]).replaceAll("%v", vaults));
                            }
                        } else {
                            sender.sendMessage(Lang.TITLE.toString() + Lang.MUST_BE_NUMBER.toString());
                        }
                        break;
                    case 2:
                        Player player = Bukkit.getPlayer(args[0]);
                        if (player == null) break;
                        if (VaultOperations.openOtherVault(p, player, args[1])) {
                            PlayerVaults.IN_VAULT.put(sender.getName(), new VaultViewInfo(args[0], Integer.parseInt(args[1])));
                        } else {
                            sender.sendMessage(Lang.TITLE.toString() + "Failed to open vault.");
                        }
                        break;
                    default:
                        sender.sendMessage(Lang.TITLE + "/pv <number>");
                        sender.sendMessage(Lang.TITLE + "/pv <player> <number>");
                }
            } else {
                sender.sendMessage(Lang.TITLE.toString() + ChatColor.RED + Lang.PLAYER_ONLY);
            }
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
                    Player player = Bukkit.getPlayer(args[0]);
                    if (player == null) break;
                    VaultOperations.deleteOtherVault(sender, player, args[1]);
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
                        int i;
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
                        int i;
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
