package online.sniper.utils;

import java.security.MessageDigest;

/**
 * MD5加密工具
 *
 * @author 王培鹤
 */
public final class Md5Utils {

    private Md5Utils() {
        throw new AssertionError();
    }

    /**
     * 对字符串生成MD5编码
     */
    public static String encode(String text) {
        try {
            return encode(text.getBytes());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 对字节数组生成MD5编码
     */
    public static String encode(byte[] bytes) {
        final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            md.update(bytes);
            // 获得密文
            byte[] out = md.digest();
            // 把密文转换成十六进制的字符串形式
            int length = out.length;
            char result[] = new char[length * 2];
            int k = 0;
            for (int i = 0; i < length; i++) {
                byte b = out[i];
                result[k++] = hexDigits[b >>> 4 & 0xF];
                result[k++] = hexDigits[b & 0xF];
            }
            return new String(result);
        } catch (Exception e) {
            return null;
        }
    }

}
