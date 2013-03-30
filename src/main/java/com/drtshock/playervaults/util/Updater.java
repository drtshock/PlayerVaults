package com.drtshock.playervaults.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.bukkit.configuration.file.YamlConfiguration;

import com.drtshock.playervaults.Main;

public class Updater extends Main {

	SortedMap<String, String> lang = new TreeMap<String, String>();

	public Updater() {
		lang.put("title-name", "&4[&fPlayerVaults&4]:");
		lang.put("open-vault", "&fOpening vault &a%v");
		lang.put("open-other-vault", "&fOpening vault &a%v &fof &a%p");
		lang.put("delete-other-vault", "&fDeleted vault &a%v &fof &a%p");
		lang.put("player-only", "Sorry but that can only be run by a player!");
		lang.put("must-be-number", "&cYou need to specify a number between 1-99");
		lang.put("invalid-args", "&cInvalid args!");
		lang.put("delete-vault-error", "&cError deleting vault :(");
		lang.put("no-permissions", "&cYou don't have permission for that!");
		lang.put("insufficient-funds", "&cYou don't have enough money for that");
		lang.put("refund-amount", "&fYou were refunded &a%price &ffor deleting that vault.");
		lang.put("cost-to-create", "&fYou were charged &c%price &ffor creating that vault.");
		lang.put("cost-to-open", "&fYou were charged &c%price &ffor opening that vault.");

		YamlConfiguration langConf = super.getLang();
		for(Entry<String, String> e:lang.entrySet()) {
			if(langConf.getString(e.getKey()) == null) {
				langConf.set(e.getKey(), e.getValue());
			}
		}
		try {
			langConf.save(super.getLangFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	String newVersion = "";

	public String getNewVersion() {
		return this.newVersion;
	}
	public boolean getUpdate() throws Exception {
		String version = getDescription().getVersion();
		URL url = new URL("http://dev.bukkit.org/server-mods/playervaults/files.rss");
		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(url.openStream());
		}
		catch(UnknownHostException e) {
			return false; //Cannot connect
		}
		BufferedReader in = new BufferedReader(isr);
		String line;
		int lineNum = 0;
		while((line = in.readLine()) != null) {
			if(line.length() != line.replace("<title>", "").length()) {
				line = line.replaceAll("<title>", "").replaceAll("</title>", "").replaceAll("	", "").substring(1); //Substring 1 for me, takes off the beginning v on my file name "v1.3.2"
				if(lineNum == 1) {
					this.newVersion = line;
					Integer newVer = Integer.parseInt(line.replace(".", ""));
					Integer oldVer = Integer.parseInt(version.replace(".", ""));
					if(oldVer < newVer) {
						return true; //They are using an old version
					}
					else if(oldVer > newVer) {
						return false; //They are using a FUTURE version!
					}
					else {
						return false; //They are up to date!
					}
				}
				lineNum = lineNum + 1;
			}
		}
		in.close();
		return false;
	}
}