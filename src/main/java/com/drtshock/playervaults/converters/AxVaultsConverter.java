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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.logging.Level;

public class AxVaultsConverter implements Converter {

    @SuppressWarnings("unchecked")
    @Override
    public int run(CommandSender initiator) {
        PlayerVaults plugin = PlayerVaults.getInstance();
        VaultManager vaultManager = VaultManager.getInstance();

        Plugin axVaultsPlugin = plugin.getServer().getPluginManager().getPlugin("AxVaults");
        Object database;

        Set<String> uuids = new HashSet<>();

        if (axVaultsPlugin == null) {
            plugin.getLogger().warning("AxVaults not running. Need it to convert.");
            return -1;
        }
        try {
            Class<?> pluginClass = Class.forName("com.artillexstudios.axvaults.AxVaults");
            Class<?> databaseClass = Class.forName("com.artillexstudios.axvaults.database.Database");

            MethodHandles.Lookup lookup = MethodHandles.publicLookup();

            MethodType typeGetDatabase = MethodType.methodType(databaseClass);
            MethodHandle getDataStorage = lookup.findStatic(pluginClass, "getDatabase", typeGetDatabase);

            database = getDataStorage.invoke();

            Field field = database.getClass().getDeclaredField("conn");
            field.setAccessible(true);

            Object conn = field.get(database);

            MethodType typePrepareStatement = MethodType.methodType(PreparedStatement.class, String.class);
            MethodHandle prepareStatement = lookup.findVirtual(conn.getClass(), "prepareStatement", typePrepareStatement);

            try (PreparedStatement statement = (PreparedStatement) prepareStatement.invoke(conn, "SELECT * FROM axvaults_data ORDER BY uuid ASC")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String uuid = resultSet.getString("uuid");
                        uuids.add(uuid);
                        ItemStack[] items;
                        try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(resultSet.getBinaryStream("storage"))) {
                            items = (ItemStack[]) dataInput.readObject();
                        } catch (Exception e) {
                            initiator.getServer().getLogger().log(Level.WARNING, "Failed to load vault " + id + " for " + uuid, e);
                            continue;
                        }
                        Inventory inventory = Bukkit.createInventory(null, items.length % 9 == 0 ? items.length : (6 * 9), "Converting!");
                        inventory.setContents(items);
                        vaultManager.saveVault(inventory, uuid, id);
                    }
                }
            }

        } catch (Throwable e) {
            initiator.getServer().getLogger().log(Level.SEVERE, "Failed to convert vaults", e);
            return -1;
        }

        return uuids.size();
    }

    @Override
    public boolean canConvert() {
        return Bukkit.getServer().getPluginManager().isPluginEnabled("AxVaults");
    }

    @Override
    public String getName() {
        return "AxVaults";
    }
}
