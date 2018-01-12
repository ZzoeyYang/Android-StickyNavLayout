package online.sniper.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 深度克隆工具，原理为序列化与反序列化
 * <p>
 * Created by wangpeihe on 2017/1/10.
 */
public final class CloneUtils {

    private CloneUtils() {
    }

    public static <T extends Serializable> T clone(T src) {
        if (src == null) {
            return null;
        }
        ByteArrayOutputStream byteOut = null;
        ObjectOutputStream objOut = null;
        ByteArrayInputStream byteIn = null;
        ObjectInputStream objIn = null;
        try {
            byteOut = new ByteArrayOutputStream();
            objOut = new ObjectOutputStream(byteOut);
            objOut.writeObject(src);
            byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            objIn = new ObjectInputStream(byteIn);
            return (T) objIn.readObject();
        } catch (IOException e) {
            throw new RuntimeException("Clone Object failed in IO.", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found.", e);
        } finally {
            CloseUtils.close(byteOut);
            CloseUtils.close(objOut);
            CloseUtils.close(byteIn);
            CloseUtils.close(objIn);
        }
    }

}
