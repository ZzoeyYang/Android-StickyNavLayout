package online.sniper.utils;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 焦点工具
 * <p>
 * Created by wangpeihe on 2017/11/9.
 */
public final class FocusUtils {
    private FocusUtils() {
    }

    /**
     * 设置焦点
     */
    public static void setFocusable(View view) {
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.requestFocusFromTouch();
    }

    /**
     * 点击其它区域{@link android.widget.EditText}失去焦点
     */
    public static void loseFocusAt(Activity activity) {
        if (activity == null) {
            return;
        }
        View decorView = activity.getWindow().getDecorView();
        View contentView = decorView.findViewById(android.R.id.content);
        if (contentView instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) contentView;
            int count = vg.getChildCount();
            if (count == 1) {
                loseFocusAt(vg.getChildAt(0));
            }
        }
    }

    /**
     * 点击其它区域{@link android.widget.EditText}失去焦点
     */
    public static void loseFocusAt(Fragment fragment) {
        if (fragment == null) {
            return;
        }
        loseFocusAt(fragment.getView());
    }

    /**
     * 点击其它区域{@link android.widget.EditText}失去焦点
     */
    public static void loseFocusAt(View view) {
        if (view == null) {
            return;
        }
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                setFocusable(v);
                return false;
            }
        });
    }
}
