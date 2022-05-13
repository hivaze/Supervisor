package me.litefine.supervisor.network.connection.communication;

import me.litefine.supervisor.network.messages.files.FileSendingMessage;

import java.io.IOException;
import java.nio.ByteBuffer;

public class FileReceiver {

    private volatile FileReceiving currentFileReceiving = null;

    public FileReceiving getReceiving() {
        return currentFileReceiving;
    }

    public void receiveDataChunk(ByteBuffer byteBuffer) throws IOException {
        currentFileReceiving.fileChannel.write(byteBuffer);
    }

    public long receivedBytes() throws IOException {
        return currentFileReceiving.fileChannel.size();
    }

    public void stopReceivingMode() {
        try {
            currentFileReceiving.fileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            currentFileReceiving = null;
        }
    }

    public boolean isReceivingMode() {
        return currentFileReceiving != null;
    }

    public void createNewReceiving(FileSendingMessage message) throws Exception {
        currentFileReceiving = new FileReceiving(message);
    }

}