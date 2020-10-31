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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SignCommand implements CommandExecutor {
    private final PlayerVaults plugin;

    public SignCommand(PlayerVaults plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("playervaults.signs.set")) {
            if (!PlayerVaults.getInstance().getConf().isSigns()) {
                this.plugin.getTL().signsDisabled().title().send(sender);
                return true;
            }
            if (sender instanceof Player) {
                if (args.length == 1) {
                    int i;
                    try {
                        i = Integer.parseInt(args[0]);
                    } catch (NumberFormatException nfe) {
                        this.plugin.getTL().mustBeNumber().title().send(sender);
                        sender.sendMessage("              /" + label + " [owner] <#>");
                        return true;
                    }
                    PlayerVaults.getInstance().getSetSign().put(sender.getName(), new SignSetInfo(i));
                    this.plugin.getTL().clickASign().title().send(sender);
                } else if (args.length >= 2) {
                    int i;
                    try {
                        i = Integer.parseInt(args[1]);
                    } catch (NumberFormatException nfe) {
                        this.plugin.getTL().mustBeNumber().title().send(sender);
                        sender.sendMessage("              /" + label + " [owner] <#>");
                        return true;
                    }
                    PlayerVaults.getInstance().getSetSign().put(sender.getName(), new SignSetInfo(args[0].toLowerCase(), i));
                    this.plugin.getTL().clickASign().title().send(sender);
                } else {
                    this.plugin.getTL().invalidArgs().title().send(sender);
                }
            } else {
                this.plugin.getTL().playerOnly().title().send(sender);
            }
        } else {
            this.plugin.getTL().noPerms().title().send(sender);
        }

        return true;
    }
}