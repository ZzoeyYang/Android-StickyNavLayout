package online.sniper.utils.sdcard;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * SD卡工具类
 *
 * @author wangpeihe
 */
public class SDCardUtils {

    public static final String STORAGE_SDCARD = File.separator + "STORAGE_SDCARD" + File.separator;
    public final static String SDCARD_NAME_INTERNAL = "内置卡";
    public final static String SDCARD_NAME_EXTERNAL = "扩展卡";

    private static final List<String> sVolumePaths = new ArrayList<String>();
    private static final List<SDCard> sSDCards = new ArrayList<SDCard>();

    /**
     * 判断默认SD卡是否存在
     */
    public static boolean isMounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 获取默认SD卡路径
     */
    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * 获取SD卡名称
     */
    public static String getSDCardName(File file) {
        if (file == null) {
            return "";
        }

        String path;
        try {
            path = file.getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
            path = file.getAbsolutePath();
        }

        for (SDCard sdcard : sSDCards) {
            if (TextUtils.equals(path, sdcard.rootPath)) {
                return sdcard.name;
            }
        }

        return file.getName();
    }

    /**
     * 判断一个路径是否为SD卡
     */
    public static boolean isSDCard(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        return isSDCard(new File(path));
    }

    /**
     * 判断一个文件是否为SD卡
     */
    public static boolean isSDCard(File file) {
        if (file == null) {
            return false;
        }

        if (!file.exists() || !file.isDirectory()) {
            return false;
        }

        String path;
        try {
            path = file.getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
            path = file.getAbsolutePath();
        }

        for (SDCard sdCardStat : sSDCards) {
            if (TextUtils.equals(path, sdCardStat.rootPath)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断是否仅有一张SD卡
     */
    public static boolean hasOnlyOneCard() {
        return sSDCards.size() == 1;
    }

    /**
     * 真实路径变为名称路径
     */
    public static String realPathToNamePath(String realPath) {
        if (TextUtils.isEmpty(realPath)) {
            return realPath;
        }

        int pathLength = realPath.length();
        for (SDCard sdcard : sSDCards) {
            String name = STORAGE_SDCARD + sdcard.name;
            int length = sdcard.rootPath.length();
            if (pathLength > length) {
                if (realPath.startsWith(sdcard.rootPath + File.separator)) {
                    return realPath.replace(sdcard.rootPath, name);
                }
            } else if (pathLength == length) {
                if (realPath.equals(sdcard.rootPath)) {
                    return name;
                }
            }
        }
        return realPath;
    }

    /**
     * 真实路径变为带SD卡名称的路径
     */
    public static String getSDCardNamePath(String realPath) {
        if (TextUtils.isEmpty(realPath)) {
            return realPath;
        }

        int pathLength = realPath.length();
        for (SDCard sdcard : sSDCards) {
            String name = sdcard.name + ":";
            int length = sdcard.rootPath.length();
            if (pathLength > length) {
                if (realPath.startsWith(sdcard.rootPath + File.separator)) {
                    return realPath.replace(sdcard.rootPath, name);
                }
            } else if (pathLength == length) {
                if (realPath.equals(sdcard.rootPath)) {
                    return name;
                }
            }
        }
        return realPath;
    }

    /**
     * 名称路径变为真实路径
     */
    public static String namePathToRealPath(String namePath) {
        if (TextUtils.isEmpty(namePath)) {
            return namePath;
        }

        int pathLength = namePath.length();
        for (SDCard sdcard : sSDCards) {
            String name = STORAGE_SDCARD + sdcard.name;
            int length = name.length();
            if (pathLength > length) {
                if (namePath.startsWith(name + File.separator)) {
                    return namePath.replace(name, sdcard.rootPath);
                }
            } else if (pathLength == length) {
                if (namePath.equals(name)) {
                    return sdcard.rootPath;
                }
            }
        }
        return namePath;
    }

    /**
     * 检查全部SD卡
     */
    public static List<SDCard> checkSDCards(Context context) {
        List<SDCard> list = checkSDCardsInternal(context);
        sSDCards.clear();
        if (list != null && !list.isEmpty()) {
            sSDCards.addAll(list);
        }
        return list;
    }

    /**
     * 获取全部SD卡
     */
    public static List<SDCard> getSDCards() {
        return sSDCards;
    }

    /**
     * 获取可写SD卡
     */
    public static List<SDCard> getWritableSDCards() {
        List<SDCard> list = new ArrayList<SDCard>();
        for (SDCard sdcard : sSDCards) {
            if (sdcard.hasWritablePath()) {
                list.add(sdcard);
            }
        }
        return list;
    }

    /**
     * 检查全部SD卡
     */
    private static ArrayList<SDCard> checkSDCardsInternal(Context context) {
        ArrayList<SDCard> list = new ArrayList<SDCard>();
        List<String> volumePaths = getVolumePaths(context);
        if (volumePaths == null || volumePaths.isEmpty()) {
            return list;
        }

        File[] filesDirs = ContextCompat.getExternalFilesDirs(context, null);
        for (String volumePath : volumePaths) {
            SDCard sdcard = getSDCard(volumePath, filesDirs);
            if (sdcard != null) {
                list.add(sdcard);
            }
        }

        int size = list.size();
        if (size <= 0) {
            return list;
        }
        SDCard first = list.get(0);
        int id = 0;
        if (first.rootPath.equals(getSDCardPath())) {
            first.name = SDCARD_NAME_INTERNAL;
        } else {
            id++;
            first.name = SDCARD_NAME_EXTERNAL;
        }
        for (int i = 1; i < size; i++) {
            SDCard stat = list.get(i);
            id++;
            if (id == 1) {
                stat.name = SDCARD_NAME_EXTERNAL;
            } else {
                stat.name = SDCARD_NAME_EXTERNAL + id;
            }
        }

        return list;
    }

    /**
     * 获取SD卡
     */
    private static SDCard getSDCard(String volumePath, File[] filesDirs) {
        try {
            SDCard sdcard = new SDCard(volumePath);
            if (canWriteTo(volumePath)) {
                sdcard.canWriteToRootPath = true;
                sdcard.writePath = volumePath;
            } else {
                // 4.4及以上版本不支持往外置SD卡根目录写权限
                sdcard.canWriteToRootPath = false;
                sdcard.writePath = "";
                for (File file : filesDirs) {
                    if (canWriteTo(file) && file.getCanonicalPath().startsWith(volumePath)) {
                        sdcard.writePath = file.getAbsolutePath();
                        break;
                    }
                }
            }
            return sdcard;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<File> getSDCardFiles() {
        List<File> sortList = new ArrayList<File>();
        for (SDCard sdcard : sSDCards) {
            sortList.add(new File(sdcard.rootPath));
        }
        return sortList;
    }

    /**
     * 获取所有的卷路径
     */
    public static List<String> getVolumePaths() {
        synchronized (sVolumePaths) {
            return new ArrayList<String>(sVolumePaths);
        }
    }

    /**
     * 获取所有的卷路径，建议在非UI线程中调用
     */
    public static List<String> getVolumePaths(Context context) {
        List<String> paths = getVolumePathsInternal(context);
        synchronized (sVolumePaths) {
            sVolumePaths.clear();
            if (!paths.isEmpty()) {
                sVolumePaths.addAll(paths);
            }
        }
        return paths;
    }

    /**
     * 获取所有的卷路径，建议在非UI线程中调用
     */
    private static List<String> getVolumePathsInternal(Context context) {
        List<String> paths = getDefaultVolumePaths();
        // 通过反射获得的卷路径都是正确的卷路径，大多数4.0以上的手机都能够通过反射获得卷路径信息
        List<String> temp = null;
        if (Build.VERSION.SDK_INT >= 14) {
            temp = getVolumePathsByReflect(context);
            if (temp != null) {
                paths.addAll(temp);
                return paths;
            }
        }
        // 通过mount命令获得的卷路径可能比较多，需要认真排查一下
        temp = getVolumePathsByMount();
        if (temp != null) {
            paths.addAll(temp);
            return paths;
        }
        return paths;
    }

    /**
     * 获取默认的卷路径信息
     */
    public static List<String> getDefaultVolumePaths() {
        List<String> paths = new ArrayList<String>();
        String state = Environment.getExternalStorageState();
        File file = Environment.getExternalStorageDirectory();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            paths.add(file.getAbsolutePath());
        }
        return paths;
    }

    /**
     * 根据反射获取卷路径信息
     */
    private static List<String> getVolumePathsByReflect(Context context) {
        final String defaultVolumePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        List<String> paths = new ArrayList<String>();
        try {
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumeList = StorageManager.class.getMethod("getVolumeList");
            Method getVolumeState = StorageManager.class.getMethod("getVolumeState", String.class);
            Object volumeList = getVolumeList.invoke(storageManager);

            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            final int length = Array.getLength(volumeList);
            for (int i = 0; i < length; i++) {
                Object storageVolume = Array.get(volumeList, i);
                String path = (String) getPath.invoke(storageVolume);
                if (TextUtils.isEmpty(path) || path.equals(defaultVolumePath)) {
                    continue;
                }
                String state = (String) getVolumeState.invoke(storageManager, path);
                if (Environment.MEDIA_MOUNTED.equals(state)
                        || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                    paths.add(path);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            paths = null;
        }
        return paths;
    }

    /**
     * 根据mount命令获取卷路径信息
     */
    private static List<String> getVolumePathsByMount() {
        final String defaultVolumePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        Set<String> paths = new HashSet<String>();
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            // obtain executed result of command line code of 'mount', to judge
            // whether tfCard exists by the result
            Process process = Runtime.getRuntime().exec("mount");
            is = process.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            String line = null;
            final int mountPathIndex = 1;
            while ((line = br.readLine()) != null) {
                final String text = line.toLowerCase(Locale.getDefault());
                // format of sdcard file system: vfat/fuse/ntfs
                if (!text.contains("fat") && !text.contains("fuse")
                        && !text.contains("ntfs")) {
                    continue;
                }

                if (text.contains("root") || text.contains("shell")
                        || text.contains("acct")
                        || text.contains("asec")
                        || text.contains("cache")
                        || text.contains("data")
                        || text.contains("firmware")
                        || text.contains("legacy")
                        || text.contains("media")
                        || text.contains("misc")
                        || text.contains("obb")
                        || text.contains("proc")
                        || text.contains("secure")
                        || text.contains("sys")
                        || text.contains("tmpfs")
                        || text.contains("uicc")) {
                    continue;
                }

                String[] parts = line.split(" ");
                int length = parts.length;
                if (mountPathIndex >= length) {
                    continue;
                }
                String path = parts[mountPathIndex];
                if (TextUtils.isEmpty(path) || !path.startsWith(File.separator)
                        || path.equals(defaultVolumePath) || paths.contains(path)) {
                    continue;
                }
                File root = new File(path);
                if (!root.exists() || !root.isDirectory() || !canWriteTo(root)) {
                    continue;
                }
                paths.add(path);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            paths = null;
        } finally {
            close(br);
            close(isr);
            close(is);
        }
        return paths != null ? new ArrayList<String>(paths) : null;
    }

    /**
     * 判断卷根目录是否可写
     */
    public static boolean canWriteTo(String dir) {
        if (TextUtils.isEmpty(dir)) {
            return false;
        }

        return canWriteTo(new File(dir));
    }

    /**
     * 判断卷根目录是否可写
     */
    public static boolean canWriteTo(File dir) {
        if (dir == null || !dir.isDirectory() || !dir.canWrite()) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= 19) {
            // canWrite() 在4.4系统不起作用，只要路径存在总是返回true
            File canWriteFile = new File(dir, ".canWrite" + System.currentTimeMillis());
            if (canWriteFile.mkdirs()) {
                canWriteFile.delete();
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * 关闭流
     */
    private static void close(Closeable close) {
        if (close != null) {
            try {
                close.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
