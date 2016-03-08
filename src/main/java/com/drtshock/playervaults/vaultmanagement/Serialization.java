/* 
 * Copyright (C) 2013 drtshock
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.drtshock.playervaults.vaultmanagement;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Fancy JSON serialization mostly by evilmidget38.
 *
 * @author evilmidget38, gomeow
 */
public class Serialization {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(JSONObject object) {
        Map<String, Object> map = new HashMap<>();
        for (Object key : object.keySet()) {
            map.put(key.toString(), fromJson(object.get(key)));
        }
        return map;
    }

    private static Object fromJson(Object json) {
        if (json == null) {
            return null;
        } else if (json instanceof JSONObject) {
            return toMap((JSONObject) json);
        } else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }

    public static List<Object> toList(JSONArray array) {
        List<Object> list = new ArrayList<>();
        for (Object value : array) {
            list.add(fromJson(value));
        }
        return list;
    }

    public static List<String> toString(Inventory inv) {
        List<String> result = new ArrayList<>();
        List<ConfigurationSerializable> items = new ArrayList<>();
        Collections.addAll(items, inv.getContents());
        for (ConfigurationSerializable cs : items) {
            if (cs == null) {
                result.add("null");
            } else {
                result.add(new JSONObject(serialize(cs)).toString());
            }
        }
        return result;
    }

    public static Inventory toInventory(List<String> stringItems, int number, int size, String title) {
        VaultHolder holder = new VaultHolder(number);
        Inventory inv = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inv);
        List<ItemStack> contents = new ArrayList<>();
        for (String piece : stringItems) {
            if (piece.equalsIgnoreCase("null")) {
                contents.add(null);
            } else {
                ItemStack item = (ItemStack) deserialize(toMap((JSONObject) JSONValue.parse(piece)));
                contents.add(item);
            }
        }
        ItemStack[] items = new ItemStack[contents.size()];
        for (int x = 0; x < contents.size(); x++) {
            items[x] = contents.get(x);
        }
        inv.setContents(items);
        return inv;
    }

    public static Map<String, Object> serialize(ConfigurationSerializable cs) {
        Map<String, Object> returnVal = handleSerialization(cs.serialize());
        returnVal.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(cs.getClass()));
        return returnVal;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> handleSerialization(Map<String, Object> map) {
        Map<String, Object> serialized = recreateMap(map);
        for (Entry<String, Object> entry : serialized.entrySet()) {
            if (entry.getValue() instanceof ConfigurationSerializable) {
                entry.setValue(serialize((ConfigurationSerializable) entry.getValue()));
            } else if (entry.getValue() instanceof Iterable<?>) {
                List<Object> newList = new ArrayList<>();
                for (Object object : ((Iterable) entry.getValue())) {
                    if (object instanceof ConfigurationSerializable) {
                        object = serialize((ConfigurationSerializable) object);
                    }
                    newList.add(object);
                }
                entry.setValue(newList);
            } else if (entry.getValue() instanceof Map<?, ?>) {
                // unchecked cast here.  If you're serializing to a non-standard Map you deserve ClassCastExceptions
                entry.setValue(handleSerialization((Map<String, Object>) entry.getValue()));
            }
        }
        return serialized;
    }

    public static Map<String, Object> recreateMap(Map<String, Object> original) {
        Map<String, Object> map = new HashMap<>();
        map.putAll(original);
        return map;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ConfigurationSerializable deserialize(Map<String, Object> map) {
        for (Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map && ((Map) entry.getValue()).containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                entry.setValue(deserialize((Map) entry.getValue()));
            } else if (entry.getValue() instanceof Iterable) {
                entry.setValue(convertIterable((Iterable) entry.getValue()));
            }
        }
        return ConfigurationSerialization.deserializeObject(map);
    }

    private static List<?> convertIterable(Iterable<?> iterable) {
        List<Object> newList = new ArrayList<>();
        for (Object object : iterable) {
            if (object instanceof Map) {
                object = deserialize((Map<String, Object>) object);
            } else if (object instanceof List) {
                object = convertIterable((Iterable) object);
            }
            newList.add(object);
        }
        return newList;
    }
}