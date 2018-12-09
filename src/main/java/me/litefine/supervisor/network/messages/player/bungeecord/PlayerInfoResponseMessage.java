package me.litefine.supervisor.network.messages.player.bungeecord;

import io.netty.buffer.ByteBuf;
import me.litefine.supervisor.network.messages.AbstractMessage;
import me.litefine.supervisor.utils.NettyUtils;

public class PlayerInfoResponseMessage extends AbstractMessage {

    private String playerName, response;

    public PlayerInfoResponseMessage() {}

    public PlayerInfoResponseMessage(String playerName, String response) {
        this.playerName = playerName;
        this.response = response;
    }

    @Override
    public void encodeIn(ByteBuf byteBuf) throws Exception {
        NettyUtils.writeString(byteBuf, playerName);
        NettyUtils.writeString(byteBuf, response);
    }

    @Override
    public void decodeFrom(ByteBuf byteBuf) throws Exception {
        this.playerName = NettyUtils.readNextString(byteBuf);
        this.response = NettyUtils.readNextString(byteBuf);
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getResponse() {
        return response;
    }

    public boolean isFound() {
        return response.startsWith("FOUND");
    }

}