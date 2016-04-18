package ru.spbau.mit;

import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

public class TrackerClientHandlerFactory implements HandlerFactory {
    private List<FileInfo> filesList;
    private Map<ClientInfo, Set<Integer>> clientSeededFiles;
    private Map<ClientInfo, TimerTask> toRemoveClientTasks;

    TrackerClientHandlerFactory(List<FileInfo> filesList, Map<ClientInfo, Set<Integer>> clientSeededFiles,
                                Map<ClientInfo, TimerTask> toRemoveClientTasks) {
        this.filesList = filesList;
        this.clientSeededFiles = clientSeededFiles;
        this.toRemoveClientTasks = toRemoveClientTasks;
    }

    @Override
    public Runnable getHandler(Socket socket) {
        return new TrackerClientHandler(socket, filesList, clientSeededFiles, toRemoveClientTasks);
    }
}
