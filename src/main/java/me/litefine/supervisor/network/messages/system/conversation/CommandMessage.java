package me.litefine.supervisor.network.messages.system.conversation;

import io.netty.buffer.ByteBuf;
import me.litefine.supervisor.network.messages.AbstractMessage;
import me.litefine.supervisor.utils.NettyUtils;

public class CommandMessage extends AbstractMessage {

    private String targetName, command;

    public CommandMessage() {}

    public CommandMessage(String targetName, String command) {
        this.targetName = targetName;
        this.command = command;
    }

    @Override
    public void encodeIn(ByteBuf byteBuf) throws Exception {
        NettyUtils.writeString(byteBuf, targetName);
        NettyUtils.writeString(byteBuf, command);
    }

    @Override
    public void decodeFrom(ByteBuf byteBuf) throws Exception {
        targetName = NettyUtils.readNextString(byteBuf);
        command = NettyUtils.readNextString(byteBuf);
    }

    public String getTargetName() {
        return targetName;
    }

    public String getCommand() {
        return command;
    }

    public enum TargetType {
        ALL_BUNGEECORDS, ALL_MC_SERVERS, ALL_API_HANDLERS
    }

}