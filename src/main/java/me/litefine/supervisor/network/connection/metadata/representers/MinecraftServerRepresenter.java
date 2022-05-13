package me.litefine.supervisor.network.connection.metadata.representers;

import me.litefine.supervisor.network.NettyServer;
import me.litefine.supervisor.network.connection.metadata.ConnectionMetadata;

import java.util.HashMap;

public class MinecraftServerRepresenter extends ServerRepresenter {

    private float tps = -1;

    public MinecraftServerRepresenter(ConnectionMetadata connectionMetadata) {
        super(connectionMetadata);
    }

    @Override
    public String getFormattedData() {
        return "`" + online + "`" + maxPlayers + "`" + motd + "`" + tps + "`";
    }

    public float getTPS() {
        return tps;
    }

    public void setTps(float tps) {
        this.tps = tps;
    }

    @Override
    protected boolean isFilled0() {
        return tps != -1;
    }

    public HashMap<String, Object> getYamlMap() {
        HashMap<String, Object> info = new HashMap<>();
        info.put("address", connectionMetadata.getFormattedAddress());
        info.put("restricted", false);
        info.put("motd", "Generated by Supervisor");
        HashMap<String, Object> server = new HashMap<>();
        server.put(connectionMetadata.getIdentificator(), info);
        return server;
    }

    public static int getTotalOnline() {
        return NettyServer.getMinecraftServers().mapToInt(ServerRepresenter::getOnline).sum();
    }

    public static int getTotalMaxCount() {
        return NettyServer.getMinecraftServers().mapToInt(ServerRepresenter::getMaxPlayers).sum();
    }

}