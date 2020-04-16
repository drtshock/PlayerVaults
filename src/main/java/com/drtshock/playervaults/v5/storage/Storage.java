package com.drtshock.playervaults.v5.storage;

import com.drtshock.playervaults.v5.VaultInfo;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Storage {
    /**
     * Returns a, at most, six by nine array of inventory data.
     *
     * @param vaultInfo
     * @return
     */
    byte[][] getVault(@NonNull VaultInfo vaultInfo);

    byte[][] getVault(@NonNull VaultInfo vaultInfo, int maxRows);
}
