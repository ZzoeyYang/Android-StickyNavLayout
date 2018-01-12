package online.sniper.utils;

import java.util.Locale;

public class MathUtils {
    public static double toDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NullPointerException|NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static float toFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch (NullPointerException|NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String formatMoney(double money) {
        return String.format(Locale.getDefault(), "%.2f", money);
    }
}