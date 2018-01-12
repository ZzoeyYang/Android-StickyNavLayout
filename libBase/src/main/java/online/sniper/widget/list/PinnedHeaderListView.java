package online.sniper.widget.list;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

public class PinnedHeaderListView extends ListView implements OnScrollListener {

    public static final String TAG = "SCROLL";
    private OnScrollListener mOnScrollListener;

    public interface PinnedSectionedHeaderAdapter {
        boolean isSectionHeader(int position);

        int getSectionForPosition(int position);

        View getSectionHeaderView(int section, View convertView, ViewGroup parent);

        int getSectionHeaderViewType(int section);

        int getCount();
    }

    private PinnedSectionedHeaderAdapter mAdapter;
    private LinearLayout mHeaderViewContainer;
    private View mCurrentHeader;
    private int mCurrentHeaderViewType = 0;
    private boolean mShouldPin = true;
    private int mCurrentSection = 0;
    private int mWidthMode;
    private int mHeightMode;

    public PinnedHeaderListView(Context context) {
        this(context, null);
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.setOnScrollListener(this);

        mHeaderViewContainer = new LinearLayout(getContext());
        addHeaderView(mHeaderViewContainer);
        mHeaderViewContainer.removeAllViews();
    }

    public void setPinHeaders(boolean shouldPin) {
        mShouldPin = shouldPin;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        mCurrentHeader = null;
        mAdapter = (PinnedSectionedHeaderAdapter) adapter;
        super.setAdapter(adapter);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }

        if (mAdapter == null || mAdapter.getCount() == 0 || !mShouldPin || (firstVisibleItem < getHeaderViewsCount())) {
            mCurrentHeader = null;
            for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
                View child = getChildAt(i - firstVisibleItem);
                if (child != null) {
                    child.setVisibility(VISIBLE);
                }
            }
            return;
        }

        firstVisibleItem -= getHeaderViewsCount();

        int section = mAdapter.getSectionForPosition(firstVisibleItem);
        int viewType = mAdapter.getSectionHeaderViewType(section);
        mCurrentHeader = getSectionHeaderView(section, mCurrentHeaderViewType != viewType ? null : mCurrentHeader);
        ensurePinnedHeaderLayout(mCurrentHeader);
        mCurrentHeaderViewType = viewType;

        int headerOffset = 0;
        int pinnedHeaderWidth = mCurrentHeader.getMeasuredWidth();
        int pinnedHeaderHeight = mCurrentHeader.getMeasuredHeight();
        for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
            if (mAdapter.isSectionHeader(i)) {
                View header = getChildAt(i - firstVisibleItem);
                int headerTop = header.getTop();
                header.setVisibility(VISIBLE);
                if (pinnedHeaderHeight >= headerTop && headerTop > 0) {
                    headerOffset = headerTop - header.getMeasuredHeight();
                } else if (headerTop <= 0) {
                    header.setVisibility(INVISIBLE);
                }
            }
        }
        mCurrentHeader.layout(0, headerOffset, pinnedHeaderWidth, headerOffset + pinnedHeaderHeight);

        invalidate();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    private View getSectionHeaderView(int section, View oldView) {
        boolean shouldLayout = section != mCurrentSection || oldView == null;

        View view = mAdapter.getSectionHeaderView(section, oldView, this);
        if (shouldLayout) {
            // a new section, thus a new header. We should lay it out again
            ensurePinnedHeaderLayout(view);
            mCurrentSection = section;
        }
        return view;
    }

    private void ensurePinnedHeaderLayout(View header) {
        if (!header.isLayoutRequested()) {
            return;
        }
        ViewParent vp = header.getParent();
        if (vp instanceof ViewGroup) {
            ((ViewGroup) vp).removeView(header);
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        header.setLayoutParams(lp);
        header.setVisibility(View.GONE);
        mHeaderViewContainer.removeAllViews();
        mHeaderViewContainer.addView(header);

        int widthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), mWidthMode);
        int heightSpec;
        ViewGroup.LayoutParams layoutParams = header.getLayoutParams();
        if (layoutParams != null && layoutParams.height > 0) {
            heightSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY);
        } else {
            heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        header.measure(widthSpec, heightSpec);
        header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mAdapter == null || !mShouldPin || mCurrentHeader == null) {
            return;
        }
        drawChild(canvas, mCurrentHeader, getDrawingTime());
    }

    /**
     * 如果 HeaderView 是可见的 , 此函数用于判断是否点击了 HeaderView, 并对做相应的处理 , 因为 HeaderView
     * 是画上去的 , 所以设置事件监听是无效的 , 只有自行控制 .
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mAdapter == null || !mShouldPin || mCurrentHeader == null) {
            return super.dispatchTouchEvent(ev);
        }

        final float x = ev.getX();
        final float y = ev.getY();
        if (mCurrentHeader.getLeft() <= x && x < mCurrentHeader.getRight()
                && mCurrentHeader.getTop() <= y && y < mCurrentHeader.getBottom()) {
            ev.setLocation(x - mCurrentHeader.getLeft(), y - mCurrentHeader.getTop());
            return mCurrentHeader.dispatchTouchEvent(ev);
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mOnScrollListener = l;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        mHeightMode = MeasureSpec.getMode(heightMeasureSpec);
    }

    public void setOnItemClickListener(PinnedHeaderListView.OnItemClickListener listener) {
        super.setOnItemClickListener(listener);
    }

    public static abstract class OnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int rawPosition, long id) {
            SectionedBaseAdapter adapter;
            if (adapterView.getAdapter().getClass().equals(HeaderViewListAdapter.class)) {
                HeaderViewListAdapter wrapperAdapter = (HeaderViewListAdapter) adapterView.getAdapter();
                adapter = (SectionedBaseAdapter) wrapperAdapter.getWrappedAdapter();
            } else {
                adapter = (SectionedBaseAdapter) adapterView.getAdapter();
            }
            int section = adapter.getSectionForPosition(rawPosition);
            int position = adapter.getPositionInSectionForPosition(rawPosition);

            if (position == -1) {
                onSectionClick(adapterView, view, section, id);
            } else {
                onItemClick(adapterView, view, section, position, id);
            }
        }

        public abstract void onItemClick(AdapterView<?> adapterView, View view, int section, int position, long id);

        public abstract void onSectionClick(AdapterView<?> adapterView, View view, int section, long id);
    }
}