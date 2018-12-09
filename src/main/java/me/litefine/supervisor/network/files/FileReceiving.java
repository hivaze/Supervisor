package me.litefine.supervisor.network.files;

import me.litefine.supervisor.network.messages.files.FileSendingMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class FileReceiving {

    private final String path;
    private final File file;
    final FileChannel fileChannel;
    private final long maxFileLength, startTimestamp;

    FileReceiving(FileSendingMessage message) throws Exception {
        this.path = message.getPath();
        this.file = File.createTempFile("Supervisor-Receiving-", ".dwn");
        this.maxFileLength = message.getFileSize();
        this.fileChannel = new FileOutputStream(file).getChannel();
        this.startTimestamp = System.currentTimeMillis();
    }

    public String getPath() {
        return path;
    }

    public File getFile() {
        return file;
    }

    public long getMaxFileLength() {
        return maxFileLength;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

}