
package online.sniper.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * UI的帮助类
 */
public class DisplayUtils {

    private static DisplayMetrics sDisplayMetrics = null;

    public static DisplayMetrics getDisplayMetrics(Context context) {
        if (sDisplayMetrics == null) {
            sDisplayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();
        }
        return sDisplayMetrics;
    }

    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     *
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        return (int) (pxValue / getDisplayMetrics(context).density + 0.5f);
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     * @param dipValue（DisplayMetrics类中属性density）
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        return (int) (dipValue * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @return
     */
    public static int px2sp(Context activity, float pxValue) {
        return (int) (pxValue / getDisplayMetrics(activity).scaledDensity + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        return (int) (spValue * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static int getTextWidth(Context context, String text, int size) {
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        float density = context.getResources().getDisplayMetrics().density;
        textPaint.density = density;
        textPaint.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, size, context.getResources()
                        .getDisplayMetrics()));
        int width = (int) textPaint.measureText(text);
        return width;
    }

    public static int getStatusBarHeight(Activity activity) {
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        return statusBarHeight;
    }

    public static float getScreenDensity(Context context) {
        final DisplayMetrics m = context.getResources().getDisplayMetrics();
        return m.density;
    }

    public static float getScreenDensityDpi(Context context) {
        final DisplayMetrics m = context.getResources().getDisplayMetrics();
        return m.densityDpi;
    }

    /**
     * 获取屏幕宽度
     *
     * @param context context
     * @return 屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        final DisplayMetrics m = context.getResources().getDisplayMetrics();

        Configuration configuration = context.getResources().getConfiguration(); //获取设置的配置信息
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return m.heightPixels;
        } else {
            return m.widthPixels;
        }
    }

    /**
     * 获取屏幕高度
     *
     * @param context context
     * @return 屏幕高度
     */
    public static int getScreenHeight(Context context) {
        final DisplayMetrics m = context.getResources().getDisplayMetrics();
        Configuration configuration = context.getResources().getConfiguration(); //获取设置的配置信息
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return m.widthPixels;
        } else {
            return m.heightPixels;
        }
    }

    /**
     * 获取屏幕大小
     *
     * @param context context
     * @return 返回屏幕大小，result[0] 为宽度，result[1]为高度
     */
    public static int[] getScreenSize(Context context) {
        int[] result = new int[2];
        final DisplayMetrics m = context.getResources().getDisplayMetrics();
        Configuration configuration = context.getResources().getConfiguration(); //获取设置的配置信息
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            result[0] = m.heightPixels;
            result[1] = m.widthPixels;
        } else {
            result[0] = m.widthPixels;
            result[1] = m.heightPixels;
        }
        return result;
    }

    public static int getBaseScreenHeight(Context context) {
        final DisplayMetrics m = context.getResources().getDisplayMetrics();

        Configuration configuration = context.getResources().getConfiguration(); //获取设置的配置信息
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return m.widthPixels;
        } else {
            return m.heightPixels;
        }
    }

    public static int getBaseScreenWidth(Context context) {
        final DisplayMetrics m = context.getResources().getDisplayMetrics();
        Configuration configuration = context.getResources().getConfiguration(); //获取设置的配置信息
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return m.heightPixels;
        } else {
            return m.widthPixels;
        }
    }

    public static float getDENSITY(Context context) {
        final DisplayMetrics m = context.getResources().getDisplayMetrics();

        return m.density;
    }

    public static int getDENSITYDPI(Context context) {
        final DisplayMetrics m = context.getResources().getDisplayMetrics();

        return m.densityDpi;
    }

    public static ColorStateList toColorStateList(@ColorInt int normalColor, @ColorInt int pressedColor,
                                                  @ColorInt int focusedColor, @ColorInt int unableColor) {
        int[] colors = new int[]{pressedColor, focusedColor, normalColor, focusedColor, unableColor, normalColor};
        int[][] states = new int[6][];
        states[0] = new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled};
        states[1] = new int[]{android.R.attr.state_enabled, android.R.attr.state_focused};
        states[2] = new int[]{android.R.attr.state_enabled};
        states[3] = new int[]{android.R.attr.state_focused};
        states[4] = new int[]{android.R.attr.state_window_focused};
        states[5] = new int[]{};
        return new ColorStateList(states, colors);
    }

    public static ColorStateList toColorStateList(@ColorInt int normalColor, @ColorInt int pressedColor) {
        return toColorStateList(normalColor, pressedColor, pressedColor, normalColor);
    }
}
