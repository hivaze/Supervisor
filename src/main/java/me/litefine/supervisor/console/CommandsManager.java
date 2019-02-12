package me.litefine.supervisor.console;

import io.netty.channel.epoll.Epoll;
import me.litefine.supervisor.main.Settings;
import me.litefine.supervisor.main.Supervisor;
import me.litefine.supervisor.network.NettyServer;
import me.litefine.supervisor.network.connection.ClientConnection;
import me.litefine.supervisor.network.connection.metadata.ConnectionMetadata;
import me.litefine.supervisor.network.connection.metadata.representers.BungeeServerRepresenter;
import me.litefine.supervisor.network.connection.metadata.representers.MinecraftServerRepresenter;
import me.litefine.supervisor.network.connection.metadata.representers.ServerRepresenter;
import me.litefine.supervisor.utils.StringUtils;
import me.litefine.supervisor.utils.YamlUtils;
import me.litefine.supervisor.utils.mojang.MojangAPI;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;

public class CommandsManager {

    private static final HashMap<String, CommandConsumer> commands = new HashMap<>();

    public static void execureCommand(String command) {
        if (!command.trim().isEmpty()) {
            command = StringUtils.removeExtraSpaces(command);
            String[] args = StringUtils.getArguments(command);
            command = command.split("\\s")[0].toLowerCase();
            if (commands.containsKey(command)) {
                long time = System.currentTimeMillis();
                commands.get(command).execute(args);
                Supervisor.getLogger().debug("[EXECUTION] Command '" + command + "' executed in " + (System.currentTimeMillis() - time) + " ms");
            }
            else Supervisor.getLogger().warn("Неизвестная команда.");
        } else Supervisor.getLogger().warn("Ввведена пустая команда!");
    }

    public static void registerCommands() {
        commands.put("info", args -> {
            System.out.println();
            System.out.println("Supervisor information:");
            System.out.println();
            System.out.println(" Всего соединений: " + NettyServer.getConnections().size());
            System.out.println(" Идентифицированные: " + NettyServer.getIdentifiedConnections().count());
            System.out.println(" API соединения: " + NettyServer.getConnections(ConnectionMetadata::isAPIHandler).count());
            System.out.println();
            System.out.println(" Все сервера: " + NettyServer.getServersAsStream().count());
            System.out.println(" BungeeCord сервера: " + NettyServer.getBungeeServers().count());
            System.out.println(" Minecraft сервера: " + NettyServer.getMinecraftServers().count());
            System.out.println();
            System.out.println(" Онлайн BungeeCord серверов: " + BungeeServerRepresenter.getTotalOnline() + "/" + BungeeServerRepresenter.getTotalMaxCount());
            System.out.println(" Онлайн Minecraft серверов: " + MinecraftServerRepresenter.getTotalOnline() + "/" + MinecraftServerRepresenter.getTotalMaxCount());
            System.out.println(" Объем кэша профилей: " + MojangAPI.getCashedProfiles().size());
            System.out.println();
            System.out.println(" Epoll mode: " + Epoll.isAvailable());
            System.out.println(" Uptime: " + StringUtils.millisToPattern(ManagementFactory.getRuntimeMXBean().getUptime()));
            System.out.println(" RAM usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576 + " MB");
            System.out.println();
        });
        commands.put("connections", args -> {
            if (NettyServer.getIdentifiedConnections().count() == 0) System.out.println("Текущих соединений нету!");
            else {
                Stream<ClientConnection> targetConnections;
                if (args.length == 0) {
                    System.out.println("Current identified connections:");
                    targetConnections = NettyServer.getIdentifiedConnections();
                } else if (args[0].equalsIgnoreCase("bungeecord")) {
                    System.out.println("Current bungeecords connections:");
                    targetConnections = NettyServer.getConnections(ConnectionMetadata::isBungeeServer);
                } else if (args[0].equalsIgnoreCase("minecraft")) {
                    System.out.println("Current minecraft servers connections:");
                    targetConnections = NettyServer.getConnections(ConnectionMetadata::isMinecraftServer);
                } else if (args[0].equalsIgnoreCase("api")) {
                    System.out.println("Current API connections:");
                    targetConnections = NettyServer.getConnections(ConnectionMetadata::isAPIHandler);
                } else {
                    System.out.println("Usage: connections <bungeecord/minecraft/api>");
                    return;
                }
                targetConnections.forEach(connection -> {
                    System.out.println("-------------------------------");
                    System.out.println("IP: " + connection.getMetadata().getFormattedAddress());
                    System.out.println(" Type: " + connection.getMetadata().getConnectionType());
                    System.out.println(" Connection time: " + StringUtils.millisToPattern(connection.getMetadata().millisSinceConnect()));
                    System.out.println(" Identificator: " + connection.getMetadata().getIdentificator());
                    if (connection.getMetadata().supportsServerRepresenter()) {
                        ServerRepresenter serverRepresenter = connection.getMetadata().getServerRepresenter();
                        System.out.println(" Server stats:");
                        System.out.println("  Online: " + serverRepresenter.getOnline() + "/" + serverRepresenter.getMaxPlayers());
                        System.out.println("  Motd: " + serverRepresenter.getMotd());
                        if (connection.getMetadata().isMinecraftServer())
                            System.out.println("  TPS: " + ((MinecraftServerRepresenter) serverRepresenter).getTPS());
                    }
                });
            }
        });
        commands.put("genbungeeservers", args -> {
            if (NettyServer.getMinecraftServers().count() > 0) {
                File newFile = new File(Settings.getOutFolder(), System.currentTimeMillis() + ".yml");
                try {
                    HashMap<String, Object> servers = new HashMap<>();
                    NettyServer.getMinecraftServers().forEach(serverInfo -> servers.putAll(serverInfo.getYamlMap()));
                    HashMap<String, Object> finalMap = new HashMap<>();
                    finalMap.put("servers", servers);
                    YamlUtils.dump(finalMap, newFile);
                    Supervisor.getLogger().info("Bungee servers file generated - " + newFile);
                } catch (IOException e) {
                    Supervisor.getLogger().info("Can't generate new bungee servers file " + newFile.getName() + " - " + e);
                    if (newFile.exists()) newFile.delete();
                }
            } else System.out.println("Нету соединений с Minecraft серверами!");
        });
        commands.put("command", args -> {
            if (args.length <= 1) System.out.println("Использование: command <serverName|all> <command>");
            else {
                String command = Arrays.stream(Arrays.copyOfRange(args, 1, args.length)).reduce(((s, s2) -> s + " " + s2)).get();
                if (args[0].equalsIgnoreCase("all")) {
                    if (NettyServer.getServersAsStream().count() > 0) NettyServer.getServersAsStream().forEach(server -> server.sendCommand(command));
                    else System.out.println("Не нейдено серверов для отправления команды!");
                } else {
                    ClientConnection connection = NettyServer.getConnection(args[0], true).orElse(null);
                    if (connection != null) connection.getMetadata().getServerRepresenter().sendCommand(command);
                    else System.out.println("Сервер с названием '" + args[0] + "' не найден!");
                }
            }
        });
        commands.put("profile", args -> {
            if (args.length == 0) System.out.println("Использование: profile <playerName>");
            else {
                if (args[0].length() > 2) {
                    String defaultUUID = UUID.randomUUID().toString();
                    System.out.println(MojangAPI.fetchProfile(args[0], defaultUUID));
                } else System.out.println("Слишком короткий ник " + args[0]);
            }
        });
        commands.put("stop", args -> Supervisor.shutdown());
    }

}