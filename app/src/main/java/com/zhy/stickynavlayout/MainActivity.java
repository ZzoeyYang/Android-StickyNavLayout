package com.zhy.stickynavlayout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

import com.zhy.stickynavlayout.view.BannerItem;
import com.zhy.stickynavlayout.view.BannerView;
import com.zhy.stickynavlayout.view.StickyNavLayout;

import java.util.ArrayList;
import java.util.List;

import online.sniper.utils.ScrollUtils;
import online.sniper.widget.indicator.SlidingTabPageIndicator;
import online.sniper.widget.pager.ChangedTabPagerAdapter;
import online.sniper.widget.refresh.CoolRefreshView;
import online.sniper.widget.refresh.OnRefreshingListener;


public class MainActivity extends FragmentActivity {
    private String[] mTitles = new String[]{"简介", "评价", "相关"};
    private CoolRefreshView mRefreshView;
    private SlidingTabPageIndicator mIndicator;
    private ViewPager mViewPager;
    private ChangedTabPagerAdapter mAdapter;
    private TabFragment[] mFragments = new TabFragment[mTitles.length];
    protected BannerView mBannerView;
    private StickyNavLayout mStickyNavLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initDatas();
        initEvents();
    }

    private void initEvents() {

    }

    private void initDatas() {
        mAdapter.clear();
        for (int i = 0; i <mTitles.length ; i++) {
            mAdapter.addTab(mTitles[i],TabFragment.class,null);
        }
        mAdapter.notifyDataSetChanged();
        mIndicator.notifyDataSetChanged();
    }

    private void initViews() {
        mRefreshView = (CoolRefreshView) findViewById(R.id.refresh_view);
        mIndicator = (SlidingTabPageIndicator) findViewById(R.id.id_stickynavlayout_indicator);
        mViewPager = (ViewPager) findViewById(R.id.view_pager1);
        mBannerView = (BannerView) findViewById(R.id.banner_view);
        mRefreshView.setContentScrollDetector(new CoolRefreshView.ContentScrollDetector() {
            @Override
            public boolean contentCanScrollUp(CoolRefreshView refreshView) {
//                Log.d("TAG", "contentCanScrollUp: " + mStickyNavLayout.canScrollUp());
                return mStickyNavLayout.canScrollUp()||ScrollUtils.canScrollUp(mStickyNavLayout);
            }
        });
//        mStickyNavLayout.setScrollDetector(new StickyNavLayout.ScrollDetector() {
//            @Override
//            public boolean canScroll() {
//                return false;
//            }
//        });
        mBannerView.setAspectRadio(720, 240);
        mAdapter = new ChangedTabPagerAdapter(this);
        mViewPager.setAdapter(mAdapter);
        mIndicator.setViewPager(mViewPager);
        mIndicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                mViewPager.setCurrentItem(position);
            }
        });

        mStickyNavLayout = (StickyNavLayout) findViewById(R.id.id_stickyNavLayout);
//        mStickyNavLayout.setScrollDetector(new StickyNavLayout.ScrollDetector() {
//            @Override
//            public boolean canScroll() {
//                return true;
//            }
//        });
        List<BannerItem> list = new ArrayList<>();
        BannerItem item = new BannerItem("");
        list.add(item);
        list.add(item);
        list.add(item);
        list.add(item);
        mBannerView.setItems(list);

        mRefreshView.addOnPullListener(new OnRefreshingListener() {
            @Override
            public void onRefreshing(CoolRefreshView refreshView) {
                mRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mRefreshView.isRefreshing()) {
                            mRefreshView.setRefreshing(false);
                        }
                    }
                }, 1000);

            }
        });

		/*
        RelativeLayout ll = (RelativeLayout) findViewById(R.id.id_stickynavlayout_topview);
		TextView tv = new TextView(this);
		tv.setText("我的动态添加的");
		tv.setBackgroundColor(0x77ff0000);
		ll.addView(tv, new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, 600));
		*/
    }


}
