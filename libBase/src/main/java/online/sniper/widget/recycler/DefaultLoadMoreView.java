package online.sniper.widget.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import online.sniper.R;
import online.sniper.widget.recycler.RecyclerViewHelper.OnLoadMoreListener;

/**
 * Created by luozhaocheng on 02/11/2016.
 */

public class DefaultLoadMoreView implements ILoadMoreView {

    private View mRootView;
    private View mLoadingView;
    private View mNoMoreView;
    private View mErrorView;

    private boolean isCanLoad = true;

    public DefaultLoadMoreView(Context context, final OnLoadMoreListener loadMoreListener) {
        mRootView = LayoutInflater.from(context).inflate(R.layout.recycler_view_default_load_more_view, null);
        mLoadingView = findView(R.id.loading_view);
        mNoMoreView = findView(R.id.no_more_view);
        mErrorView = findView(R.id.error_view);
        mErrorView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loadMoreListener != null) {
                    loadMoreListener.onLoadMore();
                }
                showLoading();
            }
        });
    }

    private <T> T findView(int resId) {
        View view = mRootView.findViewById(resId);
        if (view == null) {
            return null;
        } else {
            return (T) view;
        }
    }

    @Override
    public void showLoadError() {
        showView(mErrorView);
        isCanLoad = false;
    }

    @Override
    public void showNoMore() {
        showView(mNoMoreView);
        isCanLoad = false;
    }

    @Override
    public void showLoading() {
        showView(mLoadingView);
        isCanLoad = true;
    }

    private void showView(View view) {
        mRootView.setVisibility(View.VISIBLE);
        this.mLoadingView.setVisibility(View.GONE);
        this.mNoMoreView.setVisibility(View.GONE);
        this.mErrorView.setVisibility(View.GONE);
        view.setVisibility(View.VISIBLE);
    }

    @Override
    public View getLoadMoreView() {
        return mRootView;
    }

    @Override
    public boolean isCanLoad() {
        return isCanLoad;
    }

    @Override
    public void hideLoading() {
        mRootView.setVisibility(View.GONE);
    }
}
