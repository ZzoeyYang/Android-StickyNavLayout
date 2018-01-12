package online.sniper.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * I/O流处理工具
 *
 * @author wangpeihe
 */
public class StreamUtils {

    /**
     * 将输入流中的数据拷贝到输出流中，默认缓存大小未1024
     *
     * @param in  输入流
     * @param out 输出流
     * @throws IOException 读写异常
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        copy(in, out, 1024);
    }

    /**
     * 将输入流中的数据拷贝到输出流中
     *
     * @param in         输入流
     * @param out        输出流
     * @param bufferSize 缓存大小
     * @throws IOException 读写异常
     */
    public static void copy(InputStream in, OutputStream out, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int size = 0;
        while ((size = in.read(buffer)) > 0) {
            out.write(buffer, 0, size);
        }
        out.flush();
    }

    /**
     * 获取输入流中的字节数组
     *
     * @param in 输入流
     * @return 字节数组
     * @throws IOException 读写异常
     */
    public static byte[] getBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            copy(in, out);
            return out.toByteArray();
        } finally {
            CloseUtils.close(out);
        }
    }

    /**
     * 从输入流中获取字符串，默认字符集为UTF-8
     *
     * @param in 输入流
     * @return 字符串
     * @throws IOException 读写异常
     */
    public static String getString(InputStream in) throws IOException {
        return new String(getBytes(in), "UTF-8");
    }

    /**
     * 从输入流中获取字符串
     *
     * @param in      输入流
     * @param charset 字符集
     * @return 字符串
     * @throws IOException 读写异常
     */
    public static String getString(InputStream in, String charset) throws IOException {
        return new String(getBytes(in), charset);
    }

}
