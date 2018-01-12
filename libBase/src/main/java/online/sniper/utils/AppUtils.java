package online.sniper.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

/**
 * Created by wangpeihe on 2016/11/2.
 */

public final class AppUtils {

    private static String sVersionName = "";
    private static int sVersionCode = -1;

    private AppUtils() {
    }

    public static int getAppUid(Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            return ai.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getVersionName(Context context) {
        if (!TextUtils.isEmpty(sVersionName)) {
            return sVersionName;
        }
        try {
            sVersionName = getPackageInfo(context).versionName;
        } catch (Exception e) {
            sVersionName = "";
        }
        return sVersionName;
    }

    public static int getVersionCode(Context context) {
        if (sVersionCode >= 0) {
            return sVersionCode;
        }
        try {
            sVersionCode = getPackageInfo(context).versionCode;
        } catch (Exception e) {
            sVersionCode = 0;
        }
        return sVersionCode;
    }

    public static String getPackageName(Context context) {
        return context.getPackageName();
    }

    public static String getCurrentProcessName(Context context) {
        String currentProcessName = "";
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                currentProcessName = processInfo.processName;
                break;
            }
        }
        return currentProcessName;
    }

    public static PackageInfo getPackageInfo(Context context) {
        return getPackageInfo(context, context.getPackageName());
    }

    public static PackageInfo getPackageInfo(Context context, String packageName) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }


    /**
     * 获取MetaData数据
     *
     * @param context 上下文
     * @param key     键
     * @return 值，不存在则返回null
     */
    public static String getMetaData(Context context, String key) {
        try {
            PackageManager manager = context.getPackageManager();
            ApplicationInfo ai = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Object value = ai.metaData.get(key);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
