package me.litefine.supervisor.network.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import me.litefine.supervisor.main.Settings;
import me.litefine.supervisor.main.Supervisor;
import me.litefine.supervisor.network.NettyServer;
import me.litefine.supervisor.network.connection.metadata.ConnectionMetadata;
import me.litefine.supervisor.network.connection.metadata.representers.MinecraftServerRepresenter;
import me.litefine.supervisor.network.files.FileReceiver;
import me.litefine.supervisor.network.handlers.LogicHandler;
import me.litefine.supervisor.network.messages.AbstractMessage;
import me.litefine.supervisor.network.messages.files.FileSendingMessage;
import me.litefine.supervisor.network.messages.files.FilesRequestMessage;
import me.litefine.supervisor.network.messages.files.FilesResponseMessage;
import me.litefine.supervisor.network.messages.player.bungeecord.PlayerInfoRequestMessage;
import me.litefine.supervisor.network.messages.player.bungeecord.PlayerInfoResponseMessage;
import me.litefine.supervisor.network.messages.player.chat.PlayerChatMessage;
import me.litefine.supervisor.network.messages.player.chat.PlayerTitleMessage;
import me.litefine.supervisor.network.messages.player.skin.PlayerSkinRequestMessage;
import me.litefine.supervisor.network.messages.player.skin.PlayerSkinResponseMessage;
import me.litefine.supervisor.network.messages.system.connection.DisconnectMessage;
import me.litefine.supervisor.network.messages.system.connection.IdentificationMessage;
import me.litefine.supervisor.network.messages.system.conversation.CommandMessage;
import me.litefine.supervisor.network.messages.system.conversation.CustomPayloadMessage;
import me.litefine.supervisor.network.messages.system.conversation.servers.ServerInfoEventMessage;
import me.litefine.supervisor.network.messages.system.conversation.servers.ServerUpdateMessage;
import me.litefine.supervisor.utils.StringUtils;
import me.litefine.supervisor.utils.mojang.CashedProfile;
import me.litefine.supervisor.utils.mojang.MojangAPI;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClientConnection {

    private final Channel channel;
    private final InetSocketAddress address;
    private final FileReceiver fileReceiver = new FileReceiver();
    private ConnectionMetadata metadata = null;
    private String disconnectReason = "REASON NOT DEFINED";
    private final ExecutorService extendExecutorService = Executors.newCachedThreadPool();

    public ClientConnection(ChannelHandlerContext ctx) throws IllegalAccessException {
        this.channel = ctx.channel();
        this.address = (InetSocketAddress) channel.remoteAddress();
        if (Settings.isSecurityEnable()) {
            if (!Settings.getAllowedIPs().contains(address.getAddress().getHostAddress())) {
                ctx.close();
                throw new IllegalAccessException(address.getAddress().getHostAddress() + " - IP not allowed");
            }
        }
        NettyServer.getConnections().put(channel, this);
        extendExecutorService.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
                if (NettyServer.getConnection(channel).isPresent() && metadata == null) {
                    NettyServer.getConnections().remove(channel);
                    close("IDENTIFICATION NOT PASSED IN 5 SECONDS");
                    Supervisor.getLogger().debug("Connection " + address + " dropped: Identification timeout");
                }
            } catch (InterruptedException ignored) {}
        });
    }

    public void handleMessage(AbstractMessage message) throws Exception {
        if (message instanceof IdentificationMessage) {
            metadata = new ConnectionMetadata(this, (IdentificationMessage) message);
            HashMap<String, String> serversDataMap = new HashMap<>();
            NettyServer.getConnections(md -> md != metadata && !md.isAPIHandler() && (md.getConnectionType() == metadata.getConnectionType() || metadata.isAPIHandler()))
                    .forEach(cc -> serversDataMap.put(cc.getMetadata().getIdentificator(), cc.getMetadata().getServerRepresenter().getFormattedData()));
            sendMessage(new CustomPayloadMessage("ServersInfos", serversDataMap));
        }
        else if (message instanceof CustomPayloadMessage) NettyServer.getConnections(md -> md != metadata).forEach(cc -> cc.sendMessage(message));
        else if (message instanceof FileSendingMessage) {
            FileSendingMessage sendingMessage = (FileSendingMessage) message;
            LogicHandler.CURRENT_FILES_RECEIVINGS.add(sendingMessage.getPath());
            fileReceiver.createNewReceiving(sendingMessage);
        }
        else if (message instanceof FilesRequestMessage) {
            String path = ((FilesRequestMessage) message).getPath();
            File target = Paths.get(Settings.getDataFolder().getAbsolutePath() + "/" + path).toFile();
            if (LogicHandler.CURRENT_FILES_RECEIVINGS.contains(path)) sendMessage(new FilesResponseMessage(path, -1));
            else if (target.exists()) {
                if (target.isFile()) {
                    sendMessage(new FilesResponseMessage(path, 1));
                    channel.writeAndFlush(target);
                } else {
                    sendMessage(new FilesResponseMessage(path, target.listFiles().length));
                    for (File file : target.listFiles()) channel.writeAndFlush(file);
                }
            } else channel.writeAndFlush(new FilesResponseMessage(path, 0));
        } else if (message instanceof ServerUpdateMessage) {
            if (!metadata.isAPIHandler()) {
                ServerUpdateMessage updateMessage = (ServerUpdateMessage) message;
                boolean firstFill = !metadata.getServerRepresenter().isFilled();
                String[] dataSplit = StringUtils.splitFormattedData(updateMessage.getFormattedInfoData());
                metadata.getServerRepresenter().setOnline(Integer.parseInt(dataSplit[0]));
                metadata.getServerRepresenter().setMaxPlayers(Integer.parseInt(dataSplit[1]));
                metadata.getServerRepresenter().setMotd(dataSplit[2]);
                if (metadata.isMinecraftServer()) ((MinecraftServerRepresenter) metadata.getServerRepresenter()).setTps(Float.parseFloat(dataSplit[3]));
                ServerInfoEventMessage abstractMessage = firstFill ? new ServerInfoEventMessage(ServerInfoEventMessage.ServerEvent.ADD_SERVER, metadata.getIdentificator(), updateMessage.getFormattedInfoData()) :
                        new ServerInfoEventMessage(ServerInfoEventMessage.ServerEvent.STATUS_UPDATE, metadata.getIdentificator(), updateMessage.getFormattedInfoData());
                NettyServer.getConnections(md -> ((metadata != md && metadata.getConnectionType() == md.getConnectionType()) || md.isAPIHandler()) && md.isSubscribedTo(metadata)).forEach(cc -> cc.sendMessage(abstractMessage));
            }
        } else if (message instanceof PlayerInfoRequestMessage) {
            String requestedPlayer = ((PlayerInfoRequestMessage) message).getPlayerName();
            PlayerInfoResponseMessage nullMessage = new PlayerInfoResponseMessage(requestedPlayer, "NULL");
            if (NettyServer.getBungeeServers().isEmpty()) sendMessage(nullMessage);
            else {
                extendExecutorService.submit(() -> {
                    for (ClientConnection clientConnection : NettyServer.getConnections(ConnectionMetadata::isBungeeServer)) {
                        if (!clientConnection.getChannel().isActive()) continue;
                        clientConnection.sendMessage(message);
                        PlayerInfoResponseMessage responseMessage = clientConnection.getChannel().pipeline().get(LogicHandler.class).waitForObject(PlayerInfoResponseMessage.class, response -> response.getPlayerName().equals(requestedPlayer), 100L).orElse(null);
                        if (responseMessage != null && responseMessage.isFound()) {
                            if (channel.isActive()) sendMessage(responseMessage);
                            return;
                        }
                    }
                    if (channel.isActive()) sendMessage(nullMessage);
                });
            }
        } else if (message instanceof CommandMessage) {
            CommandMessage commandMessage = (CommandMessage) message;
            String target = commandMessage.getTargetName();
            Supervisor.getLogger().debug("Client with id " + metadata.getIdentificator() + " request '" + commandMessage.getCommand() + "' command exeqution on " + target);
            try {
                CommandMessage.TargetType targetType = CommandMessage.TargetType.valueOf(target);
                switch (targetType) {
                    case ALL_MC_SERVERS:
                        NettyServer.getMinecraftServers().forEach(server -> server.sendCommand(commandMessage.getCommand()));
                        break;
                    case ALL_BUNGEECORDS:
                        NettyServer.getBungeeServers().forEach(server -> server.sendCommand(commandMessage.getCommand()));
                        break;
                    case ALL_API_HANDLERS:
                        NettyServer.getConnections(ConnectionMetadata::isAPIHandler).forEach(cc -> sendMessage(message));
                        break;
                }
            } catch (IllegalArgumentException ex) {
                NettyServer.getConnection(target, true).ifPresent(connection -> connection.sendMessage(message));
            }
        } else if (message instanceof PlayerChatMessage || message instanceof PlayerTitleMessage) {
            if (!metadata.isAPIHandler()) NettyServer.getConnections(md -> md.getConnectionType() == metadata.getConnectionType() && md != metadata).forEach(cc -> cc.sendMessage(message));
            else NettyServer.getConnections(ConnectionMetadata::isBungeeServer).forEach(cc -> cc.sendMessage(message));
        } else if (message instanceof PlayerSkinRequestMessage) {
            PlayerSkinRequestMessage requestMessage = (PlayerSkinRequestMessage) message;
            CashedProfile cashedProfile = MojangAPI.fetchProfile(requestMessage.getPlayerName(), requestMessage.getDefaultUUID());
            sendMessage(new PlayerSkinResponseMessage(requestMessage.getPlayerName(), cashedProfile));
        } else if (message instanceof DisconnectMessage) {
            DisconnectMessage disconnectMessage = (DisconnectMessage) message;
            disconnectReason = disconnectMessage.getReason();
            channel.close().syncUninterruptibly();
        }
    }

    public void sendMessage(AbstractMessage message) {
        channel.writeAndFlush(message);
    }

    public FileReceiver getFileReceiver() {
        return fileReceiver;
    }

    public void close(String reason) {
        disconnectReason = reason;
        sendMessage(new DisconnectMessage(reason));
        channel.close().syncUninterruptibly();
    }

    public ExecutorService getExtendExecutorService() {
        return extendExecutorService;
    }

    public boolean isIdentified() {
        return metadata != null;
    }

    public ConnectionMetadata getMetadata() {
        return metadata;
    }

    public Channel getChannel() {
        return channel;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public String getDisconnectReason() {
        return disconnectReason;
    }

}