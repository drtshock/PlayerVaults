package com.drtshock.playervaults.commands;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.converters.BackpackConverter;
import com.drtshock.playervaults.converters.Converter;
import com.drtshock.playervaults.util.Lang;
import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import com.turt2live.uuid.CachingServiceProvider;
import com.turt2live.uuid.ServiceProvider;
import com.turt2live.uuid.turt2live.v2.ApiV2Service;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;

public class ConvertCommand implements CommandExecutor {

    private final List<Converter> converters = new ArrayList<>();
    private ServiceProvider uuidProvider;

    public ConvertCommand() {
        converters.add(new BackpackConverter());
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("playervaults.convert")) {
            sender.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS);
        } else {
            if (args.length == 0) {
                sender.sendMessage(Lang.TITLE + "/pvconvert <all | plugin name>");
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
                            if (uuidProvider == null) {
                                CachingServiceProvider cachingUuidProvider = new CachingServiceProvider(new ApiV2Service());
                                Map<UUID, String> seed = new HashMap<>();

                                for (OfflinePlayer player : PlayerVaults.getInstance().getServer().getOfflinePlayers()) {
                                    if (player.hasPlayedBefore()) {
                                        seed.put(player.getUniqueId(), player.getName());
                                    }
                                }

                                cachingUuidProvider.seedLoad(seed, 6 * 60 * 60); // 6 hour cache time
                                uuidProvider = cachingUuidProvider;
                            }

                            int converted = 0;
                            VaultOperations.setLocked(true);
                            for (Converter converter : applicableConverters) {
                                if (converter.canConvert()) {
                                    converted += converter.run(sender, uuidProvider);
                                }
                            }
                            VaultOperations.setLocked(false);
                            sender.sendMessage(Lang.TITLE + Lang.CONVERT_COMPLETE.toString().replace("%converted", converted + ""));
                        }
                    }, 5); // This comment is to annoy evilmidget38
                }
            }
        }

        return true;
    }
}
