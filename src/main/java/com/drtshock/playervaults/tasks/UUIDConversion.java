package com.drtshock.playervaults.tasks;

import com.drtshock.playervaults.PlayerVaults;
import net.minecraft.util.org.apache.commons.io.FileUtils;
import net.minecraft.util.org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Class to convert vaults by name to vaults by UUID.
 */
public final class UUIDConversion extends BukkitRunnable {

    @Override
    public void run() {
        if (new File(PlayerVaults.PLUGIN.getDataFolder(), "uuidvaults").exists()) {
            PlayerVaults.LOG.log(Level.INFO, "Files already converted to UUID.");
            return;
        }

        PlayerVaults.LOG.log(Level.INFO, "********** Starting PlayerVault conversion to UUIDs **********");
        PlayerVaults.LOG.log(Level.INFO, "This might take awhile.");
        PlayerVaults.LOG.log(Level.INFO, "plugins/PlayerVaults/vaults will still be there as a backup but unused.");

        for (File file : new File(PlayerVaults.PLUGIN.getDataFolder() + File.separator + "vaults").listFiles()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(FilenameUtils.removeExtension(file.toString()).replace(".yml", ""));
            if (player == null) {
                PlayerVaults.LOG.log(Level.WARNING, "Unable to convert file because player never joined the server: " + file.getName());
                break;
            }
            UUID uuid = player.getUniqueId();
            File newFile = new File(PlayerVaults.PLUGIN.getDataFolder(), "uuidvaults" + uuid.toString() + ".yml");
            file.mkdirs();
            try {
                FileUtils.copyFile(file, newFile);
                PlayerVaults.LOG.log(Level.INFO, "Successfully converted vault file for " + player.getName());
            } catch (IOException e) {
                PlayerVaults.LOG.log(Level.SEVERE, "Couldn't convert vault file for " + player.getName());
            }
        }
        PlayerVaults.LOG.log(Level.INFO, "********** Conversion done ;D **********");
    }
}
