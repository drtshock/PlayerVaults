package com.drtshock.playervaults.tasks;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.vaultmanagement.UUIDVaultManager;
import com.drtshock.playervaults.vaultmanagement.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;

import java.io.File;
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
        if (newDir.exists()) {
            return;
        }

        newDir.mkdirs();

        File oldVaults = PlayerVaults.getInstance().getUuidData();
        if (!oldVaults.exists()) {
            logger.info("No uuidvaults found to convert to base64.");
            return;
        }

        newDir.mkdirs();

        UUIDVaultManager oldManager = UUIDVaultManager.getInstance();
        VaultManager manager = VaultManager.getInstance();

        logger.info("********** Starting conversion to Base64 for PlayerVaults **********");
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

                int vaultNumber = Integer.valueOf(key.replace("vault", ""));

                try {
                    Inventory inventory = oldManager.getVault(holderUUID, vaultNumber);
                    manager.saveVault(inventory, holderUUID, vaultNumber);
                    vaults++;
                } catch (Exception e) {
                    logger.severe("Failed to parse vault " + vaultNumber + " for " + holderUUID);
                    failed++;
                }
            }

            players++;
        }

        logger.info(String.format("Converted %d vaults for %d players to base64. %d failed to convert", vaults, players, failed));
    }
}
