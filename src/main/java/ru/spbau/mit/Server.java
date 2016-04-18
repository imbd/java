package ru.spbau.mit;

public interface Server {
    void start();
    void stop();
    void join() throws InterruptedException;
}
