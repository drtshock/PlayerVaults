package com.drtshock.playervaults.commands;

/**
 * A class for setting signs. Stores information about the sign owner, number,
 * and whether or not is opens a self vault or another person's vault.
 */
public class SignSetInfo {

    private String owner;
    private int number;
    private boolean self = false;

    /**
     * Construct a SignSetInfo object for another person.
     *
     * @param s The vault owner.
     * @param i The vault number.
     */
    public SignSetInfo(String s, int i) {
        this.owner = s;
        this.number = i;
    }

    /**
     * Construct a SignSetInfo object for opening to self.
     *
     * @param i The vault number.
     */
    public SignSetInfo(int i) {
        this.number = i;
        this.self = true;
    }

    /**
     * Get whether or not the sign will open their own vault or another
     * person's.
     *
     * @return Whether or not it is a 'self' sign.
     */
    public boolean isSelf() {
        return this.self;
    }

    /**
     * Get the owner of the vault.
     *
     * @return The owner of the vault.
     */
    public String getOwner() {
        return this.owner;
    }

    /**
     * Get the vault number.
     *
     * @return The vault number.
     */
    public int getChest() {
        return this.number;
    }
}
