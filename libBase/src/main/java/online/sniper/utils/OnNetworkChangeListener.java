
package online.sniper.utils;

import android.content.Context;

/**
 * 网络变化监听器
 *
 * @author wangpeihe
 */
public interface OnNetworkChangeListener {

    /**
     * 网络变化时调用
     *
     * @param context     Context
     * @param networkType 网络类型
     */
    void onNetworkChanged(Context context, String networkType);
}
