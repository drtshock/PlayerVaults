package com.drtshock.playervaults;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.drtshock.playervaults.commands.Commands;
import com.drtshock.playervaults.util.Lang;
import com.drtshock.playervaults.util.Metrics;
import com.drtshock.playervaults.util.Updater;
import com.drtshock.playervaults.util.VaultManager;

public class PlayerVaults extends JavaPlugin {

    public static PlayerVaults PLUGIN;
    public Logger log;
    public static boolean UPDATE = false;
    public static String NEWVERSION = "";
    public static String LINK = "";
    Commands commands;
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

    @Override
    public void onEnable() {
        loadLang();
        log = getServer().getLogger();
        getServer().getPluginManager().registerEvents(new Listeners(this), this);
        loadConfig();
        loadSigns();
        startMetrics();
        Updater u = new Updater();
        if(getConfig().getBoolean("check-update")) {
            try {
                if(u.getUpdate(getDescription().getVersion())) {
                    UPDATE = true;
                }
            } catch(IOException e) {
                log.log(Level.WARNING, "PlayerVaults: Failed to check for updates.");
                log.log(Level.WARNING, "PlayerVaults: Report this stack trace to drtshock and gomeow.");
                e.printStackTrace();
            }
        }

        commands = new Commands();
        getCommand("pv").setExecutor(commands);
        getCommand("pvdel").setExecutor(commands);
        getCommand("pvsign").setExecutor(commands);
        getCommand("workbench").setExecutor(commands);
        setupEconomy();

        if(getConfig().getBoolean("drop-on-death.enabled")) {
            DROP_ON_DEATH = true;
            INVENTORIES_TO_DROP = getConfig().getInt("drop-on-death.inventories");
        }

        new File(DIRECTORY + File.separator + "backups").mkdirs();
        VM = new VaultManager(this);
    }

    public void startMetrics() {
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private boolean setupEconomy() {
        if(getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null) {
            return false;
        }
        ECON = rsp.getProvider();
        USE_VAULT = true;
        return ECON != null;
    }

    public void loadConfig() {
        File config = new File(getDataFolder() + File.separator + "config.yml");
        if(!config.exists()) {
            saveDefaultConfig();
        } else {
            updateConfig();
        }
    }

    public void loadSigns() {
        File signs = new File(getDataFolder(), "signs.yml");
        if(!signs.exists()) {
            try {
                signs.createNewFile();
            } catch(IOException e) {
                log.severe("PlayerVaults has encountered a fatal error trying to load the signs file.");
                log.severe("Please report this error to drtshock and gomeow.");
                e.printStackTrace();
            }
        }
        PlayerVaults.SIGNS_FILE = signs;
        PlayerVaults.SIGNS = YamlConfiguration.loadConfiguration(signs);
    }

    public YamlConfiguration getSigns() {
        return PlayerVaults.SIGNS;
    }

    public void saveSigns() {
        try {
            PlayerVaults.SIGNS.save(PlayerVaults.SIGNS_FILE);
        } catch(IOException e) {
            log.severe("PlayerVaults has encountered an error trying to save the signs file.");
            log.severe("Please report this error to drtshock and gomeow.");
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
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public <T> void setInConfig(String path, T object, YamlConfiguration conf) {
        conf.set(path, object);
    }

    public YamlConfiguration loadLang() {
        File lang = new File(getDataFolder(), "lang.yml");
        if(!lang.exists()) {
            try {
                getDataFolder().mkdir();
                lang.createNewFile();
                InputStream defConfigStream = this.getResource("lang.yml");
                if(defConfigStream != null) {
                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                    defConfig.save(lang);
                    Lang.setFile(defConfig);
                    return defConfig;
                }
            } catch(IOException e) {
                e.printStackTrace(); // So they notice
                log.severe("[PlayerVaults] Couldn't create language file.");
                log.severe("[PlayerVaults] This is a fatal error. Now disabling");
                this.setEnabled(false); // Without it loaded, we can't send them messages
            }
        }
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
        Lang.setFile(conf);
        PlayerVaults.LANG = conf;
        PlayerVaults.LANG_FILE = lang;
        return conf;
    }

    public YamlConfiguration getLang() {
        return LANG;
    }

    public File getLangFile() {
        return LANG_FILE;
    }
}
