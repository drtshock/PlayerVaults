package me.shock.playervaults;

import java.io.IOException;

import me.shock.playervaults.commands.Commands;
import me.shock.playervaults.util.VaultManager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

public class Listeners implements Listener {

	public Main plugin;
	public Listeners(Main instance) {
		this.plugin = instance;
	}
	VaultManager vm = new VaultManager(plugin);

	public void doSaveStuff(Player p) {
		if(Commands.inVault.containsKey(p.getName())) {
			Inventory inv = p.getOpenInventory().getTopInventory();
			int number = Commands.inVault.get(p.getName());
			try {
				vm.saveVault(inv, p, number);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		doSaveStuff(player);
	}
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		vm.playerVaultFile(player.getName());
		if(player.isOp() && Main.update)
		{
			player.sendMessage(ChatColor.GREEN + "Version " + Main.name + " of PlayerVaults is up for download!");
			player.sendMessage(ChatColor.GREEN + "http://dev.bukkit.org/server-mods/playervaults to view the changelog and download!");
		}
	}
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		doSaveStuff(player);
	}

	@EventHandler 
	public void onClose(InventoryCloseEvent event) {
		System.out.println(Commands.inVault.size());
		HumanEntity he = event.getPlayer();
		if(he instanceof Player) {
			Player player = (Player) he;
			doSaveStuff(player);
		}
	}

	/**
	 * Check if a player is trying to do something while
	 * in a vault.
	 * Don't let them open up another chest.
	 * @param event
	 */
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(Commands.inVault.containsKey(player.getName()) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();

			/**
			 * Different inventories that
			 * we don't want the player to open.
			 */
			if(block.getType() == Material.CHEST 
					|| block.getType() == Material.ENDER_CHEST
					|| block.getType() == Material.FURNACE
					|| block.getType() == Material.BURNING_FURNACE
					|| block.getType() == Material.BREWING_STAND
					|| block.getType() == Material.BEACON) {
				event.setCancelled(true);
			}
		}
	}

	/**
	 * Don't let a player open a trading inventory OR a minecart
	 * while he has his vault open.
	 * @param event
	 */
	@EventHandler
	public void onInteractEntity(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		EntityType type = event.getRightClicked().getType();
		if((type == EntityType.VILLAGER||type==EntityType.MINECART) && Commands.inVault.containsKey(player.getName())) {
			event.setCancelled(true);
		}
	}
}
