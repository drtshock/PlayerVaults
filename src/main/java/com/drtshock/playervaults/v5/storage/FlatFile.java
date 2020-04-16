package com.drtshock.playervaults.v5.storage;

import com.drtshock.playervaults.v5.VaultInfo;
import org.checkerframework.checker.nullness.qual.NonNull;

public class FlatFile implements Storage {
    @Override
    public byte[][] getVault(@NonNull VaultInfo vaultInfo) {
        return new byte[6][];
    }

    @Override
    public byte[][] getVault(@NonNull VaultInfo vaultInfo, int maxRows) {
        return new byte[6][];
    }
}
