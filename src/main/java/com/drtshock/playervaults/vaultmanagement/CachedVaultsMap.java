package com.drtshock.playervaults.vaultmanagement;

import java.util.HashMap;

import org.bukkit.inventory.Inventory;

@SuppressWarnings("serial")
public class CachedVaultsMap extends HashMap<Integer, Inventory> {
    public void setCachedVault(int id, Inventory inventory){
        this.put(id, inventory);
    }
    
    public Inventory getCachedVault(int id){
        return this.containsKey(id) ? this.get(id) : null;
    }
}
