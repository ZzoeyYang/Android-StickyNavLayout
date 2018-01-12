package online.sniper.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public final class HandlerUtils {

    private HandlerUtils() {
    }

    public static void uiPost(Runnable action) {
        if (action == null) {
            return;
        }
        uiHandler().post(action);
    }

    public static void uiPostDelayed(Runnable action, long delayMillis) {
        if (action == null) {
            return;
        }
        uiHandler().postDelayed(action, delayMillis);
    }

    public static void uiRemoveCallbacks(Runnable action) {
        uiHandler().removeCallbacks(action);
    }

    public static void threadPost(Runnable action) {
        if (action == null) {
            return;
        }
        threadHandler().post(action);
    }

    public static void threadPostDelayed(Runnable action, long delayMillis) {
        if (action == null) {
            return;
        }
        threadHandler().postDelayed(action, delayMillis);
    }

    public static void threadRemoveCallbacks(Runnable action) {
        threadHandler().removeCallbacks(action);
    }

    private static Handler uiHandler() {
        return Holder.handler;
    }

    private interface Holder {
        Handler handler = new Handler(Looper.getMainLooper());
    }

    private static Handler sThreadHandler;

    private static synchronized Handler threadHandler() {
        if (sThreadHandler == null) {
            HandlerThread thread = new HandlerThread("HandlerUtils.sThreadHandler");
            thread.start();
            sThreadHandler = new Handler(thread.getLooper());
        }
        return sThreadHandler;
    }
}