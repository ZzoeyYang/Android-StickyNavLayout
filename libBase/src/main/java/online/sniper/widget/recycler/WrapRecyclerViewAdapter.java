package online.sniper.widget.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import java.util.ArrayList;
import java.util.List;


/**
 * 处理带header和footer的RecyclerView.Adapter
 * Created by pingyongxia on 2015/11/23.
 */
public class WrapRecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerViewHolder> {

    private final String TAG = this.getClass().getName();
    private RecyclerViewAdapter<T> mAdapter;
    private Context mContext;
    private List<View> mHeaderView = new ArrayList<>();
    private List<View> mFooterView = new ArrayList<>();

    public WrapRecyclerViewAdapter(Context context, ArrayList<View> headerViews, ArrayList<View> footViews, RecyclerViewAdapter<T> adapter) {
        this(context, adapter);
        this.mHeaderView.addAll(headerViews);
        this.mFooterView.addAll(headerViews);
    }

    public WrapRecyclerViewAdapter(Context context, RecyclerViewAdapter<T> adapter) {
        mAdapter = adapter;
        mContext = context;
    }

    public int getHeadersCount() {
        return mHeaderView.size();
    }

    public int getInvisibleHeadersCount() {
        int count = 0;
        for (View view : mHeaderView) {
            if (view.getVisibility() == View.GONE) {
                count++;
            }
        }
        return count;
    }

    public void addHeaderView(View view) {
        mHeaderView.add(0, view);
        notifyItemInserted(0);
    }

    public void removeHeaderView(View view) {
        int index = mHeaderView.indexOf(view);
        if (index >= 0) {
            mHeaderView.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void resetHeaderView() {
        int headerCount = mHeaderView.size();
        mHeaderView.clear();
        notifyItemRangeRemoved(0, headerCount);
    }

    public int getFootersCount() {
        return mFooterView.size();
    }

    public int getInvisibleFootersCount() {
        int count = 0;
        for (View view : mFooterView) {
            if (view.getVisibility() == View.GONE) {
                count++;
            }
        }
        return count;
    }

    public void addFooterView(View view) {
        mFooterView.add(0, view);
        int mAdapterCount = 0;
        if (mAdapter != null) {
            mAdapterCount = mAdapter.getItemCount();
        }
        notifyItemInserted(mHeaderView.size() + mAdapterCount);
    }

    public void removeFooterView(View view) {
        int index = mFooterView.indexOf(view);
        if (index >= 0) {
            mFooterView.remove(index);
            int mAdapterCount = 0;
            if (mAdapter != null) {
                mAdapterCount = mAdapter.getItemCount();
            }
            notifyItemRemoved(index + mHeaderView.size() + mAdapterCount);
        }
    }

    public void resetFooterView() {
        int footerCount = mFooterView.size();
        mFooterView.clear();
        int mAdapterCount = 0;
        if (mAdapter != null) {
            mAdapterCount = mAdapter.getItemCount();
        }
        notifyItemRangeRemoved(mHeaderView.size() + mAdapterCount, footerCount);
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RecyclerView.INVALID_TYPE || viewType == RecyclerView.INVALID_TYPE - 1) {
            FrameLayout view = new FrameLayout(mContext);
            view.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new HeaderViewHolder(view);
        } else {
            return mAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder viewHolder, int position) {
        int adapterCount = 0;
        if (mAdapter != null) {
            adapterCount = mAdapter.getItemCount();
        }
        int headViewSize = mHeaderView.size();
        if (position < headViewSize) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) viewHolder;
            headerViewHolder.setHeader(mHeaderView.get(position));
        } else if (position >= headViewSize + adapterCount) {
            HeaderViewHolder footerViewHolder = (HeaderViewHolder) viewHolder;
            footerViewHolder.setHeader(mFooterView.get(position - (headViewSize + adapterCount)));
        } else {
            mAdapter.onBindViewHolder((RecyclerViewHolder) viewHolder, position - headViewSize);
        }
    }

    @Override
    public int getItemCount() {
        return mHeaderView.size() + mFooterView.size() + (mAdapter != null ? mAdapter.getItemCount() : 0);
    }

    public int getRealItemCount() {
        return (mAdapter != null ? mAdapter.getItemCount() : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mHeaderView.size()) {
            return RecyclerView.INVALID_TYPE;
        }
        int adapterCount = 0;
        if (mAdapter != null) {
            adapterCount = mAdapter.getItemCount();
        }
        if (position >= mHeaderView.size() + adapterCount) {
            return RecyclerView.INVALID_TYPE - 1;
        } else {
            return mAdapter.getItemViewType(position - mHeaderView.size());
        }
    }

    @Override
    public long getItemId(int position) {
        int numHeaders = 1;
        if (mAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            int adapterCount = mAdapter.getItemCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getItemId(adjPosition);
            }
        }
        return -1;
    }

    public RecyclerViewAdapter<T> getAdapter() {
        return mAdapter;
    }

    public void parentDataSetChanged() {
        notifyDataSetChanged();
    }

    private static class HeaderViewHolder extends RecyclerViewHolder {

        private FrameLayout mRootView;

        public HeaderViewHolder(FrameLayout itemView) {
            super(itemView);
            mRootView = itemView;
        }

        public void setHeader(View view) {
            ViewParent viewParent = view.getParent();
            if (viewParent != null) {
                ((ViewGroup) viewParent).removeView(view);
            }
            mRootView.addView(view);
        }

        @Override
        public void onSetData(Object data, int position) {

        }
    }
}
