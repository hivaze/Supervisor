package me.litefine.supervisor.console;

import jline.console.ConsoleReader;
import me.litefine.supervisor.main.Supervisor;
import me.litefine.supervisor.network.NettyServer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.io.IoBuilder;

import java.io.IOException;

public class ConsoleManager {

    private static ConsoleReader reader;
    private static Thread consoleThread;

    public static void setup() throws IOException {
        reader = new ConsoleReader(System.in, System.out);
        reader.setExpandEvents(false);
        CommandsManager.registerCommands();
        consoleThread = new Thread(() -> {
            while (NettyServer.isBooted()) {
                try {
                    String command = reader.readLine();
                    CommandsManager.execureCommand(command);
                } catch (Exception e) {
                    Supervisor.getLogger().error("[CONSOLE] An error in console reader - " + e.getMessage());
                    break;
                }
            }
        }, "Console Thread");
        System.setOut(IoBuilder.forLogger(Supervisor.getLogger()).setLevel(Level.INFO).buildPrintStream());
    }

    public static ConsoleReader getReader() {
        return reader;
    }

    public static Thread getConsoleThread() {
        return consoleThread;
    }

}