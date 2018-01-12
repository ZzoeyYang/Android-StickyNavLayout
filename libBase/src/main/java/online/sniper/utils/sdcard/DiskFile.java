package online.sniper.utils.sdcard;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 操作磁盘文件
 */
public final class DiskFile {

    private DiskFile() {
    }

    /**
     * 读数据块
     */
    public static byte[] readBlock(String filePath, long seek, int blockSize) throws IOException {
        byte[] bytes = new byte[blockSize];
        RandomAccessFile in = null;
        try {
            in = new RandomAccessFile(filePath, "r");
            in.seek(seek);
            in.readFully(bytes, 0, blockSize);
            return bytes;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }


    /**
     * 写数据块
     */
    public static void writeBlock(String filePath, long fileSize, long seek, byte[] bytes) throws IOException {
        writeBlock(filePath, fileSize, seek, bytes, 0, bytes.length);
    }

    /**
     * 写数据块
     */
    public static void writeBlock(String filePath, long fileSize, long seek, byte[] bytes, int offset, int length) throws IOException {
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        RandomAccessFile out = null;
        try {
            out = new RandomAccessFile(filePath, "rws");
            if (out.length() == 0) {
                out.setLength(fileSize);
            }
            if (out.length() != fileSize) {
                throw new IOException("File size changed.");
            }
            out.seek(seek);
            out.write(bytes, offset, length);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 删除文件
     */
    public static void delete(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

}
