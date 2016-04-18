package ru.spbau.mit;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PeerToPeerConnection extends AbstractServer implements PeerToPeerClient {
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Map<Integer, Set<Integer>> availableFileParts; //stores numbers of available parts of given id
    private Map<Integer, Path> filesPaths; //stores path of file with given id

    public PeerToPeerConnection(short port) {
        super(port);
        availableFileParts = new HashMap<>();
        filesPaths = new HashMap<>();
        setHandlerFactory(new PeerToPeerClientHandlerFactory(availableFileParts, filesPaths));
    }

    @Override
    public void connect(byte[] ip, short port) throws IOException {
        socket = new Socket(InetAddress.getByAddress(ip), port);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void disconnect() throws IOException {
        if (socket != null) {
            socket.close();
        }
    }

    @Override
    public List<Integer> executeStat(int id) throws IOException {
        outputStream.writeInt(Constants.STAT_REQUEST);
        outputStream.writeInt(id);
        outputStream.flush();
        int size = inputStream.readInt();
        List<Integer> partsList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            partsList.add(inputStream.readInt());
        }
        return partsList;
    }

    @Override
    public byte[] executeGet(int id, int part) throws IOException {
        outputStream.writeInt(Constants.GET_REQUEST);
        outputStream.writeInt(id);
        outputStream.writeInt(part);
        outputStream.flush();
        byte[] buffer = new byte[Constants.DATA_BLOCK_SIZE];
        inputStream.readFully(buffer);
        return buffer;
    }

    public List<Integer> getAvailableFileIds() {
        return new ArrayList<>(availableFileParts.keySet());
    }

    public void addFile(int id, Path path) {
        long size = path.toFile().length();
        filesPaths.put(id, path);
        Set<Integer> fileParts = new HashSet<>();
        for (int i = 0; i < (size + Constants.DATA_BLOCK_SIZE - 1) / Constants.DATA_BLOCK_SIZE; i++) {
            fileParts.add(i);
        }
        availableFileParts.put(id, fileParts);
    }

    public void addFilePart(int id, int part, Path path) {
        if (!filesPaths.containsKey(id)) {
            filesPaths.put(id, path);
            availableFileParts.put(id, new HashSet<>());
        }
        availableFileParts.get(id).add(part);
    }

    public void save() throws IOException {
        File file = Constants.TO_SAVE_PATH.toFile();
        if (!file.exists()) {
            Files.createFile(Constants.TO_SAVE_PATH);
            file = Constants.TO_SAVE_PATH.toFile();
        }
        DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file));

        outputStream.writeInt(availableFileParts.size());
        for (Map.Entry<Integer, Set<Integer>> entry : availableFileParts.entrySet()) {
            outputStream.writeInt(entry.getKey());
            Set<Integer> availableParts = entry.getValue();
            outputStream.writeInt(availableParts.size());
            for (int part : availableParts) {
                outputStream.writeInt(part);
            }
        }

        outputStream.writeInt(filesPaths.size());
        for (Map.Entry<Integer, Path> filePath : filesPaths.entrySet()) {
            outputStream.writeInt(filePath.getKey());
            outputStream.writeUTF(filePath.getValue().toString());
        }
        outputStream.close();
    }

    public void restore() throws IOException {
        File file = Constants.TO_SAVE_PATH.toFile();
        DataInputStream inputStream = new DataInputStream(new FileInputStream(file));

        availableFileParts = new HashMap<>();
        filesPaths = new HashMap<>();

        int availableFilePartsSize = inputStream.readInt();
        for (int i = 0; i < availableFilePartsSize; i++) {
            int id = inputStream.readInt();
            int setSize = inputStream.readInt();
            Set<Integer> availableParts = new HashSet<>();
            for (int j = 0; j < setSize; j++) {
                availableParts.add(inputStream.readInt());
            }
            availableFileParts.put(id, availableParts);
        }

        int filesPathsSize = inputStream.readInt();
        for (int i = 0; i < filesPathsSize; i++) {
            int id = inputStream.readInt();
            String path = inputStream.readUTF();
            filesPaths.put(id, Paths.get(path));
        }
    }
}
