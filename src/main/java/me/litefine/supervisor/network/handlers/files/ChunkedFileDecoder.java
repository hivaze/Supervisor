package me.litefine.supervisor.network.handlers.files;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import me.litefine.supervisor.main.Supervisor;
import me.litefine.supervisor.network.NettyServer;
import me.litefine.supervisor.network.connection.ClientConnection;
import me.litefine.supervisor.network.connection.communication.FileReceiver;
import me.litefine.supervisor.network.connection.communication.FileReceiving;

@ChannelHandler.Sharable
public class ChunkedFileDecoder extends SimpleChannelInboundHandler<ByteBuf> {

    public static final ChunkedFileDecoder INSTANCE = new ChunkedFileDecoder();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        ClientConnection connection = NettyServer.getConnection(ctx.channel()).orElse(null);
        if (connection != null) {
            FileReceiver fileReceiver = connection.communication().fileReceiver();
            ReferenceCountUtil.retain(byteBuf);
            if (fileReceiver.isReceivingMode()) {
                FileReceiving fileReceiving = fileReceiver.getReceiving();
                long offset = fileReceiving.getMaxFileLength() - (fileReceiver.receivedBytes() + byteBuf.readableBytes());
                if (offset <= 0) {
                    int errorIndex = (int) (offset + byteBuf.readableBytes());
                    ByteBuf fixed = byteBuf.slice(0, errorIndex), extra = byteBuf.skipBytes(errorIndex).slice();
                    fileReceiver.receiveDataChunk(fixed.nioBuffer());
                    fileReceiver.stopReceivingMode();
                    ctx.fireChannelRead(fileReceiving);
                    if (extra.readableBytes() > 0) ctx.fireChannelRead(extra.retain());
                } else fileReceiver.receiveDataChunk(byteBuf.nioBuffer());
                ReferenceCountUtil.release(byteBuf);
            } else ctx.fireChannelRead(byteBuf);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Supervisor.getLogger().warn("[NETWORK] (ChunkedFileDecoder) Can't read ByteBuf from " + ctx.channel().remoteAddress());
        cause.printStackTrace();
    }

}