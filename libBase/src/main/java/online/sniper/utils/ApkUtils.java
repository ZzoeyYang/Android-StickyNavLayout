package online.sniper.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.util.DisplayMetrics;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 签名工具
 */
public final class ApkUtils {

    private ApkUtils() {
        throw new AssertionError();
    }

    /**
     * 使用反射机制获取APK的签名
     */
    public static Signature[] getSignature(Context context, String apkPath) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES
                    | PackageManager.GET_SIGNATURES);
            if (pi == null) {
                return null;
            }
            // 获取插件名称比较复杂
            ApplicationInfo app = pi.applicationInfo;
            app.sourceDir = apkPath;
            app.publicSourceDir = apkPath;

            // 获取签名
            Signature[] signs = pi.signatures;
            if (signs == null || signs.length <= 0) {
                // 特殊机型获取不到签名，需要通过反射机制获取
                signs = getSignature(apkPath);
            }
            return signs;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 使用反射机制获取APK的签名
     */
    private static Signature[] getSignature(String apkPath) {
        final String parserName = "android.content.pm.PackageParser";
        try {
            // 这是一个Package 解释器, 是隐藏的
            // 构造函数的参数只有一个, apk文件的路径
            Class<?> parserClass = Class.forName(parserName);
            Class<?>[] typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Object[] valueArgs = new Object[1];
            valueArgs[0] = apkPath;
            Object parser;
            if (Build.VERSION.SDK_INT > 19) {
                parser = parserClass.newInstance();
            } else {
                Constructor<?> constructor = parserClass.getConstructor(typeArgs);
                parser = constructor.newInstance(valueArgs);
            }

            // 这个是与显示有关的, 里面涉及到一些像素显示等等, 我们使用默认的情况
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            Object Package = null;
            if (Build.VERSION.SDK_INT > 19) {
                valueArgs = new Object[2];
                valueArgs[0] = new File(apkPath);
                valueArgs[1] = PackageManager.GET_SIGNATURES;
                Method parsePackageMethod = parserClass.getDeclaredMethod("parsePackage", typeArgs);
                parsePackageMethod.setAccessible(true);

                typeArgs = new Class[2];
                typeArgs[0] = File.class;
                typeArgs[1] = int.class;
                Package = parsePackageMethod.invoke(parser, valueArgs);
            } else {
                typeArgs = new Class[4];
                typeArgs[0] = File.class;
                typeArgs[1] = String.class;
                typeArgs[2] = DisplayMetrics.class;
                typeArgs[3] = int.class;

                Method parsePackageMethod = parserClass.getDeclaredMethod("parsePackage", typeArgs);
                parsePackageMethod.setAccessible(true);

                valueArgs = new Object[4];
                valueArgs[0] = new File(apkPath);
                valueArgs[1] = apkPath;
                valueArgs[2] = metrics;
                valueArgs[3] = PackageManager.GET_SIGNATURES;
                Package = parsePackageMethod.invoke(parser, valueArgs);
            }

            typeArgs = new Class[2];
            typeArgs[0] = Package.getClass();
            typeArgs[1] = int.class;
            Method collectCertificates = parserClass.getDeclaredMethod("collectCertificates", typeArgs);
            valueArgs = new Object[2];
            valueArgs[0] = Package;
            valueArgs[1] = PackageManager.GET_SIGNATURES;
            collectCertificates.invoke(parser, valueArgs);
            // 应用程序信息包, 这个公开的, 不过有些函数, 变量没公开
            Field mSignaturesField = Package.getClass().getDeclaredField("mSignatures");
            Signature[] signs = (Signature[]) mSignaturesField.get(Package);
            return signs;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
