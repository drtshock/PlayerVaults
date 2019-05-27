/*
 * PlayerVaultsX
 * Copyright (C) 2013 Trent Hensler
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

package com.drtshock.playervaults.translations;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * An enum for requesting strings from the language file.
 */
public enum Lang {
    TITLE("title-name", "&4[&fPlayerVaults&4]:"),
    OPEN_VAULT("open-vault", "&fOpening vault &a%v"),
    OPEN_OTHER_VAULT("open-other-vault", "&fOpening vault &a%v &fof &a%p"),
    INVALID_ARGS("invalid-args", "&cInvalid args!"),
    DELETE_VAULT("delete-vault", "&fDeleted vault &a%v"),
    DELETE_OTHER_VAULT("delete-other-vault", "&fDeleted vault &a%v &fof &a%p"),
    DELETE_OTHER_VAULT_ALL("delete-other-vault-all", "&4Deleted all vaults belonging to &a%p"),
    PLAYER_ONLY("player-only", "&cSorry but that can only be run by a player!"),
    MUST_BE_NUMBER("must-be-number", "&cYou need to specify a valid number."),
    NO_PERMS("no-permissions", "&cYou don''t have permission for that!"),
    INSUFFICIENT_FUNDS("insufficient-funds", "&cYou don''t have enough money for that!"),
    REFUND_AMOUNT("refund-amount", "&fYou were refunded &a%price &ffor deleting that vault."),
    COST_TO_CREATE("cost-to-create", "&fYou were charged &c%price &ffor creating a vault."),
    COST_TO_OPEN("cost-to-open", "&fYou were charged &c%price &ffor opening that vault."),
    VAULT_DOES_NOT_EXIST("vault-does-not-exist", "&cThat vault does not exist!"),
    CLICK_A_SIGN("click-a-sign", "&fNow click a sign!"),
    NOT_A_SIGN("not-a-sign", "&cYou must click a sign!"),
    SET_SIGN("set-sign-success", "&fYou have successfully set a PlayerVault access sign!"),
    EXISTING_VAULTS("existing-vaults", "&f%p has vaults: &a%v"),
    VAULT_TITLE("vault-title", "&4Vault #%number"),
    OPEN_WITH_SIGN("open-with-sign", "&fOpening vault &a%v &fof &a%p"),
    NO_OWNER_FOUND("no-owner-found", "&cCannot find vault owner: &a%p"),
    CONVERT_PLUGIN_NOT_FOUND("plugin-not-found", "&cNo converter found for that plugin"),
    CONVERT_COMPLETE("conversion-complete", "&aConverted %converted players to PlayerVaults"),
    CONVERT_BACKGROUND("conversion-background", "&fConversion has been forked to the background. See console for updates."),
    LOCKED("vaults-locked", "&cVaults are currently locked while conversion occurs. Please try again in a moment!"),
    HELP("help", "/pv <number>"),
    BLOCKED_ITEM("blocked-item", "&6%m &cis blocked from vaults"),
    SIGNS_DISABLED("signs-disabled", "&cVault signs are currently disabled.");

    private static YamlConfiguration LANG;
    private final String path;
    private final String def;

    /**
     * Lang enum constructor.
     *
     * @param path  The string path.
     * @param start The default string.
     */
    Lang(String path, String start) {
        this.path = path;
        this.def = start;
    }

    /**
     * Set the {@code YamlConfiguration} to use.
     *
     * @param config The config to set.
     */
    public static void setFile(YamlConfiguration config) {
        LANG = config;
    }

    @Override
    public String toString() {
        if (this == TITLE) {
            return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, def)) + " ";
        }
        return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, def));
    }

    /**
     * Get the default value of the path.
     *
     * @return The default value of the path.
     */
    public String getDefault() {
        return this.def;
    }

    /**
     * Get the path to the string.
     *
     * @return The path to the string.
     */
    public String getPath() {
        return this.path;
    }
}
