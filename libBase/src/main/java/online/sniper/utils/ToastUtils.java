package online.sniper.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

/**
 * Created by wangpeihe on 2016/5/27.
 */
public final class ToastUtils {

    private static final int DURATION = Toast.LENGTH_SHORT;
    private static Toast sToast;

    /**
     * 显示Toast消息
     *
     * @param context Context
     * @param msg     消息
     */
    public static void show(Context context, int msg) {
        show(context, context.getString(msg));
    }

    /**
     * 显示Toast消息
     *
     * @param context Context
     * @param msg     消息
     */
    public static void show(final Context context, final String msg) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    show(context, msg);
                }
            });
            return;
        }
        Toast toast = getToast(context);
        toast.setText(msg);
        toast.setDuration(DURATION);
        View view = toast.getView();
        if (view != null) {
            view.forceLayout();
            view.invalidate();
        }
        toast.show();
    }

    /**
     * 获取Toast对象
     *
     * @param context Context
     * @return Toast
     */
    private static Toast getToast(Context context) {
        if (sToast == null) {
            synchronized (ToastUtils.class) {
                if (sToast == null) {
                    sToast = Toast.makeText(context.getApplicationContext(), "", DURATION);
                }
            }
        }
        return sToast;
    }
}
