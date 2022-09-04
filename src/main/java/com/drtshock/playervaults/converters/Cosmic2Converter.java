/*
 * PlayerVaultsX
 * Copyright (C) 2013 Trent Hensler, turt2live
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

package com.drtshock.playervaults.converters;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.vaultmanagement.VaultManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;

import java.nio.file.Files;
import java.nio.file.Path;

public class Cosmic2Converter implements Converter {

    @Override
    public int run(CommandSender initiator) {
        PlayerVaults plugin = PlayerVaults.getInstance();
        VaultManager vaults = VaultManager.getInstance();
        // Cosmic 2.x
        Path path = plugin.getDataFolder().toPath().getParent().resolve("CosmicVaults").resolve("Data.yml");
        if (!Files.exists(path)) {
            plugin.getLogger().warning("Could not find CosmicVaults folder and/or Data.yml!");
            return -1;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());

        ConfigurationSection players = config.getConfigurationSection("players");
        if (players == null || players.getKeys(false).isEmpty()) {
            plugin.getLogger().warning("Found 0 players to convert!");
            return 0;
        }

        int converted = 0;
        long lastUpdate = 0;
        for (String playerId : players.getKeys(false)) {
            ConfigurationSection player = players.getConfigurationSection(playerId);
            for (String vaultId : player.getKeys(false)) {
                ConfigurationSection vault = player.getConfigurationSection(vaultId).getConfigurationSection("contents");
                if (vault.getKeys(false).size() == 0) {
                    continue;
                }
                Inventory inventory = plugin.getServer().createInventory(null, 54);
                for (String slotS : vault.getKeys(false)) {
                    inventory.setItem(Integer.parseInt(slotS), vault.getItemStack(slotS));
                }
                vaults.saveVault(inventory, playerId, Integer.parseInt(vaultId));
                converted++;
                if (System.currentTimeMillis() - lastUpdate >= 1500) {
                    plugin.getLogger().info(converted + " vaults have been converted...");
                    lastUpdate = System.currentTimeMillis();
                }
            }
        }
        return converted;
    }

    @Override
    public boolean canConvert() {
        return Files.exists(PlayerVaults.getInstance().getDataFolder().toPath().getParent().resolve("CosmicVaults").resolve("Data.yml"));
    }

    @Override
    public String getName() {
        return "CosmicVaults2";
    }
}
