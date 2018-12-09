package me.litefine.supervisor.network.messages.system.connection;

import io.netty.buffer.ByteBuf;
import me.litefine.supervisor.network.connection.ConnectionType;
import me.litefine.supervisor.network.messages.AbstractMessage;
import me.litefine.supervisor.utils.NettyUtils;

public class IdentificationMessage extends AbstractMessage {

    private String identificator, serversInfoPattern;
    private ConnectionType connectionType;
    private int remotePort;

    public IdentificationMessage() {}

    @Override
    public void encodeIn(ByteBuf byteBuf) {
        NettyUtils.writeString(byteBuf, identificator);
        NettyUtils.writeString(byteBuf, connectionType.name());
        byteBuf.writeInt(remotePort);
        NettyUtils.writeString(byteBuf, serversInfoPattern);
    }

    @Override
    public void decodeFrom(ByteBuf byteBuf) {
        identificator = NettyUtils.readNextString(byteBuf);
        connectionType = ConnectionType.valueOf(NettyUtils.readNextString(byteBuf));
        remotePort = byteBuf.readInt();
        serversInfoPattern = NettyUtils.readNextString(byteBuf);
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getIdentificator() {
        return identificator;
    }

    public String getServersInfoPattern() {
        return serversInfoPattern;
    }

}