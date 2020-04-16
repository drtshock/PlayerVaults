/*
 * PlayerVaultsX
 * Copyright (C) 2013-2020 Trent Hensler, Laxwashere, CmdrKittens
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

package com.drtshock.playervaults.v5;


import com.drtshock.playervaults.PlayerVaults;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;

/**
 * Created by Lax on 6/6/2017.
 */
public class Serialization {

    public static @NonNull String toBase64Lines(byte[] input) {
        return Base64Coder.encodeLines(input);
    }

    public static byte[] toBytes(@NonNull Inventory inventory, int size, @NonNull VaultInfo vaultInfo) {
        try {
            ByteArrayOutputStream finalOutputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream temporaryOutputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(temporaryOutputStream);
            int failedItems = 0;

            // Write the size of the inventory
            dataOutput.writeInt(size);

            // Save every element in the list
            for (int i = 0; i < inventory.getSize(); i++) {
                try {
                    dataOutput.writeObject(inventory.getItem(i));
                } catch (Exception ignored) {
                    failedItems++;
                    temporaryOutputStream.reset();
                } finally {
                    if (temporaryOutputStream.size() == 0) {
                        dataOutput.writeObject(null);
                    }
                    finalOutputStream.write(temporaryOutputStream.toByteArray());
                    temporaryOutputStream.reset();
                }
            }

            if (failedItems > 0) {
                PlayerVaults.getInstance().getLogger().severe("Failed to save " + failedItems + " invalid items to vault " + vaultInfo);
            }
            PlayerVaults.debug("Serialized " + inventory.getSize() + " items");

            // Serialize that array
            dataOutput.close();
            return finalOutputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot into itemstacksz!", e);
        }
    }

    public static byte[] fromBase64Lines(@NonNull String data) {
        return Base64Coder.decodeLines(data);
    }

    public static @Nullable Inventory fromBytes(byte[] data, VaultInfo vaultInfo) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());
            // Read the serialized inventory
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, (ItemStack) dataInput.readObject());
            }
            dataInput.close();
            PlayerVaults.debug("Read " + inventory.getSize() + " items");
            return inventory;
        } catch (Exception e) {
            PlayerVaults.getInstance().getLogger().log(Level.SEVERE, "Failed to load vault " + vaultInfo, e);
        }
        return null;
    }
}
