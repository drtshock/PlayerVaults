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

import java.io.File;

public class Cleanup implements Runnable {

    private final long diff;

    public Cleanup(int diff) {
        this.diff = diff * 86400000;
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
            if (file.isDirectory()) {
                continue;
            }
            if (time - file.lastModified() > diff) {
                PlayerVaults.getInstance().getLogger().info("Deleting vault file (cleanup): " + file.getName());
                file.delete();
            }
        }
    }
}
