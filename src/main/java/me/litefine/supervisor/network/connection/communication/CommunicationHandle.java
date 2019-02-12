package me.litefine.supervisor.network.connection.communication;

import io.netty.util.internal.ConcurrentSet;
import me.litefine.supervisor.network.connection.ClientConnection;
import me.litefine.supervisor.network.messages.AbstractMessage;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class CommunicationHandle {

    private final ClientConnection connection;
    private final FileReceiver fileReceiver = new FileReceiver();
    private final MessageReceiver messageReceiver = new MessageReceiver();

    private final Set<InboundObjectWaiter> objectWaiters = new ConcurrentSet<>();

    public CommunicationHandle(ClientConnection connection) {
        this.connection = connection;
    }

    public FileReceiver fileReceiver() {
        return fileReceiver;
    }

    public MessageReceiver messageReceiver() {
        return messageReceiver;
    }

    public void sendMessage(AbstractMessage message) {
        connection.getChannel().writeAndFlush(message);
    }

    public boolean checkForReleasedWaitings(Object object) {
        return objectWaiters.removeIf(mw -> mw.test(object));
    }

    public void notifyAllObjectWaiters() {
        objectWaiters.forEach(waiter -> {
            synchronized (waiter) {
                waiter.notify();
            }
        });
        objectWaiters.clear();
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