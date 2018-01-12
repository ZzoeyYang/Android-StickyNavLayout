
package online.sniper.utils.log;

import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.ConnectException;

/**
 * ===============================================
 * DEVELOPER : RenYang <br/>
 * DATE : 2016/8/23 <br/>
 * DESCRIPTION :
 */
public class LogUtils {

    protected static boolean isDebug = true;
    private static boolean isInit = false;

    public static void b(String tag, String msg) {
        base(tag, msg, Type.D);
    }

    public static void b(String msg) {
        base(null, msg, Type.D);
    }

    public static void d(String tag, String msg) {
        base(tag, msg, Type.D);
    }

    public static void d(String msg) {
        base(null, msg, Type.D);
    }

    public static void v(String tag, String msg) {
        base(tag, msg, Type.D);
    }

    public static void v(String msg) {
        base(null, msg, Type.D);
    }

    public static void i(String tag, String msg) {
        base(tag, msg, Type.D);
    }

    public static void i(String msg) {
        base(null, msg, Type.D);
    }

    public static void e(String tag, String msg) {
        base(tag, msg, Type.E);
    }

    public static void e(String tag, Throwable e) {
        base(tag, throwableToString(e), Type.E);
    }

    public static void e(Throwable e) {
        base(null, throwableToString(e), Type.E);
    }

    public static void e(String msg) {
        base(null, msg, Type.E);
    }

    public static void f(String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            f(msg);
        } else {
            f(tag + "\n" + msg);
        }
    }

    public static void f(String msg) {
        base(FileLog.TAG_LOG, msg, Type.F);
    }

    public static void f(Throwable throwable) {
        base(FileLog.TAG_LOG, throwableToString(throwable), Type.F);
    }

    public static void j(String tag, String msg) {
        base(tag, msg, Type.J);
    }

    public static void j(String msg) {
        base(null, msg, Type.J);
    }

    public static void networkF(Throwable throwable) {
        base(FileLog.TAG_NETWORK, throwableToString(throwable), Type.F);
    }

    public static void networkF(String msg) {
        base(FileLog.TAG_NETWORK, msg, Type.F);
    }

    public static void crashF(Throwable throwable) {
        base(FileLog.TAG_CRASH, throwableToString(throwable), Type.F);
    }

    private static void base(String tag, String msg, Type type) {
        if (!isDebug && type != Type.F) {
            return;
        }
        if (!isInit) {
            Logger.init(LogUtils.class.getSimpleName()).methodCount(3).hideThreadInfo().methodOffset(2);
            isInit = true;
        }
        if (isDebug && TextUtils.isEmpty(tag)) {
            tag = generateTagName(new Throwable().getStackTrace());
        }

        switch (type) {
            case D:
                if (TextUtils.isEmpty(tag)) {
                    Logger.d(msg);
                } else {
                    Logger.t(tag).d(msg);
                }
                break;
            case E:
                if (TextUtils.isEmpty(tag)) {
                    Logger.e(msg);
                } else {
                    Logger.t(tag).e(msg);
                }
                break;
            case J:
                if (TextUtils.isEmpty(tag)) {
                    Logger.json(msg);
                } else {
                    Logger.t(tag).json(msg);
                }
                break;
            case F:
                if (TextUtils.isEmpty(tag)) {
                    Logger.d(msg);
                } else {
                    Logger.t(tag).d(msg);
                }
                FileLog.getInstance().info(tag, msg);
                break;
        }
    }

    private static String generateTagName(StackTraceElement[] sElements) {
        String className = sElements[2].getFileName();
        className = className.substring(0, className.length() - 5);
        String methodName = sElements[2].getMethodName();
        int lineNumber = sElements[2].getLineNumber();
        StringBuffer buffer = new StringBuffer();
        buffer.append("");
        buffer.append(className);
        buffer.append(".");
        buffer.append(methodName);
        buffer.append("():");
        buffer.append(lineNumber);
        buffer.append("");
        return buffer.toString();
    }

    private static String throwableToString(Throwable throwable) {
        if (throwable == null) {
            return "invalid throwable";
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(out);
        if (throwable instanceof ConnectException) {
            printStream.println("  " + throwable.getMessage());
            if (throwable.getCause() != null)
                printStream.println("  " + throwable.getCause().getMessage());
        } else if (throwable instanceof java.net.SocketException
                || throwable instanceof java.net.SocketTimeoutException
                || throwable instanceof java.net.HttpRetryException
                || throwable instanceof java.net.UnknownHostException) {
            if (throwable.getMessage() != null)
                printStream.print("  " + throwable.getMessage());
            else
                throwable.printStackTrace(printStream);
            printStream.println("");
        } else {
            throwable.printStackTrace(printStream);
            printStream.println("");
        }
        printStream.close();
        return out.toString();
    }

    enum Type {
        D, E, J, F
    }
}
