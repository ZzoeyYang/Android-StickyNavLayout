package online.sniper.widget.recycler;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import java.util.HashSet;

/**
 * This class is from the v7 samples of the Android SDK. It's not by me!
 * <p/>
 * See the license above for details.
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    public static final int HORIZONTAL_LIST = 0;

    public static final int VERTICAL_LIST = 1;

    public static final int ALL_LIST = 2;

    private Drawable mDivider;

    private int mColorSize = 1;
    private boolean mUseDefaultSize = false;

    private int mOrientation;
    private HashSet<PositionIndex> mLastRows = new HashSet<>();
    private HashSet<PositionIndex> mLastColoums = new HashSet<>();

    /**
     * 利用theme中定义的分割线
     */
    public DividerItemDecoration(Context context, int orientation) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        mUseDefaultSize = true;
        a.recycle();
        setOrientation(orientation);
    }

    /**
     * 传分割线图片
     */
    public DividerItemDecoration(Context context, int orientation, Drawable divider) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = divider;
        a.recycle();
        setOrientation(orientation);
    }

    /**
     * 传颜色值和线的高度
     */
    public DividerItemDecoration(Context context, int orientation, int color, int height) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = new ColorDrawable(color);
        mColorSize = height;
        mUseDefaultSize = true;
        a.recycle();
        setOrientation(orientation);
    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST && orientation != ALL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent) {
        if (mOrientation == VERTICAL_LIST) {
            drawHorizontal(c, parent);
        } else if (mOrientation == HORIZONTAL_LIST) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
            drawVertical(c, parent);
        }

    }

    /**
     * 画横线
     *
     * @param c      canvas
     * @param parent recyclerview
     */
    public void drawHorizontal(Canvas c, RecyclerView parent) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
//            if (mLastRows.contains(new PositionIndex(i))) {
//                LogUtil.d("drawHorizontal", "position:" + i);
//                continue;
//            }
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getLeft() - params.leftMargin;
            final int right = child.getRight() + params.rightMargin;
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + getDrawableHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    private int getDrawableWidth() {
        return mUseDefaultSize ? mColorSize : mDivider.getIntrinsicWidth();
    }

    private int getDrawableHeight() {
        return mUseDefaultSize ? mColorSize : mDivider.getIntrinsicHeight();
    }

    /**
     * 画竖线
     *
     * @param c      画布
     * @param parent parent
     */
    public void drawVertical(Canvas c, RecyclerView parent) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
//            if (mLastColoums.contains(new PositionIndex(i))) {
//                LogUtil.d("drawVertical", "position:" + i);
//                continue;
//            }
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getTop() - params.topMargin;
            int bottom = child.getBottom() + params.bottomMargin;
            int left = child.getRight() + params.rightMargin;
            int right = left + getDrawableWidth();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }


    private boolean isLastColum(RecyclerView parent, int pos, int spanCount, int childCount) {
        LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            SpanSizeLookup spanSizeLookup = ((GridLayoutManager) layoutManager).getSpanSizeLookup();
            int spanSize = spanSizeLookup.getSpanSize(pos);
            int spanIndex = spanSizeLookup.getSpanIndex(pos, spanCount);
            if (spanSize + spanIndex == spanCount) {
                return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if ((pos + 1) % spanCount == 0)// 如果是最后一列，则不需要绘制右边
                {
                    return true;
                }
            } else {
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount)// 如果是最后一列，则不需要绘制右边
                    return true;
            }
        } else if (layoutManager instanceof LinearLayoutManager) {
            if (((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                return pos + 1 == layoutManager.getItemCount();
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean isLastRaw(RecyclerView parent, int pos, int spanCount, int childCount) {
        LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            int offset = childCount - 1;
            while (((GridLayoutManager) layoutManager).getSpanSizeLookup().getSpanIndex(offset, spanCount) > 0) {
                offset--;
            }
            if (pos >= offset)// 如果是最后一行，则不需要绘制底部
                return true;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            // StaggeredGridLayoutManager 且纵向滚动
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                childCount = childCount - childCount % spanCount;
                // 如果是最后一行，则不需要绘制底部
                if (pos >= childCount)
                    return true;
            } else {
                // StaggeredGridLayoutManager 且横向滚动
                // 如果是最后一行，则不需要绘制底部
                if ((pos + 1) % spanCount == 0) {
                    return true;
                }
            }
        } else if (layoutManager instanceof LinearLayoutManager) {
            if (((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.VERTICAL) {
                return pos + 1 == layoutManager.getItemCount();
            } else {
                return true;
            }
        }
        return false;
    }


    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        int spanCount = getSpanCount(parent);
        int childCount = parent.getAdapter().getItemCount();
        PositionIndex positionIndex = new PositionIndex(itemPosition);
        int right = 0;
        int bottom = 0;
        if (isLastColum(parent, itemPosition, spanCount, childCount)) {
            right = 0;
            mLastColoums.add(positionIndex);
        } else {
            right = getDrawableWidth();
            mLastColoums.remove(positionIndex);
        }
        if (isLastRaw(parent, itemPosition, spanCount, childCount)) {
            bottom = 0;
            mLastRows.add(positionIndex);
        } else {
            bottom = getDrawableHeight();
            mLastRows.remove(positionIndex);
        }
        outRect.set(0, 0, right, bottom);
    }

    private int getSpanCount(RecyclerView parent) {
        // 列数
        int spanCount = -1;
        LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            spanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }
        return spanCount;
    }

    private class PositionIndex {

        private int position;

        public PositionIndex(int position) {
            this.position = position;
        }

        @Override
        public int hashCode() {
            return String.valueOf(position).hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (o instanceof PositionIndex) {
                return ((PositionIndex) o).position == this.position;
            }
            return false;
        }
    }

}
