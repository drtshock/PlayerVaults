package com.drtshock.playervaults.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.server.v1_5_R1.NBTBase;
import net.minecraft.server.v1_5_R1.NBTTagCompound;
import net.minecraft.server.v1_5_R1.NBTTagList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_5_R1.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_5_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.drtshock.playervaults.Main;

public class BackwardsCompatibility {

	Main plugin;

	public BackwardsCompatibility(Main m) {
		this.plugin = m;
	}

	public static Inventory pre2_0_0ToCurrent(String invString) {
		String[] serializedBlocks = invString.split(";");
		Inventory deserializedInventory = Bukkit.getServer().createInventory(null, 54);

		for (int i = 1; i < serializedBlocks.length; i++) {
			String[] serializedBlock = serializedBlocks[i].split("#");
			int stackPosition = Integer.valueOf(serializedBlock[0]).intValue();

			if (stackPosition < deserializedInventory.getSize()) {
				ItemStack is = null;
				Boolean createdItemStack = Boolean.valueOf(false);

				String[] serializedItemStack = serializedBlock[1].split(":");
				for (String itemInfo : serializedItemStack) {
					String[] itemAttribute = itemInfo.split("@");
					if (itemAttribute[0].equals("t")) {
						is = new ItemStack(Material.getMaterial(Integer.valueOf(itemAttribute[1]).intValue()));
						createdItemStack = Boolean.valueOf(true);
					} else if ((itemAttribute[0].equals("d")) && (createdItemStack.booleanValue())) {
						is.setDurability(Short.valueOf(itemAttribute[1]).shortValue());
					} else if ((itemAttribute[0].equals("a")) && (createdItemStack.booleanValue())) {
						is.setAmount(Integer.valueOf(itemAttribute[1]).intValue());
					} else if ((itemAttribute[0].equals("e")) && (createdItemStack.booleanValue())) {
						is.addEnchantment(Enchantment.getById(Integer.valueOf(itemAttribute[1]).intValue()), Integer.valueOf(itemAttribute[2]).intValue());
					} else {
						if ((!itemAttribute[0].equals("i")) || (!createdItemStack.booleanValue()))
							continue;
						ItemMeta meta = is.getItemMeta();
						meta.setDisplayName(itemAttribute[1]);
						is.setItemMeta(meta);
					}
				}
				deserializedInventory.setItem(stackPosition, is);
			}
		}
		return deserializedInventory;
	}

	public static Inventory pre3_0_0ToCurrent(String data) {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
		NBTTagList itemList = (NBTTagList) NBTBase.b(new DataInputStream(inputStream));
		Inventory inventory = new CraftInventoryCustom(null, itemList.size(), ChatColor.DARK_RED + "Vault");

		for (int i = 0; i < itemList.size(); i++) {
			NBTTagCompound inputObject = (NBTTagCompound) itemList.get(i);

			if (!inputObject.isEmpty()) {
				inventory.setItem(i, CraftItemStack.asCraftMirror(net.minecraft.server.v1_5_R1.ItemStack.createStack(inputObject)));
			}

		}

		return inventory;
	}

	private static CraftItemStack getCraftVersion(org.bukkit.inventory.ItemStack stack) {
		if ((stack instanceof CraftItemStack))
			return (CraftItemStack) stack;
		if (stack != null) {
			return CraftItemStack.asCraftCopy(stack);
		}
		return null;
	}

}
