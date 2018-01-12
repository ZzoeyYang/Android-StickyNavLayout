package online.sniper.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import java.util.HashSet;

/**
 * 网络监听器
 *
 * @author wangpeihe
 */
public class NetworkMonitor extends BroadcastReceiver {

    private final HashSet<OnNetworkChangeListener> mOnNetworkChangeListeners = new HashSet<>();

    private Context mContext;

    private interface Holder {
        NetworkMonitor INSTANCE = new NetworkMonitor();
    }

    public static NetworkMonitor getInstance() {
        return Holder.INSTANCE;
    }

    private NetworkMonitor() {
    }

    /**
     * 注册网络监听器
     */
    public void register(Context context) {
        mContext = context.getApplicationContext();
        unregister(mContext);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(this, filter);
        onNetworkChanged(mContext);
    }

    /**
     * 注销网络监听器
     */
    public void unregister(Context context) {
        try {
            context.getApplicationContext().unregisterReceiver(this);
        } catch (Throwable e) {
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent != null ? intent.getAction() : "";
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            onNetworkChanged(context);
        }
    }

    /**
     * 当网络变化时调用
     */
    private void onNetworkChanged(Context context) {
        String networkType = NetworkUtils.getNetworkType(context);
        HashSet<OnNetworkChangeListener> listeners;
        synchronized (mOnNetworkChangeListeners) {
            listeners = new HashSet<>(mOnNetworkChangeListeners);
        }

        for (OnNetworkChangeListener l : listeners) {
            try {
                l.onNetworkChanged(context, networkType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 添加网络变化监听器
     */
    public void addNetworkChangeListener(OnNetworkChangeListener l) {
        synchronized (mOnNetworkChangeListeners) {
            mOnNetworkChangeListeners.add(l);
        }
    }

    /**
     * 移除网络变化监听器
     */
    public void removeNetworkChangeListener(OnNetworkChangeListener l) {
        synchronized (mOnNetworkChangeListeners) {
            mOnNetworkChangeListeners.remove(l);
        }
    }
}
