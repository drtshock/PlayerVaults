package com.drtshock.playervaults.converters;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.vaultmanagement.UUIDVaultManager;
import com.turt2live.uuid.PlayerRecord;
import com.turt2live.uuid.ServiceProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Simple converter for Backpack (http://dev.bukkit.org/bukkit-plugins/backpack/)
 *
 * @author turt2live
 */
public class BackpackConverter implements Converter {

    @Override
    public int run(CommandSender initiator, ServiceProvider uuidProvider) {
        if(uuidProvider == null)throw new IllegalArgumentException();

        PlayerVaults plugin = PlayerVaults.getInstance();
        File destination = new File(plugin.getDataFolder().getParentFile(), "Backpack" + File.separator + "backpacks");
        if (!destination.exists()) return -1;

        int converted = 0;

        File[] worldDirs = destination.listFiles();
        int vaultNum = 1;
        for (File file : worldDirs != null ? worldDirs : new File[0]) {
            if (file.isDirectory()) {
                converted += convert(file, vaultNum, uuidProvider);
                vaultNum++;
            }
        }

        return converted;
    }

    private int convert(File worldFolder, int intoVaultNum, ServiceProvider uuidProvider) {
        PlayerVaults plugin = PlayerVaults.getInstance();
        UUIDVaultManager vaults = UUIDVaultManager.getInstance();
        int converted = 0;
        long lastUpdate = 0;
        File[] files = worldFolder.listFiles();
        for (File file : files != null ? files : new File[0]) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".yml")) {
                PlayerRecord player = uuidProvider.doLookup(file.getName().substring(0,file.getName().lastIndexOf('.')));
                if (player == null || player.getUuid() == null) {
                    plugin.getLogger().warning("Unable to convert Backpack for player: " + (player != null ? player.getName() : file.getName()));
                } else {
                    UUID uuid = player.getUuid();
                    FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                    ConfigurationSection section = yaml.getConfigurationSection("backpack");
                    if (section.getKeys(false).size() <= 0) continue; // No slots

                    Inventory vault = vaults.getVault(uuid, intoVaultNum);
                    if (vault == null) vault = plugin.getServer().createInventory(null, section.getKeys(false).size());
                    for (String key : section.getKeys(false)) {
                        ConfigurationSection slotSection = section.getConfigurationSection(key);
                        ItemStack item = slotSection.getItemStack("ItemStack");
                        if (item == null) continue;

                        // Overwrite
                        vault.setItem(Integer.parseInt(key.split(" ")[1]), item);
                    }
                    try {
                        vaults.saveVault(vault, uuid, intoVaultNum);
                        converted++;
                    } catch (IOException e) {
                        plugin.getLogger().severe("Error converting Backpack: " + file.getName());
                        e.printStackTrace();
                    }

                    if (System.currentTimeMillis() - lastUpdate >= 1500) {
                        plugin.getLogger().info(converted + " backpacks have been converted in " + worldFolder.getAbsolutePath());
                        lastUpdate = System.currentTimeMillis();
                    }
                }
            }
        }
        return converted;
    }

    @Override
    public boolean canConvert() {
        PlayerVaults plugin = PlayerVaults.getInstance();
        File expectedFolder = new File(plugin.getDataFolder().getParentFile(), "Backpack");
        if (!expectedFolder.exists()) return false;
        File backpackDir = new File(expectedFolder, "backpacks");
        return backpackDir.exists();
    }

    @Override
    public String getName() {
        return "Backpack";
    }
}
