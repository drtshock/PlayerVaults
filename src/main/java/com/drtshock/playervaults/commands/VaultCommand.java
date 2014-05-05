package com.drtshock.playervaults.commands;

import com.drtshock.playervaults.util.Lang;
import com.drtshock.playervaults.vaults.VaultManagement;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class VaultCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.PLAYER_ONLY.toString());
            return true;
        }

        Player player = (Player) sender;
        if (args.length > 2) {
            return false;
        }

        String vaultNumber = args.length == 1 ? args[0] : "1"; // so they can do /pv and open the first vault.
        try {
            Integer.valueOf(vaultNumber);
        } catch (NumberFormatException e) {
            openOtherVault(player, args[0], Integer.valueOf(args[1])); // TODO: better logic for checking args.
            return true;
        }

        Integer num = Integer.valueOf(vaultNumber);
        Inventory inv = VaultManagement.
        return true;
    }

    private void openOtherVault(Player player, String target, int num) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
        if (offlinePlayer == null) {
            player.sendMessage(Lang.TITLE.toString() + Lang.VAULT_DOES_NOT_EXIST.toString());
            return;
        }

        Inventory vault = VaultManagement.getVault(offlinePlayer, num);
        if (vault == null) {
            player.sendMessage(Lang.TITLE.toString() + Lang.VAULT_DOES_NOT_EXIST.toString());
            return;
        }

        player.openInventory(vault); // TODO: Don't allow same vault to be opened multiple times.
    }
}
