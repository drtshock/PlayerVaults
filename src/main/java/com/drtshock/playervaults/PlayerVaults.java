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
import com.drtshock.playervaults.listeners.SignListener;
import com.drtshock.playervaults.listeners.VaultPreloadListener;
import com.drtshock.playervaults.tasks.Base64Conversion;
import com.drtshock.playervaults.tasks.Cleanup;
import com.drtshock.playervaults.tasks.UUIDConversion;
import com.drtshock.playervaults.translations.Lang;
import com.drtshock.playervaults.translations.Language;
import com.drtshock.playervaults.vaultmanagement.UUIDVaultManager;
import com.drtshock.playervaults.vaultmanagement.VaultManager;
import com.drtshock.playervaults.vaultmanagement.VaultViewInfo;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerVaults extends JavaPlugin {
    public static boolean DEBUG = false;
    private static PlayerVaults instance;
    private final HashMap<String, SignSetInfo> setSign = new HashMap<>();
    // Player name - VaultViewInfo
    private final HashMap<String, VaultViewInfo> inVault = new HashMap<>();
    // VaultViewInfo - Inventory
    private final HashMap<String, Inventory> openInventories = new HashMap<>();
    private final Set<Material> blockedMats = new HashSet<>();
    private Economy economy = null;
    private boolean useVault = false;
    private YamlConfiguration signs;
    private File signsFile;
    private boolean saveQueued;
    private boolean backupsEnabled;
    private File backupsFolder = null;
    private File uuidData;
    private File vaultData;
    private String _versionString;

    public static PlayerVaults getInstance() {
        return instance;
    }

    public static void debug(String s, long start) {
        long elapsed = System.currentTimeMillis() - start;
        if (DEBUG || elapsed > 4) {
            Bukkit.getLogger().log(Level.INFO, "At {0}. Time since start: {1}ms", new Object[]{s, (elapsed)});
        }
    }

    public static void debug(String s) {
        if (DEBUG) {
            Bukkit.getLogger().log(Level.INFO, s);
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        loadConfig();
        DEBUG = getConfig().getBoolean("debug", false);
        debug("config", System.currentTimeMillis());
        uuidData = new File(this.getDataFolder(), "uuidvaults");
        vaultData = new File(this.getDataFolder(), "base64vaults");
        debug("vaultdata", System.currentTimeMillis());
        getServer().getScheduler().runTask(this, new UUIDConversion()); // Convert to UUIDs first. Class checks if necessary.
        debug("uuid conversion", System.currentTimeMillis());
        new VaultManager();
        getServer().getScheduler().runTask(this, new Base64Conversion());
        debug("base64 conversion", System.currentTimeMillis());
        loadLang();
        debug("lang", System.currentTimeMillis());
        new UUIDVaultManager();
        debug("uuidvaultmanager", System.currentTimeMillis());
        getServer().getPluginManager().registerEvents(new Listeners(this), this);
        getServer().getPluginManager().registerEvents(new VaultPreloadListener(), this);
        debug("registering listeners", System.currentTimeMillis());
        this.backupsEnabled = this.getConfig().getBoolean("backups.enabled", true);
        loadSigns();
        debug("loaded signs", System.currentTimeMillis());
        debug("check update", System.currentTimeMillis());
        getCommand("pv").setExecutor(new VaultCommand());
        getCommand("pvdel").setExecutor(new DeleteCommand());
        getCommand("pvconvert").setExecutor(new ConvertCommand());
        debug("registered commands", System.currentTimeMillis());
        useVault = setupEconomy();
        debug("setup economy", System.currentTimeMillis());

        if (getConfig().getBoolean("cleanup.enable", false)) {
            getServer().getScheduler().runTaskAsynchronously(this, new Cleanup(getConfig().getInt("cleanup.lastEdit", 30)));
            debug("cleanup task", System.currentTimeMillis());
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (saveQueued) {
                    saveSignsFile();
                }
            }
        }.runTaskTimer(this, 20, 20);

        debug("enable done", System.currentTimeMillis());
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.inVault.containsKey(player.getUniqueId().toString())) {
                Inventory inventory = player.getOpenInventory().getTopInventory();
                if (inventory.getViewers().size() == 1) {
                    VaultViewInfo info = this.inVault.get(player.getUniqueId().toString());
                    VaultManager.getInstance().saveVault(inventory, player.getUniqueId().toString(), info.getNumber());
                    this.openInventories.remove(info.toString());
                    // try this to make sure that they can't make further edits if the process hangs.
                    player.closeInventory();
                }

                this.inVault.remove(player.getUniqueId().toString());
                debug("Closing vault for " + player.getName());
                player.closeInventory();
            }
        }

        if (getConfig().getBoolean("cleanup.enable", false)) {
            saveSignsFile();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("pvreload")) {
            reloadConfig();
            loadConfig(); // To update blocked materials.
            loadLang();
            sender.sendMessage(ChatColor.GREEN + "Reloaded PlayerVault's configuration and lang files.");
        }
        return true;
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
        if (!getConfig().getBoolean("signs-enabled", true)) {
            return;
        }

        getCommand("pvsign").setExecutor(new SignCommand());
        getServer().getPluginManager().registerEvents(new SignListener(this), this);
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
        if (!getConfig().getBoolean("signs-enabled", true)) {
            return;
        }

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

    public void loadLang() {
        File folder = new File(getDataFolder(), "lang");
        if (!folder.exists()) {
            folder.mkdir();
        }

        String definedLanguage = getConfig().getString("language", "english");

        // Save as default just incase.
        File english = null;
        File definedFile = null;

        for (Language lang : Language.values()) {
            String fileName = lang.getFriendlyName() + ".yml";
            File file = new File(folder, fileName);
            if (lang == Language.ENGLISH) {
                english = file;
            }

            if (definedLanguage.equalsIgnoreCase(lang.getFriendlyName())) {
                definedFile = file;
            }

            // Have Bukkit save the file.
            if (!file.exists()) {
                saveResource("lang/" + fileName, false);
            }
        }

        if (definedFile != null && !definedFile.exists()) {
            getLogger().severe("Failed to load language for " + definedLanguage + ". Defaulting to English.");
            definedFile = english;
        }

        if (definedFile == null) {
            getLogger().severe("Failed to load custom language settings. Loading plugin defaults. This should never happen, go ask for help.");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(definedFile);
        Lang.setFile(config);
        getLogger().info("Loaded lang for " + definedLanguage);
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

    public Economy getEconomy() {
        return this.economy;
    }

    public boolean isEconomyEnabled() {
        return this.getConfig().getBoolean("economy.enabled", false) && this.useVault;
    }

    public File getVaultData() {
        return this.vaultData;
    }

    /**
     * Get the legacy UUID vault data folder.
     * Deprecated in favor of base64 data.
     *
     * @return
     */
    @Deprecated
    public File getUuidData() {
        return this.uuidData;
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

    /**
     * Tries to get a name from a given String that we hope is a UUID.
     *
     * @param potentialUUID - potential UUID to try to get the name for.
     * @return the player's name if we can find it, otherwise return what got passed to us.
     */
    public String getNameIfPlayer(String potentialUUID) {
        UUID uuid;
        try {
            uuid = UUID.fromString(potentialUUID);
        } catch (Exception e) {
            return potentialUUID;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        return offlinePlayer != null ? offlinePlayer.getName() : potentialUUID;
    }

    public boolean isBlockedMaterial(Material mat) {
        return blockedMats.contains(mat);
    }

    /**
     * Tries to grab the server version as a string.
     *
     * @return Version as raw string
     */
    public String getVersion() {
        if (_versionString == null) {
            if (Bukkit.getServer() == null) {
                return null;
            }
            final String name = Bukkit.getServer().getClass().getPackage().getName();
            _versionString = name.substring(name.lastIndexOf(46) + 1) + ".";
        }
        return _versionString;
    }
}
