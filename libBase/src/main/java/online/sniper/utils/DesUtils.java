package online.sniper.utils;

import android.text.TextUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * DES 加密解密工具
 */
public class DesUtils {
    private static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";

    /**
     * 加密
     */
    public static String encrypt(String s, String key) {
        if (s == null || TextUtils.isEmpty(key)) {
            return s;
        }
        return encrypt(s, key.getBytes());
    }

    /**
     * 加密
     */
    public static String encrypt(String s, byte[] keyBytes) {
        if (s == null || keyBytes == null) {
            return s;
        }
        try {
            SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
            IvParameterSpec iv = new IvParameterSpec(keyBytes);
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] bytes = cipher.doFinal(s.getBytes());
            return Base64.encode(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解密
     */
    public static String decrypt(String s, String key) {
        if (TextUtils.isEmpty(s) || TextUtils.isEmpty(key)) {
            return s;
        }
        return decrypt(s, key.getBytes());
    }

    /**
     * 解密
     */
    public static String decrypt(String s, byte[] keyBytes) {
        if (TextUtils.isEmpty(s) || keyBytes == null) {
            return s;
        }
        try {
            byte[] bytes = Base64.decode(s);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
            IvParameterSpec iv = new IvParameterSpec(keyBytes);
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return new String(cipher.doFinal(bytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
