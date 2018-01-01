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
     * @param initiator    the initiator of the conversion. May be null
     *
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
