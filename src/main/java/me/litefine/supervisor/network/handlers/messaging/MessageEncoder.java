package me.litefine.supervisor.network.handlers.messaging;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import me.litefine.supervisor.network.messages.AbstractMessage;
import me.litefine.supervisor.utils.NettyUtils;

@ChannelHandler.Sharable
public class MessageEncoder extends ChannelOutboundHandlerAdapter {

    public static final MessageEncoder INSTANCE = new MessageEncoder();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof AbstractMessage) {
            ByteBuf byteBuf = ctx.alloc().buffer();
            AbstractMessage message = (AbstractMessage) msg;
            String className = message.getClass().getName();
            NettyUtils.writeString(byteBuf, className.substring(className.indexOf("messages")));
            message.encodeIn(byteBuf);
            ByteBuf wrapper = byteBuf.alloc().buffer(2 + byteBuf.readableBytes()).writeShort(byteBuf.readableBytes()).writeBytes(byteBuf);
            super.write(ctx, wrapper, promise);
        } else super.write(ctx, msg, promise);
    }

}