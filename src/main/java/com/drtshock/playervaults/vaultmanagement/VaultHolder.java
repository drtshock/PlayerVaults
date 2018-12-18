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

package com.drtshock.playervaults.vaultmanagement;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Represents a VaultHolder to assist in detection of Player Vaults from other plugins.
 */
public class VaultHolder implements InventoryHolder {

    private Inventory inventory;
    private int vaultNumber = 0;

    /**
     * Creates a new vault holder
     *
     * @param vaultNumber the vault number this holder is using
     */
    public VaultHolder(int vaultNumber) {
        this.vaultNumber = vaultNumber;
    }

    /**
     * Gets the vault number this holder is currently using
     *
     * @return the vault number
     */
    public int getVaultNumber() {
        return vaultNumber;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Sets the inventory this vault holder holds
     *
     * @param inventory the inventory, may be null
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
