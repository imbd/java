package ru.spbau.mit;

import java.net.Socket;

public interface HandlerFactory {
    Runnable getHandler(Socket socket);
}