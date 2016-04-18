package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FileInfo {
    private static final int P = 31;

    private int id;
    private String name;
    private long size;

    public FileInfo(String name, long size) {
        this(0, name, size);
    }

    public FileInfo(int id, String name, long size) {
        this.id = id;
        this.name = name;
        this.size = size;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FileInfo)) {
            return false;
        }
        FileInfo fileInfo = (FileInfo) obj;
        return id == fileInfo.id && name.equals(fileInfo.name) && size == fileInfo.size;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * P * P + id * P + (int) size;
    }

    public void write(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(id);
        outputStream.writeUTF(name);
        outputStream.writeLong(size);
    }

    public static FileInfo read(DataInputStream inputStream) throws IOException {
        int id = inputStream.readInt();
        String name = inputStream.readUTF();
        long size = inputStream.readLong();
        return new FileInfo(id, name, size);
    }

    public static FileInfo readWithoutId(DataInputStream inputStream) throws IOException {
        String name = inputStream.readUTF();
        long size = inputStream.readLong();
        return new FileInfo(name, size);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public long getSize() {
        return size;
    }

    public String getName() {
        return name;
    }
}
