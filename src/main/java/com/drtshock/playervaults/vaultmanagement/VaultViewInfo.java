package com.drtshock.playervaults.vaultmanagement;

/**
 * A class that stores information about a vault viewing including the holder of
 * the vault, and the vault number.
 */
public class VaultViewInfo {

    String holder;
    int number;

    /**
     * Make a VaultViewObject
     *
     * @param s The holder of the vault.
     * @param i The vault number.
     */
    public VaultViewInfo(String s, int i) {
        this.holder = s;
        this.number = i;
    }

    /**
     * Get the holder of the vault.
     *
     * @return The holder of the vault.
     */
    public String getHolder() {
        return this.holder;
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
        return this.holder + " " + this.number;
    }
}
