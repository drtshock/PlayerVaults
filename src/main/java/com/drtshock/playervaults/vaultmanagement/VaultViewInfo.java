package com.drtshock.playervaults.vaultmanagement;

/**
 * A class that stores information about a vault viewing including the holder of the vault, and the vault number.
 */
public class VaultViewInfo {

    final String vaultName;
    final int number;

    /**
     * Makes a VaultViewInfo object. Used for opening a vault owned by the opener.
     *
     * @param i vault number.
     */
    public VaultViewInfo(String vaultName, int i) {
        this.number = i;
        this.vaultName = vaultName;
    }

    /**
     * Get the holder of the vault.
     *
     * @return The holder of the vault.
     */
    public String getVaultName() {
        return this.vaultName;
    }

    /**
     * Get the vault number.
     *
     * @return The vault number.
     */
    public int getNumber() {
        return this.number;
    }

    @Override
    public String toString() {
        return this.vaultName + " " + this.number;
    }
}