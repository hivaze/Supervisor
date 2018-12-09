package me.litefine.supervisor.network.files;

import me.litefine.supervisor.network.messages.files.FileSendingMessage;

import java.io.IOException;
import java.nio.ByteBuffer;

public class FileReceiver {

    private volatile FileReceiving fileReceiving = null;

    public FileReceiving getReceiving() {
        return fileReceiving;
    }

    public void receiveDataChunk(ByteBuffer byteBuffer) throws IOException {
        fileReceiving.fileChannel.write(byteBuffer);
    }

    public long receivedBytes() throws IOException {
        return fileReceiving.fileChannel.size();
    }

    public void stopReceivingMode() {
        try {
            fileReceiving.fileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fileReceiving = null;
        }
    }

    public boolean isReceivingMode() {
        return fileReceiving != null;
    }

    public void createNewReceiving(FileSendingMessage message) throws Exception {
        fileReceiving = new FileReceiving(message);
    }

}