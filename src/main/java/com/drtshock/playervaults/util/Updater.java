package com.drtshock.playervaults.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.drtshock.playervaults.PlayerVaults;

/**
 * A class for updating the lang.yml and checking for updates at DBO.
 */
public class Updater extends PlayerVaults {

    SortedMap<String, String> lang = new TreeMap<String, String>();

    /**
     * Check whether or not there is a new update.
     * @param currentVersion The current running version.
     * @return Whether or not an update is available.
     * @throws IOException Oh no!
     */
    public boolean getUpdate(String currentVersion) throws IOException {
        JSONObject json;
        try {
            json = getInfo();
            String version = json.getString("dbo_version");
            String link = json.getString("link");
            PlayerVaults.NEWVERSION = version;
            String goodLink = new BufferedReader(new InputStreamReader(new URL("http://is.gd/create.php?format=simple&url=" + link).openStream())).readLine();
            PlayerVaults.LINK = goodLink;
            if (!version.equalsIgnoreCase(currentVersion)) {
                return true;
            }
        } catch(JSONException e) {
            throw new IOException();
        }
        return false;
    }

    /**
     * Get the information about versions from DBO.
     * @return The information in JSON.
     * @throws IOException Oh no!
     */
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
            throw new IOException("Oh no!");
        }
    }
}