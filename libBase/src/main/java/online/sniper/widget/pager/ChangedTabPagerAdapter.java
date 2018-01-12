package online.sniper.widget.pager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 内容变化的标签化的PagerAdapter
 */
public class ChangedTabPagerAdapter extends FragmentStatePagerAdapter {

    protected final FragmentActivity mContext;
    protected final HashMap<Integer, Fragment> mFragments = new HashMap<>();
    protected final ArrayList<TabInfo> mTabs = new ArrayList<>();
    protected String mTag = "";

    public static final class TabInfo {
        private final String tag;
        private final Class<?> clazz;
        private final Bundle args;

        public TabInfo(String _tag, Class<?> _class, Bundle _args) {
            tag = _tag;
            clazz = _class;
            args = _args;
        }
    }

    public ChangedTabPagerAdapter(Fragment fragment) {
        super(fragment.getChildFragmentManager());
        mContext = fragment.getActivity();
    }

    public ChangedTabPagerAdapter(FragmentActivity activity) {
        super(activity.getSupportFragmentManager());
        mContext = activity;
    }

    public void addTab(TabInfo info) {
        mTabs.add(info);
    }

    public void addTab(String _tag, Class<?> _class, Bundle _args) {
        TabInfo info = new TabInfo(_tag, _class, _args);
        mTabs.add(info);
    }

    @Override
    public Fragment getItem(int position) {
        TabInfo info = mTabs.get(position);
        Fragment fragment = Fragment.instantiate(mContext, info.clazz.getName(), info.args);
        return fragment;
    }

    @Override
    public Fragment instantiateItem(ViewGroup container, int position) {
        Fragment f = (Fragment) super.instantiateItem(container, position);
        mFragments.put(position, f);
        return f;
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    public Fragment getFragment(int position) {
        Fragment fragment = mFragments.get(position);
        if (fragment != null) {
            return fragment;
        } else {
            return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        TabInfo info = mTabs.get(position);
        if (info != null) {
            return info.tag;
        } else {
            return null;
        }
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public String getTag() {
        return mTag;
    }

    public void clear() {
        mFragments.clear();
        mTabs.clear();
        mTag = "";
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
