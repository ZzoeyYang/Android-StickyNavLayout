package com.zhy.stickynavlayout.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.zhy.stickynavlayout.R;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("HandlerLeak")
public class BannerView extends RelativeLayout implements BannerViewPager.TimerController {

    public static final long DEFAULT_TIMER_INTERNAL = 3000;//默认轮播间隔(单位：ms)

    private BannerViewPager mViewPager;
    private BannerAdapter mAdapter;
    private LinearLayout mIndicator; // 指示器
    private View mCloseButton; //关闭按钮

    private ScaleType mScaleType = ScaleType.CENTER_CROP;

    private int mPosition = 0; //记录上次选中的位置ViewPager
    private OnItemClickListener mOnItemClickListener;

    // 定时器相关
    private long mTimerInterval = DEFAULT_TIMER_INTERNAL; //广告轮播实际间隔时间
    private boolean isPlaying = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                mViewPager.setCurrentItem(msg.arg1);
            }
        }
    };
    private Runnable mTimerTask = new Runnable() {

        @Override
        public void run() {
            try {
                int count = mAdapter.getCount();
                if (count <= 0) {
                    return;
                }
                int position = (mViewPager.getCurrentItem() + 1) % count;
                //发送消息，让主线程刷新gallery的选中状态
                Message msg = new Message();
                msg.what = 1; // 消息标识
                msg.arg1 = position; // 当前位置
                mHandler.sendMessage(msg);
                if (isPlaying) {
                    startTimer(mTimerInterval);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 高宽比
     */
    private double mAspectRadio = 0.3;

    public BannerView(Context context) {
        this(context, null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.banner_view, this);
        mViewPager = (BannerViewPager) findViewById(R.id.view_pager);
        mIndicator = (LinearLayout) findViewById(R.id.indicator);
        mCloseButton = findViewById(R.id.close_button);
        mCloseButton.setVisibility(View.GONE);

        mAdapter = new BannerAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.setTimerController(this);
        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                onIndicatorChanged(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = (int) (width * mAspectRadio + 0.5);
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
    }

    public void setScaleType(ScaleType scaleType) {
        mScaleType = scaleType;
    }

    /**
     * 设置轮播图
     */
    public void setItems(List<BannerItem> items) {
        if (items == null || items.size() <= 0) {
            return;
        }

        mAdapter.setItems(items);
        mViewPager.setCurrentItem(0);
        mViewPager.invalidate();
        mIndicator.removeAllViews();
        int size = items.size();
        for (int i = 0; i < items.size(); i++) {
            ImageView dotImage = new ImageView(getContext());
            dotImage.setPadding(7, 0, 7, 0);
            dotImage.setImageResource(R.drawable.banner_indicator);
            if (i == 0) {
                dotImage.setSelected(true);
            } else {
                dotImage.setSelected(false);
            }
            mIndicator.addView(dotImage);
        }

        mIndicator.setVisibility(size > 1 ? View.VISIBLE : View.GONE);
    }

    public List<BannerItem> getItems() {
        return mAdapter.mItems;
    }

    /**
     * 设置关闭监听器
     */
    public void setCloseListener(OnClickListener l) {
        if (l != null) {
            this.mCloseButton.setVisibility(View.VISIBLE);
            this.mCloseButton.setOnClickListener(l);
        } else {
            this.mCloseButton.setVisibility(View.GONE);
        }
    }

    /**
     * 指示器变化了
     */
    private void onIndicatorChanged(int position) {
        if (mIndicator == null || mPosition == position) {
            return;
        }
        ImageView lastDotImage = (ImageView) mIndicator.getChildAt(mPosition);
        ImageView dotImage = (ImageView) mIndicator.getChildAt(position);
        if (lastDotImage != null && dotImage != null) {
            lastDotImage.setSelected(false); // 之前选中的圆点显示正常状态
            dotImage.setSelected(true); // 当前的原点显示选中状态
            mPosition = position;
        }
    }

    /**
     * 设置高宽比
     */
    public void setAspectRadio(double width, double height) {
        mAspectRadio = height / width;
    }

    /**
     * 设置高宽比
     */
    public void setAspectRadio(double radio) {
        mAspectRadio = radio;
    }

    /**
     * 获取高宽比
     */
    public double getAspectRadio() {
        return mAspectRadio;
    }

    /**
     * 开启定时器
     */
    public void startTimer() {
        startTimer(getTimerInterval());
    }

    /**
     * 开启定时器
     */
    public void startTimer(long delay) {
        isPlaying = true;
        mHandler.removeCallbacks(mTimerTask);
        mHandler.postDelayed(mTimerTask, delay);
    }

    /**
     * 暂停定时器
     */
    public void cancelTimer() {
        isPlaying = false;
        mHandler.removeCallbacks(mTimerTask);
    }

    /**
     * 获取轮播时两张图片之间的时间间隔
     */
    public long getTimerInterval() {
        return mTimerInterval;
    }

    /**
     * 设置轮播时两张图片之间的时间间隔
     */
    public void setTimerInterval(long interval) {
        mTimerInterval = interval;
    }

    /**
     * 判断是否在自动轮播中
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * 点击轮播图的监听器
     */
    public interface OnItemClickListener {
        void onItemClick(BannerItem item, int position);
    }

    private class BannerAdapter extends PagerAdapter {

        private final List<BannerItem> mItems = new ArrayList<BannerItem>();
        private final ArrayList<View> mImageViews = new ArrayList<View>();

        public BannerAdapter() {
        }

        @SuppressLint("InflateParams")
        private void onItemsChanged() {
            int count = getCount();
            final int imageViewSize = mImageViews.size();
            for (int i = 0; i < count; i++) {
                View convertView = null;
                if (i < imageViewSize) {
                    convertView = mImageViews.get(i);
                } else {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.banner_item_view, null);
                }
                final ImageView imageView = (ImageView) convertView.findViewById(R.id.banner_image);
                if (mScaleType != null) {
                    imageView.setScaleType(mScaleType);
                }

                final BannerItem item = mItems.get(i);
                final int position = i;

                Glide.with(getContext())
                        .load(item.getImage())
                        .placeholder(R.drawable.banner_default)
                        .error(R.drawable.banner_default)
                        .into(imageView);

                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (mOnItemClickListener != null) {
                            mOnItemClickListener.onItemClick(item, position);
                        }
                    }
                });

                if (i < imageViewSize) {
                    mImageViews.set(i, convertView);
                } else {
                    mImageViews.add(convertView);
                }
            }
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View convertView = mImageViews.get(position);
            container.addView(convertView);
            return convertView;
        }

        public void setItems(List<BannerItem> items) {
            mItems.clear();
            if (items != null && !items.isEmpty()) {
                mItems.addAll(items);
            }
            onItemsChanged();
            notifyDataSetChanged();
        }

        public List<BannerItem> getItems() {
            return mItems;
        }
    }
}
