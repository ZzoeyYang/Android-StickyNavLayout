package online.sniper.widget.list;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.LinearLayout;

public class PinnedHeaderExpandableListView extends ExpandableListView implements OnScrollListener, OnGroupClickListener {
    public PinnedHeaderExpandableListView(Context context) {
        this(context, null);
    }

    public PinnedHeaderExpandableListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinnedHeaderExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.setOnScrollListener(this);
        setOnGroupClickListener(this);

        mHeaderViewContainer = new LinearLayout(getContext());
        addHeaderView(mHeaderViewContainer);
        mHeaderViewContainer.setOrientation(LinearLayout.VERTICAL);

        mHeaderViewWrapper = new LinearLayout(getContext());
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
        mHeaderViewContainer.addView(mHeaderViewWrapper, lp);
        mHeaderViewWrapper.setVisibility(View.GONE);
        setHeaderExpandable(true);
    }

    /**
     * Adapter 接口 . 列表必须实现此接口 .
     */
    public interface PinnedHeaderAdapter {
        int PINNED_HEADER_GONE = 0;
        int PINNED_HEADER_VISIBLE = 1;
        int PINNED_HEADER_PUSHED_UP = 2;

        /**
         * 设置HeaderView
         *
         * @param headerView
         * @param groupPosition
         * @param isExpanded
         * @param isCustomHeader
         * @param alpha
         */
        void setHeaderView(View headerView, int groupPosition, boolean isExpanded, boolean isCustomHeader, int alpha);

        /**
         * 获取HeaderType
         *
         * @param groupPosition
         */
        int getHeaderType(int groupPosition);

        /**
         * 获取HeaderView
         *
         * @param groupPosition
         * @param isExpanded
         * @param convertView
         * @param parent
         */
        View getHeaderView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent);
    }

    private static final int MAX_ALPHA = 255;

    private PinnedHeaderAdapter mAdapter;

    /**
     * 用于在列表头显示的 View,mHeaderViewVisible 为 true 才可见
     */
    private View mHeaderView;
    private View mCustomHeaderView;
    private int mHeaderViewType = -1;

    private final LinearLayout mHeaderViewContainer;
    private final LinearLayout mHeaderViewWrapper;

    /**
     * 列表头是否可见
     */
    private volatile boolean mHeaderViewVisible;
    private int mWidthMode;

    public void setHeaderView(int id) {
        View view = LayoutInflater.from(getContext()).inflate(id, mHeaderViewWrapper, false);
        setHeaderView(view);
    }

    public void setHeaderView(View view) {
        mCustomHeaderView = view;
        mHeaderView = view;
        ensurePinnedHeaderLayout(mHeaderView);
    }

    /**
     * 点击 HeaderView 触发的事件
     */
    private void onHeaderViewClick() {
        long packedPosition = getExpandableListPosition(getFirstVisiblePosition());
        int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);

        if (isGroupExpanded(groupPosition)) {
            collapseGroup(groupPosition);
        } else {
            expandGroup(groupPosition);
        }

        packedPosition = ExpandableListView.getPackedPositionForGroup(groupPosition);
        final int position = getFlatListPosition(packedPosition);
        setSelectionFromTop(position, 0);
    }

    private boolean shouldPassThroughEvent = false;

    /**
     * 如果 HeaderView 是可见的 , 此函数用于判断是否点击了 HeaderView, 并对做相应的处理 , 因为 HeaderView
     * 是画上去的 , 所以设置事件监听是无效的 , 只有自行控制 .
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_DOWN) {
            if (!isHeaderExpandable) {
                shouldPassThroughEvent = false;
            }
        }
        if (mHeaderViewVisible && mHeaderView != null && !shouldPassThroughEvent) {
            final float x = ev.getX();
            final float y = ev.getY();

            View header = mHeaderViewWrapper;
            if (header.getLeft() <= x && x < header.getRight()
                    && header.getTop() <= y && y < header.getBottom()) {
                ev.setLocation(x - header.getLeft(), y - header.getTop());
                boolean handled = header.dispatchTouchEvent(ev);
                if (action == MotionEvent.ACTION_DOWN && !isHeaderExpandable && !handled) {
                    shouldPassThroughEvent = true;
                    ev.setLocation(x, y);
                    return super.dispatchTouchEvent(ev);
                }
                return handled;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void setAdapter(ExpandableListAdapter adapter) {
        super.setAdapter(adapter);
        mAdapter = (PinnedHeaderAdapter) adapter;
    }

    /**
     * 点击了 Group 触发的事件 , 要根据根据当前点击 Group 的状态来
     */
    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        if (!isExpandable) {
            return true;
        }

        if (isGroupExpanded(groupPosition)) {
            parent.collapseGroup(groupPosition);
        } else {
            parent.expandGroup(groupPosition);

            long packedPosition = ExpandableListView.getPackedPositionForGroup(groupPosition);
            final int position = getFlatListPosition(packedPosition);
            ListViewHelper.smoothScrollToPositionFromTop(this, position, 0, 500);
        }

        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int mOldState = -1;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        final long flatPosition = getExpandableListPosition(getFirstVisiblePosition());
        final int groupPosition = ExpandableListView.getPackedPositionGroup(flatPosition);
        final int childPosition = ExpandableListView.getPackedPositionChild(flatPosition);

        if (mAdapter != null && groupPosition >= 0 && childPosition >= 0) {
            int state = getHeaderViewState(groupPosition, childPosition);
            if (mHeaderView != null && mAdapter != null && state != mOldState) {
                mOldState = state;
                LinearLayout header = mHeaderViewWrapper;
                header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());
            }

            configHeaderView(groupPosition, childPosition);
        }
    }

    private int getHeaderViewState(int groupPosition, int childPosition) {
        View header = mHeaderViewWrapper;
        if (mHeaderView == null || header.getMeasuredHeight() <= 0) {
            return PinnedHeaderAdapter.PINNED_HEADER_GONE;
        }

        if (!isGroupExpanded(groupPosition)) {
            return PinnedHeaderAdapter.PINNED_HEADER_GONE;
        }

        int nextGroupIndex = getFlatListPosition(ExpandableListView.getPackedPositionForGroup(groupPosition + 1));
        View nextGroupView = getChildAt(nextGroupIndex - getFirstVisiblePosition());
        if (nextGroupView == null) {
            return PinnedHeaderAdapter.PINNED_HEADER_VISIBLE;
        }

        int nextGroupTop = nextGroupView.getTop();
        if (header.getMeasuredHeight() > nextGroupTop) {
            return PinnedHeaderAdapter.PINNED_HEADER_PUSHED_UP;
        }
        return PinnedHeaderAdapter.PINNED_HEADER_VISIBLE;
    }

    private void configHeaderView(int groupPosition, int childPosition) {
        if (mAdapter == null) {
            mHeaderViewVisible = false;
            return;
        }
        int groupCount = ((ExpandableListAdapter) mAdapter).getGroupCount();
        if (groupPosition < 0 || groupPosition > groupCount - 1) {
            // 列表参数有误的情况下，隐藏header
            mHeaderViewVisible = false;
            return;
        }

        if (mCustomHeaderView == null) {
            int viewType = mAdapter.getHeaderType(groupPosition);
            mHeaderView = mAdapter.getHeaderView(groupPosition, isGroupExpanded(groupPosition), mHeaderViewType != viewType ? null : mHeaderView, mHeaderViewWrapper);
            ensurePinnedHeaderLayout(mHeaderView);
            mHeaderViewType = viewType;
        }

        if (mHeaderView == null) {
            // 列表参数有误的情况下，隐藏header
            mHeaderViewVisible = false;
            return;
        }

        LinearLayout header = mHeaderViewWrapper;
        int state = getHeaderViewState(groupPosition, childPosition);
        switch (state) {
            case PinnedHeaderAdapter.PINNED_HEADER_GONE: {
                mHeaderViewVisible = false;
                break;
            }
            case PinnedHeaderAdapter.PINNED_HEADER_VISIBLE: {
                mAdapter.setHeaderView(mHeaderView, groupPosition, isGroupExpanded(groupPosition), mCustomHeaderView != null, MAX_ALPHA);
                header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());
                mHeaderViewVisible = true;
                break;
            }
            case PinnedHeaderAdapter.PINNED_HEADER_PUSHED_UP: {
                int nextGroupIndex = getFlatListPosition(ExpandableListView.getPackedPositionForGroup(groupPosition + 1));
                View nextGroupView = getChildAt(nextGroupIndex - getFirstVisiblePosition());
                int bottom = nextGroupView.getTop();
                int y;
                int alpha;
                if (bottom < header.getMeasuredHeight()) {
                    y = (bottom - header.getMeasuredHeight());
                    alpha = MAX_ALPHA * (header.getMeasuredHeight() + y) / header.getMeasuredHeight();
                } else {
                    y = 0;
                    alpha = MAX_ALPHA;
                }
                mAdapter.setHeaderView(mHeaderView, groupPosition, isGroupExpanded(groupPosition), mCustomHeaderView != null, alpha);
                header.layout(0, y, header.getMeasuredWidth(), header.getMeasuredHeight() + y);
                mHeaderViewVisible = header.getMeasuredHeight() + y >= 0;
                break;
            }
        }
    }

    private void ensurePinnedHeaderLayout(View header) {
        if (header == null) {
            mHeaderViewWrapper.removeAllViews();
            return;
        }
        if (!header.isLayoutRequested()) {
            return;
        }
        ViewParent vp = header.getParent();
        if (vp instanceof ViewGroup) {
            ((ViewGroup) vp).removeView(header);
        }

        ViewGroup.LayoutParams layoutParams = header.getLayoutParams();
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, layoutParams.height);
        mHeaderViewWrapper.removeAllViews();
        mHeaderViewWrapper.addView(header, lp);

        int widthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), mWidthMode);
        int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        mHeaderViewWrapper.measure(widthSpec, heightSpec);
        mHeaderViewWrapper.layout(0, 0, mHeaderViewWrapper.getMeasuredWidth(), mHeaderViewWrapper.getMeasuredHeight());
    }

    /**
     * 列表界面更新时调用该方法(如滚动时)
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHeaderViewVisible) {
            // 分组栏是直接绘制到界面中，而不是加入到ViewGroup中
            drawChild(canvas, mHeaderViewWrapper, getDrawingTime());
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        final long flatPos = getExpandableListPosition(firstVisibleItem);
        int groupPosition = ExpandableListView.getPackedPositionGroup(flatPos);
        int childPosition = ExpandableListView.getPackedPositionChild(flatPos);

        configHeaderView(groupPosition, childPosition);
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    public void expandAllGroup() {
        expandAllGroup(false);
    }

    public void expandAllGroup(boolean animate) {
        ExpandableListAdapter adapter = getExpandableListAdapter();
        final int count = adapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            expandGroup(i, animate);
        }
    }

    public void collapseAllGroup() {
        ExpandableListAdapter adapter = getExpandableListAdapter();
        final int count = adapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            collapseGroup(i);
        }
    }

    protected OnScrollListener mOnScrollListener;

    public void setOnScrollListener(OnScrollListener l) {
        mOnScrollListener = l;
    }

    protected boolean isExpandable = true;

    public void setExpandable(boolean expandable) {
        isExpandable = expandable;
    }

    public boolean isExpandable() {
        return isExpandable;
    }

    protected boolean isHeaderExpandable = true;

    public void setHeaderExpandable(boolean expandable) {
        isHeaderExpandable = expandable;
        if (expandable) {
            mHeaderViewWrapper.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isExpandable) {
                        onHeaderViewClick();
                    }
                }
            });
        } else {
            mHeaderViewWrapper.setOnClickListener(null);
            mHeaderViewWrapper.setClickable(false);
        }
    }

    public boolean isHeaderExpandable() {
        return isHeaderExpandable;
    }
}
