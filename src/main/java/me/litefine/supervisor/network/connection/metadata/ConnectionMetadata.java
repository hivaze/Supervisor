package me.litefine.supervisor.network.connection.metadata;

import me.litefine.supervisor.network.NettyServer;
import me.litefine.supervisor.network.connection.ClientConnection;
import me.litefine.supervisor.network.connection.ConnectionType;
import me.litefine.supervisor.network.connection.metadata.representers.BungeeServerRepresenter;
import me.litefine.supervisor.network.connection.metadata.representers.MinecraftServerRepresenter;
import me.litefine.supervisor.network.connection.metadata.representers.ServerRepresenter;
import me.litefine.supervisor.network.messages.system.connection.IdentificationMessage;
import me.litefine.supervisor.utils.StringUtils;

import java.util.regex.Pattern;

public class ConnectionMetadata {

    private final String identificator;
    private final ClientConnection connection;
    private final ConnectionType connectionType;
    private final long identificationTimestamp;
    private final int remotePort;
    private final Pattern serverInfoPattern;

    private final ServerRepresenter serverRepresenter;

    public ConnectionMetadata(ClientConnection clientConnection, IdentificationMessage message) {
        connection = clientConnection;
        if (!NettyServer.getConnection(message.getIdentificator()).isPresent()) identificator = message.getIdentificator();
        else identificator = message.getIdentificator() + "-" + connection.getChannel().id().asShortText();
        connectionType = message.getConnectionType();
        remotePort = message.getRemotePort();
        serverInfoPattern = Pattern.compile(message.getServersInfoPattern());
        identificationTimestamp = System.currentTimeMillis();
        if (connectionType == ConnectionType.BUNGEECORD_SERVER) serverRepresenter = new BungeeServerRepresenter(this);
        else if (connectionType == ConnectionType.MINECRAFT_SERVER) serverRepresenter = new MinecraftServerRepresenter(this);
        else serverRepresenter = null;
    }

    public boolean supportsServerRepresenter() {
        return connectionType != ConnectionType.API_HANDLER && serverRepresenter.isFilled();
    }

    public ClientConnection getConnection() {
        return connection;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public boolean isBungeeServer() {
        return connectionType == ConnectionType.BUNGEECORD_SERVER;
    }

    public boolean isMinecraftServer() {
        return connectionType == ConnectionType.MINECRAFT_SERVER;
    }

    public boolean isAPIHandler() {
        return connectionType == ConnectionType.API_HANDLER;
    }

    public String getFormattedAddress() {
        return StringUtils.getStringRepresentation(connection.getChannel().remoteAddress()) + ":" + remotePort;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public long millisSinceConnect() {
        return System.currentTimeMillis() - identificationTimestamp;
    }

    public ServerRepresenter getServerRepresenter() {
        return serverRepresenter;
    }

    public String getIdentificator() {
        return identificator;
    }

    public boolean isSubscribedTo(ConnectionMetadata metadata) {
        return serverInfoPattern.matcher(metadata.identificator).find();
    }

}