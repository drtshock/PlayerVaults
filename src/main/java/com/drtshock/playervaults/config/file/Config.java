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
package com.drtshock.playervaults.config.file;

import com.drtshock.playervaults.config.annotation.Comment;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings({"FieldCanBeLocal", "InnerClassMayBeStatic", "unused"})
public class Config {
    public class Block {
        private boolean enabled = true;
        @Comment("Material list for blocked items (does not support ID's), only effective if the feature is enabled.\n" +
                " If you don't know material names: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html")
        private List<String> list = new ArrayList<String>() {
            {
                this.add("PUMPKIN");
                this.add("DIAMOND_BLOCK");
            }
        };

        public boolean isEnabled() {
            return this.enabled;
        }

        public List<String> getList() {
            if (this.list == null) {
                this.list = new ArrayList<>();
            }
            return Collections.unmodifiableList(list);
        }
    }

    public class Economy {
        @Comment("Set me to true to enable economy features!")
        private boolean enabled = false;
        private double feeToCreate = 100;
        private double feeToOpen = 10;
        private double refundOnDelete = 50;

        public boolean isEnabled() {
            return this.enabled;
        }

        public double getFeeToCreate() {
            return this.feeToCreate;
        }

        public double getFeeToOpen() {
            return this.feeToOpen;
        }

        public double getRefundOnDelete() {
            return this.refundOnDelete;
        }
    }

    public class PurgePlanet {
        private boolean enabled = false;
        @Comment("Time, in days, since last edit")
        private int daysSinceLastEdit = 30;

        public boolean isEnabled() {
            return this.enabled;
        }

        public int getDaysSinceLastEdit() {
            return this.daysSinceLastEdit;
        }
    }

    public class Storage {
        public class FlatFile {
            @Comment("Backups\n" +
                    " Enabling this will create backups of vaults automagically.")
            private boolean backups = true;

            public boolean isBackups() {
                return this.backups;
            }
        }

        private FlatFile flatFile = new FlatFile();
        private String storageType = "flatfile";

        public FlatFile getFlatFile() {
            return this.flatFile;
        }

        public String getStorageType() {
            return this.storageType;
        }
    }

    @Comment("PlayerVaults\n" +
            "Created by: https://github.com/drtshock/PlayerVaults/graphs/contributors/\n" +
            "Resource page: https://www.spigotmc.org/resources/51204/\n" +
            "Discord server: https://discordapp.com/invite/JZcWDEt/\n" +
            "Made with love <3")
    private boolean aPleasantHello=true;

    @Comment("Debug Mode\n" +
            " This will print everything the plugin is doing to console.\n" +
            " You should only enable this if you're working with a contributor to fix something.")
    private boolean debug = false;

    @Comment("Can be 1 through 6.\n" +
            "Default: 6")
    private int defaultVaultRows = 6;

    @Comment("Language\n" +
            " This determines which language file the plugin will read from.\n" +
            "  Valid options are (don't include .yml): bulgarian, danish, dutch, english, german, turkish, russian")
    private String language = "english";

    @Comment("Signs\n" +
            " This will determine whether vault signs are enabled.\n" +
            " If you don't know what this is or if it's for you, see the resource page.")
    private boolean signs = false;

    @Comment("Economy\n" +
            " These are all of the settings for the economy integration. (Requires Vault)\n" +
            "  Bypass permission is: playervaults.free")
    private Economy economy = new Economy();

    @Comment("Blocked Items\n" +
            " This will allow you to block specific materials from vaults.\n" +
            "  Bypass permission is: playervaults.bypassblockeditems")
    private Block itemBlocking = new Block();

    @Comment("Cleanup\n" +
            " Enabling this will purge vaults that haven't been touched in the specified time frame.\n" +
            "  Reminder: This is only checked during startup.\n" +
            "            This will not lag your server or touch the backups folder.")
    private PurgePlanet purge = new PurgePlanet();

    @Comment("Sets the highest vault amount this plugin will test perms for")
    private int maxVaultAmountPermTest = 99;

    @Comment("Storage option. Currently only flatfile, but soon more! :)")
    private Storage storage = new Storage();

    public void setFromConfig(Logger l, FileConfiguration c) {
        l.info("Importing old configuration...");
        l.info("debug = "+(this.debug = c.getBoolean("debug", false)));
        l.info("language = "+(this.language = c.getString("language", "english")));
        l.info("signs = "+(this.signs = c.getBoolean("signs-enabled", false)));
        l.info("economy enabled = "+(this.economy.enabled = c.getBoolean("economy.enabled", false)));
        l.info(" creation fee = "+(this.economy.feeToCreate = c.getDouble("economy.cost-to-create", 100)));
        l.info(" open fee = "+(this.economy.feeToOpen = c.getDouble("economy.cost-to-open", 10)));
        l.info(" refund = "+(this.economy.refundOnDelete = c.getDouble("economy.refund-on-delete", 50)));
        l.info("item blocking enabled = "+(this.itemBlocking.enabled = c.getBoolean("blockitems", true)));
        l.info("blocked items = "+(this.itemBlocking.list = c.getStringList("blocked-items")));
        if (this.itemBlocking.list == null) {
            this.itemBlocking.list = new ArrayList<>();
            this.itemBlocking.list.add("PUMPKIN");
            this.itemBlocking.list.add("DIAMOND_BLOCK");
            l.info(" set defaults: "+this.itemBlocking.list);
        }
        l.info("cleanup purge enabled = "+(this.purge.enabled = c.getBoolean("cleanup.enable", false)));
        l.info(" days since last edit = "+(this.purge.daysSinceLastEdit = c.getInt("cleanup.lastEdit", 30)));
        l.info("flatfile storage backups = "+(this.storage.flatFile.backups = c.getBoolean("backups.enabled", true)));
        l.info("max vault amount to test via perms = "+(this.maxVaultAmountPermTest = c.getInt("max-vault-amount-perm-to-test", 99)));
    }

    public boolean isDebug() {
        return this.debug;
    }

    public int getDefaultVaultRows() {
        return this.defaultVaultRows;
    }

    public String getLanguage() {
        return this.language;
    }

    public boolean isSigns() {
        return this.signs;
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public Block getItemBlocking() {
        return this.itemBlocking;
    }

    public PurgePlanet getPurge() {
        return this.purge;
    }

    public int getMaxVaultAmountPermTest() {
        return this.maxVaultAmountPermTest;
    }

    public Storage getStorage() {
        return this.storage;
    }
}