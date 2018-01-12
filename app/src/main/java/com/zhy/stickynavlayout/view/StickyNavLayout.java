package com.zhy.stickynavlayout.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ScrollingView;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import com.zhy.stickynavlayout.R;


public class StickyNavLayout extends LinearLayout implements NestedScrollingParent, ScrollingView {
    private static final String TAG = "StickyNavLayout";

    /**
     * 决定当前控件是否能接收到其内部的View滑动时的参数
     *
     * @param child
     * @param target
     * @param nestedScrollAxes
     * @return
     */
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        if (target instanceof RecyclerView) {
            mChildCanScroll = ViewCompat.canScrollVertically(target, -1);
        }
        // mChildCanScroll = ViewCompat.canScrollVertically(target, -1);
        return true;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
    }

    @Override
    public void onStopNestedScroll(View target) {

    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    }

    /**
     * 传入内部View，如果需要消耗一定的dx,dy，就通过最后一个参数进行指定consumed[1]=dy/2代表消耗一半的dy
     *
     * @param target
     * @param dx
     * @param dy
     * @param consumed
     */
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        //如果上滑且顶部控件未完全隐藏，及消耗掉dy
        //如果下滑且内部控件已经无法继续下拉就消耗掉dy
        Boolean hiddenTop = dy > 0 && getScrollY() < mTopViewHeight;
        Boolean showTop = dy < 0 && getScrollY() >= 0 && !ViewCompat.canScrollVertically(target, -1);
        if (hiddenTop || showTop) {
            consumed[1] = dy;
            scrollBy(0, dy / 2);
        }

    }

    private int TOP_CHILD_FLING_THRESHOLD = 1;

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        //如果是recyclerView 根据判断第一个元素是哪个位置可以判断是否消耗
        //这里判断如果第一个元素的位置是大于TOP_CHILD_FLING_THRESHOLD的
        //认为已经被消耗，在animateScroll里不会对velocityY<0时做处理
        if (target instanceof RecyclerView && velocityY < 0) {
            final RecyclerView recyclerView = (RecyclerView) target;
            final View firstChild = recyclerView.getChildAt(0);
            final int childAdapterPosition = recyclerView.getChildAdapterPosition(firstChild);
            consumed = childAdapterPosition > TOP_CHILD_FLING_THRESHOLD;
        }
        if (consumed) {
            animateScroll(velocityY, computeDuration(velocityY), consumed);

        } else {
            animateScroll(velocityY, computeDuration(0), consumed);
        }
        return true;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        //不做拦截 可以传递给子View
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return 0;
    }

    /**
     * 根据速度计算滚动动画持续时间
     *
     * @param velocityY
     * @return
     */
    private int computeDuration(float velocityY) {
        final int distance;
        if (velocityY > 0) {
            distance = Math.abs(mTop.getHeight() - getScrollY());
        } else {
            distance = Math.abs(mTop.getHeight() - (mTop.getHeight() - getScrollY()));
        }

        final int duration;
        velocityY = Math.abs(velocityY);
        if (velocityY > 0) {
            duration = 3 * Math.round(1000 * (distance / velocityY));
        } else {
            final float distanceRatio = (float) distance / getHeight();
            duration = (int) ((distanceRatio + 1) * 300);
        }
        return duration;
    }

    private void animateScroll(float velocityY, final int duration, boolean consumed) {
        final int currentOffset = getScrollY();
        final int topHeight = mTop.getHeight();
        if (mOffsetAnimator == null) {
            mOffsetAnimator = new ValueAnimator();
            mOffsetAnimator.setInterpolator(mInterpolator);
            mOffsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation.getAnimatedValue() instanceof Integer) {
                        scrollTo(0, (Integer) animation.getAnimatedValue());
                    }
                }
            });
        } else {
            mOffsetAnimator.cancel();
        }

        mOffsetAnimator.setDuration(Math.min(Math.max(duration, 300), 600));
        if (velocityY >= 0) {
            mOffsetAnimator.setIntValues(currentOffset, topHeight);
            mOffsetAnimator.start();
        } else {
            //如果子View没有消耗down事件 那么就让自身滑倒0位置
            if (!consumed) {
                mOffsetAnimator.setIntValues(currentOffset, 0);
                mOffsetAnimator.start();
            }
        }
    }

    private View mTop;
    private View mNav;
    private View mViewPager;

    private int mTopViewHeight;

    private OverScroller mScroller;
    private VelocityTracker mVelocityTracker;
    private ValueAnimator mOffsetAnimator;
    private Interpolator mInterpolator;
    private int mTouchSlop;
    private int mMaximumVelocity, mMinimumVelocity;

    private float mLastY;
    private boolean mDragging;
    private StickyNavLayout.ScrollDetector mScrollDetector;
    private Boolean mChildCanScroll = true;

    public StickyNavLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);

        mScroller = new OverScroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaximumVelocity = ViewConfiguration.get(context)
                .getScaledMaximumFlingVelocity();
        mMinimumVelocity = ViewConfiguration.get(context)
                .getScaledMinimumFlingVelocity();
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(event);
        int action = event.getAction();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished())
                    mScroller.abortAnimation();
                mLastY = y;
                return true;
            case MotionEvent.ACTION_MOVE:
                float dy = y - mLastY;
                if (!mDragging && Math.abs(dy) > mTouchSlop) {
                    mDragging = true;
                }
                if (mDragging) {
                    scrollBy(0, (int) -dy);
                }
                mLastY = y;
                break;
            case MotionEvent.ACTION_CANCEL:
                mDragging = false;
                recycleVelocityTracker();
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_UP:
                mDragging = false;
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityY = (int) mVelocityTracker.getYVelocity();
                if (Math.abs(velocityY) > mMinimumVelocity) {
                    fling(-velocityY);
                }
                recycleVelocityTracker();
                break;
        }


        return super.onTouchEvent(event);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTop = findViewById(R.id.id_stickynavlayout_topview);
        mNav = findViewById(R.id.id_stickynavlayout_indicator);
        View view = findViewById(R.id.id_stickynavlayout_viewpager);
//        if (!(view instanceof ViewPager)) {
//            throw new RuntimeException(
//                    "id_stickynavlayout_viewpager show used by  ViewPager !");
//        }
        mViewPager = view;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //不限制顶部的高度
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        getChildAt(0).measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        ViewGroup.LayoutParams params = mViewPager.getLayoutParams();
        params.height = getMeasuredHeight() - mNav.getMeasuredHeight();
//        setMeasuredDimension(getMeasuredWidth(), mTop.getMeasuredHeight() + mNav.getMeasuredHeight() + mViewPager.getMeasuredHeight());
//        params.height = getMeasuredHeight();
//        setMeasuredDimension(getMeasuredWidth(), mTop.getMeasuredHeight() + mViewPager.getMeasuredHeight());

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTopViewHeight = mTop.getMeasuredHeight();
    }


    public void fling(int velocityY) {
        mScroller.fling(0, getScrollY(), 0, velocityY, 0, 0, 0, mTopViewHeight);
        invalidate();
    }

    @Override
    public void scrollTo(int x, int y) {
        if (y < 0) {
            y = 0;
        }
        if (y > mTopViewHeight) {
            y = mTopViewHeight;
        }
        if (y != getScrollY()) {
            super.scrollTo(x, y);
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            invalidate();
        }
    }


    @Override
    public int computeHorizontalScrollRange() {
        return super.computeHorizontalScrollRange();
    }

    @Override
    public int computeHorizontalScrollOffset() {
        return super.computeHorizontalScrollOffset();
    }

    @Override
    public int computeHorizontalScrollExtent() {
        return super.computeHorizontalScrollExtent();
    }

    @Override
    public int computeVerticalScrollRange() {
        final int contentHeight = getHeight() - getPaddingBottom() - getPaddingTop();
        if (mChildCanScroll && getScrollY() <= 0) {
            return contentHeight;
        }
        int scrollRange = mViewPager.getBottom();
        final int scrollY = getScrollY();
        final int overScrollBottom = Math.max(0, scrollRange - contentHeight);
        if (scrollY < 0) {
            scrollRange -= scrollY;
        } else if (scrollY > overScrollBottom) {
            scrollRange += scrollY - overScrollBottom;
        }
        if (mChildCanScroll) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return scrollRange;
    }

    @Override
    public int computeVerticalScrollOffset() {
        return super.computeVerticalScrollOffset();
    }

    @Override
    public int computeVerticalScrollExtent() {
        return super.computeVerticalScrollExtent();
    }

    /**
     * 设置滚动检测器
     */
    public void setScrollDetector(StickyNavLayout.ScrollDetector detector) {
        mScrollDetector = detector;
    }

    public boolean canScrollUp() {
        return mChildCanScroll;
    }

    public interface ScrollDetector {
        boolean canScroll();
    }
}
