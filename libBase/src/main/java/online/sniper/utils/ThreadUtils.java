package online.sniper.utils;

import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程工具
 * </p>
 * Created by wangpeihe on 2016/11/4.
 */
public class ThreadUtils {

    private final static ExecutorService sThreadPool = Executors.newCachedThreadPool();

    /**
     * 当前线程
     */
    public static Thread currentThread() {
        return Thread.currentThread();
    }

    /**
     * 当前进程ID
     */
    public static long currentThreadId() {
        return Thread.currentThread().getId();
    }

    /**
     * 当前进程名称
     */
    public static String currentThreadName() {
        return Thread.currentThread().getName();
    }

    /**
     * 判断是否为UI线程
     */
    public static boolean isUiThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /**
     * 运行于UI线程
     */
    public static void runOnUiThread(Runnable action) {
        if (action == null) {
            return;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run();
        } else {
            HandlerUtils.uiPost(action);
        }
    }

    /**
     * 运行于后台线程
     */
    public static void runOnBackgroundThread(Runnable action) {
        if (action == null) {
            return;
        }
        if (Looper.myLooper() != Looper.getMainLooper()) {
            action.run();
        } else {
            sThreadPool.submit(action);
        }
    }

    /**
     * 运行于异步线程
     */
    public static void runOnAsyncThread(Runnable action) {
        if (action == null) {
            return;
        }
        sThreadPool.submit(action);
    }

    /**
     * 运行于当前线程
     */
    public static void runOnPostThread(Runnable action) {
        if (action == null) {
            return;
        }
        action.run();
    }

    public static void backgroundToUi(final Runnable background, final Runnable ui) {
        runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                background.run();
                runOnUiThread(ui);
            }
        });
    }
}
