/*
 * Copyright (C) 2013 drtshock
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

import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * A class that stores information about a vault viewing including the holder of the vault, and the vault number.
 */
public class VaultViewInfo {

    final String holder;
    final int number;
    UUID uuid;

    /**
     * Make a VaultViewObject.
     *
     * @param s The holder of the vault.
     * @param i The vault number.
     */
    @Deprecated
    public VaultViewInfo(String s, int i) {
        this.holder = s;
        this.number = i;
    }

    /**
     * Makes a VaultViewInfo object.
     *
     * @param uuid uuid of viewer.
     * @param i    vault number
     */
    public VaultViewInfo(UUID uuid, int i) {
        this.uuid = uuid;
        this.number = i;
        this.holder = Bukkit.getOfflinePlayer(uuid).getName();
    }

    /**
     * Get the holder of the vault.
     *
     * @return The holder of the vault.
     */
    @Deprecated
    public String getHolder() {
        return this.holder;
    }

    /**
     * Get the vault holder's UUID.
     *
     * @return The vault holder's UUID.
     */
    public UUID getHolderUUID() {
        return this.uuid;
    }

    /**
     * Get the vault number.
     *
     * @return The vault number.
     */
    public int getNumber() {
        return this.number;
    }

    @Override
    public String toString() {
        return this.uuid + " " + this.number;
    }
}