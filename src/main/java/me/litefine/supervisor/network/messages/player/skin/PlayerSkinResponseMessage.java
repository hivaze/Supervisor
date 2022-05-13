package me.litefine.supervisor.network.messages.player.skin;

import io.netty.buffer.ByteBuf;
import me.litefine.supervisor.network.messages.AbstractMessage;
import me.litefine.supervisor.utils.NettyUtils;
import me.litefine.supervisor.utils.mojang.CashedProfile;

public class PlayerSkinResponseMessage extends AbstractMessage {

    private String playerName, UUID;
    private boolean hasTextures;
    private String texturesValue, signature;

    public PlayerSkinResponseMessage() {}

    public PlayerSkinResponseMessage(String playerName, CashedProfile cashedProfile) {
        this.playerName = playerName;
        this.UUID = cashedProfile.getUUID();
        this.hasTextures = cashedProfile.hasTextures();
        this.texturesValue = cashedProfile.getTextures();
        this.signature = cashedProfile.getSignature();
    }

    @Override
    public void encodeIn(ByteBuf byteBuf) throws Exception {
        NettyUtils.writeString(byteBuf, playerName);
        NettyUtils.writeString(byteBuf, UUID);
        byteBuf.writeBoolean(hasTextures);
        if (hasTextures) {
            NettyUtils.writeString(byteBuf, texturesValue);
            NettyUtils.writeString(byteBuf, signature);
        }
    }

    @Override
    public void decodeFrom(ByteBuf byteBuf) throws Exception {
        playerName = NettyUtils.readNextString(byteBuf);
        UUID = NettyUtils.readNextString(byteBuf);
        hasTextures = byteBuf.readBoolean();
        if (hasTextures) {
            texturesValue = NettyUtils.readNextString(byteBuf);
            signature = NettyUtils.readNextString(byteBuf);
        }

    }

    public String getPlayerName() {
        return playerName;
    }

    public String getUUID() {
        return UUID;
    }

    public boolean isHasTextures() {
        return hasTextures;
    }

    public String getTexturesValue() {
        return texturesValue;
    }

    public String getSignature() {
        return signature;
    }

}