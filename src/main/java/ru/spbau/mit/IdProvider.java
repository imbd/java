package ru.spbau.mit;

public final class IdProvider {
    private static IdProvider instance;
    private int currentId = 0;

    private IdProvider() {}

    public static synchronized IdProvider getInstance() {
        if (instance == null) {
            instance = new IdProvider();
        }
        return instance;
    }

    public synchronized int getNextId() {
        return currentId++;
    }

    public synchronized void reset() {
        currentId = 0;
    }
}
