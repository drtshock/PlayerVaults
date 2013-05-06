package com.drtshock.playervaults.commands;

public class SignSetInfo {

    private String o;
    private int i;
    private boolean self = false;
    
    public SignSetInfo(String o, int i) {
        this.o = o;
        this.i = i;
    }
    
    public SignSetInfo(int i) {
        this.i = i;
        this.self = true;
    }
    
    public boolean isSelf() {
        return this.self;
    }

    public String getOwner() {
        return this.o;
    }
    
    public int getChest() {
        return this.i;
    }
    
}
