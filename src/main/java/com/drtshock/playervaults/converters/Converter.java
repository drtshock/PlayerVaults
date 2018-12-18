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

import org.bukkit.command.CommandSender;

/**
 * Represents a simple converter for converting another plugin's content to PlayerVaults.
 *
 * @author turt2live
 */
public interface Converter {

    /**
     * Converts the other plugin's data.
     *
     * @param initiator the initiator of the conversion. May be null
     * @return the number of vaults converted. Returns 0 on none converted or -1 if no vaults were converted.
     */
    int run(CommandSender initiator);

    /**
     * Determines if this converter is applicable for converting to PlayerVaults. This may check for the existance of a
     * plugin, plugin folder, or otherwise.
     *
     * @return true if this converter can convert, false otherwise
     */
    boolean canConvert();

    /**
     * Gets the name of this converter
     *
     * @return the converter name
     */
    String getName();

}
