/*
 * PlayerVaultsX
 * Copyright (C) 2013 Trent Hensler, Laxwashere, CmdrKittens
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
package com.drtshock.playervaults.config;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.config.annotation.Comment;
import com.drtshock.playervaults.config.annotation.ConfigName;
import com.drtshock.playervaults.config.annotation.WipeOnReload;
import com.drtshock.playervaults.lib.com.typesafe.config.Config;
import com.drtshock.playervaults.lib.com.typesafe.config.ConfigFactory;
import com.drtshock.playervaults.lib.com.typesafe.config.ConfigRenderOptions;
import com.drtshock.playervaults.lib.com.typesafe.config.ConfigValue;
import com.drtshock.playervaults.lib.com.typesafe.config.ConfigValueFactory;
import com.drtshock.playervaults.lib.com.typesafe.config.ConfigValueType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Loader {
    public static void loadAndSave(@NonNull String fileName, @NonNull Object config) throws IOException, IllegalAccessException {
        File file = Loader.getFile(fileName);
        Loader.loadAndSave(file, Loader.getConf(file), config);
    }

    public static @NonNull File getFile(@NonNull String file) {
        Path configFolder = PlayerVaults.getInstance().getDataFolder().toPath();
        if (!configFolder.toFile().exists()) {
            configFolder.toFile().mkdir();
        }
        Path path = configFolder.resolve(file + ".conf");
        return path.toFile();
    }

    public static @NonNull Config getConf(@NonNull File file) {
        return ConfigFactory.parseFile(file);
    }

    public static void loadAndSave(@NonNull File file, @NonNull Config config, @NonNull Object configObject) throws IOException, IllegalAccessException {
        ConfigValue value = Loader.loadNode(config, configObject);
        String s = value.render(ConfigRenderOptions.defaults().setOriginComments(false).setComments(true).setJson(false));
        Files.write(file.toPath(), s.getBytes(StandardCharsets.UTF_8));
    }

    public static ConfigValue load(Config config, Object configObject) throws IOException, IllegalAccessException {
        return Loader.loadNode(config, configObject);
    }

    private static Set<Class<?>> types = new HashSet<>();

    static {
        Loader.types.add(Boolean.TYPE);
        Loader.types.add(Byte.TYPE);
        Loader.types.add(Character.TYPE);
        Loader.types.add(Double.TYPE);
        Loader.types.add(Float.TYPE);
        Loader.types.add(Integer.TYPE);
        Loader.types.add(Long.TYPE);
        Loader.types.add(Short.TYPE);
        Loader.types.add(List.class);
        Loader.types.add(Map.class);
        Loader.types.add(Set.class);
        Loader.types.add(String.class);
    }

    private static @NonNull ConfigValue loadNode(@NonNull Config config, @NonNull Object object) throws IllegalAccessException {
        return loadNode(config, "", object);
    }

    private static @NonNull ConfigValue loadNode(@NonNull Config config, String path, @NonNull Object object) throws IllegalAccessException {
        Map<String, ConfigValue> map = new HashMap<>();
        for (Field field : Loader.getFields(object.getClass())) {
            if (field.isSynthetic()) {
                continue;
            }
            if ((field.getModifiers() & Modifier.TRANSIENT) != 0) {
                if (field.getAnnotation(WipeOnReload.class) != null) {
                    field.setAccessible(true);
                    field.set(object, null);
                }
                continue;
            }
            field.setAccessible(true);
            ConfigName configName = field.getAnnotation(ConfigName.class);
            Comment comment = field.getAnnotation(Comment.class);
            String confName = configName == null || configName.value().isEmpty() ? field.getName() : configName.value();
            String newPath = path.isEmpty() ? confName : (path + '.' + confName);
            ConfigValue curValue = Loader.getOrNull(config, newPath);
            boolean needsValue = curValue == null;

            ConfigValue newValue;
            Object defaultValue = field.get(object);
            if (Loader.types.contains(field.getType())) {
                if (needsValue) {
                    newValue = ConfigValueFactory.fromAnyRef(defaultValue);
                } else {
                    try {
                        if (Set.class.isAssignableFrom(field.getType()) && curValue.valueType() == ConfigValueType.LIST) {
                            field.set(object, new HashSet<Object>((List<?>) curValue.unwrapped()));
                        } else {
                            field.set(object, curValue.unwrapped());
                        }
                        newValue = curValue;
                    } catch (IllegalArgumentException ex) {
                        System.out.println("Found incorrect type for " + confName + ": Expected " + field.getType() + ", found " + curValue.unwrapped().getClass());
                        field.set(object, defaultValue);
                        newValue = ConfigValueFactory.fromAnyRef(defaultValue);
                    }
                }
            } else {
                newValue = Loader.loadNode(config, newPath, defaultValue);
            }
            if (comment != null) {
                newValue = newValue.withOrigin(newValue.origin().withComments(Arrays.asList(comment.value().split("\n"))));
            }
            map.put(confName, newValue);
        }
        return ConfigValueFactory.fromMap(map);
    }

    private static @Nullable ConfigValue getOrNull(@NonNull Config config, @NonNull String path) {
        return config.hasPath(path) ? config.getValue(path) : null;
    }

    private static @NonNull List<Field> getFields(Class<?> clazz) {
        return Loader.getFields(new ArrayList<>(), clazz);
    }

    private static @NonNull List<Field> getFields(@NonNull List<Field> fields, @NonNull Class<?> clazz) {
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

        if (clazz.getSuperclass() != null) {
            Loader.getFields(fields, clazz.getSuperclass());
        }

        return fields;
    }
}
