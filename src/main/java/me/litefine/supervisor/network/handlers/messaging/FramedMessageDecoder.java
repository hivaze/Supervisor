package me.litefine.supervisor.network.handlers.messaging;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import me.litefine.supervisor.main.Supervisor;
import me.litefine.supervisor.network.NettyServer;
import me.litefine.supervisor.network.messages.AbstractMessage;
import me.litefine.supervisor.utils.NettyUtils;

public class FramedMessageDecoder extends SimpleChannelInboundHandler<ByteBuf> {

    private static final String NETWORK_PACKAGE_NAME;

    static {
        String packageName = FramedMessageDecoder.class.getPackage().getName().substring(0, FramedMessageDecoder.class.getPackage().getName().lastIndexOf('.'));
        NETWORK_PACKAGE_NAME = packageName.substring(0, packageName.lastIndexOf('.'));
    }

    private ByteBuf cumulation;
    private short currentFrameLength;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        ReferenceCountUtil.retain(byteBuf);
        if (cumulation == null) {
            currentFrameLength = byteBuf.readShort();
            if (byteBuf.readableBytes() < currentFrameLength) cumulation = ctx.alloc().buffer(currentFrameLength).writeBytes(byteBuf);
            else readMessageFromBuffer(ctx, byteBuf);
        } else {
            int compositeLength = cumulation.readableBytes() + byteBuf.readableBytes();
            if (compositeLength >= currentFrameLength) {
                ByteBuf newBuf = cumulation.writeBytes(byteBuf);
                cumulation = null;
                readMessageFromBuffer(ctx, newBuf);
            } else cumulation.writeBytes(byteBuf);
        }
        ReferenceCountUtil.release(byteBuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Supervisor.getLogger().warn("[NETWORK] (FramedMessageDecoder) Can't read ByteBuf from " + ctx.channel().remoteAddress());
        cause.printStackTrace();
    }

    private void readMessageFromBuffer(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        NettyServer.getConnection(ctx.channel()).ifPresent(clientConnection -> {
            String messageName = NettyUtils.readNextString(byteBuf);
            if (clientConnection.isIdentified() || messageName.equals("messages.system.connection.IdentificationMessage")) {
                try {
                    Class<?> clazz = Class.forName(NETWORK_PACKAGE_NAME + "." + messageName);
                    if (AbstractMessage.class.isAssignableFrom(clazz)) {
                        Object receivedMessage = clazz.newInstance();
                        clazz.asSubclass(AbstractMessage.class).getDeclaredMethod("decodeFrom", ByteBuf.class).invoke(receivedMessage, byteBuf);
                        ctx.fireChannelRead(receivedMessage);
                        if (byteBuf.readerIndex() < byteBuf.writerIndex()) ctx.pipeline().fireChannelRead(byteBuf.retain());
                    } else throw new ReflectiveOperationException(clazz.getName());
                } catch (ReflectiveOperationException ex) {
                    Supervisor.getLogger().warn("[NETWORK] Unknown message type '" + messageName + "' received from " + ctx.channel().remoteAddress());
                }
            }
        });
    }

}