package online.sniper.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Locale;

import online.sniper.AppConfig;

public class NetworkUtils {
    public static final String TYPE_DISCONNECTED = "disconnected";
    public static final String TYPE_WIFI = "wifi";
    public static final String TYPE_MOBILE = "mobile";
    public static final String TYPE_UNKNOWN = "unknown";
    public static final String TYPE_2G = "2g";
    public static final String TYPE_3G = "3g";
    public static final String TYPE_4G = "4g";

    public static String getNetworkType() {
        return getNetworkType(getContext());
    }

    public static String getDetailNetworkType() {
        return getDetailNetworkType(getContext());
    }

    public static boolean isNetworkAvailable() {
        return isNetworkAvailable(getContext());
    }

    public static boolean isWifiNetwork() {
        return isWifiNetwork(getContext());
    }

    public static boolean isMobileNetwork() {
        return isMobileNetwork(getContext());
    }

    public static String getIpAddress() {
        return getIpAddress(getContext());
    }

    private static Context getContext() {
        return AppConfig.getContext();
    }

    /**
     * 判断当前网络是否可用
     */
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isAvailable() && info.isConnected();
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * 获取网络类型，不区分移动网络的类型
     */
    public static String getNetworkType(Context context) {
        if (context == null) {
            return TYPE_DISCONNECTED;
        }
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni == null || !ni.isAvailable() || !ni.isConnected()) {
                return TYPE_DISCONNECTED;
            }
            switch (ni.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    return TYPE_WIFI;
                case ConnectivityManager.TYPE_MOBILE:
                    return TYPE_MOBILE;
                default:
                    return TYPE_UNKNOWN;
            }
        } catch (Throwable e) {
            return TYPE_DISCONNECTED;
        }
    }

    /**
     * 获取网络类型，区分移动网络的类型(2G|3G|4G)
     */
    public static String getDetailNetworkType(Context context) {
        if (context == null) {
            return TYPE_DISCONNECTED;
        }
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni == null || !ni.isAvailable() || !ni.isConnected()) {
                return TYPE_DISCONNECTED;
            }
            switch (ni.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    return TYPE_WIFI;
                case ConnectivityManager.TYPE_MOBILE:
                    String subtype = ni.getSubtypeName().toLowerCase(Locale.getDefault());
                    if (subtype.contains("gsm") || subtype.contains("gprs") || subtype.contains("edge")) {
                        return TYPE_2G;
                    } else if (subtype.contains("cdma") || subtype.contains("umts") || subtype.contains("hsdpa") || subtype.contains("hspa+") || subtype.contains("hspa")) {
                        return TYPE_3G;
                    } else if (subtype.contains("lte") || subtype.contains("umb")) {
                        return TYPE_4G;
                    } else {
                        return TYPE_MOBILE;
                    }
                default:
                    return TYPE_UNKNOWN;
            }
        } catch (Throwable e) {
            return TYPE_DISCONNECTED;
        }
    }

    /**
     * 判断当前网络是否为WiFi网络
     */
    public static boolean isWifiNetwork(Context context) {
        return TYPE_WIFI.equals(getNetworkType(context));
    }

    /**
     * 判断当前网络是否为移动网络
     */
    public static boolean isMobileNetwork(Context context) {
        final String type = getNetworkType(context);
        return TYPE_MOBILE.equals(type);
    }

    /**
     * 获取当前IP地址
     */
    public static String getIpAddress(Context context) {
        String type = getNetworkType(context);
        if (TYPE_WIFI.equals(type)) {
            return getIpAddressWifi(context);
        } else if (TYPE_MOBILE.equals(type)) {
            return getIpAddressMobile();
        }
        return null;
    }

    /**
     * 获取WiFi下的IP地址
     */
    public static String getIpAddressWifi(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取移动网络下的IP地址
     */
    public static String getIpAddressMobile() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}