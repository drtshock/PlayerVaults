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

public class VaultCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (VaultOperations.isLocked()) {
            sender.sendMessage(Lang.TITLE + Lang.LOCKED.toString());
            return true;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (PlayerVaults.getInstance().getInVault().containsKey(player.getName())) {
                // don't let them open another vault.
                return true;
            }

            switch (args.length) {
                case 1:
                    if (VaultOperations.openOwnVault(player, args[0], true)) {
                        PlayerVaults.getInstance().getInVault().put(player.getUniqueId().toString(), new VaultViewInfo(player.getUniqueId(), Integer.parseInt(args[0])));
                    } else if (sender.hasPermission("playervaults.admin")) {
                        OfflinePlayer searchPlayer = Bukkit.getOfflinePlayerSync(args[0]);
                        if (searchPlayer == null || !searchPlayer.hasPlayedBefore()) {
                            sender.sendMessage(Lang.TITLE.toString() + Lang.NO_PLAYER_FOUND.toString().replaceAll("%p", args[0]));
                            break;
                        }

                        YamlConfiguration file = UUIDVaultManager.getInstance().getPlayerVaultFile(searchPlayer.getUniqueId());
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
                    OfflinePlayer searchPlayer = Bukkit.getOfflinePlayerSync(args[0]);
                    if (searchPlayer == null || !searchPlayer.hasPlayedBefore()) {
                        sender.sendMessage(Lang.TITLE.toString() + Lang.NO_PLAYER_FOUND.toString().replaceAll("%p", args[0]));
                        break;
                    }

                    if (VaultOperations.openOtherVault(player, searchPlayer, args[1])) {
                        PlayerVaults.getInstance().getInVault().put(player.getUniqueId().toString(), new VaultViewInfo(searchPlayer.getUniqueId(), Integer.parseInt(args[1])));
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