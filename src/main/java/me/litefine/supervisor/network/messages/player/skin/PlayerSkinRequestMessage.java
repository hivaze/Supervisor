package me.litefine.supervisor.network.messages.player.skin;

import io.netty.buffer.ByteBuf;
import me.litefine.supervisor.network.messages.AbstractMessage;
import me.litefine.supervisor.utils.NettyUtils;

public class PlayerSkinRequestMessage extends AbstractMessage {

    private String playerName, defaultUUID;

    public PlayerSkinRequestMessage() {}

    public PlayerSkinRequestMessage(String playerName, String defaultUUID) {
        this.playerName = playerName;
        this.defaultUUID = defaultUUID;
    }

    @Override
    public void encodeIn(ByteBuf byteBuf) throws Exception {
        NettyUtils.writeString(byteBuf, playerName);
        NettyUtils.writeString(byteBuf, defaultUUID);
    }

    @Override
    public void decodeFrom(ByteBuf byteBuf) throws Exception {
        playerName = NettyUtils.readNextString(byteBuf);
        defaultUUID = NettyUtils.readNextString(byteBuf);
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getDefaultUUID() {
        return defaultUUID;
    }

}