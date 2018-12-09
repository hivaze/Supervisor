package me.litefine.supervisor.network.handlers.files;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.stream.ChunkedFile;
import me.litefine.supervisor.main.Supervisor;
import me.litefine.supervisor.network.messages.files.FileSendingMessage;

import java.io.File;

@ChannelHandler.Sharable
public class ChunkedFileEncoder extends ChannelOutboundHandlerAdapter {

    public static final ChunkedFileEncoder INSTANCE = new ChunkedFileEncoder();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof File) {
            File file = (File) msg;
            if (file.exists() && file.isFile()) {
                Supervisor.getLogger().debug("Sending file: " + file.getAbsolutePath() +  " to socket " + ctx.channel().unsafe().remoteAddress());
                super.write(ctx, new FileSendingMessage(file), promise);
                super.write(ctx, new ChunkedFile(file), promise);
            } else throw new IllegalArgumentException("Invalid file " + file);
        } else super.write(ctx, msg, promise);
    }

}