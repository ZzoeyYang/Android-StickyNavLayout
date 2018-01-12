package online.sniper.utils;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * Created by zhangzhigang on 2016/12/20.
 */
public class RSAUtils {

    public static final String KEY_ALGORITHM = "RSA";
    public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    public static final String RSA_ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding";
    public static final String UTF_8 = "UTF-8";

    /**
     * 校验数字签名
     *
     * @param dataStr   加密数据
     * @param publicKey 公钥
     * @param signStr   数字签名
     */
    public static boolean verify(String dataStr, PublicKey publicKey, String signStr) throws Exception {
        byte[] signData = Base64.decode(signStr);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(dataStr.getBytes());
        return signature.verify(signData);
    }

    public static PublicKey loadPublicKey(InputStream in) throws Exception {
        try {
            return loadPublicKey(readKey(in));
        } catch (IOException e) {
            throw new Exception("公钥数据流读取错误");
        } catch (NullPointerException e) {
            throw new Exception("公钥输入流为空");
        }
    }

    private static PublicKey loadPublicKey(String publicKeyStr) throws Exception {
        try {
            byte[] buffer = Base64.decode(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("公钥非法");
        } catch (NullPointerException e) {
            throw new Exception("公钥数据为空");
        }
    }

    private static String readKey(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String readLine = null;
        StringBuilder sb = new StringBuilder();
        while ((readLine = br.readLine()) != null) {
            if (readLine.charAt(0) == '-') {
                continue;
            } else {
                sb.append(readLine);
                sb.append('\r');
            }
        }
        return sb.toString();
    }

    private static final int RSA_KEY_LEN = 128;//128bytes

    private static final String RSA_KEY_PREFIX_PATTERN = "-BEGIN";

    public static boolean decryptByPublicKey(byte[] keyInByte, byte[] source, byte[] sign) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(keyInByte);
        PublicKey pubKey = keyFactory.generatePublic(pubSpec);
        sig.initVerify(pubKey);
        sig.update(source);
        return sig.verify(sign);
    }

    /**
     * 使用公钥加密
     *
     * @param content
     * @param key
     * @return
     */
    public static String encryptByPublic(String content, String key) {
        try {
            PublicKey pubkey = getPublicKeyFromX509(KEY_ALGORITHM, key);
            Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS1_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, pubkey);

            byte plaintext[] = content.getBytes(UTF_8);

            //分段加密
            int splitLen = RSA_KEY_LEN - 11;//最长加密长度
            int x = plaintext.length / splitLen;
            int y = plaintext.length % splitLen;
            int z = 0;
            if (y != 0) {
                z = 1;
            }

            byte[] output = new byte[RSA_KEY_LEN * (x + z)];
            byte[] outputTemp = null;
            byte[] inputTemp = null;
            int doCount = 0;
            for (int i = 0; i < plaintext.length; i += splitLen) {
                doCount = (i + splitLen) > plaintext.length ? (plaintext.length - i) : splitLen;
                inputTemp = new byte[doCount];
                System.arraycopy(plaintext, i, inputTemp, 0, doCount);
                outputTemp = cipher.doFinal(inputTemp);
                System.arraycopy(outputTemp, 0, output, (i / splitLen) * RSA_KEY_LEN, outputTemp.length);
            }
//            return new String(android.util.Base64.encode(output, android.util.Base64.NO_PADDING | android.util.Base64.NO_WRAP));
            return new String(Base64.encode(output));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 使用公钥解密
     *
     * @param content
     * @param key
     * @return
     */
    public static String decryptByPublic(String content, String key) {
        try {
            PublicKey pubkey = getPublicKeyFromX509(KEY_ALGORITHM, key);
            Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS1_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, pubkey);
            InputStream ins = new ByteArrayInputStream(Base64.decode(content));
            ByteArrayOutputStream writer = new ByteArrayOutputStream();
            byte[] buf = new byte[RSA_KEY_LEN];
            int bufl;
            while ((bufl = ins.read(buf)) != -1) {
                byte[] block = null;
                if (buf.length == bufl) {
                    block = buf;
                } else {
                    block = new byte[bufl];
                    for (int i = 0; i < bufl; i++) {
                        block[i] = buf[i];
                    }
                }
                writer.write(cipher.doFinal(block));
            }
            return new String(writer.toByteArray(), UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 得到公钥
     *
     * @param algorithm
     * @param bysKey
     * @return
     */
    public static PublicKey getPublicKeyFromX509(String algorithm, String bysKey) throws Exception {
        //如果key带前后缀， 去掉第1行和最后2行
        if (bysKey != null && bysKey.contains(RSA_KEY_PREFIX_PATTERN)) {
            int firstLine = bysKey.indexOf("\n");
            int lastLine = bysKey.lastIndexOf("\n");
            bysKey = bysKey.substring(firstLine + 1, lastLine);
//            lastLine = bysKey.lastIndexOf("\n");
//            bysKey = bysKey.substring(0, lastLine);
        }
//        byte[] decodedKey = android.util.Base64.decode(bysKey, android.util.Base64.DEFAULT);
        byte[] decodedKey = Base64.decode(bysKey);
        X509EncodedKeySpec x509 = new X509EncodedKeySpec(decodedKey);

        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePublic(x509);
    }
}
