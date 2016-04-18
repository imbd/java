package ru.spbau.mit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class AbstractServer implements Server {
    private ServerSocket serverSocket;
    private short port;
    private ExecutorService taskExecutor;
    private HandlerFactory handlerFactory;

    public AbstractServer(short port) {
        this.port = port;
    }

    @Override
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        taskExecutor = Executors.newCachedThreadPool();
        taskExecutor.execute(() -> {
            while (true) {
                synchronized (this) {
                    if (serverSocket == null || serverSocket.isClosed()) {
                        break;
                    }
                }
                try {
                    Socket clientSocket = serverSocket.accept();
                    taskExecutor.execute(handlerFactory.getHandler(clientSocket));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void stop() {
        if (serverSocket == null) {
            return;
        }
        try {
            taskExecutor.shutdown();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverSocket = null;
    }

    @Override
    public void join() throws InterruptedException {
        taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    protected void setHandlerFactory(HandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
    }
}
