package com.drtshock.playervaults.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;

import com.drtshock.playervaults.PlayerVaults;

public class Updater extends PlayerVaults {

    SortedMap<String, String> lang = new TreeMap<String, String>();
    String version;

    public Updater(String version) {
        this.version = version;
        YamlConfiguration langConf = super.getLang();
        for(Lang item:Lang.values()) {
            if(langConf.getString(item.getPath()) == null) {
                langConf.set(item.getPath(), item.getDefault());
            }
        }
        try {
            langConf.save(super.getLangFile());
        } catch(IOException e) {
            log.log(Level.WARNING, "PlayerVaults: Failed to save lang.yml.");
            log.log(Level.WARNING, "PlayerVaults: Report this stack trace to drtshock and gomeow.");
            e.printStackTrace();
        }
    }

    String newVersion = "";

    public String getNewVersion() {
        return this.newVersion;
    }

    public boolean getUpdate() throws Exception {
        String version = this.version;
        URL url = new URL("http://dev.bukkit.org/server-mods/playervaults/files.rss");
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(url.openStream());
        } catch(UnknownHostException e) {
            return false;
        }
        BufferedReader in = new BufferedReader(isr);
        String line;
        int lineNum = 0;
        while ((line = in.readLine()) != null) {
            if(line.length() != line.replace("<title>", "").length()) {
                line = line.replaceAll("<title>", "").replaceAll("</title>", "").replaceAll("	", "").substring(1);
                if(lineNum == 1) {
                    this.newVersion = line;
                    Integer newVer = Integer.parseInt(line.replace(".", ""));
                    Integer oldVer = Integer.parseInt(version.replace(".", ""));
                    if(oldVer < newVer) {
                        return true;
                    }
                    else if(oldVer > newVer) {
                        return false;
                    }
                    else {
                        return false;
                    }
                }
                lineNum = lineNum + 1;
            }
        }
        in.close();
        return false;
    }
}