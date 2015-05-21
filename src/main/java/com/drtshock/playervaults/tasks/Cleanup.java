package com.drtshock.playervaults.tasks;

import com.drtshock.playervaults.PlayerVaults;

import java.io.File;

public class Cleanup implements Runnable {

    private long diff;

    public Cleanup(int diff) {
        this.diff = diff * 86400;
    }

    @Override
    public void run() {
        File directory = PlayerVaults.getInstance().getVaultData();
        if (!directory.exists()) {
            // folder doesn't exist, don't run
            return;
        }

        long time = System.currentTimeMillis();
        for (File file : directory.listFiles()) {
            if (time - file.lastModified() > diff) {
                PlayerVaults.getInstance().getLogger().info("Deleting vault file (cleanup): " + file.getName());
                file.delete();
            }
        }
    }
}
