package me.litefine.supervisor.network.connection.metadata.representers;

import me.litefine.supervisor.main.Supervisor;
import me.litefine.supervisor.network.connection.metadata.ConnectionMetadata;
import me.litefine.supervisor.network.messages.system.conversation.CommandMessage;

public abstract class ServerRepresenter {

    protected final ConnectionMetadata connectionMetadata;

    protected String motd;
    protected int online = -1, maxPlayers = -1;

    public ServerRepresenter(ConnectionMetadata connectionMetadata) {
        this.connectionMetadata = connectionMetadata;
    }

    public abstract String getFormattedData();

    public String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void sendCommand(String command) {
        connectionMetadata.getConnection().communication().sendMessage(new CommandMessage(connectionMetadata.getIdentificator(), command));
        Supervisor.getLogger().info("[EXECUTION] Executing command '" + command + "' on server " + connectionMetadata.getIdentificator());
    }

    public boolean isFilled() {
        return motd != null && online != -1 && maxPlayers != -1 && isFilled0();
    }

    protected abstract boolean isFilled0();

    public ConnectionMetadata getConnectionMetadata() {
        return connectionMetadata;
    }

}