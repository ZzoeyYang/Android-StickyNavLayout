package online.sniper.widget.recycler;

import android.view.View;

/**
 * Created by luozhaocheng on 02/11/2016.
 */

public interface ILoadMoreView {

    void showLoadError();

    void showNoMore();

    void showLoading();

    boolean isCanLoad();

    View getLoadMoreView();

    void hideLoading();
}
