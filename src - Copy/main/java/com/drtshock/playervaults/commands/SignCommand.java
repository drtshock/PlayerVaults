package com.drtshock.playervaults.commands;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.translations.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SignCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
                    PlayerVaults.getInstance().getSetSign().put(sender.getName(), new SignSetInfo(i));
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
                    PlayerVaults.getInstance().getSetSign().put(sender.getName(), new SignSetInfo(args[0].toLowerCase(), i));
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

        return true;
    }
}