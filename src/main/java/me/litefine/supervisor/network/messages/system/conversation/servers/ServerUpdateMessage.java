package me.litefine.supervisor.network.messages.system.conversation.servers;

import io.netty.buffer.ByteBuf;
import me.litefine.supervisor.network.messages.AbstractMessage;
import me.litefine.supervisor.utils.NettyUtils;

public class ServerUpdateMessage extends AbstractMessage {

    private String formattedInfoData;

    public ServerUpdateMessage() {}

    @Override
    public void encodeIn(ByteBuf byteBuf) throws Exception {
        NettyUtils.writeString(byteBuf, formattedInfoData);
    }

    @Override
    public void decodeFrom(ByteBuf byteBuf) throws Exception {
        formattedInfoData = NettyUtils.readNextString(byteBuf);
    }

    public String getFormattedInfoData() {
        return formattedInfoData;
    }

}