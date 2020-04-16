package com.drtshock.playervaults.v5;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents an entity that can view and own vaults.
 */
public class VaultViewer implements InventoryHolder {
    private final String name;
    private final UUID uuid;
    private final OfflinePlayer offlinePlayer;
    private Inventory inventory;

    public VaultViewer(@NonNull String name) {
        Objects.requireNonNull(name, "Vault viewer name cannot be null");
        this.name = name;
        UUID uu;
        try {
            uu = UUID.fromString(name);
        } catch (IllegalArgumentException e) {
            uu = null;
        }
        this.uuid = uu;
        this.offlinePlayer = (this.uuid == null) ? null : Bukkit.getOfflinePlayer(this.uuid);
    }

    public VaultViewer(@NonNull UUID uuid) {
        this.name = uuid.toString();
        this.uuid = uuid;
        this.offlinePlayer = Bukkit.getOfflinePlayer(this.uuid);
    }

    public VaultViewer(@NonNull OfflinePlayer player) {
        this.uuid = player.getUniqueId();
        this.name = this.uuid.toString();
        this.offlinePlayer = player;
    }

    public @NonNull String getName() {
        return this.name;
    }

    public @Nullable UUID getUuid() {
        return this.uuid;
    }

    public @Nullable OfflinePlayer getOfflinePlayer() {
        return this.offlinePlayer;
    }

    public @Nullable Player getPlayer() {
        return this.offlinePlayer == null ? null : this.offlinePlayer.getPlayer();
    }

    @Override
    public @Nullable Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(@Nullable Inventory inventory) {
        this.inventory = inventory;
    }

    public boolean isProbablyPlayer() {
        return this.uuid != null && (this.uuid.version() == 3 || this.uuid.version() == 4);
    }
}
