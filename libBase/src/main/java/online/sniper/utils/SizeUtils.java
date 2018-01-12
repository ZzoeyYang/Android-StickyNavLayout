package online.sniper.utils;

import java.util.Locale;

/**
 * Created by wangpeihe on 2016/12/8.
 */

public final class SizeUtils {
    private SizeUtils() {
    }

    public static final long SIZE_K = 1024;
    public static final long SIZE_M = SIZE_K * 1024;
    public static final long SIZE_G = SIZE_M * 1024;
    public static final long SIZE_T = SIZE_G * 1024;

    public static String format(long size) {
        if (size <= 0) {
            return "0B";
        } else if (size < SIZE_K) {
            return size + "B";
        } else if (size < SIZE_M) {
            return trim(size, SIZE_K) + "K";
        } else if (size < SIZE_G) {
            return trim(size, SIZE_M) + "M";
        } else if (size < SIZE_T) {
            return trim(size, SIZE_G) + "G";
        } else {
            return trim(size, SIZE_T) + "T";
        }
    }

    public static String formatWithB(long size) {
        if (size <= 0) {
            return "0B";
        } else if (size < SIZE_K) {
            return size + "B";
        } else if (size < SIZE_M) {
            return trim(size, SIZE_K) + "KB";
        } else if (size < SIZE_G) {
            return trim(size, SIZE_M) + "MB";
        } else if (size < SIZE_T) {
            return trim(size, SIZE_G) + "GB";
        } else {
            return trim(size, SIZE_T) + "TB";
        }
    }

    public static String formatNum(long size) {
        if (size <= 0) {
            return "0";
        } else if (size < SIZE_K) {
            return size + "";
        } else if (size < SIZE_M) {
            return trim(size, SIZE_K) + "";
        } else if (size < SIZE_G) {
            return trim(size, SIZE_M) + "";
        } else {
            return trim(size, SIZE_G) + "";
        }
    }

    public static double formatSize(long size) {
        if (size <= 0) {
            return 0;
        } else if (size < SIZE_K) {
            return size;
        } else if (size < SIZE_M) {
            return size / SIZE_K;
        } else if (size < SIZE_G) {
            return size / SIZE_M;
        } else {
            return size / SIZE_G;
        }
    }

    private static String trim(double size, double unit) {
        String result = String.format(Locale.getDefault(), "%.02f", size / unit);
        if (result.endsWith(".00")) {
            return result.substring(0, result.length() - 3);
        } else if (result.endsWith("0")) {
            return result.substring(0, result.length() - 1);
        } else {
            return result;
        }
    }

    /**
     * 将GB转换为Byte
     */
    public static String GB2Byte(String sizeGB) {
        return String.valueOf(Long.valueOf(sizeGB) * SIZE_G);
    }

}
