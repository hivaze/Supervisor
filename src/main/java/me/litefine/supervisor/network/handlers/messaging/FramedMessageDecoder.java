package me.litefine.supervisor.network.handlers.messaging;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import me.litefine.supervisor.main.Supervisor;
import me.litefine.supervisor.network.NettyServer;
import me.litefine.supervisor.network.connection.ClientConnection;
import me.litefine.supervisor.network.connection.communication.MessageReceiver;
import me.litefine.supervisor.network.messages.AbstractMessage;
import me.litefine.supervisor.utils.NettyUtils;

@ChannelHandler.Sharable
public class FramedMessageDecoder extends SimpleChannelInboundHandler<ByteBuf> {

    public static final FramedMessageDecoder INSTANCE = new FramedMessageDecoder();
    private static final String NETWORK_PACKAGE_NAME;

    static {
        String packageName = FramedMessageDecoder.class.getPackage().getName().substring(0, FramedMessageDecoder.class.getPackage().getName().lastIndexOf('.'));
        NETWORK_PACKAGE_NAME = packageName.substring(0, packageName.lastIndexOf('.'));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        ClientConnection connection = NettyServer.getConnection(ctx.channel()).orElse(null);
        if (connection != null) {
            MessageReceiver receiver = connection.communication().messageReceiver();
            ReferenceCountUtil.retain(byteBuf);
            if (!receiver.hasCumulation()) {
                receiver.setCurrentFrameLength(byteBuf.readShort());
                if (byteBuf.readableBytes() < receiver.getCurrentFrameLength())
                    receiver.setCumulation(ctx.alloc().buffer(receiver.getCurrentFrameLength()).writeBytes(byteBuf));
                else readMessageFromBuffer(connection, ctx, byteBuf);
            } else {
                int compositeLength = receiver.readableBytes() + byteBuf.readableBytes();
                if (compositeLength >= receiver.getCurrentFrameLength()) {
                    ByteBuf newBuf = receiver.writeBytes(byteBuf);
                    receiver.setCumulation(null);
                    readMessageFromBuffer(connection, ctx, newBuf);
                } else receiver.writeBytes(byteBuf);
            }
            ReferenceCountUtil.release(byteBuf);
        }
    }

    private void readMessageFromBuffer(ClientConnection connection, ChannelHandlerContext ctx, ByteBuf byteBuf) {
        String messageName = NettyUtils.readNextString(byteBuf);
        if (connection.isIdentified() || messageName.equals("messages.system.connection.IdentificationMessage")) {
            try {
                Class<?> clazz = Class.forName(NETWORK_PACKAGE_NAME + "." + messageName);
                if (AbstractMessage.class.isAssignableFrom(clazz)) {
                    Object receivedMessage = clazz.newInstance();
                    clazz.asSubclass(AbstractMessage.class).getDeclaredMethod("decodeFrom", ByteBuf.class).invoke(receivedMessage, byteBuf);
                    ctx.fireChannelRead(receivedMessage);
                    if (byteBuf.readerIndex() < byteBuf.writerIndex()) ctx.pipeline().fireChannelRead(byteBuf.retain());
                } else throw new ReflectiveOperationException(clazz.getName());
            } catch (ReflectiveOperationException ex) {
                Supervisor.getLogger().warn("[NETWORK] Unknown message type '" + messageName + "' received from " + connection.getAddress());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Supervisor.getLogger().warn("[NETWORK] (FramedMessageDecoder) Can't read ByteBuf from " + ctx.channel().remoteAddress());
        cause.printStackTrace();
    }

}