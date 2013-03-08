package com.drtshock.playervaults.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public enum Lang {
	TITLE("title-name"),
	OPEN_VAULT("open-vault"),
	OPEN_OTHER_VAULT("open-other-vault"),
	INVALID_ARGS("invalid-args"),
	DELETE_VAULT("delete-vault"),
	DELETE_OTHER_VAULT("delete-other-vault"),
	PLAYER_ONLY("player-only"),
	MUST_BE_NUMBER("must-be-number"),
	DELETE_VAULT_ERROR("delete-vault-error");
	
	private String path = "";
	private static YamlConfiguration lang;
	
	Lang(String path) {
		this.path = path;
	}
	
	public static void setFile(YamlConfiguration yc) {
		lang = yc;
	}
	
	@Override
	public String toString() {
		return ChatColor.translateAlternateColorCodes('&', lang.getString(this.path));
	}
}
