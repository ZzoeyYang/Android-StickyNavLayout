/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package online.sniper.widget.tab;


import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import online.sniper.widget.tab.custom.TabHost;

/**
 * Special TabHost that allows the use of {@link Fragment} objects for
 * its tab content.  When placing this in a view hierarchy, after inflating
 * the hierarchy you must call {@link #setup(Context, FragmentManager)}
 * to complete the initialization of the tab host.
 * <p>
 * 自定义的{@link TabHost}，防止{@link Fragment}重复调用{@link Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}等方法.
 * </p>
 * Created by wangpeihe on 2016/6/16.
 */
public final class FinalTabHost extends TabHost
        implements TabHost.OnTabChangeListener {
    private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
    private Context mContext;
    private FragmentManager mFragmentManager;
    private OnTabChangeListener mOnTabChangeListener;
    private TabInfo mLastTab;
    private boolean mAttached;

    static final class TabInfo {
        private final String tag;
        private final Class<?> clazz;
        private final Bundle args;
        private final boolean detachable;
        private Fragment fragment;

        TabInfo(String _tag, Class<?> _clazz, Bundle _args, boolean _detachable) {
            tag = _tag;
            clazz = _clazz;
            args = _args;
            detachable = _detachable;
        }
    }

    static class DummyTabFactory implements TabContentFactory {
        private final Context mContext;

        public DummyTabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }

    static class SavedState extends BaseSavedState {
        String curTab;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            curTab = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(curTab);
        }

        @Override
        public String toString() {
            return "FragmentTabHost.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " curTab=" + curTab + "}";
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public FinalTabHost(Context context) {
        // Note that we call through to the version that takes an AttributeSet,
        // because the simple Context construct can result in a broken object!
        super(context, null);
        initFragmentTabHost(context, null);
    }

    public FinalTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFragmentTabHost(context, attrs);
    }

    private void initFragmentTabHost(Context context, AttributeSet attrs) {
        super.setOnTabChangedListener(this);
    }

    /**
     * @deprecated Don't call the original TabHost setup, you must instead
     * call {@link #setup(Context, FragmentManager)}.
     */
    @Override
    @Deprecated
    public void setup() {
        throw new IllegalStateException(
                "Must call setup() that takes a Context and FragmentManager");
    }

    public void setup(Context context, FragmentManager manager) {
        super.setup();
        mContext = context;
        mFragmentManager = manager;

        // We must have an ID to be able to save/restore our state.  If
        // the owner hasn't set one at this point, we will set it ourself.
        if (getId() == View.NO_ID) {
            setId(android.R.id.tabhost);
        }
    }

    @Override
    public void setOnTabChangedListener(OnTabChangeListener l) {
        mOnTabChangeListener = l;
    }

    public void addTab(TabSpec tabSpec, Class<?> clazz, Bundle args) {
        addTab(tabSpec, clazz, args, false);
    }

    public void addTab(TabSpec tabSpec, Class<?> clazz, Bundle args, boolean detachable) {
        tabSpec.setContent(new DummyTabFactory(mContext));
        String tag = tabSpec.getTag();

        TabInfo info = new TabInfo(tag, clazz, args, detachable);

        if (mAttached) {
            // If we are already attached to the window, then check to make
            // sure this tab's fragment is inactive if it exists.  This shouldn't
            // normally happen.
            info.fragment = mFragmentManager.findFragmentByTag(tag);
            if (info.fragment != null && !info.fragment.isDetached()) {
                FragmentTransaction ft = mFragmentManager.beginTransaction();
                setVisibleToUser(info.fragment, false);
                if (info.detachable) {
                    ft.detach(info.fragment);
                } else {
                    ft.hide(info.fragment);
                }
                ft.commitAllowingStateLoss();
            }
        }

        mTabs.add(info);
        addTab(tabSpec);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        String currentTab = getCurrentTabTag();

        // Go through all tabs and make sure their fragments match
        // the correct state.
        FragmentTransaction ft = null;
        for (int i = 0; i < mTabs.size(); i++) {
            TabInfo tab = mTabs.get(i);
            tab.fragment = mFragmentManager.findFragmentByTag(tab.tag);
            if (tab.fragment != null && !tab.fragment.isDetached()) {
                if (tab.tag.equals(currentTab)) {
                    // The fragment for this tab is already there and
                    // active, and it is what we really want to have
                    // as the current tab.  Nothing to do.
                    mLastTab = tab;
                } else {
                    // This fragment was restored in the active state,
                    // but is not the current tab.  Deactivate it.
                    if (ft == null) {
                        ft = mFragmentManager.beginTransaction();
                    }
                    setVisibleToUser(tab.fragment, false);
                    if (tab.detachable) {
                        ft.detach(tab.fragment);
                    } else {
                        ft.hide(tab.fragment);
                    }
                }
            }
        }

        // We are now ready to go.  Make sure we are switched to the
        // correct tab.
        mAttached = true;
        ft = doTabChanged(currentTab, ft);
        if (ft != null) {
            ft.commitAllowingStateLoss();
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttached = false;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.curTab = getCurrentTabTag();
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentTabByTag(ss.curTab);
    }

    @Override
    public void onTabChanged(String tabId) {
        if (mAttached) {
            FragmentTransaction ft = doTabChanged(tabId, null);
            if (ft != null) {
                ft.commitAllowingStateLoss();
            }
        }
        if (mOnTabChangeListener != null) {
            mOnTabChangeListener.onTabChanged(tabId);
        }
    }

    private FragmentTransaction doTabChanged(String tabId, FragmentTransaction ft) {
        TabInfo newTab = null;
        for (int i = 0; i < mTabs.size(); i++) {
            TabInfo tab = mTabs.get(i);
            if (tab.tag.equals(tabId)) {
                newTab = tab;
            }
        }
        if (newTab == null) {
            throw new IllegalStateException("No tab known for tag " + tabId);
        }
        if (mLastTab != newTab) {
            if (ft == null) {
                ft = mFragmentManager.beginTransaction();
            }
            if (mLastTab != null) {
                if (mLastTab.fragment != null) {
                    setVisibleToUser(mLastTab.fragment, false);
                    if (mLastTab.detachable) {
                        ft.detach(mLastTab.fragment);
                    } else {
                        ft.hide(mLastTab.fragment);
                    }
                }
            }
            if (newTab != null) {
                if (newTab.fragment == null) {
                    newTab.fragment = Fragment.instantiate(mContext,
                            newTab.clazz.getName(), newTab.args);
                    ft.add(android.R.id.tabcontent, newTab.fragment, newTab.tag);
                } else {
                    if (newTab.detachable) {
                        ft.attach(newTab.fragment);
                    } else {
                        ft.show(newTab.fragment);
                    }
                }
                setVisibleToUser(newTab.fragment, true);
            }

            mLastTab = newTab;
        }
        return ft;
    }

    @Override
    public FinalTabWidget getTabWidget() {
        return (FinalTabWidget) super.getTabWidget();
    }

    @Override
    public TabViewWrapper getCurrentTabView() {
        return (TabViewWrapper) super.getCurrentTabView();
    }

    public Fragment getCurrentFragment() {
        return getFragment(getCurrentTab());
    }

    public Fragment getFragment(int index) {
        if (index < 0 || index >= mTabs.size()) {
            return null;
        }

        TabInfo tab = mTabs.get(index);
        return tab != null ? tab.fragment : null;
    }

    public Fragment getFragmentByTag(String tag) {
        for (TabInfo tab : mTabs) {
            if (TextUtils.equals(tab.tag, tag)) {
                return tab.fragment;
            }
        }
        return null;
    }

    private void setVisibleToUser(Fragment fragment, boolean isVisibleToUser){
        if (fragment != null) {
            fragment.setMenuVisibility(isVisibleToUser);
            fragment.setUserVisibleHint(isVisibleToUser);
        }
    }
}
