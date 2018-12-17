package com.drtshock.playervaults.listeners;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.translations.Lang;
import com.drtshock.playervaults.vaultmanagement.VaultManager;
import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import com.drtshock.playervaults.vaultmanagement.VaultViewInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class SignListener implements Listener {

    private PlayerVaults plugin;

    public SignListener(PlayerVaults plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (PlayerVaults.getInstance().getInVault().containsKey(player.getUniqueId().toString())) {
                Block block = event.getClickedBlock();
                // Different inventories that we don't want the player to open.
                if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST || block.getType() == Material.ENDER_CHEST || block.getType() == Material.FURNACE || block.getType() == Material.BREWING_STAND || block.getType() == Material.ENCHANTING_TABLE || block.getType() == Material.BEACON) {
                    event.setCancelled(true);
                }
            }
        }
        if (PlayerVaults.getInstance().getSetSign().containsKey(player.getName())) {
            int i = PlayerVaults.getInstance().getSetSign().get(player.getName()).getChest();
            boolean self = PlayerVaults.getInstance().getSetSign().get(player.getName()).isSelf();
            String owner = self ? null : PlayerVaults.getInstance().getSetSign().get(player.getName()).getOwner();
            PlayerVaults.getInstance().getSetSign().remove(player.getName());
            event.setCancelled(true);
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (event.getClickedBlock().getType() == Material.WALL_SIGN || event.getClickedBlock().getType() == Material.SIGN) {
                    Sign s = (Sign) event.getClickedBlock().getState();
                    Location l = s.getLocation();
                    String world = l.getWorld().getName();
                    int x = l.getBlockX();
                    int y = l.getBlockY();
                    int z = l.getBlockZ();
                    if (self) {
                        plugin.getSigns().set(world + ";;" + x + ";;" + y + ";;" + z + ".self", true);
                    } else {
                        plugin.getSigns().set(world + ";;" + x + ";;" + y + ";;" + z + ".owner", owner);
                    }
                    plugin.getSigns().set(world + ";;" + x + ";;" + y + ";;" + z + ".chest", i);
                    plugin.saveSigns();
                    player.sendMessage(Lang.TITLE.toString() + Lang.SET_SIGN);
                } else {
                    player.sendMessage(Lang.TITLE.toString() + Lang.NOT_A_SIGN);
                }
            } else {
                player.sendMessage(Lang.TITLE.toString() + Lang.NOT_A_SIGN);
            }
            return;
        }
        Block b = event.getClickedBlock();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN) {
                Location l = b.getLocation();
                String world = l.getWorld().getName();
                int x = l.getBlockX();
                int y = l.getBlockY();
                int z = l.getBlockZ();
                if (plugin.getSigns().getKeys(false).contains(world + ";;" + x + ";;" + y + ";;" + z)) {
                    int num = PlayerVaults.getInstance().getSigns().getInt(world + ";;" + x + ";;" + y + ";;" + z + ".chest", 1);
                    if (player.hasPermission("playervaults.signs.use") || player.hasPermission("playervaults.signs.bypass")) {
                        boolean self = PlayerVaults.getInstance().getSigns().getBoolean(world + ";;" + x + ";;" + y + ";;" + z + ".self", false);
                        String owner = self ? player.getName() : PlayerVaults.getInstance().getSigns().getString(world + ";;" + x + ";;" + y + ";;" + z + ".owner");
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner != null ? owner : event.getPlayer().getName()); // Not best way but :\
                        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
                            player.sendMessage(Lang.TITLE.toString() + Lang.VAULT_DOES_NOT_EXIST.toString());
                            return;
                        }
                        if (self) {
                            // We already checked that they can use signs, now lets check if they have this many vaults.
                            if (VaultOperations.checkPerms(player, num)) {
                                Inventory inv = VaultManager.getInstance().loadOwnVault(player, num, VaultOperations.getMaxVaultSize(player));
                                if (inv != null) {
                                    player.openInventory(inv);
                                }
                            } else {
                                player.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS.toString());
                                return; // Otherwise it would try to add vault view info down there.
                            }
                        } else {
                            Inventory inv = VaultManager.getInstance().loadOtherVault(offlinePlayer.getUniqueId().toString(), num, VaultOperations.getMaxVaultSize(offlinePlayer));
                            if (inv == null) {
                                player.sendMessage(Lang.TITLE.toString() + Lang.VAULT_DOES_NOT_EXIST.toString());
                            } else {
                                player.openInventory(inv);
                            }
                        }
                        PlayerVaults.getInstance().getInVault().put(player.getUniqueId().toString(), new VaultViewInfo(self ? player.getUniqueId().toString() : offlinePlayer.getUniqueId().toString(), num));
                        event.setCancelled(true);
                        player.sendMessage(Lang.TITLE.toString() + Lang.OPEN_WITH_SIGN.toString().replace("%v", String.valueOf(num)).replace("%p", owner));
                    } else {
                        player.sendMessage(Lang.TITLE.toString() + Lang.NO_PERMS);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        blockChangeCheck(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        blockChangeCheck(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        blockChangeCheck(event.getBlock().getLocation());
    }

    /**
     * Check if the location given is a sign, and if so, remove it from the signs.yml file
     *
     * @param location The location to check
     */
    public void blockChangeCheck(Location location) {
        if (plugin.getSigns().getKeys(false).isEmpty()) {
            return; // Save us a check.
        }

        String world = location.getWorld().getName();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        if (plugin.getSigns().getKeys(false).contains(world + ";;" + x + ";;" + y + ";;" + z)) {
            plugin.getSigns().set(world + ";;" + x + ";;" + y + ";;" + z, null);
            plugin.saveSigns();
        }
    }
}
