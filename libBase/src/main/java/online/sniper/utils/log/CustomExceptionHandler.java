package online.sniper.utils.log;

import android.content.Context;
import android.text.TextUtils;

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    public  Context sContext;

    private Thread.UncaughtExceptionHandler mDefaultHandler;

    public void attachToApplication(Context context, String logName, String appInfo, boolean isDebug) {
        sContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        FileLog.init(TextUtils.isEmpty(logName) ? "less" : logName, appInfo);
        LogUtils.isDebug = isDebug;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LogUtils.crashF(e);
        mDefaultHandler.uncaughtException(t, e);
    }
}
