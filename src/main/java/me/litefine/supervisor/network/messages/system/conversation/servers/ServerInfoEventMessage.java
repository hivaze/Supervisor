package me.litefine.supervisor.network.messages.system.conversation.servers;

import io.netty.buffer.ByteBuf;
import me.litefine.supervisor.network.messages.AbstractMessage;
import me.litefine.supervisor.utils.NettyUtils;

import java.io.IOException;

public class ServerInfoEventMessage extends AbstractMessage {

    private ServerEvent serverEvent;
    private String identificator, formattedInfoData;

    public ServerInfoEventMessage() {}

    public ServerInfoEventMessage(ServerEvent serverEvent, String identificator, String jsonInfoData) {
        this.serverEvent = serverEvent;
        this.identificator = identificator;
        this.formattedInfoData = jsonInfoData;
    }

    @Override
    public void encodeIn(ByteBuf byteBuf) throws IOException {
        byteBuf.writeByte(serverEvent.ordinal());
        NettyUtils.writeString(byteBuf, identificator);
        if (serverEvent != ServerEvent.REMOVE_SERVER) NettyUtils.writeString(byteBuf, formattedInfoData);
    }

    @Override
    public void decodeFrom(ByteBuf byteBuf) throws IOException, ClassNotFoundException {
        serverEvent = ServerEvent.values()[byteBuf.readByte()];
        identificator = NettyUtils.readNextString(byteBuf);
        if (serverEvent != ServerEvent.REMOVE_SERVER) formattedInfoData = NettyUtils.readNextString(byteBuf);
    }

    public ServerEvent getServerEvent() {
        return serverEvent;
    }

    public String getIdentificator() {
        return identificator;
    }

    public String getFormattedInfoData() {
        return formattedInfoData;
    }

    public enum ServerEvent {
        ADD_SERVER, STATUS_UPDATE, REMOVE_SERVER
    }

}