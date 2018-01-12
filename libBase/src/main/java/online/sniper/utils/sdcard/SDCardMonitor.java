package online.sniper.utils.sdcard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.List;

/**
 * SD卡监控器
 *
 * @author wangpeihe
 */
public class SDCardMonitor extends BroadcastReceiver {

    private static SDCardMonitor sInstance = new SDCardMonitor();
    /**
     * 是否发生了关机事件
     */
    private static volatile boolean isShutdown = false;

    private SDCardMonitor() {
    }

    /**
     * 注册SD卡监听器
     */
    public static void register(Context context) {
        context = context.getApplicationContext();
        unregister(context);

        // 监听关机事件
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        context.registerReceiver(sInstance, filter);

        // 监听SD卡相关事件
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        context.registerReceiver(sInstance, filter);

        checkAllSDCard(context);
    }

    /**
     * 注销SD卡监听器
     */
    public static void unregister(Context context) {
        try {
            context = context.getApplicationContext();
            context.unregisterReceiver(sInstance);
        } catch (Throwable e) {
        }
    }

    /**
     * 检查SD卡状态，启动线程
     */
    public static void checkAllSDCard(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doCheckAllSDCard(context);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 检查SD卡状态
     */
    private static void doCheckAllSDCard(Context context) {
        // 检查所有的SD卡
        List<SDCard> sdCards = SDCardUtils.checkSDCards(context);

        // 检查是否准备关机
        if (isShutdown) {
            return;
        }

//        // 检查是否已登录
//        RuntimeConfig config = GlobalManager.getInstance().config();
//        if (!GlobalManager.getInstance().hasLogin() || config.userSettings == null) {
//            return;
//        }
//
//        String oldSDCard = config.userSettings.getSelectedSDCard();
//        // 若之前选中的下载位置所在的SD卡还存在，则不作处理
//        if (!TextUtils.isEmpty(oldSDCard)) {
//            for (SDCard sdcard : sdCards) {
//                if (!sdcard.hasWritablePath()) {
//                    continue;
//                }
//                if (TextUtils.equals(sdcard.writePath, oldSDCard)) {
//                    return;
//                }
//            }
//        }
//
//        // 检查是否准备关机
//        if (isShutdown) {
//            return;
//        }
//
//        // 变更文件下载地址
//        for (SDCard sdcard : sdCards) {
//            if (!sdcard.hasWritablePath()) {
//                continue;
//            }
//            config.userSettings.setSelectedSDCard(sdcard.writePath);
//            config.USER_DIR = new File(config.userSettings.getUserDir(), config.user.qid);
//            return;
//        }
//
//        // 检查是否准备关机
//        if (isShutdown) {
//            return;
//        }
//
//        // 未检测到SD卡
//        config.userSettings.setSelectedSDCard("");
//        config.USER_DIR = new File(config.userSettings.getUserDir(), config.user.qid);
//        Util.show(context, context.getString(R.string.sdcard_unmount_tips));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent != null ? intent.getAction() : "";
        if (Intent.ACTION_SHUTDOWN.equals(action)) {
            isShutdown = true;
        } else {
            // 此处不要判断isShutdowning，以防止他人模拟Intent.ACTION_SHUTDOWN，导致无法实时判断SD卡列表
            checkAllSDCard(context);
        }
    }

}
