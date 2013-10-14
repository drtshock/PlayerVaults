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
package com.drtshock.playervaults;

import com.drtshock.playervaults.listeners.Listeners;
import com.drtshock.playervaults.commands.Commands;
import com.drtshock.playervaults.commands.SignSetInfo;
import com.drtshock.playervaults.vaultmanagement.VaultViewInfo;
import com.drtshock.playervaults.util.Lang;
import com.drtshock.playervaults.util.Metrics;
import com.drtshock.playervaults.util.Updater;
import com.drtshock.playervaults.util.Updater.UpdateResult;
import com.drtshock.playervaults.util.Updater.UpdateType;
import com.drtshock.playervaults.vaultmanagement.VaultManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerVaults extends JavaPlugin {

    public static PlayerVaults PLUGIN;
    public static Logger LOG;
    public static boolean UPDATE = false;
    public static String NEWVERSION = "";
    public static String LINK = "";
    public static Commands commands;
    public static HashMap<String, SignSetInfo> SET_SIGN = new HashMap<String, SignSetInfo>();
    public static HashMap<String, VaultViewInfo> IN_VAULT = new HashMap<String, VaultViewInfo>();
    public static HashMap<String, Inventory> OPENINVENTORIES = new HashMap<String, Inventory>();
    public static Economy ECON = null;
    public static boolean DROP_ON_DEATH = false;
    public static int INVENTORIES_TO_DROP = 0;
    public static boolean USE_VAULT = false;
    public static YamlConfiguration LANG;
    public static File LANG_FILE;
    public static YamlConfiguration SIGNS;
    public static File SIGNS_FILE;
    public static String DIRECTORY = "plugins" + File.separator + "PlayerVaults" + File.separator + "vaults";
    public static VaultManager VM;
    public static Listeners listener;

    @Override
    public void onEnable() {
        loadLang();
        LOG = getServer().getLogger();
        getServer().getPluginManager().registerEvents(listener = new Listeners(this), this);
        loadConfig();
        loadSigns();
        checkUpdate();
        commands = new Commands();
        getCommand("pv").setExecutor(commands);
        getCommand("pvdel").setExecutor(commands);
        getCommand("pvsign").setExecutor(commands);
        getCommand("workbench").setExecutor(commands);
        setupEconomy();
        startMetrics();

        if (getConfig().getBoolean("drop-on-death.enabled")) {
            DROP_ON_DEATH = true;
            INVENTORIES_TO_DROP = getConfig().getInt("drop-on-death.inventories");
        }

        new File(DIRECTORY + File.separator + "backups").mkdirs();
        VM = new VaultManager(this);
    }

    private void startMetrics() {
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException ex) {
            getLogger().warning("Failed to load metrics :(");
        }
    }

    @Override
    public void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (IN_VAULT.containsKey(p.getName())) {
                Inventory inv = p.getOpenInventory().getTopInventory();
                if (inv.getViewers().size() == 1) {
                    VaultViewInfo info = PlayerVaults.IN_VAULT.get(p.getName());
                    try {
                        VM.saveVault(inv, info.getHolder(), info.getNumber());
                    } catch (IOException e) {
                    }
                    PlayerVaults.OPENINVENTORIES.remove(info.toString());
                }
                PlayerVaults.IN_VAULT.remove(p.getName());
            }
            p.closeInventory();
        }
    }

    public void checkUpdate() {
        if (getConfig().getBoolean("check-update")) {
            final PlayerVaults plugin = this;
            final File file = this.getFile();
            final Updater.UpdateType updateType = (getConfig().getBoolean("download-update", false) ? UpdateType.DEFAULT : UpdateType.NO_DOWNLOAD);
            getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
                @Override
                public void run() {
                    Updater updater = new Updater(plugin, 50123, file, updateType, false);
                    PlayerVaults.UPDATE = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
                    PlayerVaults.NEWVERSION = updater.getLatestName();
                    if (updater.getResult() == UpdateResult.SUCCESS) {
                        getLogger().log(Level.INFO, "Successfully updated Playervaults to version {0} for next restart!", updater.getLatestName());
                    } else if (updater.getResult() == UpdateResult.NO_UPDATE) {
                        getLogger().log(Level.INFO, "We didn't find an update!");
                    }
                }
            });
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        ECON = rsp.getProvider();
        USE_VAULT = true;
        return ECON != null;
    }

    private void loadConfig() {
        File config = new File(getDataFolder() + File.separator + "config.yml");
        if (!config.exists()) {
            saveDefaultConfig();
        } else {
            updateConfig();
        }
    }

    private void loadSigns() {
        File signs = new File(getDataFolder(), "signs.yml");
        if (!signs.exists()) {
            try {
                signs.createNewFile();
            } catch (IOException e) {
                LOG.severe("PlayerVaults has encountered a fatal error trying to load the signs file.");
                LOG.severe("Please report this error to drtshock and gomeow.");
                e.printStackTrace();
            }
        }
        PlayerVaults.SIGNS_FILE = signs;
        PlayerVaults.SIGNS = YamlConfiguration.loadConfiguration(signs);
    }

    /**
     * Get the signs.yml config.
     *
     * @return The signs.yml config.
     */
    public YamlConfiguration getSigns() {
        return PlayerVaults.SIGNS;
    }

    /**
     * Save the signs.yml file.
     */
    public void saveSigns() {
        try {
            PlayerVaults.SIGNS.save(PlayerVaults.SIGNS_FILE);
        } catch (IOException e) {
            LOG.severe("PlayerVaults has encountered an error trying to save the signs file.");
            LOG.severe("Please report this error to drtshock and gomeow.");
            e.printStackTrace();
        }
    }

    public void updateConfig() {
        boolean checkUpdate = getConfig().getBoolean("check-update", true);
        boolean ecoEnabled = getConfig().getBoolean("economy.enabled", false);
        int ecoCreate = getConfig().getInt("economy.cost-to-create", 100);
        int ecoOpen = getConfig().getInt("economy.cost-to-open", 10);
        int ecoDelete = getConfig().getInt("economy.refund-on-delete", 50);
        boolean dropEnabled = getConfig().getBoolean("drop-on-death.enabled", false);
        int dropInvs = getConfig().getInt("drop-on-death.inventories", 50);
        File configFile = new File(getDataFolder(), "config.yml");
        configFile.delete();
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(getResource("config.yml"));
        setInConfig("check-update", checkUpdate, conf);
        setInConfig("economy.enabled", ecoEnabled, conf);
        setInConfig("economy.cost-to-create", ecoCreate, conf);
        setInConfig("economy.cost-to-open", ecoOpen, conf);
        setInConfig("economy.refund-on-delete", ecoDelete, conf);
        setInConfig("drop-on-death.enabled", dropEnabled, conf);
        setInConfig("drop-on-death.inventories", dropInvs, conf);
        try {
            conf.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set an object in the config.yml
     *
     * @param path The path in the config.
     * @param object What to be saved.
     * @param conf Where to save the object.
     */
    public <T> void setInConfig(String path, T object, YamlConfiguration conf) {
        conf.set(path, object);
    }

    private void loadLang() {
        File lang = new File(getDataFolder(), "lang.yml");
        OutputStream out = null;
        InputStream defLangStream = this.getResource("lang.yml");
        if (!lang.exists()) {
            try {
                getDataFolder().mkdir();
                lang.createNewFile();
                if (defLangStream != null) {
                    out = new FileOutputStream(lang);
                    int read = 0;
                    byte[] bytes = new byte[1024];

                    while ((read = defLangStream.read(bytes)) != -1) {
                        out.write(bytes, 0, read);
                    }
                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defLangStream);
                    Lang.setFile(defConfig);
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace(); // So they notice
                LOG.severe("[PlayerVaults] Couldn't create language file.");
                LOG.severe("[PlayerVaults] This is a fatal error. Now disabling");
                this.setEnabled(false); // Without it loaded, we can't send them messages
            } finally {
                if (defLangStream != null) {
                    try {
                        defLangStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
        for (Lang item : Lang.values()) {
            if (conf.getString(item.getPath()) == null) {
                conf.set(item.getPath(), item.getDefault());
            }
        }
        Lang.setFile(conf);
        PlayerVaults.LANG = conf;
        PlayerVaults.LANG_FILE = lang;
        try {
            conf.save(getLangFile());
        } catch (IOException e) {
            LOG.log(Level.WARNING, "PlayerVaults: Failed to save lang.yml.");
            LOG.log(Level.WARNING, "PlayerVaults: Report this stack trace to drtshock and gomeow.");
            e.printStackTrace();
        }
    }

    /**
     * Gets the lang.yml config.
     *
     * @return The lang.yml config.
     */
    public YamlConfiguration getLang() {
        return LANG;
    }

    /**
     * Get the lang.yml file.
     *
     * @return The lang.yml file.
     */
    public File getLangFile() {
        return LANG_FILE;
    }
}
