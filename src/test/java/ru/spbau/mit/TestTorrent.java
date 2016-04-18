package ru.spbau.mit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestTorrent {
    private static final Path TEST_DIRECTORY = Paths.get("src", "test", "resources", "test");
    private static final Path DIR_PATH1 = TEST_DIRECTORY.resolve("dir1");
    private static final Path DIR_PATH2 = TEST_DIRECTORY.resolve("dir2");
    private static final String FILE_NAME1 = "file1.txt";
    private static final String FILE_NAME2 = "file2.txt";
    private static final Path FILE_PATH1 = DIR_PATH1.resolve(FILE_NAME1);
    private static final Path FILE_PATH2 = DIR_PATH2.resolve(FILE_NAME2);
    private static final long FILE_LENGTH1 = Constants.DATA_BLOCK_SIZE + 2;
    private static final long FILE_LENGTH2 = 50;
    private static final short PORT1 = 12345;
    private static final short PORT2 = 12346;
    private static final byte[] LOCALHOST = new byte[] {127, 0, 0, 1};
    private static final long DELAY = 1000;

    @Before
    public void createTestDirectory() throws IOException {
        Files.createDirectory(TEST_DIRECTORY);
    }

    @After
    public void deleteTestDirectory() throws IOException {
        FileUtils.deleteDirectory(TEST_DIRECTORY.toFile());
    }
    @Test
    public void testList() throws IOException {
        IdProvider.getInstance().reset();
        Server server = new TorrentServer();
        server.start();

        Client client1 = new TorrentClient(PORT1);
        client1.start(LOCALHOST);
        Files.createDirectory(DIR_PATH1);
        createFile(FILE_PATH1, FILE_LENGTH1);
        client1.upload(FILE_PATH1.toString());

        Client client2 = new TorrentClient(PORT2);
        client2.start(LOCALHOST);
        List<FileInfo> filesList = client2.getFilesList();
        assertEquals(Collections.singletonList(new FileInfo(0, FILE_NAME1, FILE_LENGTH1)), filesList);

        Files.createDirectory(DIR_PATH2);
        createFile(FILE_PATH2, FILE_LENGTH2);
        client2.upload(FILE_PATH2.toString());
        List<FileInfo> filesList2 = client1.getFilesList();
        assertEquals(Arrays.asList(
                new FileInfo(0, FILE_NAME1, FILE_LENGTH1),
                new FileInfo(1, FILE_NAME2, FILE_LENGTH2)
        ), filesList2);

        client1.stop();
        client2.stop();
        server.stop();
    }

    @Test
    public void testDownload() throws IOException, InterruptedException {
        IdProvider.getInstance().reset();
        Server server = new TorrentServer();
        server.start();

        Client client1 = new TorrentClient(PORT1);
        client1.start(LOCALHOST);
        Files.createDirectory(DIR_PATH1);
        createFile(FILE_PATH1, FILE_LENGTH1);
        client1.upload(FILE_PATH1.toString());

        Client client2 = new TorrentClient(PORT2);
        client2.start(LOCALHOST);
        Files.createDirectory(DIR_PATH2);
        Thread.sleep(DELAY);
        client2.download(0, DIR_PATH2);
        assertEquals(FILE_LENGTH1, DIR_PATH1.resolve(FILE_NAME1).toFile().length());

        client1.stop();
        client2.stop();
        server.stop();
    }


    private void createFile(Path filePath, long fileLength) throws IOException {
        RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "rw");
        file.setLength(fileLength);
        file.close();
    }
}
