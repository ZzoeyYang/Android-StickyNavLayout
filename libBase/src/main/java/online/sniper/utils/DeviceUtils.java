
package online.sniper.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class DeviceUtils {

    public static final String SP_NAME = "device.utils";
    public static final String KEY_M2 = "key.m2";
    private static String sDeviceId;

    /**
     * 获得当前终端的UA(型号)
     *
     * @return
     */
    public static String getPhoneModel() {
        String model = android.os.Build.MODEL;
        if (TextUtils.isEmpty(model)) {
            return "UNKNOWN";
        } else {
            return model;
        }
    }

    public static String getDeviceId(Context cxt) {
        if (!TextUtils.isEmpty(sDeviceId)) {
            return sDeviceId;
        }
        sDeviceId = getDeviceIdInternal(cxt);
        return sDeviceId;
    }

    @NonNull
    private static String getDeviceIdInternal(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wm.getConnectionInfo();
            if (info != null) {
                deviceId = info.getMacAddress();
            }
        }
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = "";
        } else {
            deviceId = Md5Utils.encode(deviceId);
        }
        return deviceId;
    }

    @SuppressWarnings("deprecation")
    public static long getFreeSpaces(String path) {
        if (path == null) {
            // 网盘里默认的情况
            path = Environment.getExternalStorageDirectory().getPath();
        }

        File filePath = new File(path);
        filePath.mkdirs();
        if (!filePath.exists()) {
            return -1;
        }
        try {
            StatFs stat = new StatFs(path);
            long freeSize = ((long) stat.getBlockSize() * (long) stat.getAvailableBlocks());
            if (freeSize == 0 && stat.getBlockCount() == 0) {
                return -1;
            }
            return freeSize;
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface ni = en.nextElement();
                for (Enumeration<InetAddress> addresses = ni.getInetAddresses(); addresses.hasMoreElements(); ) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 目的
     * 为了便于收集真实客户端设备号，便于今后做活动推广的时候可以有针对性的做风控处理，需要客户端采用新的设备号算法；
     * <p>
     * 服务端可以对该设备号做签名验证，保证相对有效性，对所有请求记录设备号，后续根据需要，在接口处对设备号做适当限制；
     * <p>
     * 算法
     * 使用原唯一设备号作为基础值 bdno（32位）
     * nbdno: prefix + bdno + postfix
     * fullsign: md5(nbdno);
     * shortsign: 截取md5结果串第4-7位
     * 新设备号dno = bdno + shortsign （36位）
     * 说明：prefix、postfix和shortsign截取位数可能根据客户端版本号不同，服务端根据版本号分别计算
     * <p>
     * 示例
     * 原设备号 e824a5dfe858266fd3ee04f9d0e1b123
     * prefix "PM"
     * postfix "pm"
     * 截取 第4-7位
     * 新设备号 e824a5dfe858266fd3ee04f9d0e1b12393f6
     */
    public static String getNewDeviceId(Context context) {
        String m2 = getM2(context);
        final String prefix = "CC";
        final String suffix = "cc";
        String sign = Md5Utils.encode(prefix + m2 + suffix);
        String newDeviceId = m2 + sign.substring(3, 7);
        return newDeviceId;
    }

    /**
     * 获取设备ANDROID_ID
     */
    public static String getAndroidId(Context context) {
        try {
            ContentResolver resolver = context.getContentResolver();
            return android.provider.Settings.System.getString(resolver, android.provider.Settings.Secure.ANDROID_ID);
        } catch (Throwable e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取IMEI
     */
    public static String getIMEI(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) (context.getSystemService(Context.TELEPHONY_SERVICE));
            return tm == null ? "" : (tm.getDeviceId() == null ? "" : tm.getDeviceId());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取M2
     */
    public static String getM2(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String m2 = sp.getString(KEY_M2, "");
        if (!TextUtils.isEmpty(m2)) {
            return m2;
        }
        try {
            String imei = getIMEI(context);
            String androidId = getAndroidId(context);
            String serialNo = getDeviceSerial();
            m2 = Md5Utils.encode("" + imei + androidId + serialNo);
            sp.edit().putString(KEY_M2, m2).commit();
            return m2;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getDeviceSerial() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }
}
