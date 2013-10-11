package com.drtshock.playervaults.vaultmanagement;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Represents a VaultHolder to assist in detection of Player Vaults from other
 * plugins.
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

    /**
     * Sets the inventory this vault holder holds
     *
     * @param inventory the inventory, may be null
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
