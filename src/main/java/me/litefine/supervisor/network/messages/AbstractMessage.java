package me.litefine.supervisor.network.messages;

import io.netty.buffer.ByteBuf;

/**
 *
 * <h3>Each instance must contains nullary constructor
 * for automatic packet decoding</h3>
 *
 */
public abstract class AbstractMessage {

    public abstract void encodeIn(ByteBuf byteBuf) throws Exception;

    public abstract void decodeFrom(ByteBuf byteBuf) throws Exception;

}