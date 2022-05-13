package me.litefine.supervisor.network.messages.player.chat;

import io.netty.buffer.ByteBuf;
import me.litefine.supervisor.network.messages.AbstractMessage;
import me.litefine.supervisor.utils.NettyUtils;

public class PlayerTitleMessage extends AbstractMessage {

    private String playerName, title, subtitle;
    private int fadeIn, stay, fadeOut;

    public PlayerTitleMessage() {}

    public PlayerTitleMessage(String playerName, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        this.playerName = playerName;
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    @Override
    public void encodeIn(ByteBuf byteBuf) throws Exception {
        NettyUtils.writeString(byteBuf, playerName);
        NettyUtils.writeString(byteBuf, title);
        NettyUtils.writeString(byteBuf, subtitle);
        byteBuf.writeInt(fadeIn).writeInt(stay).writeInt(fadeOut);
    }

    @Override
    public void decodeFrom(ByteBuf byteBuf) throws Exception {
        playerName = NettyUtils.readNextString(byteBuf);
        title = NettyUtils.readNextString(byteBuf);
        subtitle = NettyUtils.readNextString(byteBuf);
        fadeIn = byteBuf.readInt();
        stay = byteBuf.readInt();
        fadeOut = byteBuf.readInt();
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public int getStay() {
        return stay;
    }

    public int getFadeOut() {
        return fadeOut;
    }

}