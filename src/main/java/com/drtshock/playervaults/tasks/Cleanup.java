package com.drtshock.playervaults.tasks;

import com.drtshock.playervaults.PlayerVaults;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class Cleanup extends BukkitRunnable {

    private long diff;

    public Cleanup(int diff) {
        this.diff = diff * 86400;
    }


    @Override
    public void run() {
        File file = new File(PlayerVaults.getInstance().getDataFolder(), "vaults");
        if (!file.exists()) return;

        long time = System.currentTimeMillis();
        for (File f : file.listFiles()) {
            if (time - f.lastModified() > diff) {
                f.delete();
                PlayerVaults.getInstance().getLogger().info("Deleting vault file: " + f.getName());
            }
        }
    }
}
