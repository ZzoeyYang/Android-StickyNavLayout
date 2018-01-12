package online.sniper.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.OverScroller;
import android.widget.RelativeLayout;

import online.sniper.R;

/**
 * 可以滚动的用于分段显示内容的视图，
 * <p/>
 *
 * @author wangpeihe
 */
public class StagedScrollLayout extends RelativeLayout {

    public static final String TAG = StagedScrollLayout.class.getSimpleName();
    public static final boolean DEBUG = true;

    static final int ANIMATED_SCROLL_GAP = 250;

    static final float MAX_SCROLL_FACTOR = 0.5f;
    /**
     * Sentinel value for no current active pointer.
     * Used by {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;
    private final Rect mTempRect = new Rect();
    private long mLastScroll;
    private View mHeaderView;
    private View mContentView;
    private boolean mInflated = false;
    private OverScroller mScroller;
    /**
     * Position of the last motion event.
     */
    private float mLastMotionY;
    /**
     * True if the user is currently dragging this ScrollView around. This is
     * not the same as 'is being flinged', which can be checked by
     * mScroller.isFinished() (flinging begins when the user lifts his finger).
     */
    private boolean mIsBeingDragged = false;
    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;
    /**
     * Whether arrow scrolling is animated.
     */
    private boolean mSmoothScrollingEnabled = true;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mOverScrollDistance;
    private int mOverFlingDistance;
    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private int mActivePointerId = INVALID_POINTER;
    private double mDistanceX, mDistanceY;
    private float mLastX, mLastY;
    private ScrollDetector mScrollDetector;
    private OnShrinkHeaderListener mOnShrinkHeaderListener;
    private boolean mStickyMode = true;

    public StagedScrollLayout(Context context) {
        this(context, null);
    }

    public StagedScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initScrollView();
    }

    private void initScrollView() {
        mScroller = new OverScroller(getContext());
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mOverScrollDistance = configuration.getScaledOverscrollDistance();
        mOverFlingDistance = configuration.getScaledOverflingDistance();
    }

    /**
     * 获取头部视图
     */
    public View getHeaderView() {
        return mHeaderView;
    }

    /**
     * 获内容视图
     */
    public View getContentView() {
        return mContentView;
    }

    /**
     * 设置滚动检测器
     */
    public void setScrollDetector(ScrollDetector detector) {
        mScrollDetector = detector;
    }

    /**
     * 设置监听头部视图收缩的监听器
     */
    public void setOnShrinkHeaderListener(OnShrinkHeaderListener l) {
        mOnShrinkHeaderListener = l;
    }

    /**
     * 设置粘性模式，即自动吸顶模式
     */
    public void setStickyMode(boolean sticky) {
        mStickyMode = sticky;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        try {
            setup(R.id.magicbox_header, R.id.magicbox_content);
        } catch (Exception e) {
        }
    }

    public void setup(int headerId, int contentId) {
        if (mInflated) {
            return;
        }
        View headerView = findViewById(headerId);
        if (headerView == null) {
            throw new RuntimeException("Can not find mHeaderView by headerId=" + headerId + ".");
        }
        if (headerView.getParent() != this) {
            throw new RuntimeException("mHeaderView's parent must be this view.");
        }
        View contentView = findViewById(contentId);
        if (contentView == null) {
            throw new RuntimeException("Can not find mContentView by contentId=" + contentId + ".");
        }
        if (contentView.getParent() != this) {
            throw new RuntimeException("mContentView's parent must be this view.");
        }

        LayoutParams lp;
        lp = (LayoutParams) headerView.getLayoutParams();
        lp.addRule(ALIGN_PARENT_TOP);
        lp.addRule(ALIGN_PARENT_LEFT);
        lp.addRule(ALIGN_PARENT_RIGHT);
        lp.setMargins(0, 0, 0, 0);

        lp = (LayoutParams) contentView.getLayoutParams();
        lp.addRule(ALIGN_PARENT_LEFT);
        lp.addRule(ALIGN_PARENT_RIGHT);
        lp.addRule(BELOW, headerId);
        lp.setMargins(0, 0, 0, 0);

        mHeaderView = headerView;
        mContentView = contentView;
        mInflated = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!mInflated) {
            throw new RuntimeException("Must call setup().");
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int height = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        int widthSpec = MeasureSpec.makeMeasureSpec(width > 0 ? width : 0, MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(height > 0 ? height : 0, MeasureSpec.EXACTLY);
        mContentView.measure(widthSpec, heightSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        int ll = getPaddingLeft();
        int tt = mHeaderView.getBottom();
        int rr = getPaddingLeft() + mContentView.getMeasuredWidth();
        int bb = mHeaderView.getBottom() + mContentView.getMeasuredHeight();

        mContentView.layout(ll, tt, rr, bb);

        // Calling this with the present values causes it to re-clam them
        scrollTo(getScrollX(), getScrollY());
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // 此处不要调用super.requestDisallowInterceptTouchEvent()，
        // 否则不会调用onInterceptTouchEvent().
//        if (disallowIntercept) {
//            recycleVelocityTracker();
//        }
//        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mDistanceX = mDistanceY = 0f;
                mLastX = ev.getX();
                mLastY = ev.getY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final float x = ev.getX();
                final float y = ev.getY();

                mDistanceX = +Math.abs(x - mLastX);
                mDistanceY = +Math.abs(y - mLastY);
                mLastX = x;
                mLastY = y;

                if (mDistanceX > mDistanceY) {
                    mIsBeingDragged = false;
                    mLastMotionY = y;
                    mActivePointerId = ev.getPointerId(0);

                    initOrResetVelocityTracker();
                    mVelocityTracker.addMovement(ev);
                    return false;
                }
                break;
            }
        }

        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }

                final int pointerIndex = ev.findPointerIndex(activePointerId);
                final float y = ev.getY(pointerIndex);
                final float yDelta = (int) (y - mLastMotionY);
                if (Math.abs(yDelta) > mTouchSlop) {
                    if (yDelta < 0) {
                        // 向上推时，若mHeaderView未完全收缩则拦截
                        mIsBeingDragged = getScrollY() < mHeaderView.getHeight();
                    } else {
                        // 向下拉时，若允许滑动则拦截
                        mIsBeingDragged = canScroll();
                    }

                    mLastMotionY = y;
                    initVelocityTrackerIfNotExists();
                    mVelocityTracker.addMovement(ev);
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                final float y = ev.getY();
                if (!inChild((int) ev.getX(), (int) y)) {
                    mIsBeingDragged = false;
                    recycleVelocityTracker();
                    break;
                }

                mLastMotionY = y;
                mActivePointerId = ev.getPointerId(0);

                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                mIsBeingDragged = !mScroller.isFinished();
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // mIsBeingDragged=true时，一定要拦截，不然会产生点击事件
                final boolean intercept = mIsBeingDragged;
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;

                initVelocityTrackerIfNotExists();
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityX = (int) velocityTracker.getXVelocity(mActivePointerId);
                int velocityY = (int) velocityTracker.getYVelocity(mActivePointerId);

                int vx = Math.abs(velocityX);
                int vy = Math.abs(velocityY);
                if (vy > vx && vy > mMinimumVelocity) {
                    fling(velocityY);
                } else {
                    if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0,
                            getScrollRange())) {
                        invalidate();
                    }
                }

                recycleVelocityTracker();
                if (intercept) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mIsBeingDragged = mInflated;
                if (!mIsBeingDragged) {
                    return false;
                }

                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                mLastMotionY = ev.getY();
                mActivePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                    final float y = ev.getY(activePointerIndex);
                    final int deltaY = (int) (mLastMotionY - y);
                    mLastMotionY = y;

                    final int oldX = getScrollX();
                    final int oldY = getScrollY();
                    final int range = getScrollRange();

                    // 若允许滑动，则滑动
                    if (canScroll()) {
                        overScrollBy(0, deltaY, 0, getScrollY(), 0, range,
                                0, mOverScrollDistance, true);
                        onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int velocityX = (int) velocityTracker.getXVelocity(mActivePointerId);
                    int velocityY = (int) velocityTracker.getYVelocity(mActivePointerId);

                    int vx = Math.abs(velocityX);
                    int vy = Math.abs(velocityY);
                    if (vy > vx && vy > mMinimumVelocity) {
                        fling(velocityY);
                    } else {
                        if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0,
                                getScrollRange())) {
                            invalidate();
                        }
                    }

                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged && mInflated) {
                    if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, getScrollRange())) {
                        invalidate();
                    }
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = ev.getActionIndex();
                mLastMotionY = ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId));
                break;
        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionY = ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY,
                                  boolean clampedX, boolean clampedY) {
        // Treat animating scrolls differently; see #computeScroll() for why.
        if (!mScroller.isFinished()) {
            scrollTo(scrollX, scrollY);
            invalidate();
            if (clampedY) {
                mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, getScrollRange());
            }
        } else {
            super.scrollTo(scrollX, scrollY);
        }
        awakenScrollBars();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mInflated && mOnShrinkHeaderListener != null) {
            final int progress = getScrollY();
            final int total = mHeaderView.getHeight();
            // 性能问题，故而post()
            post(new Runnable() {
                @Override
                public void run() {
                    if (mInflated && mOnShrinkHeaderListener != null) {
                        mOnShrinkHeaderListener.onShrink(progress, total);
                    }
                }
            });
        }
    }

    protected boolean canScroll() {
        if (!mInflated) {
            return false;
        }
        return mScrollDetector == null || mScrollDetector.canScroll();
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        if (!mInflated) {
            return 0.0f;
        }

        final int length = getVerticalFadingEdgeLength();
        final int scrollY = getScrollY();
        if (scrollY < length) {
            return scrollY / (float) length;
        }

        return 1.0f;
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        if (!mInflated) {
            return 0.0f;
        }

        final int length = getVerticalFadingEdgeLength();
        final int bottomEdge = getHeight() - getPaddingBottom();
        final int span = mContentView.getBottom() - getScrollY() - bottomEdge;
        if (span < length) {
            return span / (float) length;
        }

        return 1.0f;
    }

    /**
     * @return The maximum amount this scroll view will scroll in response to
     * an arrow event.
     */
    public int getMaxScrollAmount() {
        return (int) (MAX_SCROLL_FACTOR * getHeight());
    }

    /**
     * @return Whether arrow scrolling will animate its transition.
     */
    public boolean isSmoothScrollingEnabled() {
        return mSmoothScrollingEnabled;
    }

    /**
     * Set whether arrow scrolling will animate its transition.
     *
     * @param smoothScrollingEnabled whether arrow scrolling will animate its transition
     */
    public void setSmoothScrollingEnabled(boolean smoothScrollingEnabled) {
        mSmoothScrollingEnabled = smoothScrollingEnabled;
    }

    private boolean inChild(int x, int y) {
        if (mInflated) {
            final int scrollY = getScrollY();
            final int left = Math.min(mHeaderView.getLeft(), mContentView.getLeft());
            final int top = Math.min(mHeaderView.getTop(), mContentView.getTop());
            final int right = Math.max(mHeaderView.getRight(), mContentView.getRight());
            final int bottom = Math.max(mHeaderView.getBottom(), mContentView.getBottom());
            return !(y < top - scrollY
                    || y >= bottom - scrollY
                    || x < left
                    || x >= right);
        }
        return false;
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
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

    private int getScrollRange() {
        int scrollRange = 0;
        if (mInflated) {
            final int childHeight = mHeaderView.getHeight() + mContentView.getHeight();
            scrollRange = Math.max(0,
                    childHeight - (getHeight() - getPaddingBottom() - getPaddingTop()));
        }
        return scrollRange;
    }

    /**
     * @return whether the descendant of this scroll view is scrolled off
     * screen.
     */
    private boolean isOffScreen(View descendant) {
        return !isWithinDeltaOfScreen(descendant, 0, getHeight());
    }

    /**
     * @return whether the descendant of this scroll view is within delta
     * pixels of being on the screen.
     */
    private boolean isWithinDeltaOfScreen(View descendant, int delta, int height) {
        descendant.getDrawingRect(mTempRect);
        offsetDescendantRectToMyCoords(descendant, mTempRect);

        return (mTempRect.bottom + delta) >= getScrollY()
                && (mTempRect.top - delta) <= (getScrollY() + height);
    }

    /**
     * Smooth scroll by a Y delta
     *
     * @param delta the number of pixels to scroll by on the Y axis
     */
    private void doScrollY(int delta) {
        if (delta != 0) {
            if (mSmoothScrollingEnabled) {
                smoothScrollBy(0, delta);
            } else {
                scrollBy(0, delta);
            }
        }
    }

    /**
     * Like {@link View#scrollBy}, but scroll smoothly instead of immediately.
     *
     * @param dx the number of pixels to scroll by on the X axis
     * @param dy the number of pixels to scroll by on the Y axis
     */
    public final void smoothScrollBy(int dx, int dy) {
        if (!mInflated) {
            // Nothing to do.
            return;
        }
        long duration = AnimationUtils.currentAnimationTimeMillis() - mLastScroll;
        if (duration > ANIMATED_SCROLL_GAP) {
            final int height = getHeight() - getPaddingBottom() - getPaddingTop();
            final int bottom = mHeaderView.getHeight() + mContentView.getHeight();
            final int maxY = Math.max(0, bottom - height);
            final int scrollY = getScrollY();
            dy = Math.max(0, Math.min(scrollY + dy, maxY)) - scrollY;

            mScroller.startScroll(getScrollX(), scrollY, 0, dy);
            invalidate();
        } else {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            scrollBy(dx, dy);
        }
        mLastScroll = AnimationUtils.currentAnimationTimeMillis();
    }

    /**
     * Like {@link #scrollTo}, but scroll smoothly instead of immediately.
     *
     * @param x the position where to scroll on the X axis
     * @param y the position where to scroll on the Y axis
     */
    public final void smoothScrollTo(int x, int y) {
        smoothScrollBy(x - getScrollX(), y - getScrollY());
    }

    /**
     * <p>The scroll range of a scroll view is the overall height of all of its
     * children.</p>
     */
    @Override
    protected int computeVerticalScrollRange() {
        final int contentHeight = getHeight() - getPaddingBottom() - getPaddingTop();
        if (!mInflated) {
            return contentHeight;
        }

        int scrollRange = mContentView.getBottom();
        final int scrollY = getScrollY();
        final int overScrollBottom = Math.max(0, scrollRange - contentHeight);
        if (scrollY < 0) {
            scrollRange -= scrollY;
        } else if (scrollY > overScrollBottom) {
            scrollRange += scrollY - overScrollBottom;
        }

        return scrollRange;
    }

    @Override
    protected int computeVerticalScrollOffset() {
        return Math.max(0, super.computeVerticalScrollOffset());
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();

        int childWidthMeasureSpec;
        int childHeightMeasureSpec;

        childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, getPaddingLeft()
                + getPaddingRight(), lp.width);

        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed,
                                           int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin
                        + widthUsed, lp.width);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            // This is called at drawing time by ViewGroup.  We don't want to
            // re-show the scrollbars at this point, which scrollTo will do,
            // so we replicate most of scrollTo here.
            //
            //         It's a little odd to call onScrollChanged from inside the drawing.
            //
            //         It is, except when you remember that computeScroll() is used to
            //         animate scrolling. So unless we want to defer the onScrollChanged()
            //         until the end of the animated scrolling, we don't really have a
            //         choice here.
            //
            //         I agree.  The alternative, which I think would be worse, is to post
            //         something and tell the subclasses later.  This is bad because there
            //         will be a window where mScrollX/Y is different from what the app
            //         thinks it is.
            //
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            if (oldX != x || oldY != y) {
                final int range = getScrollRange();
                overScrollBy(x - oldX, y - oldY, oldX, oldY, 0, range,
                        0, mOverFlingDistance, false);
                onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
            }

            awakenScrollBars();

            // Keep on drawing until the animation has finished.
            postInvalidate();
        }
    }

    /**
     * If rect is off screen, scroll just enough to get it (or at least the
     * first screen size chunk of it) on screen.
     *
     * @param rect      The rectangle.
     * @param immediate True to scroll immediately without animation
     * @return true if scrolling was performed
     */
    private boolean scrollToChildRect(Rect rect, boolean immediate) {
        final int delta = computeScrollDeltaToGetChildRectOnScreen(rect);
        final boolean scroll = delta != 0;
        if (scroll) {
            if (immediate) {
                scrollBy(0, delta);
            } else {
                smoothScrollBy(0, delta);
            }
        }
        return scroll;
    }

    /**
     * Compute the amount to scroll in the Y direction in order to get
     * a rectangle completely on the screen (or, if taller than the screen,
     * at least the first screen size chunk of it).
     *
     * @param rect The rect.
     * @return The scroll delta.
     */
    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        if (!mInflated) return 0;

        int height = getHeight();
        int screenTop = getScrollY();
        int screenBottom = screenTop + height;

        int fadingEdge = getVerticalFadingEdgeLength();

        // leave room for top fading edge as long as rect isn't at very top
        if (rect.top > 0) {
            screenTop += fadingEdge;
        }

        // leave room for bottom fading edge as long as rect isn't at very bottom
        if (rect.bottom < mContentView.getBottom()) {
            screenBottom -= fadingEdge;
        }

        int scrollYDelta = 0;

        if (rect.bottom > screenBottom && rect.top > screenTop) {
            // need to move down to get it in view: move down just enough so
            // that the entire rectangle is in view (or at least the first
            // screen size chunk).

            if (rect.height() > height) {
                // just enough to get screen size chunk on
                scrollYDelta += (rect.top - screenTop);
            } else {
                // get entire rect at bottom of screen
                scrollYDelta += (rect.bottom - screenBottom);
            }

            // make sure we aren't scrolling beyond the end of our content
            int bottom = mContentView.getBottom();
            int distanceToBottom = bottom - screenBottom;
            scrollYDelta = Math.min(scrollYDelta, distanceToBottom);

        } else if (rect.top < screenTop && rect.bottom < screenBottom) {
            // need to move up to get it in view: move up just enough so that
            // entire rectangle is in view (or at least the first screen
            // size chunk of it).

            if (rect.height() > height) {
                // screen size chunk
                scrollYDelta -= (screenBottom - rect.bottom);
            } else {
                // entire rect at top
                scrollYDelta -= (screenTop - rect.top);
            }

            // make sure we aren't scrolling any further than the top our content
            scrollYDelta = Math.max(scrollYDelta, -getScrollY());
        }
        return scrollYDelta;
    }

    /**
     * When looking for focus in children of a scroll view, need to be a little
     * more careful not to give focus to something that is scrolled off screen.
     * <p/>
     * This is more expensive than the default {@link android.view.ViewGroup}
     * implementation, otherwise this behavior might have been made the default.
     */
    @Override
    protected boolean onRequestFocusInDescendants(int direction,
                                                  Rect previouslyFocusedRect) {

        // convert from forward / backward notation to up / down / left / right
        // (ugh).
        if (direction == View.FOCUS_FORWARD) {
            direction = View.FOCUS_DOWN;
        } else if (direction == View.FOCUS_BACKWARD) {
            direction = View.FOCUS_UP;
        }

        final View nextFocus = previouslyFocusedRect == null ?
                FocusFinder.getInstance().findNextFocus(this, null, direction) :
                FocusFinder.getInstance().findNextFocusFromRect(this,
                        previouslyFocusedRect, direction);

        if (nextFocus == null) {
            return false;
        }

        if (isOffScreen(nextFocus)) {
            return false;
        }

        return nextFocus.requestFocus(direction, previouslyFocusedRect);
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle,
                                                 boolean immediate) {
        // offset into coordinate space of this scroll view
        rectangle.offset(child.getLeft() - child.getScrollX(),
                child.getTop() - child.getScrollY());

        return scrollToChildRect(rectangle, immediate);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        View currentFocused = findFocus();
        if (null == currentFocused || this == currentFocused)
            return;

        // If the currently-focused view was visible on the screen when the
        // screen was at the old height, then scroll the screen to make that
        // view visible with the new screen height.
        if (isWithinDeltaOfScreen(currentFocused, 0, oldh)) {
            currentFocused.getDrawingRect(mTempRect);
            offsetDescendantRectToMyCoords(currentFocused, mTempRect);
            int scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect);
            doScrollY(scrollDelta);
        }
    }

    /**
     * Fling the scroll view
     *
     * @param velocityY The initial velocity in the Y direction. Positive
     *                  numbers mean that the finger/cursor is moving down the screen,
     *                  which means we want to scroll towards the top.
     */
    private void fling(int velocityY) {
        if (mInflated && velocityY != 0) {
            final int startX = getScrollX();
            final int startY = getScrollY();
            final int height = getHeight() - getPaddingBottom() - getPaddingTop();
            final int bottom = mContentView.getBottom();

            if (velocityY < 0) {
                if (startY < mHeaderView.getHeight()) {
                    if (mStickyMode) {
                        // 向上推时，若头部视图未完全收缩，则完全完全收缩
                        int dy = mHeaderView.getBottom() - getPaddingTop() - startY;
                        mScroller.startScroll(startX, startY, 0, dy, computeDuration(dy));
                    } else {
                        mScroller.fling(startX, startY, 0, -velocityY, 0, 0, 0,
                                Math.max(0, bottom - height), 0, height / 2);
                    }
                }
            } else {
                if (canScroll()) {
                    if (mStickyMode) {
                        // 向下拉时，若允许滑动，则完全显示头部视图
                        mScroller.startScroll(startX, startY, 0, -startY, computeDuration(-startY));
                    } else {
                        mScroller.fling(startX, startY, 0, -velocityY, 0, 0, 0,
                                Math.max(0, bottom - height), 0, height / 2);
                    }
                }
            }

            invalidate();
        }
    }

    private int computeDuration(int d) {
        return Math.max(Math.min(Math.abs(d), 500), 250);
    }

    private void endDrag() {
        mIsBeingDragged = false;

        recycleVelocityTracker();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>This version also clamps the scrolling to the bounds of our child.
     */
    @Override
    public void scrollTo(int x, int y) {
        // we rely on the fact the View.scrollBy calls scrollTo.
        if (mInflated) {
            int width = mContentView.getWidth();
            int height = mHeaderView.getHeight() + mContentView.getHeight();
            x = clamp(x, getWidth() - getPaddingRight() - getPaddingLeft(), width);
            y = clamp(y, getHeight() - getPaddingBottom() - getPaddingTop(), height);
            if (x != getScrollX() || y != getScrollY()) {
                super.scrollTo(x, y);
            }
        }
    }

    private int clamp(int n, int my, int child) {
        if (my >= child || n < 0) {
            /* my >= child is this case:
             *                    |--------------- me ---------------|
             *     |------ child ------|
             * or
             *     |--------------- me ---------------|
             *            |------ child ------|
             * or
             *     |--------------- me ---------------|
             *                                  |------ child ------|
             *
             * n < 0 is this case:
             *     |------ me ------|
             *                    |-------- child --------|
             *     |-- mScrollX --|
             */
            return 0;
        }
        if ((my + n) > child) {
            /* this case:
             *                    |------ me ------|
             *     |------ child ------|
             *     |-- mScrollX --|
             */
            return child - my;
        }
        return n;
    }

    public interface ScrollDetector {
        boolean canScroll();
    }

    public interface OnShrinkHeaderListener {
        void onShrink(int progress, int total);
    }

}
