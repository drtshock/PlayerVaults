package com.drtshock.playervaults.commands;

public class VaultViewInfo {

    String s;
    int i;

    public VaultViewInfo(String s, int i) {
        this.s = s;
        this.i = i;
    }

    public String getHolder() {
        return this.s;
    }

    public int getNumber() {
        return this.i;
    }

}
