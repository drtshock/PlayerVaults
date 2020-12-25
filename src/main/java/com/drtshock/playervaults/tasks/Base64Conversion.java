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

package com.drtshock.playervaults.tasks;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.vaultmanagement.CardboardBoxSerialization;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Class to convert vaults by Bukkit serialization to Base64.
 */
public final class Base64Conversion implements Runnable {

    @Override
    public void run() {
        Logger logger = PlayerVaults.getInstance().getLogger();

        File newDir = PlayerVaults.getInstance().getVaultData();
        File oldVaults = PlayerVaults.getInstance().getDataFolder().toPath().resolve("base64vaults").toFile();
        File reallyOldVaults = PlayerVaults.getInstance().getDataFolder().toPath().resolve("uuidvaults").toFile();
        if (newDir.exists()) {
            PlayerVaults.getInstance().getDataFolder().toPath().resolve("oldVaultsData").toFile().mkdirs();
            if (oldVaults.exists()) {
                try {
                    Files.move(oldVaults.toPath(), PlayerVaults.getInstance().getDataFolder().toPath().resolve("oldVaultsData").resolve("base64vaults"));
                } catch (IOException e) {
                    PlayerVaults.getInstance().getLogger().warning("Failed to move old vaults dir: " + e.getMessage());
                }
            }
            if (reallyOldVaults.exists()) {
                try {
                    Files.move(reallyOldVaults.toPath(), PlayerVaults.getInstance().getDataFolder().toPath().resolve("oldVaultsData").resolve("uuidvaults"));
                } catch (IOException e) {
                    PlayerVaults.getInstance().getLogger().warning("Failed to move old vaults dir: " + e.getMessage());
                }
            }
            return;
        }

        newDir.mkdirs();

        if (!oldVaults.exists()) {
            logger.info("No base64vaults found to convert to new format.");
            return;
        }


        logger.info("********** Starting conversion from Base64 for PlayerVaults **********");
        logger.info("This might take awhile.");
        logger.info(oldVaults.toString() + " will remain as a backup.");

        int players = 0;
        int vaults = 0;
        int failed = 0;
        for (File file : oldVaults.listFiles()) {
            if (file.isDirectory()) {
                continue; // backups folder.
            }

            FileConfiguration uuidFile = YamlConfiguration.loadConfiguration(file);
            String stringUUID = file.getName().replace(".yml", "");
            UUID holderUUID;
            try {
                holderUUID = UUID.fromString(stringUUID);
            } catch (Exception e) {
                logger.warning(String.format("Failed to parse uuid for %s. Trying to convert", stringUUID));
                OfflinePlayer player = Bukkit.getOfflinePlayer(stringUUID);
                if (player != null) {
                    logger.info(String.format("Successfully got UUID for %s", stringUUID));
                    holderUUID = player.getUniqueId();
                } else {
                    logger.warning(String.format("Failed to convert name to uuid for %s", stringUUID));
                    continue;
                }
            }

            for (String key : uuidFile.getKeys(false)) {
                if (!key.startsWith("vault")) {
                    continue;
                }

                int vaultNumber = Integer.parseInt(key.replace("vault", ""));

                try {
                    String data = uuidFile.getString(key);
                    String newData = CardboardBoxSerialization.convert(data, holderUUID + " " + key);
                    uuidFile.set(key, newData);
                    vaults++;
                } catch (Exception e) {
                    logger.severe("Failed to parse vault " + vaultNumber + " for " + holderUUID);
                    failed++;
                }
            }
            try {
                uuidFile.save(newDir.toPath().resolve(file.getName()).toFile());
            } catch (IOException e) {
                logger.severe("Failed to save new file " + file.getName());
            }

            players++;
        }

        logger.info(String.format("Converted %d vaults for %d players to new storage. %d failed to convert", vaults, players, failed));
    }
}
