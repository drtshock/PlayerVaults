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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.UUID;

/**
 * Simple converter for Backpack (http://dev.bukkit.org/bukkit-plugins/backpack/)
 *
 * @author turt2live
 */
public class BackpackConverter implements Converter {

    @Override
    public int run(CommandSender initiator) {

        PlayerVaults plugin = PlayerVaults.getInstance();
        File destination = new File(plugin.getDataFolder().getParentFile(), "Backpack" + File.separator + "backpacks");
        if (!destination.exists()) {
            return -1;
        }

        int converted = 0;

        File[] worldDirs = destination.listFiles();
        int vaultNum = 1;
        for (File file : worldDirs != null ? worldDirs : new File[0]) {
            if (file.isDirectory()) {
                converted += convert(file, vaultNum);
                vaultNum++;
            }
        }

        return converted;
    }

    private int convert(File worldFolder, int intoVaultNum) {
        PlayerVaults plugin = PlayerVaults.getInstance();
        VaultManager vaults = VaultManager.getInstance();
        int converted = 0;
        long lastUpdate = 0;
        File[] files = worldFolder.listFiles();
        for (File file : files != null ? files : new File[0]) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".yml")) {
                try {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(file.getName().substring(0, file.getName().lastIndexOf('.')));
                    if (player == null || player.getUniqueId() == null) {
                        plugin.getLogger().warning("Unable to convert Backpack for player: " + (player != null ? player.getName() : file.getName()));
                    } else {
                        UUID uuid = player.getUniqueId();
                        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                        ConfigurationSection section = yaml.getConfigurationSection("backpack");
                        if (section.getKeys(false).size() <= 0) {
                            continue; // No slots
                        }

                        Inventory vault = vaults.getVault(uuid.toString(), intoVaultNum);
                        if (vault == null) {
                            vault = plugin.getServer().createInventory(null, section.getKeys(false).size());
                        }
                        for (String key : section.getKeys(false)) {
                            ConfigurationSection slotSection = section.getConfigurationSection(key);
                            ItemStack item = slotSection.getItemStack("ItemStack");
                            if (item == null) {
                                continue;
                            }

                            // Overwrite
                            vault.setItem(Integer.parseInt(key.split(" ")[1]), item);
                        }
                        vaults.saveVault(vault, uuid.toString(), intoVaultNum);
                        converted++;

                        if (System.currentTimeMillis() - lastUpdate >= 1500) {
                            plugin.getLogger().info(converted + " backpacks have been converted in " + worldFolder.getAbsolutePath());
                            lastUpdate = System.currentTimeMillis();
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error converting " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }
        }
        return converted;
    }

    @Override
    public boolean canConvert() {
        PlayerVaults plugin = PlayerVaults.getInstance();
        File expectedFolder = new File(plugin.getDataFolder().getParentFile(), "Backpack");
        if (!expectedFolder.exists()) {
            return false;
        }
        File backpackDir = new File(expectedFolder, "backpacks");
        return backpackDir.exists();
    }

    @Override
    public String getName() {
        return "Backpack";
    }
}
