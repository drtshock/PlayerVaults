package com.drtshock.playervaults.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Check dev.bukkit.org to find updates for a given plugin, and download the updates if needed.
 * <p/>
 * <b>VERY, VERY IMPORTANT</b>: Because there are no standards for adding auto-update toggles in your plugin's config,
 * this system provides NO CHECK WITH YOUR CONFIG to make sure the user has allowed auto-updating. <br> It is a
 * <b>BUKKIT POLICY</b> that you include a boolean value in your config that prevents the auto-updater from running
 * <b>AT ALL</b>. <br> If you fail to include this option in your config, your plugin will be <b>REJECTED</b> when you
 * attempt to submit it to dev.bukkit.org.
 * <p/>
 * An example of a good configuration option would be something similar to 'auto-update: true' - if this value is set
 * to
 * false you may NOT run the auto-updater. <br> If you are unsure about these rules, please read the plugin submission
 * guidelines: http://goo.gl/8iU5l
 *
 * @author Gravity
 */

public class Updater {

    private Plugin plugin;
    private Updater.UpdateType type;
    private String versionName;
    private String versionLink;
    private String versionType;
    private String versionGameVersion;

    private int sizeLine; // Used for detecting file size
    private int multiplier; // Used for determining when to broadcast download updates
    private boolean announce; // Whether to announce file downloads

    private URL url; // Connecting to RSS
    private File file; // The plugin's file
    private Thread thread; // Updater thread

    private int id = -1; // Project's Curse ID
    private String apiKey = null; // BukkitDev ServerMods API key
    private static final String TITLE_VALUE = "name"; // Gets remote file's title
    private static final String LINK_VALUE = "downloadUrl"; // Gets remote file's download link
    private static final String TYPE_VALUE = "releaseType"; // Gets remote file's release type
    private static final String VERSION_VALUE = "gameVersion"; // Gets remote file's build version
    private static final String QUERY = "/servermods/files?projectIds="; // Path to GET
    private static final String HOST = "https://api.curseforge.com"; // Slugs will be appended to this to get to the project's RSS feed

    private String[] noUpdateTag = {"-DEV", "-PRE", "-SNAPSHOT"}; // If the version number contains one of these, don't update.
    private static final int BYTE_SIZE = 1024; // Used for downloading files
    private YamlConfiguration config; // Config file
    private String updateFolder = YamlConfiguration.loadConfiguration(new File("bukkit.yml")).getString("settings.update-folder"); // The folder that downloads will be placed in
    private Updater.UpdateResult result = Updater.UpdateResult.SUCCESS; // Used for determining the outcome of the update process

    public Updater(Runnable aThis, int i, File file, UpdateType updateType, boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Gives the dev the result of the update process. Can be obtained by called getResult().
     */
    public enum UpdateResult {
        /**
         * The updater found an update, and has readied it to be loaded the next time the server restarts/reloads.
         */
        SUCCESS,
        /**
         * The updater did not find an update, and nothing was downloaded.
         */
        NO_UPDATE,
        /**
         * The server administrator has disabled the updating system
         */
        DISABLED,
        /**
         * The updater found an update, but was unable to download it.
         */
        FAIL_DOWNLOAD,
        /**
         * For some reason, the updater was unable to contact dev.bukkit.org to download the file.
         */
        FAIL_DBO,
        /**
         * When running the version check, the file on DBO did not contain the a version in the format 'vVersion' such
         * as 'v1.0'.
         */
        FAIL_NOVERSION,
        /**
         * The id provided by the plugin running the updater was invalid and doesn't exist on DBO.
         */
        FAIL_BADID,
        /**
         * The server administrator has improperly configured their API key in the configuration
         */
        FAIL_APIKEY,
        /**
         * The updater found an update, but because of the UpdateType being set to NO_DOWNLOAD, it wasn't downloaded.
         */
        UPDATE_AVAILABLE
    }

    /**
     * Allows the dev to specify the type of update that will be run.
     */
    public enum UpdateType {
        /**
         * Run a version check, and then if the file is out of date, download the newest version.
         */
        DEFAULT,
        /**
         * Don't run a version check, just find the latest update and download it.
         */
        NO_VERSION_CHECK,
        /**
         * Get information about the version and the download size, but don't actually download anything.
         */
        NO_DOWNLOAD
    }

    /**
     * Initialize the updater
     *
     * @param plugin   The plugin that is checking for an update.
     * @param id       The dev.bukkit.org id of the project
     * @param file     The file that the plugin is running from, get this by doing this.getFile() from within your main
     *                 class.
     * @param type     Specify the type of update this will be. See {@link UpdateType}
     * @param announce True if the program should announce the progress of new updates in console
     */
    public Updater(Plugin plugin, int id, File file, Updater.UpdateType type, boolean announce) {
        this.plugin = plugin;
        this.type = type;
        this.announce = announce;
        this.file = file;
        this.id = id;

        File pluginFile = plugin.getDataFolder().getParentFile();
        File updaterFile = new File(pluginFile, "Updater");
        File updaterConfigFile = new File(updaterFile, "config.yml");

        if (!updaterFile.exists()) {
            updaterFile.mkdir();
        }
        if (!updaterConfigFile.exists()) {
            try {
                updaterConfigFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "The updater could not create a configuration in {0}", updaterFile.getAbsolutePath());
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(updaterConfigFile);

        config.addDefault("api-key", "PUT_API_KEY_HERE");
        config.addDefault("disable", false);

        if (config.get("api-key", null) == null) {
            config.options().copyDefaults(true);
            try {
                config.save(updaterConfigFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "The updater could not save the configuration in {0}", updaterFile.getAbsolutePath());
                e.printStackTrace();
            }
        }

        if (config.getBoolean("disable")) {
            result = Updater.UpdateResult.DISABLED;
            return;
        }

        String key = config.getString("api-key");
        if (key.equalsIgnoreCase("PUT_API_KEY_HERE") || key.equals("")) {
            key = null;
        }

        apiKey = key;

        try {
            url = new URL(HOST + QUERY + id);
        } catch (MalformedURLException e) {
            plugin.getLogger().log(Level.SEVERE, "The project ID provided for updating, {0} is invalid.", id);
            result = Updater.UpdateResult.FAIL_BADID;
            e.printStackTrace();
        }

        thread = new Thread(new Updater.UpdateRunnable());
        thread.start();
    }

    /**
     * Get the result of the update process.
     */
    public Updater.UpdateResult getResult() {
        waitForThread();
        return result;
    }

    /**
     * Get the latest version's release type (release, beta, or alpha)
     */
    public String getLatestType() {
        waitForThread();
        return versionType;
    }

    /**
     * Get the latest version's game version
     */
    public String getLatestGameVersion() {
        waitForThread();
        return versionGameVersion;
    }

    /**
     * Get the latest version's name
     */
    public String getLatestName() {
        waitForThread();
        return versionName;
    }

    /**
     * As the result of Updater output depends on the thread's completion, it is necessary to wait for the thread to
     * finish before allowing anyone to check the result.
     */
    private void waitForThread() {
        if (thread != null && thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Save an update from dev.bukkit.org into the server's update folder.
     */
    private void saveFile(File folder, String file, String u) {
        if (!folder.exists()) {
            folder.mkdir();
        }
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            // Download the file
            URL url = new URL(u);
            int fileLength = url.openConnection().getContentLength();
            in = new BufferedInputStream(url.openStream());
            fout = new FileOutputStream(folder.getAbsolutePath() + "/" + file);

            byte[] data = new byte[BYTE_SIZE];
            int count;
            if (announce) {
                plugin.getLogger().log(Level.INFO, "About to download a new update: {0}", versionName);
            }
            long downloaded = 0;
            while ((count = in.read(data, 0, BYTE_SIZE)) != -1) {
                downloaded += count;
                fout.write(data, 0, count);
                int percent = (int) (downloaded * 100 / fileLength);
                if (announce & (percent % 10 == 0)) {
                    plugin.getLogger().log(Level.INFO, "Downloading update: {0}% of {1} bytes.", new Object[]{percent, fileLength});
                }
            }
            //Just a quick check to make sure we didn't leave any files from last time...
            for (File xFile : new File("plugins/" + updateFolder).listFiles()) {
                if (xFile.getName().endsWith(".zip")) {
                    xFile.delete();
                }
            }
            // Check to see if it's a zip file, if it is, unzip it.
            File dFile = new File(folder.getAbsolutePath() + "/" + file);
            if (dFile.getName().endsWith(".zip")) {
                // Unzip
                unzip(dFile.getCanonicalPath());
            }
            if (announce) {
                plugin.getLogger().info("Finished updating.");
            }
        } catch (Exception ex) {
            plugin.getLogger().warning("The auto-updater tried to download a new update, but was unsuccessful.");
            result = Updater.UpdateResult.FAIL_DOWNLOAD;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (fout != null) {
                    fout.close();
                }
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Part of Zip-File-Extractor, modified by H31IX for use with Bukkit
     */
    private void unzip(String file) {
        try {
            File fSourceZip = new File(file);
            String zipPath = file.substring(0, file.length() - 4);
            ZipFile zipFile = new ZipFile(fSourceZip);
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                File destinationFilePath = new File(zipPath, entry.getName());
                destinationFilePath.getParentFile().mkdirs();
                if (entry.isDirectory()) {
                    continue;
                } else {
                    BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                    int b;
                    byte buffer[] = new byte[BYTE_SIZE];
                    FileOutputStream fos = new FileOutputStream(destinationFilePath);
                    BufferedOutputStream bos = new BufferedOutputStream(fos, BYTE_SIZE);
                    while ((b = bis.read(buffer, 0, BYTE_SIZE)) != -1) {
                        bos.write(buffer, 0, b);
                    }
                    bos.flush();
                    bos.close();
                    bis.close();
                    String name = destinationFilePath.getName();
                    if (name.endsWith(".jar") && pluginFile(name)) {
                        destinationFilePath.renameTo(new File("plugins/" + updateFolder + "/" + name));
                    }
                }
                entry = null;
                destinationFilePath = null;
            }
            e = null;
            zipFile.close();
            zipFile = null;

            // Move any plugin data folders that were included to the right place, Bukkit won't do this for us.
            for (File dFile : new File(zipPath).listFiles()) {
                if (dFile.isDirectory()) {
                    if (pluginFile(dFile.getName())) {
                        File oFile = new File("plugins/" + dFile.getName()); // Get current dir
                        File[] contents = oFile.listFiles(); // List of existing files in the current dir
                        for (File cFile : dFile.listFiles()) // Loop through all the files in the new dir
                        {
                            boolean found = false;
                            for (File xFile : contents) // Loop through contents to see if it exists
                            {
                                if (xFile.getName().equals(cFile.getName())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                // Move the new file into the current dir
                                cFile.renameTo(new File(oFile.getCanonicalFile() + "/" + cFile.getName()));
                            } else {
                                // This file already exists, so we don't need it anymore.
                                cFile.delete();
                            }
                        }
                    }
                }
                dFile.delete();
            }
            new File(zipPath).delete();
            fSourceZip.delete();
        } catch (IOException ex) {
            plugin.getLogger().warning("The auto-updater tried to unzip a new update file, but was unsuccessful.");
            result = Updater.UpdateResult.FAIL_DOWNLOAD;
            ex.printStackTrace();
        }
        new File(file).delete();
    }

    /**
     * Check if the name of a jar is one of the plugins currently installed, used for extracting the correct files out
     * of a zip.
     */
    private boolean pluginFile(String name) {
        for (File file : new File("plugins").listFiles()) {
            if (file.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check to see if the program should continue by evaluation whether the plugin is already updated, or shouldn't be
     * updated
     */
    private boolean versionCheck(String title) {
        if (type != Updater.UpdateType.NO_VERSION_CHECK) {
            String version = plugin.getDescription().getVersion();
            if (title.split("v").length == 2) {
                String remoteVersion = title.split("v")[1].split(" ")[0]; // Get the newest file's version number
                int remVer, curVer = 0;
                try {
                    remVer = calVer(remoteVersion);
                    curVer = calVer(version);
                } catch (NumberFormatException nfe) {
                    remVer = -1;
                }
                if (hasTag(version) || version.equalsIgnoreCase(remoteVersion) || curVer >= remVer) {
                    // We already have the latest version, or this build is tagged for no-update
                    result = Updater.UpdateResult.NO_UPDATE;
                    return false;
                }
            } else {
                // The file's name did not contain the string 'vVersion'
                plugin.getLogger().log(Level.WARNING, "The author of this plugin ({0}) has misconfigured their Auto Update system", plugin.getDescription().getAuthors().get(0));
                plugin.getLogger().warning("Files uploaded to BukkitDev should contain the version number, seperated from the name by a 'v', such as PluginName v1.0");
                plugin.getLogger().warning("Please notify the author of this error.");
                result = Updater.UpdateResult.FAIL_NOVERSION;
                return false;
            }
        }
        return true;
    }

    /**
     * Used to calculate the version string as an Integer
     */
    private Integer calVer(String s) throws NumberFormatException {
        if (s.contains(".")) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                Character c = s.charAt(i);
                if (Character.isLetterOrDigit(c)) {
                    sb.append(c);
                }
            }
            return Integer.parseInt(sb.toString());
        }
        return Integer.parseInt(s);
    }

    /**
     * Evaluate whether the version number is marked showing that it should not be updated by this program
     */
    private boolean hasTag(String version) {
        for (String string : noUpdateTag) {
            if (version.contains(string)) {
                return true;
            }
        }
        return false;
    }

    private boolean read() {
        try {
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(5000);

            if (apiKey != null) {
                conn.addRequestProperty("X-API-Key", apiKey);
            }
            conn.addRequestProperty("User-Agent", "Updater (by Gravity)");

            conn.setDoOutput(true);

            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = reader.readLine();

            JSONArray array = (JSONArray) JSONValue.parse(response);

            if (array.size() == 0) {
                plugin.getLogger().log(Level.WARNING, "The updater could not find any files for the project id {0}", id);
                result = Updater.UpdateResult.FAIL_BADID;
                return false;
            }

            versionName = (String) ((JSONObject) array.get(array.size() - 1)).get(TITLE_VALUE);
            versionLink = (String) ((JSONObject) array.get(array.size() - 1)).get(LINK_VALUE);
            versionType = (String) ((JSONObject) array.get(array.size() - 1)).get(TYPE_VALUE);
            versionGameVersion = (String) ((JSONObject) array.get(array.size() - 1)).get(VERSION_VALUE);

            return true;
        } catch (IOException e) {
            if (e.getMessage().contains("HTTP response code: 403")) {
                plugin.getLogger().warning("dev.bukkit.org rejected the API key provided in plugins/Updater/config.yml");
                plugin.getLogger().warning("Please double-check your configuration to ensure it is correct.");
                result = Updater.UpdateResult.FAIL_APIKEY;
            } else {
                plugin.getLogger().warning("The updater could not contact dev.bukkit.org for updating.");
                plugin.getLogger().warning("If you have not recently modified your configuration and this is the first time you are seeing this message, the site may be experiencing temporary downtime.");
                result = Updater.UpdateResult.FAIL_DBO;
            }
            e.printStackTrace();
            return false;
        }
    }


    private class UpdateRunnable implements Runnable {

        @Override
        public void run() {
            if (url != null) {
                // Obtain the results of the project's file feed
                if (read()) {
                    if (versionCheck(versionName)) {
                        if (versionLink != null && type != Updater.UpdateType.NO_DOWNLOAD) {
                            String name = file.getName();
                            // If it's a zip file, it shouldn't be downloaded as the plugin's name
                            if (versionLink.endsWith(".zip")) {
                                String[] split = versionLink.split("/");
                                name = split[split.length - 1];
                            }
                            saveFile(new File("plugins/" + updateFolder), name, versionLink);
                        } else {
                            result = Updater.UpdateResult.UPDATE_AVAILABLE;
                        }
                    }
                }
            }
        }
    }
}