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
import com.google.common.io.Files;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Class to convert vaults by name to vaults by UUID.
 */
public final class UUIDConversion implements Runnable {

    @Override
    public void run() {
        Logger logger = PlayerVaults.getInstance().getLogger();

        File newDir = PlayerVaults.getInstance().getVaultData();
        if (newDir.exists()) {
            return;
        }

        File oldVaults = new File(PlayerVaults.getInstance().getDataFolder() + File.separator + "vaults");
        if (oldVaults.exists()) {
            logger.info("********** Starting conversion to UUIDs for PlayerVaults **********");
            logger.info("This might take awhile.");
            logger.info(oldVaults.toString() + " will remain as a backup.");

            for (File file : oldVaults.listFiles()) {
                if (file.isDirectory()) {
                    continue; // backups folder.
                }
                OfflinePlayer player = Bukkit.getOfflinePlayer(file.getName().replace(".yml", ""));
                if (player == null) {
                    logger.warning("Unable to convert file because player never joined the server: " + file.getName());
                    break;
                }

                File newFile = new File(PlayerVaults.getInstance().getVaultData(), player.getUniqueId().toString() + ".yml");
                file.mkdirs();
                try {
                    Files.copy(file, newFile);
                    logger.info("Successfully converted vault file for " + player.getName());
                } catch (IOException e) {
                    logger.severe("Couldn't convert vault file for " + player.getName());
                }
            }

            logger.info("********** Conversion done ;D **********");
        }
    }
}
