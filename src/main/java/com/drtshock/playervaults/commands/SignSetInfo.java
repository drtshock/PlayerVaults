package com.drtshock.playervaults.commands;

public class SignSetInfo {

    private String o;
    private int i;
    
    public SignSetInfo(String o, int i) {
        this.o = o;
        this.i = i;
    }
    
    public String getOwner() {
        return this.o;
    }
    
    public int getChest() {
        return this.i;
    }
    
}
