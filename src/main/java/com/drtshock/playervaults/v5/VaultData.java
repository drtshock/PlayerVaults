package com.drtshock.playervaults.v5;

import org.bukkit.inventory.Inventory;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class VaultData {
    private final VaultInfo info;
    private final Inventory[] inventories;

    public VaultData(@NonNull VaultInfo info, @NonNull Inventory[] inventories) {
        this.info = info;
        this.inventories = inventories;
    }

    public @NonNull VaultInfo getInfo() {
        return this.info;
    }

    public @NonNull Inventory[] getInventories() {
        return this.inventories;
    }
}
