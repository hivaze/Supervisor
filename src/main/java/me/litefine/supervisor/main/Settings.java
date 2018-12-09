package me.litefine.supervisor.main;

import me.litefine.supervisor.utils.YamlUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public class Settings {

    private static File mainFolder, configFile, runnedJar, dataFolder, outFolder;

    private static InetSocketAddress supervisorHost;
    private static int nettyThreadsCount;

    private static boolean securityEnable;
    private static List<String> allowedIPs;

    static {
        runnedJar = new File(Supervisor.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        mainFolder = new File(runnedJar.getAbsolutePath().replace(runnedJar.getName(), "/Supervisor/"));
        outFolder = new File(mainFolder, "out");
        configFile = new File(mainFolder, "config.yml");
        dataFolder = new File(mainFolder, "data");
    }

    public static void loadFromConfig() throws Exception {
        Map mapRepresentation = YamlUtils.loadFrom(configFile);
        if (YamlUtils.getBoolean("Settings.enableDebug", mapRepresentation)) {
            Configurator.setLevel(Supervisor.class.getCanonicalName(), Level.DEBUG);
            Supervisor.getLogger().debug("Logger debug mode enabled!");
        }
        nettyThreadsCount = YamlUtils.getInteger("Settings.nettyThreadsCount", mapRepresentation);
        String[] host = YamlUtils.getString("Settings.supervisorHost", mapRepresentation).split("[:]");
        supervisorHost = new InetSocketAddress(host[0], Integer.parseInt(host[1]));
        securityEnable = YamlUtils.getBoolean("Security.enable", mapRepresentation);
        allowedIPs = YamlUtils.getList("Security.allowedIPs", mapRepresentation);
    }

    public static void setupFiles() throws IOException {
        mainFolder.mkdir(); dataFolder.mkdir(); outFolder.mkdir();
        if (!configFile.exists()) {
            configFile.createNewFile();
            copyResourceFile(configFile);
            Supervisor.getLogger().debug("Config file created: " + configFile.getAbsolutePath());
        }
    }

    private static void copyResourceFile(File into) throws IOException {
        InputStreamReader localConfig = new InputStreamReader(Supervisor.class.getResourceAsStream("/" + into.getName()));
        FileWriter fw = new FileWriter(into);
        while (localConfig.ready()) fw.write(localConfig.read());
        localConfig.close(); fw.close();
    }

    public static int getNettyThreadsCount() {
        return nettyThreadsCount;
    }

    public static InetSocketAddress getSupervisorHost() {
        return supervisorHost;
    }

    public static boolean isSecurityEnable() {
        return securityEnable;
    }

    public static List getAllowedIPs() {
        return allowedIPs;
    }

    public static File getMainFolder() {
        return mainFolder;
    }

    public static File getDataFolder() {
        return dataFolder;
    }

    public static File getOutFolder() {
        return outFolder;
    }

    public static File getConfigFile() {
        return configFile;
    }

    public static File getRunnedJar() {
        return runnedJar;
    }

}