package me.litefine.supervisor.console;

@FunctionalInterface
public interface CommandConsumer {

    void execute(String[] args);

}