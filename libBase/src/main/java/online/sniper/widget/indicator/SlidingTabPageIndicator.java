package online.sniper.widget.indicator;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.CheckedTextView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import online.sniper.R;


public class SlidingTabPageIndicator extends HorizontalScrollView implements PageIndicator {

    public OnPageChangeListener mListener;
    private LinearLayout.LayoutParams mDefaultTabLayoutParams;
    private LinearLayout.LayoutParams mStretchTabLayoutParams;
    private LinearLayout mTabLayout;
    private ViewPager mViewPager;
    private int mTabCount;
    private int mCurrentPage = 0;
    private float mCurrentPageOffset = 0f;
    private boolean mCanScroll = true;
    private int mScrollOffsetDirection = 0;
    private int mScrollOffset;
    private int mLastScrollX = 0;
    private Paint mRectPaint;
    private RectF mRectF = new RectF();
    private IndicatorStyle mIndicatorStyle = IndicatorStyle.AUTO;
    private int mIndicatorHeight;
    private int mIndicatorColor;
    private int mUnderlineHeight;
    private int mUnderlineColor;
    private Paint mDividerPaint;
    private int mDividerWidth;
    private int mDividerPadding;
    private int mDividerColor;
    private int mTabPadding;
    private int mTextSize;
    private ColorStateList mTextColor;
    private boolean mStretch = true;
    private boolean mSwitchPageWithAnimation = true;

    public SlidingTabPageIndicator(Context context) {
        this(context, null);
    }

    public SlidingTabPageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.vpiSlidingTabPageIndicatorStyle);
    }

    public SlidingTabPageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setFillViewport(true);
        setWillNotDraw(false);

        mTabLayout = new LinearLayout(context);
        mTabLayout.setOrientation(LinearLayout.HORIZONTAL);
        mTabLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        addView(mTabLayout);

        final Resources res = getResources();

        mScrollOffset = res.getDimensionPixelSize(R.dimen.default_sliding_tab_indicator_scroll_offset);
        mIndicatorHeight = res.getDimensionPixelSize(R.dimen.default_sliding_tab_indicator_height);
        mIndicatorColor = res.getColor(R.color.default_sliding_tab_indicator_color);

        mUnderlineHeight = res.getDimensionPixelSize(R.dimen.default_sliding_tab_indicator_underline_height);
        mUnderlineColor = res.getColor(R.color.default_sliding_tab_indicator_underline_color);

        mDividerWidth = res.getDimensionPixelSize(R.dimen.default_sliding_tab_indicator_divider_width);
        mDividerPadding = res.getDimensionPixelSize(R.dimen.default_sliding_tab_indicator_divider_padding);
        mDividerColor = res.getColor(R.color.default_sliding_tab_indicator_divider_color);

        mTabPadding = res.getDimensionPixelSize(R.dimen.default_sliding_tab_indicator_tab_padding);
        mTextSize = res.getDimensionPixelSize(R.dimen.default_sliding_tab_indicator_text_size);
        mTextColor = res.getColorStateList(R.color.default_sliding_tab_indicator_text_color);

        if (isInEditMode()) {
            return;
        }

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabPageIndicator);
        try {
            mCanScroll = a.getBoolean(R.styleable.SlidingTabPageIndicator_canScroll, true);
            mScrollOffsetDirection = a.getInteger(R.styleable.SlidingTabPageIndicator_scrollOffsetDirection, 0);
            mScrollOffset = a.getDimensionPixelSize(R.styleable.SlidingTabPageIndicator_scrollOffset, mScrollOffset);
            if (mScrollOffsetDirection == 1) {
                mScrollOffset = getResources().getDisplayMetrics().widthPixels - mScrollOffset;
            }
            mIndicatorStyle = IndicatorStyle.fromValue(a.getInteger(R.styleable.SlidingTabPageIndicator_indicatorStyle, 0));
            mIndicatorHeight = a.getDimensionPixelSize(R.styleable.SlidingTabPageIndicator_indicatorHeight, mIndicatorHeight);
            mIndicatorColor = a.getColor(R.styleable.SlidingTabPageIndicator_indicatorColor, mIndicatorColor);

            mUnderlineHeight = a.getDimensionPixelSize(R.styleable.SlidingTabPageIndicator_underlineHeight, mUnderlineHeight);
            mUnderlineColor = a.getColor(R.styleable.SlidingTabPageIndicator_underlineColor, mUnderlineColor);

            mDividerPadding = a.getDimensionPixelSize(R.styleable.SlidingTabPageIndicator_dividerPadding, mDividerPadding);
            mDividerColor = a.getColor(R.styleable.SlidingTabPageIndicator_dividerColor, mDividerColor);
            mDividerWidth = a.getDimensionPixelSize(R.styleable.SlidingTabPageIndicator_dividerWidth, mDividerWidth);

            mTabPadding = a.getDimensionPixelSize(R.styleable.SlidingTabPageIndicator_tabPadding, mTabPadding);
            mTextSize = a.getDimensionPixelSize(R.styleable.SlidingTabPageIndicator_android_textSize, mTextSize);
            ColorStateList textColor = a.getColorStateList(R.styleable.SlidingTabPageIndicator_android_textColor);
            if (textColor != null) {
                mTextColor = textColor;
            }
            mStretch = a.getBoolean(R.styleable.SlidingTabPageIndicator_stretch, mStretch);
        } finally {
            a.recycle();
        }

        mRectPaint = new Paint();
        mRectPaint.setAntiAlias(true);
        mRectPaint.setStyle(Style.FILL);

        mDividerPaint = new Paint();
        mDividerPaint.setAntiAlias(true);
        mDividerPaint.setStrokeWidth(mDividerWidth);

        mDefaultTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        mStretchTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1.0f);
    }

    public void setSwitchPageWithAnimation(boolean switchPageWithAnimation) {
        mSwitchPageWithAnimation = switchPageWithAnimation;
    }

    @SuppressWarnings("deprecation")
    public void setViewPager(ViewPager view) {
        if (mViewPager == view) {
            return;
        }
        if (mViewPager != null) {
            mViewPager.setOnPageChangeListener(null);
        }
        if (view.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        mViewPager = view;
        mViewPager.setOnPageChangeListener(this);
        invalidate();
        notifyDataSetChanged();
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.mListener = listener;
    }

    public void notifyDataSetChanged() {
        mTabLayout.removeAllViews();
        mTabCount = mViewPager.getAdapter().getCount();

        for (int i = 0; i < mTabCount; i++) {
            addTab(i, mViewPager.getAdapter().getPageTitle(i).toString());
        }

        updateTabStyles(mViewPager.getCurrentItem());

        getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mCurrentPage = mViewPager.getCurrentItem();
                scrollToChild(mCurrentPage, 0);
            }
        });
    }

    private void addTab(final int position, CharSequence title) {
        TabView tab = new TabView(getContext());
        tab.setText(title);
        tab.setGravity(Gravity.CENTER);
        tab.setSingleLine();
        tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        tab.setTextColor(mTextColor);
        tab.setFocusable(true);
        tab.setPadding(mTabPadding, 0, mTabPadding, 0);
        tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(position, mSwitchPageWithAnimation);
            }
        });
        mTabLayout.addView(tab, position, mStretch ? mStretchTabLayoutParams : mDefaultTabLayoutParams);
    }

    private void updateTabStyles(int position) {
        for (int i = 0; i < mTabCount; i++) {
            View child = mTabLayout.getChildAt(i);
            if (child instanceof TabView) {
                CheckedTextView tabView = (TabView) child;
                tabView.setChecked(i == position);
            }
        }
    }

    private void scrollToChild(int position, int offset) {
        if (mTabCount == 0 || !mCanScroll) {
            return;
        }
        View child = mTabLayout.getChildAt(position);
        if (child == null) {
            return;
        }

        int newScrollX = child.getLeft() + offset;
        if (position > 0 || offset > 0) {
            newScrollX -= mScrollOffset;
        }
        if (newScrollX != mLastScrollX) {
            mLastScrollX = newScrollX;
            scrollTo(newScrollX, 0);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode() || mTabCount == 0) {
            return;
        }

        final int height = getHeight();

        // default: line below current tab
        View currentTab = mTabLayout.getChildAt(mCurrentPage);
        if (currentTab == null) {
            return;
        }
        float lineLeft = currentTab.getLeft();
        float lineRight = currentTab.getRight();

        // if there is an offset, start interpolating left and right coordinates
        // between current and next tab
        if (mCurrentPageOffset > 0f && mCurrentPage < mTabCount - 1) {
            View nextTab = mTabLayout.getChildAt(mCurrentPage + 1);
            final float nextTabLeft = nextTab.getLeft();
            final float nextTabRight = nextTab.getRight();

            lineLeft = (mCurrentPageOffset * nextTabLeft + (1f - mCurrentPageOffset) * lineLeft);
            lineRight = (mCurrentPageOffset * nextTabRight + (1f - mCurrentPageOffset) * lineRight);
        }

        final float currentTabWidth = currentTab.getWidth();
        float padding = 0;
        switch (mIndicatorStyle) {
            case PADDING:
                padding = mTabPadding;
                break;
            case FILL:
                padding = 0;
                break;
            case AUTO:
            default:
                float indicatorWidth = currentTabWidth;
                if (currentTab instanceof TabView) {
                    TabView ctv = (TabView) currentTab;
                    CharSequence text = ctv.getText();
                    Paint paint = ctv.getPaint();
                    float needWidth = paint.measureText(text, 0, text.length()) + mTabPadding + mTabPadding;
                    if (needWidth > currentTabWidth) {
                        indicatorWidth = currentTabWidth;
                    } else {
                        indicatorWidth = needWidth;
                    }
                } else {
                    indicatorWidth = currentTabWidth;
                }

                padding = (currentTabWidth - indicatorWidth) / 2;
                break;
        }

        mRectF.left = lineLeft + padding;
        mRectF.right = lineRight - padding;
        mRectF.top = height - mIndicatorHeight;
        mRectF.bottom = height;

        // draw indicator line
        mRectPaint.setColor(mIndicatorColor);
        canvas.drawRect(mRectF, mRectPaint);

        // draw underline
        mRectPaint.setColor(mUnderlineColor);
        canvas.drawRect(0, height - mUnderlineHeight, mTabLayout.getWidth(), height, mRectPaint);

        // draw divider
        if (mDividerWidth > 0) {
            mDividerPaint.setColor(mDividerColor);
            for (int i = 0; i < mTabCount - 1; i++) {
                View tab = mTabLayout.getChildAt(i);
                canvas.drawLine(tab.getRight(), mDividerPadding, tab.getRight(), height - mDividerPadding, mDividerPaint);
            }
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPage = savedState.mCurrentPage;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mCurrentPage = mCurrentPage;
        return savedState;
    }

    @Override
    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(mViewPager);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        if (mViewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        mViewPager.setCurrentItem(item);
        mCurrentPage = item;
        invalidate();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mCurrentPage = position;
        mCurrentPageOffset = positionOffset;
        View child = mTabLayout.getChildAt(position);
        if (child != null) {
            scrollToChild(position, (int) (positionOffset * child.getWidth()));
        }
        invalidate();
        if (mListener != null) {
            mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            scrollToChild(mViewPager.getCurrentItem(), 0);
        }

        if (mListener != null) {
            mListener.onPageScrollStateChanged(state);
        }
    }

    @Override
    public void onPageSelected(int position) {
        updateTabStyles(position);
        if (mListener != null) {
            mListener.onPageSelected(position);
        }
    }

    public enum IndicatorStyle {
        AUTO(0), PADDING(1), FILL(2);

        public final int value;

        private IndicatorStyle(int value) {
            this.value = value;
        }

        public static IndicatorStyle fromValue(int value) {
            for (IndicatorStyle style : IndicatorStyle.values()) {
                if (style.value == value) {
                    return style;
                }
            }
            return null;
        }
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int mCurrentPage;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mCurrentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mCurrentPage);
        }
    }

    private class TabView extends AppCompatCheckedTextView {
        public TabView(Context context) {
            super(context, null, R.attr.vpiSlidingTabPageIndicatorStyle);
        }

        @Override
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
