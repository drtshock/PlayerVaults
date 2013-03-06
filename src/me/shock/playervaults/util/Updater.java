package me.shock.playervaults.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;

import me.shock.playervaults.Main;


public class Updater extends Main {
	
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