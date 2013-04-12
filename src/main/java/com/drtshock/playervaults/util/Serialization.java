package com.drtshock.playervaults.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Serialization {

    /*
     * All normal functions
     */

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            map.put(key, fromJson(object.get(key)));
        }
        return map;
    }

    private static Object fromJson(Object json) throws JSONException {
        if(json == JSONObject.NULL) {
            return null;
        } else if(json instanceof JSONObject) {
            return toMap((JSONObject) json);
        } else if(json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            list.add(fromJson(array.get(i)));
        }
        return list;
    }

    public static List<String> toString(Inventory inv) {
        List<String> result = new ArrayList<String>();
        List<ConfigurationSerializable> items = new ArrayList<ConfigurationSerializable>();
        for(ItemStack is:inv.getContents()) {
            items.add(is);
        }
        for(ConfigurationSerializable cs:items) {
            if(cs == null) {
                result.add("null");
            }
            else {
                result.add(new JSONObject(serialize(cs)).toString());
            }
        }
        return result;
    }

    public static Inventory toInventory(List<String> stringItems, int number) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.RED + "Vault #" + number);
        List<ItemStack> contents = new ArrayList<ItemStack>();
        for(String piece:stringItems) {
            if(piece.equalsIgnoreCase("null")) {
                contents.add(null);
            }
            else {
                try {
                    ItemStack item = (ItemStack) deserialize(toMap(new JSONObject(piece)));
                    contents.add(item);
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        ItemStack[] items = new ItemStack[contents.size()];
        for(int x = 0; x < contents.size(); x++)
            items[x] = contents.get(x);
        inv.setContents(items);
        return inv;
    }

    public static Map<String, Object> serialize(ConfigurationSerializable cs) {
        Map<String, Object> serialized = recreateMap(cs.serialize());
        for(Entry<String, Object> entry:serialized.entrySet()) {
            if(entry.getValue() instanceof ConfigurationSerializable) {
                entry.setValue(serialize((ConfigurationSerializable) entry.getValue()));
            }
        }
        serialized.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(cs.getClass()));
        return serialized;
    }

    public static Map<String, Object> recreateMap(Map<String, Object> original) {
        Map<String, Object> map = new HashMap<String, Object>();
        for(Entry<String, Object> entry:original.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static ConfigurationSerializable deserialize(Map<String, Object> map) {
        for(Entry<String, Object> entry:map.entrySet()) {
            // Check if any of its sub-maps are ConfigurationSerializable. They need to be done
            // first.
            if(entry.getValue() instanceof Map && ((Map) entry.getValue()).containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                entry.setValue(deserialize((Map) entry.getValue()));
            }
        }
        return ConfigurationSerialization.deserializeObject(map);
    }

    /*
     * All old methods for transferring
     */

}
