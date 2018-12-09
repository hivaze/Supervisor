package me.litefine.supervisor.network.messages.player.chat;

import io.netty.buffer.ByteBuf;
import me.litefine.supervisor.network.messages.AbstractMessage;
import me.litefine.supervisor.utils.NettyUtils;

public class PlayerChatMessage extends AbstractMessage {

    private String playerName, message;

    public PlayerChatMessage() {}

    public PlayerChatMessage(String playerName, String message) {
        this.playerName = playerName;
        this.message = message;
    }

    @Override
    public void encodeIn(ByteBuf byteBuf) throws Exception {
        NettyUtils.writeString(byteBuf, playerName);
        NettyUtils.writeString(byteBuf, message);
    }

    @Override
    public void decodeFrom(ByteBuf byteBuf) throws Exception {
        playerName = NettyUtils.readNextString(byteBuf);
        message = NettyUtils.readNextString(byteBuf);
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getMessage() {
        return message;
    }

}