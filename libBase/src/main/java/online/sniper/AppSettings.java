package online.sniper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import online.sniper.utils.SettingsHelper;

/**
 * Created by wangpeihe on 2016/11/2.
 */
public final class AppSettings {

    private static SettingsHelper sHelper;

    private AppSettings() {
    }

    public static void init(Context context) {
        if (sHelper != null) {
            return;
        }
        context = context.getApplicationContext();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sHelper = new SettingsHelper(sp);
    }

    public static String getString(String key, String defValue) {
        return sHelper.getString(key, defValue);
    }

    public static void setString(String key, String value) {
        sHelper.setString(key, value);
    }

    public static void setBoolean(String key, boolean value) {
        sHelper.setBoolean(key, value);
    }

    public static float getFloat(String key, float defValue) {
        return sHelper.getFloat(key, defValue);
    }

    public static int getInt(String key, int defValue) {
        return sHelper.getInt(key, defValue);
    }

    public static void setInt(String key, int value) {
        sHelper.setInt(key, value);
    }

    public static long getLong(String key, long defValue) {
        return sHelper.getLong(key, defValue);
    }

    public static void setLong(String key, long value) {
        sHelper.setLong(key, value);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return sHelper.getBoolean(key, defValue);
    }

    public static void setFloat(String key, float value) {
        sHelper.setFloat(key, value);
    }

    public static void remove(String key) {
        sHelper.remove(key);
    }
}
