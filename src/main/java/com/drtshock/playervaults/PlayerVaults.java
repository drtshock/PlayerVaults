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

import com.drtshock.playervaults.commands.*;
import com.drtshock.playervaults.listeners.Listeners;
import com.drtshock.playervaults.tasks.Cleanup;
import com.drtshock.playervaults.tasks.UUIDConversion;
import com.drtshock.playervaults.util.Lang;
import com.drtshock.playervaults.util.Metrics;
import com.drtshock.playervaults.util.Updater;
import com.drtshock.playervaults.vaultmanagement.UUIDVaultManager;
import com.drtshock.playervaults.vaultmanagement.VaultViewInfo;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.HashMap;
import java.util.logging.Level;

public class PlayerVaults extends JavaPlugin {

    private static PlayerVaults instance;
    private boolean update = false;
    private String newVersion = "";
    private String link = "";
    private HashMap<String, SignSetInfo> setSign = new HashMap<>();
    private HashMap<String, VaultViewInfo> inVault = new HashMap<>();
    private HashMap<String, Inventory> openInventories = new HashMap<>();
    private Economy economy = null;
    private boolean dropOnDeath = false;
    private boolean useVault = false;
    private int inventoriesToDrop = 0;
    private YamlConfiguration signs;
    private File signsFile;
    private boolean saveQueued;
    private String name = "";
    private File configFile;
    private File backupsFolder;
    private File vaultData;

    @Override
    public void onEnable() {
        instance = this;
        backupsFolder = new File(this.getVaultData(), "backups");
        backupsFolder.mkdirs();
        configFile = new File(getDataFolder(), "config.yml");
        vaultData = new File(this.getDataFolder(), "uuidvaults");
        getServer().getScheduler().runTask(this, new UUIDConversion()); // Convert to UUID first. Class checks if necessary.
        loadLang();
        new UUIDVaultManager();
        getServer().getPluginManager().registerEvents(new Listeners(this), this);
        loadConfig();
        loadSigns();
        checkUpdate();
        getCommand("pv").setExecutor(new VaultCommand());
        getCommand("pvdel").setExecutor(new DeleteCommand());
        getCommand("pvsign").setExecutor(new SignCommand());
        getCommand("workbench").setExecutor(new WorkbenchCommand());
        getCommand("pvconvert").setExecutor(new ConvertCommand());
        useVault = setupEconomy();
        startMetrics();

        if (getConfig().getBoolean("drop-on-death.enabled")) {
            dropOnDeath = true;
            inventoriesToDrop = getConfig().getInt("drop-on-death.inventories");
        }

        if (getConfig().getBoolean("cleanup.enable", false)) {
            getServer().getScheduler().runTaskAsynchronously(this, new Cleanup(getConfig().getInt("cleanup.lastEdit", 30)));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (saveQueued) {
                    saveSignsFile();
                }
            }
        }.runTaskTimer(this, 20, 20);
    }

    private void startMetrics() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Metrics metrics = new Metrics(PlayerVaults.this);
                    metrics.start();
                } catch (IOException ex) {
                    getLogger().warning("Failed to load metrics :(");
                }
            }
        }.runTaskAsynchronously(this);
    }

    @Override
    public void onDisable() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.inVault.containsKey(player.getName())) {
                Inventory inventory = player.getOpenInventory().getTopInventory();
                if (inventory.getViewers().size() == 1) {
                    VaultViewInfo info = this.inVault.get(player.getName());
                    try {
                        UUIDVaultManager.getInstance().saveVault(inventory, player.getUniqueId(), info.getNumber());
                    } catch (IOException e) {
                        // ignore
                    }

                    this.openInventories.remove(info.toString());
                }

                this.inVault.remove(player.getName());
            }

            player.closeInventory();
        }
        saveSignsFile();
    }

    protected void checkUpdate() {
        if (getConfig().getBoolean("check-update", true)) {
            final PlayerVaults plugin = this;
            final File file = this.getFile();
            final Updater.UpdateType updateType = getConfig().getBoolean("download-update", true) ? Updater.UpdateType.DEFAULT : Updater.UpdateType.NO_DOWNLOAD;
            final Updater updater = new Updater(plugin, 50123, file, updateType, false);
            getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
                @Override
                public void run() {
                    update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
                    name = updater.getLatestName();
                    if (updater.getResult() == Updater.UpdateResult.SUCCESS) {
                        getLogger().log(Level.INFO, "Successfully updated PlayerVaults to version {0} for next restart!", updater.getLatestName());
                    } else if (updater.getResult() == Updater.UpdateResult.NO_UPDATE) {
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

        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if (provider == null) {
            return false;
        }

        economy = provider.getProvider();

        return economy != null;
    }

    private void loadConfig() {
        if (!configFile.exists()) {
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
                getLogger().severe("PlayerVaults has encountered a fatal error trying to load the signs file.");
                getLogger().severe("Please report this error to drtshock.");
                e.printStackTrace();
            }
        }
        this.signsFile = signs;
        this.signs = YamlConfiguration.loadConfiguration(signs);
    }

    /**
     * Get the signs.yml config.
     *
     * @return The signs.yml config.
     */
    public YamlConfiguration getSigns() {
        return this.signs;
    }

    /**
     * Save the signs.yml file.
     */
    public void saveSigns() {
        saveQueued = true;
    }

    private void saveSignsFile() {
        saveQueued = false;
        try {
            signs.save(this.signsFile);
        } catch (IOException e) {
            getLogger().severe("PlayerVaults has encountered an error trying to save the signs file.");
            getLogger().severe("Please report this error to drtshock.");
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
                    int read;
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
                getLogger().severe("[PlayerVaults] Couldn't create language file.");
                getLogger().severe("[PlayerVaults] This is a fatal error. Now disabling");
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
        try {
            conf.save(lang);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "PlayerVaults: Failed to save lang.yml.");
            getLogger().log(Level.WARNING, "PlayerVaults: Report this stack trace to drtshock and gomeow.");
            e.printStackTrace();
        }
    }

    public HashMap<String, SignSetInfo> getSetSign() {
        return this.setSign;
    }

    public HashMap<String, VaultViewInfo> getInVault() {
        return this.inVault;
    }

    public HashMap<String, Inventory> getOpenInventories() {
        return this.openInventories;
    }

    public boolean needsUpdate() {
        return this.update;
    }

    public String getNewVersion() {
        return this.newVersion;
    }

    public String getLink() {
        return this.link;
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public boolean isEconomyEnabled() {
        return this.getConfig().getBoolean("economy.enabled", false) && this.useVault;
    }

    public File getVaultData() {
        return this.vaultData;
    }

    public File getBackupsFolder() {
        return this.backupsFolder;
    }

    public static PlayerVaults getInstance() {
        return instance;
    }
}
