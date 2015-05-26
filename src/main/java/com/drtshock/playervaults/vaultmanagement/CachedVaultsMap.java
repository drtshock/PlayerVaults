package com.drtshock.playervaults.vaultmanagement;

import org.bukkit.inventory.Inventory;

import java.util.HashMap;

@SuppressWarnings("serial") public class CachedVaultsMap extends HashMap<Integer, Inventory> {
    public void setCachedVault(int id, Inventory inventory) {
        this.put(id, inventory);
    }

    public Inventory getCachedVault(int id) {
        return this.containsKey(id) ? this.get(id) : null;
    }
}
