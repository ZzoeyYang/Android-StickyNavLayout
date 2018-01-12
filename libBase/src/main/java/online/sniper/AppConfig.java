package online.sniper;

import android.content.Context;
import android.os.Environment;

import java.io.File;

import online.sniper.utils.AppUtils;
import online.sniper.utils.DeviceUtils;

/**
 * SDK配置信息
 */
public class AppConfig {

    public static final String DEVICE_TYPE = "android";

    private static Context sContext;
    private static String sAppName;
    private static String sChannel = "";
    private static String sBuildNumber = "0000";
    private static int sAppUid = 0;
    private static String sVersionName = "";
    private static int sVersionCode = 0;
    private static String sDeviceId = "";
    private static String sPhoneModel = "";
    private static String sM2 = "";
    private static String sNewDeviceId = "";
    private static File sRootDir;

    public static Context getContext() {
        checkInited();
        return sContext;
    }

    public static final void init(Context context, String appName, String channel, String buildNumber) {
        sContext = context;
        sAppName = appName;
        sChannel = channel;
        sBuildNumber = buildNumber;
        sRootDir = new File(Environment.getExternalStorageDirectory(), sAppName);

        // 设备信息
        AppSettings.init(context);
        sAppUid = AppUtils.getAppUid(context);
        sVersionName = AppUtils.getVersionName(context);
        sVersionCode = AppUtils.getVersionCode(context);
        sDeviceId = DeviceUtils.getDeviceId(context);
        sPhoneModel = DeviceUtils.getPhoneModel();
        sM2 = DeviceUtils.getM2(context);
        sNewDeviceId = DeviceUtils.getNewDeviceId(context);
    }

    public static String getAppName() {
        checkInited();
        return sAppName;
    }

    public static File getRootDir() {
        checkInited();
        return sRootDir;
    }

    public static String getChannel() {
        checkInited();
        return sChannel;
    }

    public static String getBuildNumber() {
        checkInited();
        return sBuildNumber;
    }

    public static int getAppUid() {
        checkInited();
        return sAppUid;
    }

    public static int getVersionCode() {
        checkInited();
        return sVersionCode;
    }

    public static String getVersionName() {
        checkInited();
        return sVersionName;
    }

    public static String getDeviceId() {
        checkInited();
        return sDeviceId;
    }

    public static String getNewDeviceId() {
        checkInited();
        return sNewDeviceId;
    }

    public static String getPhoneModel() {
        checkInited();
        return sPhoneModel;
    }

    public static String getM2() {
        checkInited();
        return sM2;
    }

    private static void checkInited() {
        if (sContext == null) {
            throw new RuntimeException("Must call SdkConfig.init() before.");
        }
    }
}
