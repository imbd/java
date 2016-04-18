package ru.spbau.mit;

import java.util.*;

public class TorrentServer extends AbstractServer {
    public TorrentServer() {
        super(Constants.SERVER_PORT);
        List<FileInfo> filesList = Collections.synchronizedList(new ArrayList<>());

        //stores ids of files, seeded by given client
        Map<ClientInfo, Set<Integer>> clientSeededFiles = Collections.synchronizedMap(new HashMap<>());

        //stores TimerTask, which removes given client from clientSeededFiles
        Map<ClientInfo, TimerTask> toRemoveClientTasks = Collections.synchronizedMap(new HashMap<>());

        setHandlerFactory(new TrackerClientHandlerFactory(filesList, clientSeededFiles, toRemoveClientTasks));
    }

    public static void main(String[] args) {
        Server server = new TorrentServer();
        server.start();
    }
}
