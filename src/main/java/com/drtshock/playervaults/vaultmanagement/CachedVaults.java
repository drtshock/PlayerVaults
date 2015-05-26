package com.drtshock.playervaults.vaultmanagement;

import org.bukkit.inventory.Inventory;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("serial") public class CachedVaults extends HashMap<UUID, CachedVaultsMap> {
    public void setCachedVault(UUID playerUUID, int id, Inventory inventory) {
        CachedVaultsMap vaultCacheMap = this.containsKey(playerUUID) ? this.get(playerUUID) : new CachedVaultsMap();
        vaultCacheMap.setCachedVault(id, inventory);
        this.put(playerUUID, vaultCacheMap);
    }

    public Inventory getCachedVault(UUID playerUUID, int id) {
        return this.containsKey(playerUUID) ? this.get(playerUUID).getCachedVault(id) : null;
    }

    public boolean hasVaultCached(UUID playerUUID, int id) {
        return this.containsKey(playerUUID) && this.get(playerUUID).getCachedVault(id) != null;
    }

    public void clearVaultCache(UUID playerUUID) {
        if (this.containsKey(playerUUID)) {
            this.get(playerUUID).clear();
        }
    }

    public void deleteVaultCache(UUID playerUUID) {
        this.remove(playerUUID);
    }

    public void flushVaultCacheToFile(UUID playerUUID) {
        if (this.containsKey(playerUUID)) {
            for (java.util.Map.Entry<Integer, Inventory> data : this.get(playerUUID).entrySet()) {
                try {
                    UUIDVaultManager.getInstance().saveVault(data.getValue(), playerUUID, data.getKey());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            this.deleteVaultCache(playerUUID);
        }
    }
}
