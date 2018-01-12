package online.sniper.utils.log;

import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


/**
 * ===============================================
 * DEVELOPER : RenYang <br/>
 * DATE : 2016/9/21 <br/>
 * DESCRIPTION :
 */
public class FileLog {

    public static final String TAG_LOG = "log";
    public static final String TAG_NETWORK = "network";
    public static final String TAG_CRASH = "crash";

    public static File ROOT;
    public static File LOG_ROOT;
    public static File CACHE_ROOT;

    public static File LOG_PATH_LOG;
    public static File LOG_PATH_NETWORK;
    public static File LOG_PATH_CRASH;

    private static FileLog sFileLog;
    private final HashMap<String, File> mHashMap;
    private static String APP_EXTRAS = "";

    private FileLog() {
        mHashMap = new HashMap<>();
        mHashMap.put(TAG_LOG, LOG_PATH_LOG);
        mHashMap.put(TAG_NETWORK, LOG_PATH_NETWORK);
        mHashMap.put(TAG_CRASH, LOG_PATH_CRASH);

        for (Map.Entry<String, File> map : mHashMap.entrySet()) {
            try {
                initPath(map.getKey(), map.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected static FileLog getInstance() {
        if (sFileLog == null) {
            sFileLog = new FileLog();
        }
        return sFileLog;
    }

    protected static void init(String fileName, String appExtras) {
        APP_EXTRAS = appExtras;
        ROOT = new File(Environment.getExternalStorageDirectory(), fileName);
        LOG_ROOT = new File(ROOT, "log");
        CACHE_ROOT = new File(ROOT, "cache");
        LOG_PATH_LOG = new File(LOG_ROOT, TAG_LOG);
        LOG_PATH_NETWORK = new File(LOG_ROOT, TAG_NETWORK);
        LOG_PATH_CRASH = new File(LOG_ROOT, TAG_CRASH);

        deleteNotLogFiles();
    }

    private static void deleteNotLogFiles() {
        File[] files = LOG_ROOT.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.startsWith(TAG_LOG)
                        || filename.startsWith(TAG_NETWORK)
                        || filename.startsWith(TAG_CRASH)) {
                    return false;
                } else {
                    return true;
                }
            }
        });
        if (files != null && files.length > 0) {
            for (File file : files) {
                deleteFile(file);
            }
        }
    }

    private static void deleteFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    deleteFile(f);
                }
            }
            file.delete();
        } else {
            file.delete();
        }
    }

    public void info(String tag, String message) {
        if (!mHashMap.containsKey(tag)) {
            throw new RuntimeException("no support tag type");
        }
        Logger logger = Logger.getLogger(tag);
        logger.info("version:" + APP_EXTRAS + "\n" + message);
    }

    private void initPath(String tag, File filePath) throws IOException {
        Logger logger = Logger.getLogger(tag);
        if (!LOG_ROOT.exists()) {
            LOG_ROOT.mkdirs();
        }
        AsyncFileHandler handler = new AsyncFileHandler(filePath.getAbsolutePath() + "%g" + ".log", 1024 * 1024, 2, true);
        handler.setFormatter(new LogFormatter());
        logger.addHandler(handler);
    }

    public static List<File> getLogFiles() {
        List<File> files = new ArrayList<>();
        listFiles(LOG_ROOT, files);
        return files;
    }

    private static void listFiles(File file, List<File> list) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    listFiles(f, list);
                }
            }
        } else {
            if (file.length() > 0) {
                list.add(file);
            }
        }
    }
}
