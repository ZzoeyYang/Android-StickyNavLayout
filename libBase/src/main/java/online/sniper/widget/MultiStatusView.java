package online.sniper.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashSet;
import java.util.Set;

import online.sniper.R;
import online.sniper.utils.ScrollUtils;

/**
 * Created by luozhaocheng on 02/11/2016.
 */

public class MultiStatusView extends RelativeLayout {

    // View 的5个状态
    public static final int STATUS_CONTENT = 0;
    public static final int STATUS_LOADING = 1;
    public static final int STATUS_EMPTY = 2;
    public static final int STATUS_ERROR = 3;
    public static final int STATUS_NO_NETWORK = 4;


    private final int NULL_RESOURCE_ID = -1;
    // 5个View的layout resource id
    private int mLoadingViewLayoutId = NULL_RESOURCE_ID;
    private int mEmptyViewLayoutId = NULL_RESOURCE_ID;
    private int mErrorViewLayoutId = NULL_RESOURCE_ID;
    private int mNetworkErrorViewLayoutId = NULL_RESOURCE_ID;

    // 5 个不同状态的View
    private View mContentView = null;
    private View mLoadingView = null;
    private View mEmptyView = null;
    private View mErrorView = null;
    private View mNetworkErrorView = null;

    private LayoutInflater mInflater;

    private int mCurrentStatus;

    public MultiStatusView(Context context) {
        this(context, null);
    }

    public MultiStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiStatusView);
        try {
            mLoadingViewLayoutId = a.getResourceId(R.styleable.MultiStatusView_loadingViewLayout, R.layout.multi_status_loading_view);
            mEmptyViewLayoutId = a.getResourceId(R.styleable.MultiStatusView_emptyViewLayout, R.layout.multi_status_empty_view);
            mErrorViewLayoutId = a.getResourceId(R.styleable.MultiStatusView_errorViewLayout, R.layout.multi_status_error_view);
            mNetworkErrorViewLayoutId = a.getResourceId(R.styleable.MultiStatusView_networkErrorViewLayout, R.layout.multi_status_network_error_view);
        } finally {
            a.recycle();
        }
        setClickable(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int count = getChildCount();
        if (count > 1) {
            throw new RuntimeException("Content view must only one.");
        } else if (count == 1) {
            mContentView = getChildAt(0);
            showContent();
        }
        mInflater = LayoutInflater.from(getContext());
    }

    public int getCurrentStatus() {
        return mCurrentStatus;
    }

    @NonNull
    private LayoutParams newMatchParentParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    /**
     * 展示内容页面
     */
    public void showContent() {
        showViews(mContentView);
        mCurrentStatus = STATUS_CONTENT;
    }

    public View getContentView() {
        return mContentView;
    }

    private void checkLoadingView() {
        if (mLoadingView == null) {
            mLoadingView = mInflater.inflate(mLoadingViewLayoutId, this, false);
            addView(mLoadingView, newMatchParentParams());
        }
    }

    public View getLoadingView() {
        checkLoadingView();
        return mLoadingView;
    }

    public void showLoading() {
        checkLoadingView();
        showViews(mLoadingView);
        mCurrentStatus = STATUS_LOADING;
    }

    public void showContentAndLoading() {
        checkLoadingView();
        showViews(mContentView, mLoadingView);
    }

    private void checkEmptyView() {
        if (mEmptyView == null) {
            mEmptyView = mInflater.inflate(mEmptyViewLayoutId, this, false);
            addView(mEmptyView, newMatchParentParams());
        }
    }

    public View getEmptyView() {
        checkEmptyView();
        return mEmptyView;
    }

    /**
     * 显示空页面
     */
    public void showEmpty() {
        checkEmptyView();
        showViews(mEmptyView);
        mCurrentStatus = STATUS_EMPTY;
    }

    /**
     * 显示空页面
     */
    public void showEmpty(String msg) {
        showEmpty(-1, msg, null);
    }

    /**
     * 显示空页面
     */
    public void showEmpty(int imgRes, String emptyTxt) {
        showEmpty(imgRes, emptyTxt, null);
    }

    /**
     * 显示空页面
     */
    public void showEmpty(String emptyTxt, OnClickListener retryListener) {
        showEmpty(-1, emptyTxt, retryListener);
    }

    /**
     * 显示空页面
     */
    public void showEmpty(int imgRes, String emptyTxt, OnClickListener retryListener) {
        showEmpty();
        ImageView imgView = (ImageView) mEmptyView.findViewById(R.id.multi_status_empty_icon);
        if (imgRes != -1) {
            imgView.setVisibility(View.VISIBLE);
            imgView.setImageResource(imgRes);
        }
        TextView msgView = (TextView) mEmptyView.findViewById(R.id.multi_status_empty_text);
        if (!TextUtils.isEmpty(emptyTxt)) {
            msgView.setText(emptyTxt);
        }
        if (retryListener != null) {
            mEmptyView.setOnClickListener(retryListener);
        }
    }

    private void checkErrorView() {
        if (mErrorView == null) {
            mErrorView = mInflater.inflate(mErrorViewLayoutId, this, false);
            addView(mErrorView, newMatchParentParams());
        }
    }

    public View getErrorView() {
        checkErrorView();
        return mErrorView;
    }

    /**
     * 展示错误页面， 使用默认数据
     */
    public void showError() {
        checkErrorView();
        showViews(mErrorView);
        mCurrentStatus = STATUS_ERROR;
    }

    /**
     * 使用不同的图与不同的 errmsg 显示错误页面
     *
     * @param error error 信息
     */
    public void showError(String error) {
        showError(-1, error, null);
    }

    /**
     * 使用不同的图与不同的 errmsg 显示错误页面
     *
     * @param imgRes 图片所对应的资源 id
     * @param error  error 信息
     */
    public void showError(int imgRes, String error) {
        showError(imgRes, error, null);
    }

    /**
     * 使用不同的图与不同的 errmsg 显示错误页面
     *
     * @param error         error 信息
     * @param retryListener 重试按钮监听器
     */
    public void showError(String error, OnClickListener retryListener) {
        showError(-1, error, "", retryListener);
    }

    /**
     * 使用不同的图与不同的 errmsg 显示错误页面
     *
     * @param imgRes        图片所对应的资源 id
     * @param error         error 信息
     * @param retryListener 重试按钮监听器
     */
    public void showError(int imgRes, String error, OnClickListener retryListener) {
        showError(imgRes, error, "", retryListener);
    }

    /**
     * 使用不同的图与不同的 errmsg 显示错误页面
     *
     * @param imgRes        图片所对应的资源 id
     * @param error         error 信息
     * @param retryText     重试按钮显示的文字
     * @param retryListener 重试按钮监听器
     */
    public void showError(int imgRes, final String error, String retryText, OnClickListener retryListener) {
        showError();
        ImageView imgView = (ImageView) mErrorView.findViewById(R.id.multi_status_error_icon);
        if (imgRes != -1) {
            imgView.setVisibility(View.VISIBLE);
            imgView.setImageResource(imgRes);
        }
        TextView errorMsgView = (TextView) mErrorView.findViewById(R.id.multi_status_error_text);
        if (!TextUtils.isEmpty(error)) {
            errorMsgView.setText(error);
        }
        final Button retryBtn = (Button) mErrorView.findViewById(R.id.multi_status_error_retry);
        if (retryListener != null) {
            retryBtn.setOnClickListener(retryListener);
            retryBtn.setVisibility(VISIBLE);
            if (!TextUtils.isEmpty(retryText)) {
                retryBtn.setText(retryText);
            }
        } else {
            retryBtn.setVisibility(GONE);
        }
    }

    private void checkNoNetworkView() {
        if (mNetworkErrorView == null) {
            mNetworkErrorView = mInflater.inflate(mNetworkErrorViewLayoutId, this, false);
            addView(mNetworkErrorView, newMatchParentParams());
        }
    }

    public View getNoNetworkView() {
        return mNetworkErrorView;
    }

    /**
     * 显示网络异常页面
     */
    public void showNetworkError() {
        checkNoNetworkView();
        showViews(mNetworkErrorView);
        View goSetting = mNetworkErrorView.findViewById(R.id.multi_status_set_network);
        goSetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                getContext().startActivity(intent);
            }
        });
    }


    /**
     * 显示网络异常页面
     *
     * @param imgResId     图片
     * @param remindTxt    提示语
     * @param goSettingTxt 按钮文案
     */
    public void showNetworkError(int imgResId, String remindTxt, String goSettingTxt) {
        checkNoNetworkView();
        showViews(mNetworkErrorView);
        View goSetting = mNetworkErrorView.findViewById(R.id.multi_status_set_network);
        goSetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                getContext().startActivity(intent);
            }
        });

        if (!TextUtils.isEmpty(goSettingTxt)) {
            ((TextView) goSetting).setText(goSettingTxt);
        }

        ImageView imgView = (ImageView) mNetworkErrorView.findViewById(R.id.multi_status_network_error_icon);
        if (imgResId <= 0) {
            imgView.setVisibility(View.GONE);
        } else {
            imgView.setVisibility(View.VISIBLE);
            imgView.setBackgroundResource(imgResId);
        }
        TextView msgView = (TextView) mNetworkErrorView.findViewById(R.id.multi_status_network_error_text);
        if (!TextUtils.isEmpty(remindTxt)) {
            msgView.setText(remindTxt);
        }
    }

    /**
     * 显示视图
     *
     * @param status 状态
     */
    public void showView(@ViewStatus int status) {
        switch (status) {
            case STATUS_CONTENT:
                showContent();
                break;
            case STATUS_LOADING:
                showLoading();
                break;
            case STATUS_EMPTY:
                showEmpty();
                break;
            case STATUS_ERROR:
                showError();
                break;
            case STATUS_NO_NETWORK:
                showNetworkError();
                break;
        }
    }

    /**
     * 给某一个状态设置一个 View
     *
     * @param status         ViewStatus
     * @param view           View
     * @param switchToStatus 是不切换到当前设置的Status
     */
    public void setView(@ViewStatus int status, View view, boolean switchToStatus) {
        switch (status) {
            case STATUS_CONTENT:
                if (replaceView(mContentView, view)) {
                    mContentView = view;
                    addView(view, 0, newMatchParentParams());
                }
                break;
            case STATUS_LOADING:
                if (replaceView(mLoadingView, view)) {
                    mLoadingView = view;
                    addView(view, newMatchParentParams());
                }
                break;
            case STATUS_EMPTY:
                if (replaceView(mEmptyView, view)) {
                    mEmptyView = view;
                    addView(view, newMatchParentParams());
                }
                break;
            case STATUS_ERROR:
                if (replaceView(mErrorView, view)) {
                    mErrorView = view;
                    addView(view, newMatchParentParams());
                }
                break;
            case STATUS_NO_NETWORK:
                if (replaceView(mNetworkErrorView, view)) {
                    mNetworkErrorView = view;
                    addView(view, newMatchParentParams());
                }
                break;
            default:
                return;
        }

        if (view != null && switchToStatus) {
            showViews(view);
        }
    }

    /**
     * 替换状态视图
     */
    private boolean replaceView(View oldView, View newView) {
        if (oldView != null && newView != null) {
            removeView(oldView);
            return true;
        } else if (newView != null) {
            return true;
        }
        return false;
    }

    /**
     * 显示视图
     */
    public void showViews(View... views) {
        HashSet<View> set = new HashSet<>();
        addToSet(set, mContentView);
        addToSet(set, mLoadingView);
        addToSet(set, mEmptyView);
        addToSet(set, mErrorView);
        addToSet(set, mNetworkErrorView);
        if (views != null && views.length > 0) {
            for (View view : views) {
                show(view);
                removeFromSet(set, view);
            }
        }
        for (View view : set) {
            hide(view);
        }
        requestLayout();
    }

    private void addToSet(Set<View> set, View view) {
        if (view != null) {
            set.add(view);
        }
    }

    private void removeFromSet(Set<View> set, View view) {
        if (view != null) {
            set.remove(view);
        }
    }

    private void show(View view) {
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }

    private void hide(View view) {
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean canScrollVertically(int direction) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child != null && child.getVisibility() == View.VISIBLE) {
                return ScrollUtils.canScrollVertically(child, direction);
            }
        }
        return super.canScrollVertically(direction);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATUS_CONTENT, STATUS_LOADING, STATUS_EMPTY, STATUS_ERROR, STATUS_NO_NETWORK})
    public @interface ViewStatus {
    }
}