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
import org.json.JSONException;
import org.json.JSONObject;

import com.drtshock.playervaults.PlayerVaults;

public class Updater extends PlayerVaults {

    SortedMap<String, String> lang = new TreeMap<String, String>();

    public Updater() {
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

    public boolean getUpdate(String v) throws IOException {
        JSONObject json;
        try {
            json = getInfo();
            String version = json.getString("dbo_version");
            String link = json.getString("link");
            PlayerVaults.LINK = link;
            PlayerVaults.NEWVERSION = version;
            if(!version.equalsIgnoreCase(v)) {
                return true;
            }
        } catch(JSONException e) {
            throw new IOException();
        }
        return false;
    }

    public JSONObject getInfo() throws IOException {
        URL url = new URL("http://api.bukget.org/3/plugins/bukkit/playervaults/latest");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(url.openStream()));
        } catch(UnknownHostException e) {
            throw new IOException();
        }
        JSONObject json;
        try {
            json = new JSONObject(in.readLine()).getJSONArray("versions").getJSONObject(0);
            in.close();
            return json;
        } catch(JSONException e) {
        }
        return null;
    }
}