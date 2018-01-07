package com.drtshock.playervaults.translations;

public enum Language {
    ENGLISH("english"),
    BULGARIAN("bulgarian"),
    DUTCH("dutch");

    private String friendlyName;

    Language(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return this.friendlyName;
    }
}
