package online.sniper.widget.list;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import rx.exceptions.Exceptions;

/**
 * Created by wangpeihe on 2016/6/22.
 */
public class RecyclerListAdapter<DATA> extends BaseAdapter {
    private final SparseArray<ViewHolderInfo> mHolders = new SparseArray<ViewHolderInfo>();
    private final List<ItemInfo<DATA>> mItems = new ArrayList<ItemInfo<DATA>>();
    private Context mContext;
    private LayoutInflater mInflater;

    public RecyclerListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        ListViewHolderMap a = getClass().getAnnotation(ListViewHolderMap.class);
        if (a != null) {
            ListViewHolder[] holders = a.value();
            if (holders != null && holders.length > 0) {
                for (int i = 0; i < holders.length; i++) {
                    ListViewHolder holder = holders[i];
                    registerViewHolder(holder.viewType(), holder.layout(), holder.holder());
                }
            }
        }
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public int getViewTypeCount() {
        return 16;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ItemInfo<DATA> item = getItem(position);
        final ViewHolderInfo info = mHolders.get(item.viewType);
        final View view;
        final RecyclerListViewHolder holder;
        if (convertView == null) {
            view = mInflater.inflate(info.layout, parent, false);
            try {
                if (info.constructor != null) {
                    holder = info.constructor.newInstance(view);
                } else {
                    holder = onCreateViewHolder(item.viewType, view);
                }
                if (holder == null) {
                    throw new RuntimeException("Holder is null.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw Exceptions.propagate(e);
            }
            holder.setAdapter(this);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (RecyclerListViewHolder) view.getTag();
        }
        DATA data = item == null ? null : item.data;
        holder.setData(data, position);
        onSetData(holder, data, position);
        return view;
    }

    /**
     * 新建ViewHolder
     *
     * @param viewType 视图类型
     * @param view     视图
     * @return
     */
    protected RecyclerListViewHolder onCreateViewHolder(int viewType, View view) {
        throw new RuntimeException("Must override onCreateViewHolder(), can not call super.onCreateViewHolder()");
    }

    /**
     * 获取Context
     *
     * @return Context
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * 获取数据项列表
     *
     * @return 数据项列表
     */
    public List<ItemInfo<DATA>> getItems() {
        return mItems;
    }

    /**
     * 获取原数据结构的数据项
     *
     * @return 数据项列表
     */
    public List<DATA> getSameItems() {
        List<DATA> list = new ArrayList<>();
        for (ItemInfo<DATA> itemInfo : mItems) {
            list.add(itemInfo.data);
        }
        return list;
    }

    /**
     * 添加数据项，默认视图类型为0
     *
     * @param data 数据
     */
    public void addItem(DATA data) {
        addItem(0, data);
    }

    /**
     * 添加数据项
     *
     * @param viewType 视图类型
     * @param data     数据
     */
    public void addItem(int viewType, DATA data) {
        ItemInfo<DATA> item = new ItemInfo<DATA>(viewType, data);
        mItems.add(item);
    }

    /**
     * 添加数据项
     *
     * @param location 开始位置
     * @param viewType 视图类型
     * @param data     数据
     */
    public void addItem(int location, int viewType, DATA data) {
        ItemInfo<DATA> item = new ItemInfo<DATA>(viewType, data);
        mItems.add(location, item);
    }

    /**
     * 替换数据项
     *
     * @param location 开始位置
     * @param viewType 视图类型
     * @param data     数据
     */
    public void replaceItem(int location, int viewType, DATA data) {
        ItemInfo<DATA> item = new ItemInfo<DATA>(viewType, data);
        mItems.set(location, item);
    }

    public void addItems(List<ItemInfo<DATA>> list) {
        mItems.addAll(list);
    }

    /**
     * 添加数据项，默认视图类型为0
     *
     * @param dataSet 数据集
     */
    public <T extends DATA> void addItems(Collection<T> dataSet) {
        addItems(0, dataSet);
    }

    /**
     * 添加数据项
     *
     * @param viewType 视图类型
     * @param dataSet  数据集
     */
    public <T extends DATA> void addItems(int viewType, Collection<T> dataSet) {
        for (DATA data : dataSet) {
            addItem(viewType, data);
        }
    }

    /**
     * 添加数据项
     *
     * @param location 开始位置
     * @param viewType 视图类型
     * @param dataSet  数据集
     */
    public <T extends DATA> void addItems(int location, int viewType, Collection<T> dataSet) {
        for (DATA data : dataSet) {
            addItem(location++, viewType, data);
        }
    }

    /**
     * 移动Item 的位置
     *
     * @param fromPosition 从这个位置开始移动
     * @param toPosition   移动到的位置
     */
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyDataSetChanged();
    }

    /**
     * 移除数据项
     *
     * @param position 位置
     */
    public void remove(int position) {
        mItems.remove(position);
    }

    /**
     * 情况数据项
     */
    public void clear() {
        mItems.clear();
    }

    /**
     * 注册ViewHolder信息，默认视图类型为0，需要手工创建ViewHolder
     *
     * @param layout 布局ID
     */
    public void registerViewHolder(int layout) {
        registerViewHolder(0, layout, null);
    }

    /**
     * 注册ViewHolder信息，默认视图类型为0
     *
     * @param layout 布局ID
     * @param clazz  ViewHolder类
     */
    public void registerViewHolder(int layout, Class<? extends RecyclerListViewHolder> clazz) {
        registerViewHolder(0, layout, clazz);
    }

    /**
     * 注册ViewHolder信息，需要手工创建ViewHolder
     *
     * @param viewType 视图类型
     * @param layout   布局ID
     */
    public void registerViewHolder(int viewType, int layout) {
        registerViewHolder(viewType, layout, null);
    }

    /**
     * 注册ViewHolder信息
     *
     * @param viewType 视图类型
     * @param layout   布局ID
     * @param clazz    ViewHolder类
     */
    public void registerViewHolder(int viewType, int layout, Class<? extends RecyclerListViewHolder> clazz) {
        // 查找构造方法
        Constructor<? extends RecyclerListViewHolder> constructor;
        try {
            if (clazz != null) {
                constructor = clazz.getDeclaredConstructor(View.class);
            } else {
                constructor = null;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // 构造信息
        ViewHolderInfo info = new ViewHolderInfo();
        info.viewType = viewType;
        info.layout = layout;
        info.clazz = clazz;
        info.constructor = constructor;

        mHolders.put(viewType, info);
    }

    /**
     * 当设置视图数据后调用
     *
     * @param holder   ViewHolder
     * @param data     数据
     * @param position 位置
     */
    protected void onSetData(RecyclerListViewHolder<?> holder, DATA data, int position) {
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 获取数据项
     *
     * @param position 位置
     * @return 数据项
     */
    public ItemInfo<DATA> getItem(int position) {
        if (position < 0 || position >= mItems.size()) {
            return null;
        }
        return mItems.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        ItemInfo item = getItem(position);
        if (item == null) {
            throw new RuntimeException("Can't get view type.");
        }
        return item.viewType;
    }

    public DATA getItemData(int position) {
        ItemInfo<DATA> item = getItem(position);
        if (item == null) {
            throw new RuntimeException("Can't get data.");
        }
        return item.data;
    }

    /**
     * 记录数据相关信息
     */
    public static class ItemInfo<T> {
        public int viewType; // 视图类型
        public T data; // 数据

        public ItemInfo(int viewType, T data) {
            this.data = data;
            this.viewType = viewType;
        }
    }

    /**
     * 记录ViewHolder的相关信息
     */
    class ViewHolderInfo {
        int viewType; // 视图类型
        int layout; // 布局文件ID
        Class<? extends RecyclerListViewHolder> clazz; // ViewHolder的类
        Constructor<? extends RecyclerListViewHolder> constructor; // ViewHolder的构造方法
    }
}
