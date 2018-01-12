package online.sniper.utils;

import android.app.Dialog;
import android.database.Cursor;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.channels.FileChannel;
import java.util.zip.ZipFile;

/**
 * 关闭相关的工具类
 *
 * @author wangpeihe
 */
public final class CloseUtils {

    private CloseUtils() {
        throw new AssertionError();
    }

    /**
     * 调用{@link Closeable}的close()方法
     */
    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 关闭{@link ZipFile}，因为有的机型未实现{@link Closeable}
     */
    public static void close(ZipFile file) {
        if (file != null) {
            try {
                file.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 关闭{@link HttpURLConnection}，它未实现{@link Closeable}
     */
    public static void close(HttpURLConnection http) {
        if (http != null) {
            http.disconnect();
        }
    }

    public final static void close(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    public final static void close(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public final static void close(OutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public final static void close(FileChannel channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public final static void close(Dialog dialog) {
        if (dialog != null) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
