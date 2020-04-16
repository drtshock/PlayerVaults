package com.drtshock.playervaults.v5;

import com.drtshock.playervaults.Perm;
import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.translations.Lang;
import com.drtshock.playervaults.v5.storage.Storage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public final class VaultJobs {
    private static final Map<VaultViewer, VaultInfo> inVault = new ConcurrentHashMap<>();
    private static final Map<VaultInfo, Inventory> openInventories = new ConcurrentHashMap<>();
    private static Storage storage;
    private static final Inventory NO_VAULT = Bukkit.createInventory(new VaultViewer(UUID.fromString("deaddead-dead-dead-dead-deaddeaddead")), 9);

    public static void setStorage(@NonNull Storage storage) {
        if (storage.getClass() != VaultJobs.storage.getClass()) {
            VaultJobs.storage = storage;
        }
    }

    public static @NonNull CompletableFuture<Integer> getMaxVaultSize(@NonNull VaultViewer owner) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        if (!owner.isProbablyPlayer()) {
            future.complete(PlayerVaults.getInstance().getDefaultVaultSize());
            return future;
        }
        OfflinePlayer offlinePlayer = owner.getOfflinePlayer();
        Player player = owner.getPlayer();
        if (player != null) {
            future.complete(getMaxVaultSize(player::hasPermission));
        } else {
            PlayerVaults.getInstance().newChain().async(() -> {
                future.complete(getMaxVaultSize(perm -> hasPermission(offlinePlayer, perm)));
            });
        }
        return future;
    }

    private static int getMaxVaultSize(@NonNull Predicate<String> permCheckPredicate) {
        for (int i = 6; i != 0; i--) {
            if (permCheckPredicate.test(Perm.SIZE_PREFIX + i)) {
                return i * 9;
            }
        }
        return PlayerVaults.getInstance().getDefaultVaultSize();
    }

    /**
     * Requests based on permissions of the viewer.
     *
     * @param viewer
     * @param vaultInfo
     * @return
     */
    public static @NonNull CompletableFuture<Inventory> requestOpenVault(@NonNull VaultViewer viewer, @NonNull VaultInfo vaultInfo) {
        if (!viewer.isProbablyPlayer()) {
            return openVault(viewer, vaultInfo); // Not a player, go right ahead!
        }
        CompletableFuture<Inventory> future = new CompletableFuture<>();
        Player player = viewer.getPlayer();
        if (player != null) {
            String message = tryRequestOpenVault(viewer, vaultInfo, player::hasPermission);
            if (message == null) {
                openVault(viewer, vaultInfo, future);
            } else {
                future.cancel(true);
                player.sendMessage(Lang.VAULT_TITLE + message);
            }
        } else {
            PlayerVaults.getInstance().newChain()
                    .asyncFirst(() -> tryRequestOpenVault(viewer, vaultInfo, perm -> hasPermission(viewer.getOfflinePlayer(), perm)))
                    .syncLast(message -> {
                        if (message == null) {
                            openVault(viewer, vaultInfo, future);
                        } else {
                            future.cancel(true);
                            // TODO message Lang.VAULT_TITLE + message
                        }
                    })
            ;
        }
        return future;
    }

    private static @Nullable String tryRequestOpenVault(@NonNull VaultViewer viewer, @NonNull VaultInfo vaultInfo, @NonNull Predicate<String> permCheckPredicate) {
        boolean self = viewer.equals(vaultInfo.getOwner());
        if (self && !hasVaultNumPerm(permCheckPredicate, vaultInfo.getNumber())) { // Don't have that many vaults
            return Lang.NO_PERMS.toString();
        }
        if (!self && !permCheckPredicate.test(Perm.ADMIN)) { // Not admin, can't view others
            return Lang.NO_PERMS.toString();
        }
        // TODO econ

        return null;
    }

    /**
     * Check whether or not the player has permission to open the requested vault.
     *
     * @param permCheckPredicate predicate to perm check
     * @param number vault number
     * @return true if they have permission
     */
    public static boolean hasVaultNumPerm(@NonNull Predicate<String> permCheckPredicate, int number) {
        if (permCheckPredicate.test(Perm.AMOUNT_PREFIX + number)) {
            return true;
        }
        for (int x = number; x <= PlayerVaults.getInstance().getMaxVaultAmountPermTest(); x++) {
            if (permCheckPredicate.test(Perm.AMOUNT_PREFIX + x)) {
                return true;
            }
        }
        return false;
    }

    public static @NonNull CompletableFuture<Inventory> openVault(@NonNull VaultViewer viewer, @NonNull VaultInfo vaultInfo) {
        return openVault(viewer, vaultInfo, new CompletableFuture<>());
    }

    public static @NonNull CompletableFuture<Inventory> openVault(@NonNull VaultViewer viewer, @NonNull VaultInfo vaultInfo, CompletableFuture<Inventory> future) {
        if (inVault.containsKey(viewer)) {
            future.cancel(true);
            return future;
        }
        Player player = viewer.getPlayer();
        if (openInventories.containsKey(vaultInfo)) {
            Inventory inventory = openInventories.get(vaultInfo);
            if (player != null) {
                if (!player.isOnline() || player.isDead() || player.isSleeping()) {
                    // Prevent sleeping on the job
                    future.cancel(true);
                    return future; // TODO
                }
                inVault.put(viewer, vaultInfo);
                player.openInventory(inventory);
            }
            future.complete(inventory);
            return future;
        }
        PlayerVaults.getInstance().newChain()
                .asyncFirst(() -> {
                    int size;
                    try {
                        size = getMaxVaultSize(vaultInfo.getOwner()).get();
                    } catch (InterruptedException | ExecutionException e) {
                        future.cancel(true);
                        return null; // TODO
                    }
                    Inventory wholeInventory = Bukkit.createInventory(vaultInfo.getOwner(), size);
                    byte[][] bytes = storage.getVault(vaultInfo, size / 9);
                    if (bytes.length == 0) {
                        // TODO debug
                    }
                    for (int i = 0; i < bytes.length && i < 6; i++) {
                        Inventory row = Serialization.fromBytes(bytes[i], vaultInfo);
                        if (row == null) { // oh no
                            continue;// TODO
                        }
                        for (int x = 0; x < 9; x++) {
                            wholeInventory.setItem((i * 9) + x, row.getItem(x));
                        }
                    }
                    if (player == null) {
                        future.complete(wholeInventory);
                    }
                    return wholeInventory;
                })
                .abortIf(i -> player == null || i == null)
                .syncLast(inventory -> {
                    Player newPlayer = viewer.getPlayer();
                    if (newPlayer == null || !newPlayer.isOnline() || newPlayer.isDead() || newPlayer.isSleeping()) {
                        // Prevent sleeping on the job
                        future.cancel(true);
                        return; // TODO
                    }
                    if (openInventories.containsKey(vaultInfo)) {
                        inventory = openInventories.get(vaultInfo);
                    }
                    newPlayer.openInventory(inventory);
                    inVault.put(viewer, vaultInfo);
                    openInventories.put(vaultInfo, inventory);
                })
        ;
        return future;
    }

    public static void closeVault(@NonNull VaultViewer vaultViewer, @NonNull VaultInfo vaultInfo) {

    }

    /**
     * Call me async!
     *
     * @param offlinePlayer
     * @param permission
     * @return
     */
    private static boolean hasPermission(@NonNull OfflinePlayer offlinePlayer, @NonNull String permission) {
        return PlayerVaults.getInstance().getVaultPermission().playerHas(null, offlinePlayer, permission);
    }
}
