/*
 * PlayerVaultsX
 * Copyright (C) 2013 Trent Hensler, Laxwashere
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

package com.drtshock.playervaults.vaultmanagement;


import com.drtshock.playervaults.PlayerVaults;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;

/**
 * Created by Lax on 6/6/2017.
 */
public class Base64Serialization {

    public static String toBase64(Inventory inventory, int size) {
        return toBase64(inventory, size, null);
    }

    public static String toBase64(Inventory inventory, int size, String target) {
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
                PlayerVaults.getInstance().getLogger().severe("Failed to save " + failedItems + " invalid items to vault " + target);
            }
            PlayerVaults.debug("Serialized " + inventory.getSize() + " items");

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(finalOutputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot into itemstacksz!", e);
        }
    }

    public static String toBase64(ItemStack[] is, int size) {
        Inventory inventory = Bukkit.createInventory(null, size);
        inventory.setContents(is);
        return toBase64(inventory, size);
    }

    public static Inventory fromBase64(String data) {
        return fromBase64(data, null);
    }

    public static Inventory fromBase64(String data, String target) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
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
            PlayerVaults.getInstance().getLogger().log(Level.SEVERE, "Failed to load vault " + target, e);
        }
        return null;
    }
}