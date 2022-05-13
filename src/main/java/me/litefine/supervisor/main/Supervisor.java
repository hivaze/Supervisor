package me.litefine.supervisor.main;

import me.litefine.supervisor.console.ConsoleManager;
import me.litefine.supervisor.network.NettyServer;
import me.litefine.supervisor.utils.mojang.MojangAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Supervisor {

    static {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Supervisor.class.getResourceAsStream("/logo.txt")));
            while (reader.ready()) System.out.println(reader.readLine());
        } catch (IOException ignored) {}
        System.out.println("Loading libraries, please wait...");
    }

    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        logger.info("Startup initialization of the Supervisor's engine!");
        long time = System.currentTimeMillis();
        try {
            Settings.setupFiles();
            Settings.loadFromConfig();
            MojangAPI.loadProxyList();
            ConsoleManager.setup();
            NettyServer.start();
        } catch (Exception ex) {
            logger.error("An unexpected error occurred during startup - " + ex.getClass());
            System.exit(103);
        }
        logger.info("Supervisor started in " + (System.currentTimeMillis() - time) + " ms.");
        ConsoleManager.getConsoleThread().start();
    }

    public static void shutdown() {
        logger.info("Shutdown...");
        long time = System.currentTimeMillis();
        NettyServer.stop();
        logger.info("Supervisor finished in " + (System.currentTimeMillis() - time) + " ms. See you later.");
    }

    public static Logger getLogger() {
        return logger;
    }

}