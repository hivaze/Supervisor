package me.litefine.supervisor.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.ConcurrentSet;
import me.litefine.supervisor.main.Settings;
import me.litefine.supervisor.main.Supervisor;
import me.litefine.supervisor.network.NettyServer;
import me.litefine.supervisor.network.connection.ClientConnection;
import me.litefine.supervisor.network.files.FileReceiving;
import me.litefine.supervisor.network.messages.AbstractMessage;
import me.litefine.supervisor.network.messages.system.conversation.servers.ServerInfoEventMessage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class LogicHandler extends SimpleChannelInboundHandler<Object> {

    public static final Set<String> CURRENT_FILES_RECEIVINGS= new ConcurrentSet<>();
    private final Set<InboundObjectWaiter> objectWaiters = new ConcurrentSet<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        try {
            ClientConnection connection = new ClientConnection(ctx);
            Supervisor.getLogger().info("[NETWORK] Connection from " + connection.getAddress());
        } catch (IllegalAccessException e) {
            Supervisor.getLogger().info("[NETWORK] Connection attempt rejected: " + e.getMessage());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ClientConnection connection = NettyServer.getConnection(ctx.channel()).orElse(null);
        if (connection != null) {
            objectWaiters.forEach(waiter -> { synchronized (waiter) { waiter.notify(); } });
            connection.getExtendExecutorService().shutdownNow();
            NettyServer.getConnections().remove(connection.getChannel());
            if (connection.isIdentified() && !connection.getMetadata().isAPIHandler()) {
                ServerInfoEventMessage message = new ServerInfoEventMessage(ServerInfoEventMessage.ServerEvent.REMOVE_SERVER, connection.getMetadata().getIdentificator(), null);
                NettyServer.getConnections(md -> connection.getMetadata().getConnectionType() == md.getConnectionType() || md.isAPIHandler()).forEach(cc -> cc.sendMessage(message));
            }
            Supervisor.getLogger().info("[NETWORK] Connection " + connection.getAddress() + " closed: " + connection.getDisconnectReason());
        }
        else NettyServer.getConnections().remove(ctx.channel()); // Just for insurance
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object object) throws Exception {
        if (!objectWaiters.removeIf(mw -> mw.test(object))) {
            ClientConnection connection = NettyServer.getConnection(ctx.channel()).orElse(null);
            if (connection != null) {
                if (object instanceof AbstractMessage) connection.handleMessage((AbstractMessage) object);
                else if (object instanceof FileReceiving) {
                    FileReceiving fileReceiving = (FileReceiving) object;
                    File target = new File(Settings.getDataFolder(), fileReceiving.getPath());
                    target.getParentFile().mkdirs();
                    Files.move(fileReceiving.getFile().toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    CURRENT_FILES_RECEIVINGS.remove(fileReceiving.getPath());
                    Supervisor.getLogger().debug("[NETWORK] File received: (" + target.getAbsolutePath() + ", size: " + fileReceiving.getMaxFileLength() + ", time: " + (System.currentTimeMillis() - fileReceiving.getStartTimestamp()) + " ms) from socket " + connection.getAddress());
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Supervisor.getLogger().debug("Exception in socket " + ctx.channel().unsafe().remoteAddress());
        cause.printStackTrace();
        ctx.close();
    }

    public <T> Optional<T> waitForObject(Class<T> clazz, Predicate<T> condition, long timeoutMillis) {
        InboundObjectWaiter<T> objectWaiter = new InboundObjectWaiter<>(clazz, condition);
        synchronized (objectWaiter) {
            try {
                objectWaiters.add(objectWaiter);
                objectWaiter.wait(timeoutMillis);
                if (objectWaiter.result == null) {
                    objectWaiters.remove(objectWaiter);
                    return Optional.empty();
                } else return Optional.of(objectWaiter.result);
            } catch (InterruptedException e) {
                objectWaiters.remove(objectWaiter);
                return Optional.empty();
            }
        }
    }

    private static class InboundObjectWaiter <T> {

        private final Class<T> clazz;
        private final Predicate<T> condition;
        private T result = null;

        InboundObjectWaiter(Class<T> clazz, Predicate<T> condition) {
            this.clazz = clazz;
            this.condition = condition;
        }

        boolean test(Object object) {
            if (clazz == object.getClass() && (condition == null || condition.test((T) object))) {
                this.result = (T) object;
                synchronized (this) { this.notify(); }
                return true;
            }
            return false;
        }

    }

}