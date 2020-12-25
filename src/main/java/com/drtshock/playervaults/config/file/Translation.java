package com.drtshock.playervaults.config.file;

import com.drtshock.playervaults.PlayerVaults;
import com.google.common.collect.ImmutableMap;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.craftbukkit.BukkitComponentSerializer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("FieldMayBeFinal")
public class Translation {
    public static class TL extends ArrayList<String> {
        private static transient PlayerVaults plugin;
        private static transient final Pattern REPLACEMENT_PATTERN = Pattern.compile("(?:\\{([a-zA-Z0-9\\-_]+)(?:(?::)([a-zA-Z0-9_\\-|]+))?})");
        private static transient final Pattern TAG_PATTERN = Pattern.compile("(?:<(/?)([a-zA-Z\\-_]+)>)");
        private static transient final String SKIP_LINE_IF_MISSING = "s";
        private static transient final String BLANK_IF_MISSING = "b";
        private static transient final String PRE_FORMATTED = "p";

        private static @NonNull TL of(@NonNull String... strings) {
            TL list = new TL();
            Collections.addAll(list, strings);
            return list;
        }

        public static @NonNull TL copyOf(@NonNull Collection<String> collection) {
            TL list = new TL();
            list.addAll(collection);
            return list;
        }

        public class Builder {
            private transient ImmutableMap.Builder<String, String> map;
            private transient TL title;

            private Builder(@NonNull TL title) {
                this.title = title;
            }

            private Builder(@NonNull String key, @Nullable String value) {
                this.with(key, value);
            }

            public @NonNull Builder with(@NonNull String key, @Nullable String value) {
                if (this.map == null) {
                    this.map = ImmutableMap.builder();
                }
                this.map.put(key, value);
                return this;
            }

            public void send(@NonNull CommandSender sender) {
                TL.this.send(sender, this.map == null ? Collections.emptyMap() : this.map.build(), this.title);
            }

            public @NonNull String getLegacy() {
                return TL.this.getLegacy(this.map == null ? Collections.emptyMap() : this.map.build(), this.title);
            }
        }

        public @NonNull Builder with(@NonNull String key, @Nullable String value) {
            return new Builder(key, value);
        }

        public @NonNull Builder title() {
            return new Builder(TL.plugin.getTL().title());
        }

        public @NonNull Builder title(@NonNull TL title) {
            return new Builder(title);
        }

        public void send(@NonNull CommandSender sender) {
            this.send(sender, Collections.emptyMap(), null);
        }

        private void send(@NonNull CommandSender sender, @NonNull Map<String, String> map, @Nullable TL title) {
            this.send(TL.plugin.getPlatform().sender(sender), map, title);
        }

        private void send(@NonNull Audience audience, @NonNull Map<String, String> map, @Nullable TL title) {
            this.forEach(line -> {
                Component component = this.getComponent(line, map, title);
                if (component != null) {
                    audience.sendMessage(component);
                }
            });
        }

        private @Nullable Component getComponent(@NonNull String line, @NonNull Map<String, String> map, @Nullable TL title) {
            if (title != null && !title.isEmpty()) {
                line = title.get(0) + line;
            }
            StringBuffer builder = null;
            String found;
            String foundDetails;
            String repl;
            String[] features;
            Matcher replMatcher = TL.REPLACEMENT_PATTERN.matcher(line);
            while (replMatcher.find()) {
                if (builder == null) {
                    builder = new StringBuffer();
                }
                found = replMatcher.group(1);
                foundDetails = replMatcher.group(2);
                features = foundDetails == null ? null : foundDetails.split("\\|");
                repl = map.get(found);
                if (repl == null) {
                    if (this.arrContains(features, TL.SKIP_LINE_IF_MISSING)) {
                        return null;
                    }
                    if (this.arrContains(features, TL.BLANK_IF_MISSING)) {
                        replMatcher.appendReplacement(builder, "");
                    }
                } else {
                    if (this.arrContains(features, TL.PRE_FORMATTED)) {
                        repl = MiniMessage.get().escapeTokens(repl).replace("\\", "\\\\");
                    }
                    replMatcher.appendReplacement(builder, repl);
                }
            }
            if (builder != null) {
                replMatcher.appendTail(builder);
                line = builder.toString();
                builder = null;
            }

            Matcher tagMatcher = TL.TAG_PATTERN.matcher(line);
            while (tagMatcher.find()) {
                if (builder == null) {
                    builder = new StringBuffer();
                }
                found = tagMatcher.group(2);
                repl = TL.plugin.getTL().colorMappings().get(found);
                if (repl != null) {
                    tagMatcher.appendReplacement(builder, '<' + tagMatcher.group(1) + repl + '>');
                }
            }
            if (builder != null) {
                tagMatcher.appendTail(builder);
                line = builder.toString();
            }
            return MiniMessage.get().parse(line);
        }

        public @NonNull String getLegacy() {
            return this.getLegacy(Collections.emptyMap(), null);
        }

        public @NonNull String getLegacy(@NonNull Map<String, String> map) {
            return this.getLegacy(map, null);
        }

        public @NonNull String getLegacy(@NonNull Map<String, String> map, @Nullable TL title) {
            return this.stream()
                    .map(line -> this.getComponent(line, map, title))
                    .filter(Objects::nonNull)
                    .map(component -> BukkitComponentSerializer.legacy().serialize(component))
                    .collect(Collectors.joining("\n"));
        }

        public boolean arrContains(@Nullable String[] array, @NonNull String target) {
            if (array == null) {
                return false;
            }
            for (String string : array) {
                if (target.equals(string)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class Placeholders {
        private TL title = TL.of("<dark_red>[<normal>PlayerVaults<dark_red>]: ");
    }

    private static class Translations {
        private TL openVault = TL.of("<normal>Opening vault <info>{vault}</info>");
        private TL openOtherVault = TL.of("<normal>Opening vault <info>{vault}</info> of <info>{player}</info>");
        private TL invalidArgs = TL.of("<error>Invalid args!");
        private TL deleteVault = TL.of("<normal>Deleted vault <info>{vault}</info>");
        private TL deleteOtherVault = TL.of("<normal>Deleted vault <info>{vault}</info> <normal>of <info>{player}</info>");
        private TL deleteOtherVaultAll = TL.of("<dark_red>Deleted all vaults belonging to <info>{player}</info>");
        private TL playerOnly = TL.of("<error>Sorry but that can only be run by a player!");
        private TL mustBeNumber = TL.of("<error>You need to specify a valid number.");
        private TL noPerms = TL.of("<error>You don't have permission for that!");
        private TL insufficientFunds = TL.of("<error>You don't have enough money for that!");
        private TL refundAmount = TL.of("<normal>You were refunded <info>{price}</info> for deleting that vault.");
        private TL costToCreate = TL.of("<normal>You were charged <info>{price}</info> for creating a vault.");
        private TL costToOpen = TL.of("<normal>You were charged <info>{price}</info> for opening that vault.");
        private TL vaultDoesNotExist = TL.of("<error>That vault does not exist!");
        private TL clickASign = TL.of("<normal>Now click a sign!");
        private TL notASign = TL.of("<error>You must click a sign!");
        private TL setSign = TL.of("<normal>You have successfully set a PlayerVault access sign!");
        private TL existingVaults = TL.of("<normal>{player} has vaults: <info>{vault}</info>");
        private TL vaultTitle = TL.of("<dark_red>Vault #{vault}");
        private TL openWithSign = TL.of("<normal>Opening vault <info>{vault}</info> of <info>{player}</info>");
        private TL noOwnerFound = TL.of("<error>Cannot find vault owner: <info>{player}</info>");
        private TL convertPluginNotFound = TL.of("<error>No converter found for that plugin.");
        private TL convertComplete = TL.of("<normal>Converted <info>{count}</info> players to PlayerVaults.");
        private TL convertBackground = TL.of("<normal>Conversion has been forked to the background. See console for updates.");
        private TL locked = TL.of("<error>Vaults are currently locked while conversion occurs. Please try again in a moment!");
        private TL help = TL.of("/pv <number>");
        private TL blockedItem = TL.of("<gold>{item}</gold> <error>is blocked from vaults.");
        private TL signsDisabled = TL.of("<error>Vault signs are currently disabled.");
        private TL blockedBadItem = TL.of("<error>This item is not allowed in a vault.");
    }

    private Placeholders placeholders = new Placeholders();
    private Translations translations = new Translations();

    public Translation(@NonNull PlayerVaults plugin) {
        TL.plugin = plugin;
    }

    private Map<String, String> colorMappings = new HashMap<String, String>() {
        {
            this.put("error", "red");
            this.put("normal", "white");
            this.put("info", "green");
        }
    };

    public @NonNull TL title() {
        return this.placeholders.title;
    }

    public @NonNull TL openVault() {
        return this.translations.openVault;
    }

    public @NonNull TL openOtherVault() {
        return this.translations.openOtherVault;
    }

    public @NonNull TL invalidArgs() {
        return this.translations.invalidArgs;
    }

    public @NonNull TL deleteVault() {
        return this.translations.deleteVault;
    }

    public @NonNull TL deleteOtherVault() {
        return this.translations.deleteOtherVault;
    }

    public @NonNull TL deleteOtherVaultAll() {
        return this.translations.deleteOtherVaultAll;
    }

    public @NonNull TL playerOnly() {
        return this.translations.playerOnly;
    }

    public @NonNull TL mustBeNumber() {
        return this.translations.mustBeNumber;
    }

    public @NonNull TL noPerms() {
        return this.translations.noPerms;
    }

    public @NonNull TL insufficientFunds() {
        return this.translations.insufficientFunds;
    }

    public @NonNull TL refundAmount() {
        return this.translations.refundAmount;
    }

    public @NonNull TL costToCreate() {
        return this.translations.costToCreate;
    }

    public @NonNull TL costToOpen() {
        return this.translations.costToOpen;
    }

    public @NonNull TL vaultDoesNotExist() {
        return this.translations.vaultDoesNotExist;
    }

    public @NonNull TL clickASign() {
        return this.translations.clickASign;
    }

    public @NonNull TL notASign() {
        return this.translations.notASign;
    }

    public @NonNull TL setSign() {
        return this.translations.setSign;
    }

    public @NonNull TL existingVaults() {
        return this.translations.existingVaults;
    }

    public @NonNull TL vaultTitle() {
        return this.translations.vaultTitle;
    }

    public @NonNull TL openWithSign() {
        return this.translations.openWithSign;
    }

    public @NonNull TL noOwnerFound() {
        return this.translations.noOwnerFound;
    }

    public @NonNull TL convertPluginNotFound() {
        return this.translations.convertPluginNotFound;
    }

    public @NonNull TL convertComplete() {
        return this.translations.convertComplete;
    }

    public @NonNull TL convertBackground() {
        return this.translations.convertBackground;
    }

    public @NonNull TL locked() {
        return this.translations.locked;
    }

    public @NonNull TL help() {
        return this.translations.help;
    }

    public @NonNull TL blockedItem() {
        return this.translations.blockedItem;
    }

    public @NonNull TL signsDisabled() {
        return this.translations.signsDisabled;
    }

    public @NonNull TL blockedBadItem() {
        return this.translations.blockedBadItem;
    }

    public @NonNull Map<String, String> colorMappings() {
        return Collections.unmodifiableMap(this.colorMappings);
    }
}
