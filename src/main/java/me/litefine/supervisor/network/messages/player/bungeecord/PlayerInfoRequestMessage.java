package me.litefine.supervisor.network.messages.player.bungeecord;

import io.netty.buffer.ByteBuf;
import me.litefine.supervisor.network.messages.AbstractMessage;
import me.litefine.supervisor.utils.NettyUtils;

public class PlayerInfoRequestMessage extends AbstractMessage {

    private String playerName;

    public PlayerInfoRequestMessage() {}

    public PlayerInfoRequestMessage(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public void encodeIn(ByteBuf byteBuf) throws Exception {
        NettyUtils.writeString(byteBuf, playerName);
    }

    @Override
    public void decodeFrom(ByteBuf byteBuf) throws Exception {
        this.playerName = NettyUtils.readNextString(byteBuf);
    }

    public String getPlayerName() {
        return playerName;
    }

}