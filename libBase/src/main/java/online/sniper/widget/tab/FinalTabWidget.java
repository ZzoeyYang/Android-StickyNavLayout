package online.sniper.widget.tab;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import online.sniper.widget.tab.custom.TabWidget;

/**
 * 自定义的{@link TabWidget}，可以拦截TAB的点击事件.
 * <p/>
 * Created by wangpeihe on 2016/6/16.
 */
public class FinalTabWidget extends TabWidget {

    private TabViewWrapper.OnClickTabListener mOnClickTabListener;

    public FinalTabWidget(Context context) {
        super(context);
    }

    public FinalTabWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void addView(View child) {
        TabViewWrapper wrapper = new TabViewWrapper(getContext(), getChildCount(), child);
        wrapper.setOnClickTabListener(new TabViewWrapper.OnClickTabListener() {
            @Override
            public boolean onTabClick(TabViewWrapper tab) {
                if (mOnClickTabListener != null) {
                    return mOnClickTabListener.onTabClick(tab);
                }
                return false;
            }
        });
        super.addView(wrapper);
    }

    public void setOnClickTabListener(TabViewWrapper.OnClickTabListener l) {
        mOnClickTabListener = l;
    }

    @Override
    public TabViewWrapper getChildTabViewAt(int index) {
        return (TabViewWrapper) super.getChildTabViewAt(index);
    }

}
