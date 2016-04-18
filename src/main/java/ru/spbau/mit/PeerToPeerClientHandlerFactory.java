package ru.spbau.mit;

import java.net.Socket;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class PeerToPeerClientHandlerFactory implements HandlerFactory {
    private Map<Integer, Set<Integer>> availableFileParts;
    private Map<Integer, Path> filesPath;

    public PeerToPeerClientHandlerFactory(Map<Integer, Set<Integer>> availableFileParts,
                                          Map<Integer, Path> filesPath) {
        this.availableFileParts = availableFileParts;
        this.filesPath = filesPath;
    }

    @Override
    public Runnable getHandler(Socket socket) {
        return new PeerToPeerClientHandler(socket, availableFileParts, filesPath);
    }
}
