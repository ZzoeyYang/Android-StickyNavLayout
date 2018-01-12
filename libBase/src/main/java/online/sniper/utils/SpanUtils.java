package online.sniper.utils;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SpanUtils {

    public static final String REGULAR_NUMBER = "([-+]?[0-9]+)(,[0-9]+)*(\\.[0-9]+)?(%)?";

    /**
     * 为字符串中的数字染颜色
     *
     * @param s     待处理的字符串
     * @param color 需要染的颜色
     * @return
     */
    public static SpannableString setDigitalColor(String s, int color) {
        if (s == null) {
            return null;
        }
        SpannableString span = new SpannableString(s);
        Pattern p = Pattern.compile(REGULAR_NUMBER);
        Matcher m = p.matcher(s);
        while (m.find()) {
            int start = m.start();
            int end = start + m.group().length();
            span.setSpan(new ForegroundColorSpan(color), start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    /**
     * 为字符串染颜色
     *
     * @param s     待处理的字符串
     * @param color 需要染的颜色
     * @return
     */
    public static SpannableString setColor(String s, int color) {
        if (s == null) {
            return null;
        }
        SpannableString span = new SpannableString(s);
        int start = 0;
        int end = s.length();
        span.setSpan(new ForegroundColorSpan(color), start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    public static SpannableString setColor(String s, int start, int end, int color) {
        if (s == null) {
            return null;
        }
        SpannableString span = new SpannableString(s);
        span.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }
}