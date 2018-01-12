package online.sniper.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import online.sniper.utils.log.LogUtils;

public class DateTimeUtils {
    /**
     * 静态字段实例化后无法与系统时区自动同步，请谨慎使用
     * 建议出问题的模块改用
     * new SimpleDateFormat(Util.DATE_FORMAT_1.toPattern()).format(...)
     * 替代直接使用format(...)方法
     */
    public final static SimpleDateFormat DATE_FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    public final static SimpleDateFormat DATE_FORMAT_2 = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
    public final static SimpleDateFormat DATE_FORMAT_3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    public final static SimpleDateFormat DATE_FORMAT_4 = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SS", Locale.getDefault());
    public final static SimpleDateFormat DATE_FORMAT_5 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public final static SimpleDateFormat DATE_FORMAT_6 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault());
    public final static SimpleDateFormat DATE_FORMAT_7 = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
    public final static SimpleDateFormat DATE_FORMAT_8 = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
    public final static SimpleDateFormat DATE_FORMAT_9 = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
    public final static SimpleDateFormat DATE_FORMAT_10 = new SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault());
    public final static SimpleDateFormat DATE_FORMAT_11 = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    public final static SimpleDateFormat DATE_FORMAT_12 = new SimpleDateFormat("yy-MM-dd", Locale.getDefault());
    public final static SimpleDateFormat DATE_FORMAT_13 = new SimpleDateFormat("MM月dd日", Locale.getDefault());

    public final static SimpleDateFormat TIME_FORMAT_1 = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
    public final static SimpleDateFormat TIME_FORMAT_2 = new SimpleDateFormat("HHmm", Locale.getDefault());
    public final static SimpleDateFormat TIME_FORMAT_3 = new SimpleDateFormat("HH:mm", Locale.getDefault());
    public final static SimpleDateFormat TIME_FORMAT_4 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    /**
     * 获取当前时间，单位为毫秒
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    public static String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(currentTimeMillis());

        return formatter.format(curDate);
    }

    public static String convertDate(String currentData, SimpleDateFormat format1, SimpleDateFormat format2) {
        Date curDate;
        try {
            LogUtils.d("DateTimeUtils", currentData);
            curDate = format1.parse(currentData);
        } catch (ParseException e) {
            curDate = Calendar.getInstance().getTime();
        }
        return format2.format(curDate);
    }

    /**
     * 获取当前时间,并按指定模式(例如：yyyy-MM-dd HH:mm:ss)显示
     *
     * @param pattern 模式
     */
    public static String getCurrentTimeByFormat(String pattern) {
        String ret;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            Date curDate = new Date(currentTimeMillis());
            ret = formatter.format(curDate);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return ret;
    }

    public static String getYearPart(Long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        Date curDate = new Date(time);
        return formatter.format(curDate);
    }

    public static String getYearMonthPart(Long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        Date curDate = new Date(time);
        return formatter.format(curDate);
    }

    public static String getMonthDayPart(Long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd");
        Date curDate = new Date(time);
        return formatter.format(curDate);
    }

    /**
     * 获取当前hours
     */
    public static int getCurrentHours() {
        int hours = -1;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("HH");
            Date curDate = new Date(currentTimeMillis());
            String h = formatter.format(curDate);
            if (h != null && !h.equals("")) {
                hours = Integer.parseInt(h);
                if (hours < 0 || hours > 24) {
                    hours = -1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            hours = -1;
        }
        return hours;
    }

    public static String convertLongToDataTime(long longTime) {
        String ret = "";
        try {
            Date date = new Date(longTime);
            ret = DateTimeUtils.DATE_FORMAT_1.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return ret;
    }

    /**
     * 将long型的时间值,按指定模式(例如：yyyy-MM-dd HH:mm:ss)显示
     *
     * @param longTime 时间值
     * @param pattern  模式
     */
    public static String convertLongToDataTime(long longTime, String pattern) {
        String ret = "";
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            Date date = new Date(longTime);
            ret = format.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return ret;
    }

    /**
     * 获取long型的时间长度
     *
     * @param duration
     * @return
     */
    public static String getTimeDuration(long duration) {
        String ret = "";
        long HOUR_STAMP = 1 * 60 * 60 * 1000, total_second = 0;
        int minutes = 0, second = 0;
        if (duration > 0) {
            total_second = duration / 1000;
            minutes = (int) total_second / 60;
            minutes = minutes % 60;
            if (minutes >= 10) {
                ret += String.valueOf(minutes);
            } else {
                ret += "0" + String.valueOf(minutes);
            }
            ret += ":";
            second = (int) total_second % 60;
            if (second >= 10) {
                ret += String.valueOf(second);
            } else {
                ret += "0" + String.valueOf(second);
            }
            if (duration >= HOUR_STAMP) {
                ret = String.valueOf(duration / HOUR_STAMP) + ":" + ret;
            }
        } else {
            ret = "00:00";
        }
        return ret;
    }

    public static String formatDate(String fileTime) {
        Long time = (Long.parseLong(fileTime));
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(time));
        return date;
    }

    public static String getToday() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(currentTimeMillis());
        return format.format(date);
    }

    public static String getYesterday() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(currentTimeMillis() - 1000 * 60 * 60 * 24);
        return format.format(date);
    }

    public static int getCurentDataIndex(String dataStr) {
        try {
            Date date = DATE_FORMAT_3.parse(dataStr);
            int hour = date.getHours();
            int minutes = date.getMinutes();
            return (hour - 9) * 60 + minutes;
        } catch (Exception e) {
            return 1;
        }
    }
}