package online.sniper.widget.tab;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 包装TAB视图，对点击事件进行拦截.
 * <p/>
 * Created by wangpeihe on 2016/6/16.
 */
public class TabViewWrapper extends FrameLayout {

    /**
     * 拦截点击事件的接口
     */
    public static interface OnClickTabListener {
        public boolean onTabClick(TabViewWrapper tab);
    }

    private boolean mAdded = false;
    private int mTabIndex;
    private View mTabView;
    private OnClickListener mOnClickListener;
    private OnClickTabListener mOnClickTabListener;

    public TabViewWrapper(Context context, int tabIndex, View tabView) {
        super(context);
        mTabIndex = tabIndex;
        mTabView = tabView;
        addView(tabView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public int getTabIndex() {
        return mTabIndex;
    }

    public <T extends View> T getTabView() {
        return (T) mTabView;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean handled = false;
                if (mOnClickTabListener != null) {
                    handled = mOnClickTabListener.onTabClick(TabViewWrapper.this);
                }
                if (!handled) {
                    mOnClickListener.onClick(TabViewWrapper.this);
                }
            }
        });
    }

    public void setOnClickTabListener(OnClickTabListener l) {
        mOnClickTabListener = l;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!mAdded) {
            super.addView(child, index, params);
            mAdded = true;
        } else {
            throw new RuntimeException("Tab view has added.");
        }
    }

}