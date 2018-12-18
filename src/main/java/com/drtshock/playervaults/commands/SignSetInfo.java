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

package com.drtshock.playervaults.commands;

/**
 * A class for setting signs. Stores information about the sign owner, number, and whether or not is opens a self vault
 * or another person's vault.
 */
public class SignSetInfo {

    private final int number;
    private String owner;
    private boolean self = false;

    /**
     * Construct a SignSetInfo object for another person.
     *
     * @param s The vault owner.
     * @param i The vault number.
     */
    public SignSetInfo(String s, int i) {
        this.owner = s;
        this.number = i;
    }

    /**
     * Construct a SignSetInfo object for opening to self.
     *
     * @param i The vault number.
     */
    public SignSetInfo(int i) {
        this.number = i;
        this.self = true;
    }

    /**
     * Get whether or not the sign will open their own vault or another person's.
     *
     * @return Whether or not it is a 'self' sign.
     */
    public boolean isSelf() {
        return this.self;
    }

    /**
     * Get the owner of the vault.
     *
     * @return The owner of the vault.
     */
    public String getOwner() {
        return this.owner;
    }

    /**
     * Get the vault number.
     *
     * @return The vault number.
     */
    public int getChest() {
        return this.number;
    }
}
