/*
 * PlayerVaultsX
 * Copyright (C) 2013 Trent Hensler, turt2live
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

package com.drtshock.playervaults.converters;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.vaultmanagement.VaultManager;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Stream;

public class EnderVaultsConverter implements Converter {

    @SuppressWarnings("unchecked")
    @Override
    public int run(CommandSender initiator) {
        PlayerVaults plugin = PlayerVaults.getInstance();
        VaultManager vaultManager = VaultManager.getInstance();

        Path path = plugin.getDataFolder().toPath().getParent().resolve("EnderVaults").resolve("data");
        if (!Files.isDirectory(path)) {
            plugin.getLogger().warning("Could not find EnderVaults data folder");
            return -1;
        }

        MethodHandle load;
        MethodHandle getInventory;
        MethodHandle getMetadata;
        Plugin enderVaultsPlugin = plugin.getServer().getPluginManager().getPlugin("EnderVaults");
        Object dataStorage;

        if (enderVaultsPlugin == null) {
            plugin.getLogger().warning("EnderVaults not running. Need it to convert.");
            return -1;
        }
        try {
            Class<?> pluginClass = Class.forName("com.github.dig.endervaults.api.EnderVaultsPlugin");
            Class<?> dataStorageClass = Class.forName("com.github.dig.endervaults.api.storage.DataStorage");
            Class<?> bukkitVaultClass = Class.forName("com.github.dig.endervaults.bukkit.vault.BukkitVault");

            MethodHandles.Lookup lookup = MethodHandles.publicLookup();

            MethodType typeGetDataStorage = MethodType.methodType(dataStorageClass);
            MethodHandle getDataStorage = lookup.findVirtual(pluginClass, "getDataStorage", typeGetDataStorage);

            dataStorage = getDataStorage.invoke(enderVaultsPlugin);

            MethodType typeLoad = MethodType.methodType(List.class, UUID.class);
            load = lookup.findVirtual(dataStorageClass, "load", typeLoad);
            MethodType typeGetInventory = MethodType.methodType(Inventory.class);
            getInventory = lookup.findVirtual(bukkitVaultClass, "getInventory", typeGetInventory);
            MethodType typeGetMetadata = MethodType.methodType(Map.class);
            getMetadata = lookup.findVirtual(bukkitVaultClass, "getMetadata", typeGetMetadata);

            if (!pluginClass.isAssignableFrom(enderVaultsPlugin.getClass())) {
                plugin.getLogger().warning("EnderVaults plugin not of expected type");
                return -1;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return -1;
        }

        AtomicInteger playerCount = new AtomicInteger(0);
        try (Stream<Path> dir = Files.list(path)) {
            dir.forEach(f -> {
                if (!Files.isDirectory(f)) {
                    return;
                }
                try {
                    List<Object> list = (List<Object>) load.invoke(dataStorage, UUID.fromString(f.getFileName().toString()));
                    for (Object vault : list) {
                        Inventory inventory = (Inventory) getInventory.invoke(vault);
                        Map<String, Object> meta = (Map<String, Object>) getMetadata.invoke(vault);
                        Integer order = (Integer) meta.get("order");
                        vaultManager.saveVault(inventory, f.getFileName().toString(), order);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    return;
                }
                playerCount.incrementAndGet();
            });
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed. ", e);
        }
        return playerCount.get();
    }

    @Override
    public boolean canConvert() {
        return Files.isDirectory(PlayerVaults.getInstance().getDataFolder().toPath().getParent().resolve("EnderVaults").resolve("data"));
    }

    @Override
    public String getName() {
        return "EnderVaults";
    }
}
