package me.litefine.supervisor.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import me.litefine.supervisor.main.Settings;
import me.litefine.supervisor.main.Supervisor;
import me.litefine.supervisor.network.connection.ClientConnection;
import me.litefine.supervisor.network.connection.metadata.ConnectionMetadata;
import me.litefine.supervisor.network.connection.metadata.representers.BungeeServerRepresenter;
import me.litefine.supervisor.network.connection.metadata.representers.MinecraftServerRepresenter;
import me.litefine.supervisor.network.connection.metadata.representers.ServerRepresenter;
import me.litefine.supervisor.network.handlers.LogicHandler;
import me.litefine.supervisor.network.handlers.files.ChunkedFileDecoder;
import me.litefine.supervisor.network.handlers.files.ChunkedFileEncoder;
import me.litefine.supervisor.network.handlers.messaging.FramedMessageDecoder;
import me.litefine.supervisor.network.handlers.messaging.MessageEncoder;
import me.litefine.supervisor.utils.NettyUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NettyServer {

    private static final EventLoopGroup bossGroup = NettyUtils.getEventLoopGroup(2), workerGroup = NettyUtils.getEventLoopGroup(Settings.getNettyThreadsCount());
    private static final Map<Channel, ClientConnection> connections = new ConcurrentHashMap<>();

    public static void start() {
        ChannelFutureListener listener = channelFuture -> {
            if (channelFuture.isSuccess()) Supervisor.getLogger().info("Server started at " + channelFuture.channel().localAddress());
            else {
                Supervisor.getLogger().warn("Can't start server at " + Settings.getSupervisorHost() + " - " + channelFuture.cause().getMessage());
                workerGroup.shutdownNow();
                System.exit(120);
            }
        };
        ChannelInitializer<Channel> initializer = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {
                //Experimental: channel.config().setRecvByteBufAllocator(new AdaptiveRecvByteBufAllocator(64, 2*1024, 64*1024));
                channel.pipeline().addLast(new ReadTimeoutHandler(3000));
                channel.pipeline().addLast(new ChunkedWriteHandler());
                channel.pipeline().addLast(ChunkedFileDecoder.INSTANCE);
                channel.pipeline().addLast(new FramedMessageDecoder());
                channel.pipeline().addLast(MessageEncoder.INSTANCE);
                channel.pipeline().addLast(ChunkedFileEncoder.INSTANCE);
                channel.pipeline().addLast(new LogicHandler());
            }
        };
        new ServerBootstrap().group(bossGroup, workerGroup)
                .channel(NettyUtils.getServerSocketClass())
                .option(ChannelOption.TCP_NODELAY, true) //Experimental
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .childHandler(initializer)
                .localAddress(Settings.getSupervisorHost())
                .bind().addListener(listener);
    }

    public static void stop() {
        connections.forEach(((channel, clientConnection) -> clientConnection.close("SUPERVISOR SHUTDOWN")));
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();
    }

    public static boolean isBooted() {
        return !workerGroup.isShutdown();
    }

    public static Map<Channel, ClientConnection> getConnections() {
        return connections;
    }

    public static List<ClientConnection> getIdentifiedConnections() {
        return connections.values().stream().filter(ClientConnection::isIdentified).collect(Collectors.toList());
    }

    public static List<ClientConnection> getNonIdentifiedCConnections() {
        return connections.values().stream().filter(cc -> !cc.isIdentified()).collect(Collectors.toList());
    }

    public static List<ClientConnection> getConnections(Predicate<ConnectionMetadata> filter) {
        return connections.values().stream().filter(cc -> cc.isIdentified() && filter.test(cc.getMetadata())).collect(Collectors.toList());
    }

    public static Optional<ClientConnection> getConnection(Channel channel) {
        return Optional.ofNullable(connections.get(channel));
    }

    public static Optional<ClientConnection> getConnection(String identificator) {
        return connections.values().stream().filter(cc -> cc.isIdentified() && cc.getMetadata().getIdentificator().equals(identificator)).findFirst();
    }

    public static Optional<ClientConnection> getConnection(String identificator, boolean supportsServerRepresenter) {
        return connections.values().stream().filter(cc -> cc.isIdentified() && cc.getMetadata().supportsServerRepresenter() == supportsServerRepresenter && cc.getMetadata().getIdentificator().equals(identificator)).findFirst();
    }

    public static Stream<ServerRepresenter> getServersAsStream() {
        return connections.values().stream().filter(cc -> cc.isIdentified() && cc.getMetadata().supportsServerRepresenter()).map(cn -> cn.getMetadata().getServerRepresenter());
    }

    public static List<MinecraftServerRepresenter> getMinecraftServers() {
        return getServersAsStream().filter(MinecraftServerRepresenter.class::isInstance).map(MinecraftServerRepresenter.class::cast).collect(Collectors.toList());
    }

    public static List<BungeeServerRepresenter> getBungeeServers() {
        return getServersAsStream().filter(BungeeServerRepresenter.class::isInstance).map(BungeeServerRepresenter.class::cast).collect(Collectors.toList());
    }

}