package ru.spbau.mit;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TorrentClient implements Client {
    private static final String START = "start";
    private static final String STOP = "stop";
    private static final String LIST = "list";
    private static final String DOWNLOAD = "download";
    private static final String UPLOAD = "upload";
    private static final String QUIT = "quit";
    private static final String WRONG_FORMAT_ERROR = "Wrong request format! Try one more time!";
    private static final String WRONG_PATH_ERROR = "Wrong file path!";
    private static final String REQUEST_OK_STATUS = "Handled: ";
    private static final String NAME = "Name: ";
    private static final String SIZE = "Size: ";
    private static final String ID = "Id: ";
    private static final byte[] LOCALHOST = new byte[] {127, 0, 0, 1};

    private TrackerClient trackerClient = new TrackerClientImpl();
    private PeerToPeerConnection peerToPeerConnection;
    private Timer updateTimer = new Timer();
    private TimerTask updateTask;
    private short port;

    public TorrentClient(short port) {
        this.port = port;
        peerToPeerConnection = new PeerToPeerConnection(port);
    }

    @Override
    public void start(byte[] ip) throws IOException {
        trackerClient.connect(ip, Constants.SERVER_PORT);
        peerToPeerConnection.start();
        updateTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    trackerClient.executeUpdate(port, peerToPeerConnection.getAvailableFileIds());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        updateTimer.schedule(updateTask, 0, Constants.UPDATE_REQUEST_DELAY);
    }

    @Override
    public void stop() throws IOException {
        trackerClient.disconnect();
        peerToPeerConnection.stop();
        peerToPeerConnection.disconnect();
        updateTask.cancel();
    }

    @Override
    public List<FileInfo> getFilesList() throws IOException {
        return trackerClient.executeList();
    }

    @Override
    public void download(int fileId, Path path) throws IOException {
        List<ClientInfo> clientsList = trackerClient.executeSources(fileId);
        List<FileInfo> filesList = trackerClient.executeList();
        FileInfo newFileInfo = null;
        for (FileInfo fileInfo : filesList) {
            if (fileInfo.getId() == fileId) {
                newFileInfo = fileInfo;
                break;
            }
        }
        assert newFileInfo != null;
        Path filePath = path.resolve(newFileInfo.getName());
        File file = filePath.toFile();
        RandomAccessFile newFile = new RandomAccessFile(file, "rw");
        long fileSize = newFileInfo.getSize();
        newFile.setLength(fileSize);

        int partNumber = (int) ((fileSize + Constants.DATA_BLOCK_SIZE - 1) / Constants.DATA_BLOCK_SIZE);
        Set<Integer> availableParts = new HashSet<>();
        while (availableParts.size() != partNumber) {
            for (ClientInfo clientInfo : clientsList) {
                peerToPeerConnection.connect(clientInfo.getIp(), clientInfo.getPort());
                List<Integer> fileParts = peerToPeerConnection.executeStat(fileId);
                for (int part : fileParts) {
                    if (!availableParts.contains(part)) {
                        newFile.seek(part * Constants.DATA_BLOCK_SIZE);
                        int partSize = Constants.DATA_BLOCK_SIZE;
                        if (part == partNumber - 1) {
                            partSize = (int) (fileSize % Constants.DATA_BLOCK_SIZE);
                        }
                        byte[] buffer;
                        try {
                            buffer = peerToPeerConnection.executeGet(fileId, part);
                        } catch (IOException e) {
                            continue;
                        }
                        newFile.write(buffer, 0, partSize);
                        availableParts.add(part);
                        peerToPeerConnection.addFilePart(fileId, part, filePath);
                        trackerClient.executeUpdate(port, peerToPeerConnection.getAvailableFileIds());
                    }
                }

            }
        }
        newFile.close();
    }

    @Override
    public void upload(String path) throws IOException {
        Path p = Paths.get(path);
        File file = p.toFile();
        if (!file.exists() || file.isDirectory()) {
            throw new NoSuchFileException(path);
        }
        int id = trackerClient.executeUpload(p.getFileName().toString(), file.length());
        peerToPeerConnection.addFile(id, p);
        trackerClient.executeUpdate(port, peerToPeerConnection.getAvailableFileIds());
    }

    @Override
    public void save() throws IOException {
        peerToPeerConnection.save();
    }

    @Override
    public void restore() throws IOException {
        peerToPeerConnection.restore();
    }

    public static void main(String[] args) {
        Client client = new TorrentClient(Short.valueOf(args[0]));
        if (Constants.TO_SAVE_PATH.toFile().exists()) {
            try {
                client.restore();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            List<String> arguments = Arrays.asList(line.split(" "));
            if (arguments.size() == 0) {
                continue;
            }
            switch (arguments.get(0)) {
                case START:
                    handleStart(client);
                    break;
                case STOP:
                    handleStop(client);
                    break;
                case LIST:
                    handleList(client);
                    break;
                case DOWNLOAD:
                    handleDownload(client, arguments.subList(1, arguments.size()));
                    break;
                case UPLOAD:
                    handleUpload(client, arguments.subList(1, arguments.size()));
                    break;
                case QUIT:
                    handleQuit(client);
                    return;
                default:
                    System.out.println(WRONG_FORMAT_ERROR);
            }
        }
    }

    private static void handleStart(Client client) {
        try {
            client.start(LOCALHOST);
            System.out.println(REQUEST_OK_STATUS + START);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void handleStop(Client client) {
        try {
            client.stop();
            System.out.println(REQUEST_OK_STATUS + STOP);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void handleList(Client client) {
        try {
            List<FileInfo> filesList = client.getFilesList();
            for (FileInfo fileInfo : filesList) {
                System.out.println(NAME + fileInfo.getName() + ", "
                        + SIZE + fileInfo.getSize() + ", " + ID + fileInfo.getId());
            }
            System.out.println(REQUEST_OK_STATUS + LIST);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void handleDownload(Client client, List<String> arguments) {
        if (arguments.size() != 2) {
            System.out.println(WRONG_FORMAT_ERROR);
            return;
        }
        try {
            client.download(Integer.valueOf(arguments.get(0)), Paths.get(arguments.get(1)));
            System.out.println(REQUEST_OK_STATUS + DOWNLOAD);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void handleUpload(Client client, List<String> arguments) {
        if (arguments.size() != 1) {
            System.out.println(WRONG_FORMAT_ERROR);
            return;
        }
        try {
            client.upload(arguments.get(0));
            System.out.println(REQUEST_OK_STATUS + UPLOAD);
        } catch (NoSuchFileException e) {
            System.out.println(WRONG_PATH_ERROR);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void handleQuit(Client client) {
        try {
            client.save();
            System.out.println(REQUEST_OK_STATUS + QUIT);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
