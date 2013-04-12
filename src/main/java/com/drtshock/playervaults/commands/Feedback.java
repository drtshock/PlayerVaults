package com.drtshock.playervaults.commands;

import org.bukkit.command.CommandSender;

import com.drtshock.playervaults.util.Lang;

public class Feedback {
    public enum Type {
        OPEN,
        DELETE,
        WORKBENCH, // For later versions
        FURNACE;
    }

    public static void noPerms(CommandSender sender) {
        sender.sendMessage(Lang.TITLE + "" + Lang.NO_PERMS);
        return;
    }

    public static void showHelp(CommandSender sender, Type t) {
        if(t == Type.OPEN) {
            sender.sendMessage(Lang.TITLE + "/pv <number>");
            sender.sendMessage(Lang.TITLE + "/pv <player> <number>");
        }
        else if(t == Type.DELETE) {
            sender.sendMessage(Lang.TITLE + "/pvdel <number>");
            sender.sendMessage(Lang.TITLE + "/pvdel <player> <number>");
        }
    }
}
