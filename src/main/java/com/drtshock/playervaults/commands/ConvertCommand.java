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
import com.drtshock.playervaults.converters.BackpackConverter;
import com.drtshock.playervaults.converters.Converter;
import com.drtshock.playervaults.translations.Lang;
import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ConvertCommand implements CommandExecutor {

    private final List<Converter> converters = new ArrayList<>();

    public ConvertCommand() {
        converters.add(new BackpackConverter());
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("playervaults.convert")) {
            sender.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS);
        } else {
            if (args.length == 0) {
                sender.sendMessage(Lang.TITLE + "/" + label + " <all | plugin name>");
            } else {
                String name = args[0];
                final List<Converter> applicableConverters = new ArrayList<>();
                if (name.equalsIgnoreCase("all")) {
                    applicableConverters.addAll(converters);
                } else {
                    for (Converter converter : converters) {
                        if (converter.getName().equalsIgnoreCase(name)) {
                            applicableConverters.add(converter);
                        }
                    }
                }
                if (applicableConverters.size() <= 0) {
                    sender.sendMessage(Lang.TITLE.toString() + Lang.CONVERT_PLUGIN_NOT_FOUND);
                } else {
                    // Fork into background
                    sender.sendMessage(Lang.TITLE + Lang.CONVERT_BACKGROUND.toString());
                    PlayerVaults.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(PlayerVaults.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            int converted = 0;
                            VaultOperations.setLocked(true);
                            for (Converter converter : applicableConverters) {
                                if (converter.canConvert()) {
                                    converted += converter.run(sender);
                                }
                            }
                            VaultOperations.setLocked(false);
                            sender.sendMessage(Lang.TITLE + Lang.CONVERT_COMPLETE.toString().replace("%converted", converted + ""));
                        }
                    }, 5);
                }
            }
        }
        return true;
    }
}