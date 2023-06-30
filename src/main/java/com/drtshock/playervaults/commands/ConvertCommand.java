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
import com.drtshock.playervaults.converters.*;
import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ConvertCommand implements CommandExecutor {

    private final List<Converter> converters = new ArrayList<>();
    private final PlayerVaults plugin;

    public ConvertCommand(PlayerVaults plugin) {
        converters.add(new BackpackConverter());
        converters.add(new Cosmic2Converter());
        converters.add(new Cosmic3Converter());
        converters.add(new EnderVaultsConverter());
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("playervaults.convert")) {
            this.plugin.getTL().noPerms().title().send(sender);
        } else {
            if (args.length == 0) {
                sender.sendMessage("/" + label + " <all | plugin name>");
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
                    this.plugin.getTL().convertPluginNotFound().title().send(sender);
                } else {
                    // Fork into background
                    this.plugin.getTL().convertBackground().title().send(sender);
                    PlayerVaults.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(PlayerVaults.getInstance(), () -> {
                        int converted = 0;
                        VaultOperations.setLocked(true);
                        for (Converter converter : applicableConverters) {
                            if (converter.canConvert()) {
                                converted += converter.run(sender);
                            }
                        }
                        VaultOperations.setLocked(false);
                        this.plugin.getTL().convertComplete().title().with("count", converted + "").send(sender);
                    }, 5);
                }
            }
        }
        return true;
    }
}