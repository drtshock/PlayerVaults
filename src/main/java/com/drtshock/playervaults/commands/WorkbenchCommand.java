package com.drtshock.playervaults.commands;

import com.drtshock.playervaults.util.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorkbenchCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("playervaults.workbench")) {
            if (sender instanceof Player) {
                ((Player) sender).openWorkbench(null, true);
                sender.sendMessage(Lang.TITLE.toString() + Lang.OPEN_WORKBENCH);
            } else {
                sender.sendMessage(Lang.TITLE.toString() + Lang.PLAYER_ONLY);
            }
        } else {
            sender.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS);
        }

        return true;
    }
}