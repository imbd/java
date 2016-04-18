package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class PeerToPeerClientHandler implements Runnable {
    private Socket socket;
    private Map<Integer, Set<Integer>> availableFileParts;
    private Map<Integer, Path> filesPaths;

    public PeerToPeerClientHandler(Socket socket, Map<Integer, Set<Integer>> availableFileParts,
                                   Map<Integer, Path> filesPaths) {
        this.socket = socket;
        this.availableFileParts = availableFileParts;
        this.filesPaths = filesPaths;
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            DataOutputStream outputStream;
            DataInputStream inputStream;
            try {
                outputStream = new DataOutputStream(socket.getOutputStream());
                inputStream = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            int requestType;
            try {
                try {
                    requestType = inputStream.readInt();
                } catch (EOFException e) {
                    e.printStackTrace();
                    return;
                }
                switch (requestType) {
                    case Constants.STAT_REQUEST:
                        handleStat(inputStream, outputStream);
                        break;
                    case Constants.GET_REQUEST:
                        handleGet(inputStream, outputStream);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleStat(DataInputStream inputStream, DataOutputStream outputStream) throws IOException {
        int id = inputStream.readInt();
        if (!availableFileParts.containsKey(id)) {
            outputStream.writeInt(0);
        } else {
            Set<Integer> availableParts = availableFileParts.get(id);
            outputStream.writeInt(availableParts.size());
            for (Integer part : availableParts) {
                outputStream.writeInt(part);
            }
        }
        outputStream.flush();
    }

    private void handleGet(DataInputStream inputStream, DataOutputStream outputStream) throws IOException {
        int id = inputStream.readInt();
        int partNumber = inputStream.readInt();
        if (availableFileParts.containsKey(id) && availableFileParts.get(id).contains(partNumber)) {
            byte[] buffer = new byte[Constants.DATA_BLOCK_SIZE];
            DataInputStream fileInputStream = new DataInputStream(Files.newInputStream(filesPaths.get(id)));
            fileInputStream.skipBytes(partNumber * Constants.DATA_BLOCK_SIZE);
            try {
                fileInputStream.readFully(buffer);
            } catch (EOFException ignored) { }
            fileInputStream.close();
            outputStream.write(buffer);
        }
    }
}
