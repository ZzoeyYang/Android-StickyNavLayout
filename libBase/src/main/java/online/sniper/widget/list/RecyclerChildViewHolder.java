package online.sniper.widget.list;

import android.support.annotation.IdRes;
import android.view.View;

/**
 * ViewHolder的基类
 * <p/>
 * Created by wangpeihe on 2016/6/22.
 */
public abstract class RecyclerChildViewHolder<T> {
    protected RecyclerExpandableListAdapter mAdapter;
    private T mData;
    private int mGroupPosition;
    private int mChildPosition;

    public View itemView;

    public RecyclerChildViewHolder(View itemView) {
        this.itemView = itemView;
    }

    public abstract void onSetData(T data, int groupPosition, int childPosition);

    public final void setData(T data, int groupPosition, int childPosition) {
        mData = data;
        mGroupPosition = groupPosition;
        mChildPosition = childPosition;
        onSetData(data, groupPosition, childPosition);
    }

    public T getData() {
        return mData;
    }

    public int getGroupPosition() {
        return mGroupPosition;
    }

    public int getChildPosition() {
        return mChildPosition;
    }

    public void setAdapter(RecyclerExpandableListAdapter adapter) {
        mAdapter = adapter;
    }

    /**
     * 返回对应view
     *
     * @param resId view resource Id
     * @param <V>   返回view的类型
     * @return 返回View
     */
    public <V extends View> V getView(@IdRes int resId) {
        return (V) itemView.findViewById(resId);
    }

    /**
     * 取每一个Item的View
     *
     * @return 返回整个Item
     */
    public View getItemView() {
        return itemView;
    }
}
