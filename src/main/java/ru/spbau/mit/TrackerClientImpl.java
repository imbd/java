package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TrackerClientImpl implements TrackerClient {
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    @Override
    public void connect(byte[] ip, int port) throws IOException {
        socket = new Socket(InetAddress.getByAddress(ip), port);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void disconnect() throws IOException {
        socket.close();
    }

    @Override
    public synchronized List<FileInfo> executeList() throws IOException {
        outputStream.writeInt(Constants.LIST_REQUEST);
        outputStream.flush();
        int size = inputStream.readInt();
        List<FileInfo> filesList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            filesList.add(FileInfo.read(inputStream));
        }
        return filesList;
    }

    @Override
    public synchronized int executeUpload(String path, long size) throws IOException {
        outputStream.writeInt(Constants.UPLOAD_REQUEST);
        outputStream.writeUTF(path);
        outputStream.writeLong(size);
        outputStream.flush();
        return inputStream.readInt();
    }

    @Override
    public synchronized List<ClientInfo> executeSources(int id) throws IOException {
        outputStream.writeInt(Constants.SOURCES_REQUEST);
        outputStream.writeInt(id);
        outputStream.flush();
        int size = inputStream.readInt();
        List<ClientInfo> clientsList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            clientsList.add(ClientInfo.read(inputStream));
        }
        return clientsList;
    }

    @Override
    public synchronized boolean executeUpdate(short port, List<Integer> seededFiles) throws IOException {
        outputStream.writeInt(Constants.UPDATE_REQUEST);
        outputStream.writeShort(port);
        outputStream.writeInt(seededFiles.size());
        for (int id : seededFiles) {
            outputStream.writeInt(id);
        }
        outputStream.flush();
        return inputStream.readBoolean();
    }
}
