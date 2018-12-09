package me.litefine.supervisor.network.messages.files;

import io.netty.buffer.ByteBuf;
import me.litefine.supervisor.network.messages.AbstractMessage;
import me.litefine.supervisor.utils.NettyUtils;

public class FilesRequestMessage extends AbstractMessage {

    private String path;

    public FilesRequestMessage() {}

    public FilesRequestMessage(String path) {
        this.path = path;
    }

    @Override
    public void encodeIn(ByteBuf byteBuf) {
        NettyUtils.writeString(byteBuf, path);
    }

    @Override
    public void decodeFrom(ByteBuf byteBuf) {
        path = NettyUtils.readNextString(byteBuf);
    }

    public String getPath() {
        return path;
    }

}