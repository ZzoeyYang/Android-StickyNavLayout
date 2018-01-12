package online.sniper.widget.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by wangpeihe on 2016/6/22.
 */
public class RecyclerViewAdapter<DATA> extends RecyclerView.Adapter<RecyclerViewHolder> {

    public static final int DEFAULT_ITEM_TYPE = 0;

    private final SparseArray<ViewHolderInfo> mHolders = new SparseArray<ViewHolderInfo>();
    private final List<ItemInfo<DATA>> mItems = new ArrayList<ItemInfo<DATA>>();
    private Context mContext;
    private LayoutInflater mInflater;

    public RecyclerViewAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        ViewHolderMap a = getClass().getAnnotation(ViewHolderMap.class);
        if (a != null) {
            ViewHolder[] holders = a.value();
            if (holders != null && holders.length > 0) {
                for (int i = 0; i < holders.length; i++) {
                    ViewHolder holder = holders[i];
                    registerViewHolder(holder.viewType(), holder.layout(), holder.holder());
                }
            }
        }
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
     * 获取某个ViewType的所有原始数据
     *
     * @param type viewType
     * @return 数据项列表
     */
    public List<DATA> getSameItems(int type) {
        List<DATA> list = new ArrayList<>();
        for (ItemInfo<DATA> itemInfo : mItems) {
            if (itemInfo.viewType == type) {
                list.add(itemInfo.data);
            }
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
        moveItem(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    private void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        ItemInfo<DATA> data = mItems.remove(fromPosition);
        mItems.add(toPosition, data);
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
     * 更新数据
     *
     * @param data 数据
     */
    public void changeData(DATA data) {
        changeData(data, null);
    }

    public void changeData(DATA data, DataChangeCallback<DATA> callback) {
        changeData(DEFAULT_ITEM_TYPE, data, callback);
    }

    public void changeData(int type, DATA data) {
        changeData(type, data, null);
    }

    /**
     * 更新数据
     *
     * @param type viewType
     * @param data 数据
     */
    public void changeData(int type, DATA data, DataChangeCallback<DATA> callback) {
        int position = 0;
        for (ItemInfo<DATA> item : mItems) {
            if (item.viewType == type && item.data.equals(data)) {
                if (callback != null) {
                    callback.update(item.data, data);
                }
                item.data = data;
                notifyItemChanged(position);
                return;
            }
            position++;
        }
    }

    /**
     * 清空数据项
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
    public void registerViewHolder(int layout, Class<? extends RecyclerViewHolder> clazz) {
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
    public void registerViewHolder(int viewType, int layout, Class<? extends RecyclerViewHolder> clazz) {
        // 查找构造方法
        Constructor<? extends RecyclerViewHolder> constructor;
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

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolderInfo info = mHolders.get(viewType);
        if (info == null) {
            return null;
        }
        View view = mInflater.inflate(info.layout, parent, false);
        RecyclerViewHolder holder;
        try {
            if (info.constructor != null) {
                holder = info.constructor.newInstance(view);
            } else {
                holder = onCreateViewHolder(viewType, view);
            }
            if (holder == null) {
                throw new RuntimeException("Holder is null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        view.setTag(holder);
        holder.setAdapter(this);
        return holder;
    }

    /**
     * 新建ViewHolder
     *
     * @param viewType 视图类型
     * @param view     视图
     * @return
     */
    protected RecyclerViewHolder onCreateViewHolder(int viewType, View view) {
        throw new RuntimeException("Must override onCreateViewHolder(), can not call super.onCreateViewHolder()");
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        ItemInfo<DATA> item = getItem(position);
        DATA data = item == null ? null : item.data;
        holder.setData(data, position);
        onSetData(holder, data, position);
    }

    /**
     * 当设置视图数据后调用
     *
     * @param holder   ViewHolder
     * @param data     数据
     * @param position 位置
     */
    protected void onSetData(RecyclerViewHolder<?> holder, DATA data, int position) {
    }

    @Override
    public int getItemCount() {
        return mItems.size();
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
        Class<? extends RecyclerViewHolder> clazz; // ViewHolder的类
        Constructor<? extends RecyclerViewHolder> constructor; // ViewHolder的构造方法
    }

}
