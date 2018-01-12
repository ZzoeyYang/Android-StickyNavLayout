package online.sniper.utils;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

/**
 * 输入法工具类
 */
public class InputMethodUtils {

    /**
     * 延迟显示输入法
     */
    public static void showDelayed(final Context context, final View focusView, long delayMillis) {
        if (context == null || focusView == null) {
            return;
        }
        focusView.postDelayed(new Runnable() {
            @Override
            public void run() {
                show(context, focusView);
            }
        }, delayMillis);
    }

    /**
     * 显示输入法
     */
    public static void show(Context context, View focusView) {
        try {
            FocusUtils.setFocusable(focusView);
            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(focusView, 0);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    /**
     * 延迟显示输入法
     */
    public static void hideDelayed(final Context context, final View focusView, long delayMillis) {
        if (context == null || focusView == null) {
            return;
        }
        focusView.postDelayed(new Runnable() {
            @Override
            public void run() {
                hide(context, focusView);
            }
        }, delayMillis);
    }

    /**
     * 隐藏输入发
     */
    public static void hide(Activity activity) {
        try {
            View focusView = activity.getWindow().peekDecorView();
            hide(activity, focusView);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏输入发
     */
    public static void hide(Context context, View focusView) {
        try {
            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用该方法；键盘若显示则隐藏; 隐藏则显示
     */
    public static void toggle(Context context) {
        try {
            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断InputMethod的当前状态
     */
    public static boolean isShow(Context context, View focusView) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            boolean bool = imm.isActive(focusView);
            List<InputMethodInfo> inputMethodList = imm.getEnabledInputMethodList();
            final int N = inputMethodList.size();
            for (int i = 0; i < N; i++) {
                InputMethodInfo imi = inputMethodList.get(i);
                if (imi.getId().equals(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD))) {
                    // imi contains the information about the keyboard you are using
                    break;
                }
            }
            return bool;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}