package me.litefine.supervisor.network.connection.metadata.representers;

import me.litefine.supervisor.network.NettyServer;
import me.litefine.supervisor.network.connection.metadata.ConnectionMetadata;

public class BungeeServerRepresenter extends ServerRepresenter {

    public BungeeServerRepresenter(ConnectionMetadata connectionMetadata) {
        super(connectionMetadata);
    }

    @Override
    public String getFormattedData() {
        return "`" + online + "`" + maxPlayers + "`" + motd + "`";
    }

    @Override
    protected boolean isFilled0() {
        return true;
    }

    public static int getTotalOnline() {
        return NettyServer.getBungeeServers().stream().mapToInt(ServerRepresenter::getOnline).sum();
    }

    public static int getTotalMaxCount() {
        return NettyServer.getBungeeServers().stream().mapToInt(ServerRepresenter::getMaxPlayers).sum();
    }

}