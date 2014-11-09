package com.drtshock.playervaults.tasks;

import com.drtshock.playervaults.PlayerVaults;
import com.google.common.io.Files;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Class to convert vaults by name to vaults by UUID.
 */
public final class UUIDConversion extends BukkitRunnable {
    @Override
    public void run() {
        Logger logger = PlayerVaults.getInstance().getLogger();

        File newDir = PlayerVaults.getInstance().getVaultData();
        if (newDir.exists()) {
            logger.info("** Vaults have already been converted to UUIDs. If this is incorrect, shutdown your server and rename the " + newDir.toString() + " directory.");
            return;
        }

        newDir.mkdirs();

        File oldVaults = new File(PlayerVaults.getInstance().getDataFolder() + File.separator + "vaults");
        if (oldVaults.exists()) {
            logger.info("********** Starting conversion to UUIDs **********");
            logger.info("This might take awhile.");
            logger.info(oldVaults.toString() + " will remain as a backup.");

            for (File file : oldVaults.listFiles()) {
                if (file.isDirectory()) continue; // backups folder.
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
