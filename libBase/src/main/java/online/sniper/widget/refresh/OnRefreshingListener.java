package online.sniper.widget.refresh;

/**
 * Created by LuckyJayce on 2016/12/1.
 */
public abstract class OnRefreshingListener implements OnPullListener {
    @Override
    public void onPullBegin(CoolRefreshView refreshView) {
    }

    @Override
    public void onPositionChange(CoolRefreshView refreshView, int status, int dy, int currentDistance) {
    }

    @Override
    public void onReset(CoolRefreshView refreshView, boolean pullRelease) {
    }

    @Override
    public void onPullRefreshComplete(CoolRefreshView refreshView) {
    }
}
