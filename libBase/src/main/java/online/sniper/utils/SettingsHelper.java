package online.sniper.utils;

import android.content.SharedPreferences;

/**
 * 设置帮助类
 * <p>
 * Created by wangpeihe on 2016/11/10.
 */
public class SettingsHelper {
    private SharedPreferences sp;

    public SettingsHelper(SharedPreferences sp) {
        this.sp = sp;
    }

    public String getString(String key, String defValue) {
        try {
            return sp.getString(key, defValue);
        } catch (Exception e) {
            return defValue;
        }
    }

    public void setString(String key, String value) {
        sp.edit().putString(key, value).commit();
    }

    public int getInt(String key, int defValue) {
        try {
            return sp.getInt(key, defValue);
        } catch (Exception e) {
            return defValue;
        }
    }

    public void setInt(String key, int value) {
        sp.edit().putInt(key, value).commit();
    }

    public long getLong(String key, long defValue) {
        try {
            return sp.getLong(key, defValue);
        } catch (Exception e) {
            return defValue;
        }
    }

    public void setLong(String key, long value) {
        sp.edit().putLong(key, value).commit();
    }

    public boolean getBoolean(String key, boolean defValue) {
        try {
            return sp.getBoolean(key, defValue);
        } catch (Exception e) {
            return defValue;
        }
    }

    public void setBoolean(String key, boolean value) {
        sp.edit().putBoolean(key, value).commit();
    }

    public float getFloat(String key, float defValue) {
        try {
            return sp.getFloat(key, defValue);
        } catch (Exception e) {
            return defValue;
        }
    }

    public void setFloat(String key, float value) {
        sp.edit().putFloat(key, value).commit();
    }

    public void remove(String key) {
        sp.edit().remove(key).commit();
    }
}
