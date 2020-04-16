package com.drtshock.playervaults.v5;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public final class VaultInfo {
    private final VaultViewer owner;
    private final int number;
    private final String nickname;

    public VaultInfo(@NonNull VaultViewer owner, int number) {
        this(owner, number, null);
    }

    public VaultInfo(@NonNull VaultViewer owner, int number, @Nullable String nickname) {
        Objects.requireNonNull(owner, "Vault owner cannot be null");
        this.owner = owner;
        this.number = number;
        this.nickname = nickname;
    }

    public int getNumber() {
        return this.number;
    }

    public @NonNull VaultViewer getOwner() {
        return this.owner;
    }

    public @Nullable String getNickname() {
        return this.nickname;
    }

    @Override
    public int hashCode() {
        return Objects.hash("VaultData", this.owner.getName(), this.number);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof VaultInfo) {
            VaultInfo i = (VaultInfo) obj;
            return this.number == i.number && this.owner.getName().equals(i.owner.getName());
        }
        return false;
    }

    @Override
    public @NonNull String toString() {
        return "VaultInfo{" +
                "owner='" + this.owner.getName() + '\'' +
                ", number=" + this.number +
                '}';
    }
}
