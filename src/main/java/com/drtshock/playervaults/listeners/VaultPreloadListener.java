package com.drtshock.playervaults.listeners;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.vaultmanagement.VaultManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class VaultPreloadListener implements Listener {

    final VaultManager vm = VaultManager.getInstance();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        new BukkitRunnable() {
            @Override
            public void run() {
                vm.cachePlayerVaultFile(uuid.toString());
            }
        }.runTaskAsynchronously(PlayerVaults.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        vm.removeCachedPlayerVaultFile(event.getPlayer().getUniqueId().toString());
    }
}
