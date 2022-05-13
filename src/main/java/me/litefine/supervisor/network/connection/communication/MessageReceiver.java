package me.litefine.supervisor.network.connection.communication;

import io.netty.buffer.ByteBuf;

public class MessageReceiver {

    private ByteBuf cumulation;
    private short currentFrameLength;

    public int readableBytes() {
        return cumulation.readableBytes();
    }

    public ByteBuf writeBytes(ByteBuf byteBuf) {
        return cumulation.writeBytes(byteBuf);
    }

    public boolean hasCumulation() {
        return cumulation != null;
    }

    public void setCumulation(ByteBuf cumulation) {
        this.cumulation = cumulation;
    }

    public void setCurrentFrameLength(short currentFrameLength) {
        this.currentFrameLength = currentFrameLength;
    }

    public short getCurrentFrameLength() {
        return currentFrameLength;
    }

}