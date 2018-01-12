package online.sniper.widget.recycler;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Looper;
import android.os.MessageQueue;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MotionEvent;
import android.view.View;

import online.sniper.widget.recycler.listener.ItemTouchHelperGestureListener;


/**
 * Created by Knero on 14/10/2016.
 */

public class RecyclerViewHelper {

    private final Object mLock = new Object();
    private GridLayoutManager.SpanSizeLookup mSpanSizeLookup;
    private OnClickListener mOnClickListener = null;
    private OnLongClickListener mOnLongClickListener = null;
    private RecyclerViewAdapter mAdapter;
    private WrapRecyclerViewAdapter mWrapAdapter;
    private Builder mBuilder;
    private RecyclerView mRecyclerView;
    private EasySimpleItemCallback mCallback;
    private ILoadMoreView mLoadMoreView;
    private boolean isLoading = false;
    private int mShowLoadMoreCount = -1;

    private RecyclerViewHelper(Builder builder) {
        mBuilder = builder;
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        mOnLongClickListener = onLongClickListener;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void setAdapter(RecyclerViewAdapter adapter) {
        mAdapter = adapter;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public void setMovable(boolean movable) {
        mCallback.setMovable(movable);
    }

    private void apply(Context context) {
        mRecyclerView = mBuilder.mRecyclerView;
        if (mBuilder.mLayoutManager == null) {
            if (mBuilder.mColumnsNum <= 1) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(context, mBuilder.mOrientation, mBuilder.mReverseLayout));
            } else {
                GridLayoutManager layoutManager = new GridLayoutManager(context, mBuilder.mColumnsNum, mBuilder.mOrientation, false);
                layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (mSpanSizeLookup != null) {
                            return mSpanSizeLookup.getSpanSize(position);
                        }
                        if (mWrapAdapter != null && position == getItemCount() - 1) {
                            return mBuilder.mColumnsNum;
                        } else {
                            return 1;
                        }
                    }
                });
                mRecyclerView.setLayoutManager(layoutManager);
            }
        } else {
            mRecyclerView.setLayoutManager(mBuilder.mLayoutManager);
        }
        mSpanSizeLookup = mBuilder.mSpanSizeLookup;
        if (mBuilder.isUseDefaultDecoration) {
            mBuilder.mItemDecoration = mBuilder.getDefaultDecoration(context);
        }
        if (mBuilder.mItemDecoration != null) {
            mRecyclerView.addItemDecoration(mBuilder.mItemDecoration);
        }
        mOnClickListener = mBuilder.mOnClickListener;
        mOnLongClickListener = mBuilder.mOnLongClickListener;
        final GestureDetectorCompat gestureDetector = new GestureDetectorCompat(context, new ItemTouchHelperGestureListener(mRecyclerView) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder viewHolder) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(viewHolder);
                }
            }

            @Override
            public void onLongClick(RecyclerView.ViewHolder viewHolder) {
                if (mOnLongClickListener != null) {
                    mOnLongClickListener.onLongClick(viewHolder);
                }
            }
        });
        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                final int action = e.getAction() & MotionEvent.ACTION_MASK;
                gestureDetector.onTouchEvent(e);
                return action == MotionEvent.ACTION_POINTER_UP &&
                        mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                gestureDetector.onTouchEvent(e);
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });

        mAdapter = mBuilder.mAdapter;
        if (mAdapter != null) {
            if (mBuilder.mLoadMore) {
                mLoadMoreView = mBuilder.mLoadMoreView;
                if (mLoadMoreView == null) {
                    mLoadMoreView = new DefaultLoadMoreView(context, mBuilder.mOnLoadMoreListener);
                }
                mShowLoadMoreCount = mBuilder.mShowLoadMoreCount;
                showLoading();
                mWrapAdapter = new WrapRecyclerViewAdapter(context, mAdapter);
                mWrapAdapter.addFooterView(mLoadMoreView.getLoadMoreView());
                mRecyclerView.setAdapter(mWrapAdapter);
                mRecyclerView.addOnScrollListener(new OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (dy >= 0) {
                            loadMoreIfNecessary();
                        }
                    }
                });
            } else {
                mRecyclerView.setAdapter(mAdapter);
            }
        }

        mCallback = new EasySimpleItemCallback();
        mCallback.setMovable(mBuilder.mMovable);
        ItemTouchHelper touchHelper = new ItemTouchHelper(mCallback);
        touchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void loadMoreIfNecessary() {
        synchronized (mLock) {
            boolean canLoading = isLastVisibleItem(mBuilder.mLoadMoreOffset) && mBuilder.mOnLoadMoreListener != null && mLoadMoreView.isCanLoad() && !isLoading;
            if (canLoading) {
                isLoading = true;
                mBuilder.mOnLoadMoreListener.onLoadMore();
            }
        }
    }

    public void showLoadError() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mLoadMoreView != null) {
                    mLoadMoreView.showLoadError();
                }
                isLoading = false;
            }
        });
    }

    public void showLoading() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mLoadMoreView != null) {
                    mLoadMoreView.showLoading();
                }
                if (0 < mShowLoadMoreCount && mWrapAdapter.getRealItemCount() < mShowLoadMoreCount) {
                    if (mLoadMoreView != null) {
                        mLoadMoreView.hideLoading();
                    }
                }
                isLoading = false;
            }
        });
    }

    public void showNoMore() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mLoadMoreView != null) {
                    mLoadMoreView.showNoMore();
                }
                if (0 < mShowLoadMoreCount && mWrapAdapter.getRealItemCount() < mShowLoadMoreCount) {
                    if (mLoadMoreView != null) {
                        mLoadMoreView.hideLoading();
                    }
                }
                isLoading = false;
            }
        });
    }

    public void hideLoading() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mLoadMoreView != null) {
                    mLoadMoreView.hideLoading();
                }
            }
        });
    }

    private void postDelayed(Runnable action) {
        if (mRecyclerView != null) {
            mRecyclerView.post(action);
        }
    }

    public void notifyDataSetChanged() {
        if (mWrapAdapter != null) {
            mWrapAdapter.notifyDataSetChanged();
            Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                @Override
                public boolean queueIdle() {
                    loadMoreIfNecessary();
                    return false;
                }
            });
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void notifyItemChanged(int position) {
        if (mWrapAdapter != null) {
            mWrapAdapter.notifyItemChanged(position + mWrapAdapter.getHeadersCount());
        } else {
            mAdapter.notifyItemChanged(position);
        }
    }

    private boolean isLastVisibleItem(int offset) {
        LayoutManager lm = getRecyclerView().getLayoutManager();
        if (lm instanceof LinearLayoutManager) {
            LinearLayoutManager llm = (LinearLayoutManager) lm;
            int lastVisibleItemPosition = llm.findLastVisibleItemPosition();
            int itemCount = getRecyclerView().getAdapter().getItemCount();
            if (mWrapAdapter != null) {
                int invisibleFootersCount = mWrapAdapter.getInvisibleFootersCount();
                int invisibleHeadersCount = mWrapAdapter.getInvisibleHeadersCount();
                if (lastVisibleItemPosition >= itemCount - invisibleHeadersCount - invisibleFootersCount - 1 - offset) {
                    return true;
                }
            } else {
                //判断是否滚动到底部
                if (lastVisibleItemPosition >= itemCount - 1 - offset) {
                    return true;
                }
            }
        }

        return false;
    }

    public void scroll2Top() {
        getRecyclerView().scrollToPosition(0);
    }

    public int getHeadersCount() {
        if (mWrapAdapter != null) {
            return mWrapAdapter.getHeadersCount();
        }
        return 0;
    }

    public int getFootersCount() {
        if (mWrapAdapter != null) {
            return mWrapAdapter.getFootersCount();
        }
        return 0;
    }

    public int getItemCount() {
        if (mWrapAdapter != null) {
            return mWrapAdapter.getItemCount();
        }
        return mAdapter.getItemCount();
    }

    public static class Builder {

        private int mRecyclerViewResId = -1;
        private RecyclerView mRecyclerView;

        // 设置RecyclerView布局参数
        private int mColumnsNum = -1;
        private GridLayoutManager.SpanSizeLookup mSpanSizeLookup;
        private int mOrientation = OrientationHelper.VERTICAL;
        private boolean mReverseLayout = false;
        private LayoutManager mLayoutManager = null;

        // 分割线
        private boolean isUseDefaultDecoration = false;
        private ItemDecoration mItemDecoration = null;

        // 点击事件回调
        private OnClickListener mOnClickListener = null;
        private OnLongClickListener mOnLongClickListener = null;

        // Adapter
        private RecyclerViewAdapter mAdapter;

        // 是否可以拖动
        private boolean mMovable = false;// 默认不可拖动

        // 是不有footer
        private boolean mLoadMore = false;
        private ILoadMoreView mLoadMoreView;
        private OnLoadMoreListener mOnLoadMoreListener;
        private int mLoadMoreOffset = 1;
        private int mShowLoadMoreCount = -1;

        public Builder setRecyclerView(Activity activity, int resId) {
            mRecyclerView = (RecyclerView) activity.findViewById(resId);
            return this;
        }

        public Builder setRecyclerView(View rootView, int resId) {
            mRecyclerView = (RecyclerView) rootView.findViewById(resId);
            return this;
        }

        public Builder setRecyclerView(RecyclerView recyclerView) {
            mRecyclerView = recyclerView;
            return this;
        }

        public Builder setColumnsNum(int columnsNum) {
            mColumnsNum = columnsNum;
            return this;
        }

        public Builder setSpanSizeLookup(GridLayoutManager.SpanSizeLookup lookup) {
            mSpanSizeLookup = lookup;
            return this;
        }

        public Builder setOrientation(int orientation) {
            mOrientation = orientation;
            return this;
        }

        public Builder setReverseLayout(boolean reverseLayout) {
            mReverseLayout = reverseLayout;
            return this;
        }

        public Builder setLayoutManager(LayoutManager layoutManager) {
            mLayoutManager = layoutManager;
            return this;
        }

        public Builder useDefaultDecoration() {
            this.isUseDefaultDecoration = true;
            return this;
        }

        public Builder setItemDecoration(ItemDecoration itemDecoration) {
            mItemDecoration = itemDecoration;
            return this;
        }

        public Builder setOnClickListener(OnClickListener clickListener) {
            this.mOnClickListener = clickListener;
            return this;
        }

        public Builder setOnLongClickListener(OnLongClickListener longClickListener) {
            this.mOnLongClickListener = longClickListener;
            return this;
        }

        public Builder setAdapter(RecyclerViewAdapter adapter) {
            this.mAdapter = adapter;
            return this;
        }

        public Builder enableLoadMore() {
            this.mLoadMore = true;
            return this;
        }

        public Builder disableLoadMore() {
            this.mLoadMore = false;
            return this;
        }

        public Builder setLoadMoreView(ILoadMoreView loadMoreView) {
            if (loadMoreView != null) {
                this.mLoadMore = true;
            }
            this.mLoadMoreView = loadMoreView;
            return this;
        }

        public Builder setOnLoadMoreListener(OnLoadMoreListener loadMoreListener) {
            this.mOnLoadMoreListener = loadMoreListener;
            return this;
        }

        public Builder setLoadMoreOffset(int offset) {
            mLoadMoreOffset = offset;
            return this;
        }

        /**
         * @return 返回decoration
         */
        private ItemDecoration getDefaultDecoration(Context context) {
            DividerItemDecoration decoration = new DividerItemDecoration(context, DividerItemDecoration.ALL_LIST, Color.parseColor("#EDEDED"), 1);
            return decoration;
        }

        public Builder setShowLoadMoreCount(int count) {
            mShowLoadMoreCount = count;
            return this;
        }

        public RecyclerViewHelper build(Context context) {
            RecyclerViewHelper recyclerViewHelper = new RecyclerViewHelper(this);
            recyclerViewHelper.apply(context);
            return recyclerViewHelper;
        }

        public Builder movable() {
            mMovable = true;
            return this;
        }
    }

    public interface OnClickListener {

        public void onClick(RecyclerView.ViewHolder viewHolder);
    }

    public interface OnLongClickListener {

        public void onLongClick(RecyclerView.ViewHolder viewHolder);
    }

    public interface OnLoadMoreListener {

        public void onLoadMore();
    }
}
