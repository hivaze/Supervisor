package me.litefine.supervisor.network.messages.files;

import io.netty.buffer.ByteBuf;
import me.litefine.supervisor.main.Settings;
import me.litefine.supervisor.network.messages.AbstractMessage;
import me.litefine.supervisor.utils.NettyUtils;

import java.io.File;

public class FileSendingMessage extends AbstractMessage {

    private String path;
    private long fileSize;

    public FileSendingMessage() {}

    public FileSendingMessage(File file) {
        path = Settings.getDataFolder().toPath().relativize(file.toPath()).toString();
        fileSize = file.length();
    }

    @Override
    public void encodeIn(ByteBuf byteBuf) {
        NettyUtils.writeString(byteBuf, path);
        byteBuf.writeLong(fileSize);
    }

    @Override
    public void decodeFrom(ByteBuf byteBuf) {
        path = NettyUtils.readNextString(byteBuf);
        fileSize = byteBuf.readLong();
    }

    public String getPath() {
        return path;
    }

    public long getFileSize() {
        return fileSize;
    }

}