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
import com.drtshock.playervaults.listeners.VaultPreloadListener;
import com.drtshock.playervaults.tasks.Cleanup;
import com.drtshock.playervaults.tasks.UUIDConversion;
import com.drtshock.playervaults.util.Lang;
import com.drtshock.playervaults.util.Updater;
import com.drtshock.playervaults.vaultmanagement.UUIDVaultManager;
import com.drtshock.playervaults.vaultmanagement.VaultViewInfo;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class PlayerVaults extends JavaPlugin {

    private static PlayerVaults instance;
    private boolean update = false;
    private String newVersion = "";
    private HashMap<String, SignSetInfo> setSign = new HashMap<>();
    // Player name - VaultViewInfo
    private HashMap<String, VaultViewInfo> inVault = new HashMap<>();
    // VaultViewInfo - Inventory
    private HashMap<String, Inventory> openInventories = new HashMap<>();
    private Economy economy = null;
    private boolean useVault = false;
    private YamlConfiguration signs;
    private File signsFile;
    private boolean saveQueued;
    private boolean backupsEnabled;
    private File backupsFolder = null;
    private File vaultData;
    private Set<Material> blockedMats = new HashSet<>();

    @Override
    public void onEnable() {
        instance = this;
        loadConfig();
        vaultData = new File(this.getDataFolder(), "uuidvaults");
        getServer().getScheduler().runTask(this, new UUIDConversion()); // Convert to UUIDs first. Class checks if necessary.
        loadLang();
        new UUIDVaultManager();
        getServer().getPluginManager().registerEvents(new Listeners(this), this);
        getServer().getPluginManager().registerEvents(new VaultPreloadListener(), this);
        this.backupsEnabled = this.getConfig().getBoolean("backups.enabled", true);
        loadSigns();
        checkUpdate();
        getCommand("pv").setExecutor(new VaultCommand());
        getCommand("pvdel").setExecutor(new DeleteCommand());
        getCommand("pvsign").setExecutor(new SignCommand());
        getCommand("workbench").setExecutor(new WorkbenchCommand());
        getCommand("pvconvert").setExecutor(new ConvertCommand());
        useVault = setupEconomy();

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

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.inVault.containsKey(player.getName())) {
                Inventory inventory = player.getOpenInventory().getTopInventory();
                if (inventory.getViewers().size() == 1) {
                    VaultViewInfo info = this.inVault.get(player.getName());
                    UUIDVaultManager.getInstance().saveVault(inventory, player.getUniqueId(), info.getNumber(), false);
                    this.openInventories.remove(info.toString());
                }

                this.inVault.remove(player.getName());
            }

            player.closeInventory();
        }
        saveSignsFile();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (cmd.getName().equals("pvreload")) {
            reloadConfig();
            loadConfig(); // To update blocked materials.
            loadLang();
            sender.sendMessage(ChatColor.GREEN + "Reloaded PlayerVaults' configuration and lang files.");
        }
        return true;
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
                    newVersion = updater.getLatestName();
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
        saveDefaultConfig();

        // Clear just in case this is a reload.
        blockedMats.clear();
        if (getConfig().getBoolean("blockitems", false) && getConfig().contains("blocked-items")) {
            for (String s : getConfig().getStringList("blocked-items")) {
                Material mat = Material.matchMaterial(s);
                if (mat != null) {
                    blockedMats.add(mat);
                    getLogger().log(Level.INFO, "Added {0} to list of blocked materials.", mat.name());
                }
            }
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

    /**
     * Set an object in the config.yml
     *
     * @param path   The path in the config.
     * @param object What to be saved.
     * @param conf   Where to save the object.
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

    public Economy getEconomy() {
        return this.economy;
    }

    public boolean isEconomyEnabled() {
        return this.getConfig().getBoolean("economy.enabled", false) && this.useVault;
    }

    public File getVaultData() {
        return this.vaultData;
    }

    public boolean isBackupsEnabled() {
        return this.backupsEnabled;
    }

    public File getBackupsFolder() {
        // having this in #onEnable() creates the 'uuidvaults' directory, preventing the conversion from running
        if (this.backupsFolder == null) {
            this.backupsFolder = new File(this.getVaultData(), "backups");
            this.backupsFolder.mkdirs();
        }

        return this.backupsFolder;
    }

    public boolean isBlockedMaterial(Material mat) {
        return blockedMats.contains(mat);
    }

    public static PlayerVaults getInstance() {
        return instance;
    }
}
