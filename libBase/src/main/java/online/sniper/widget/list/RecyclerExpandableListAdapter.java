package online.sniper.widget.list;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangpeihe on 2017/3/27.
 */

public abstract class RecyclerExpandableListAdapter<G, C> extends PinnedHeaderExpandableListAdapter {

    protected final List<GroupChildren<G, C>> mList = new ArrayList<>();

    private final SparseArray<ViewHolderInfo> mGroupHolders = new SparseArray<ViewHolderInfo>();
    private final SparseArray<ViewHolderInfo> mChildHolders = new SparseArray<ViewHolderInfo>();

    private Context mContext;
    private LayoutInflater mInflater;

    public RecyclerExpandableListAdapter(Context context) {
        super();
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public Context getContext() {
        return mContext;
    }

    public void addAll(List<GroupChildren<G, C>> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void registerGroupViewHolder(int viewType, int layout) {
        ViewHolderInfo info = new ViewHolderInfo();
        info.viewType = viewType;
        info.layout = layout;
        mGroupHolders.put(viewType, info);
    }

    public void registerChildViewHolder(int viewType, int layout) {
        ViewHolderInfo info = new ViewHolderInfo();
        info.viewType = viewType;
        info.layout = layout;
        mChildHolders.put(viewType, info);
    }

    @Override
    public int getGroupCount() {
        return mList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mList.get(groupPosition).children.size();
    }

    @Override
    public int getGroupTypeCount() {
        return 16;
    }

    @Override
    public int getChildTypeCount() {
        return 16;
    }

    @Override
    public G getGroup(int groupPosition) {
        return mList.get(groupPosition).data;
    }

    @Override
    public C getChild(int groupPosition, int childPosition) {
        return mList.get(groupPosition).children.get(childPosition);
    }

    @Override
    public void setHeaderView(View headerView, int groupPosition, boolean isExpanded, boolean isCustomHeader, int alpha) {
    }

    @Override
    public int getHeaderType(int groupPosition) {
        return getGroupType(groupPosition);
    }

    @Override
    public View getHeaderView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final int viewType = getHeaderType(groupPosition);
        final ViewHolderInfo info = mGroupHolders.get(viewType);
        final View view;
        final RecyclerGroupViewHolder holder;
        if (convertView == null) {
            view = mInflater.inflate(info.layout, parent, false);
            holder = onCreateHeaderViewHolder(viewType, view);
            if (holder == null) {
                throw new RuntimeException("Holder is null.");
            }
            holder.setAdapter(this);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (RecyclerGroupViewHolder) view.getTag();
        }
        G data = getGroup(groupPosition);
        holder.setData(data, groupPosition, isExpanded);
        return view;
    }

    protected RecyclerGroupViewHolder onCreateHeaderViewHolder(int viewType, View view) {
        return onCreateGroupViewHolder(viewType, view);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final int viewType = getGroupType(groupPosition);
        final ViewHolderInfo info = mGroupHolders.get(viewType);
        final View view;
        final RecyclerGroupViewHolder holder;
        if (convertView == null) {
            view = mInflater.inflate(info.layout, parent, false);
            holder = onCreateGroupViewHolder(viewType, view);
            if (holder == null) {
                throw new RuntimeException("Holder is null.");
            }
            holder.setAdapter(this);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (RecyclerGroupViewHolder) view.getTag();
        }
        G data = getGroup(groupPosition);
        holder.setData(data, groupPosition, isExpanded);
        return view;
    }

    protected abstract RecyclerGroupViewHolder onCreateGroupViewHolder(int viewType, View view);

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final int viewType = getChildType(groupPosition, childPosition);
        final ViewHolderInfo info = mChildHolders.get(viewType);
        final View view;
        final RecyclerChildViewHolder holder;
        if (convertView == null) {
            view = mInflater.inflate(info.layout, parent, false);
            holder = onCreateChildViewHolder(viewType, view);
            if (holder == null) {
                throw new RuntimeException("Holder is null.");
            }
            holder.setAdapter(this);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (RecyclerChildViewHolder) view.getTag();
        }
        C data = getChild(groupPosition, childPosition);
        holder.setData(data, groupPosition, childPosition);
        return view;
    }

    protected abstract RecyclerChildViewHolder onCreateChildViewHolder(int viewType, View view);

    /**
     * 记录ViewHolder的相关信息
     */
    class ViewHolderInfo {
        int viewType; // 视图类型
        int layout; // 布局文件ID
    }
}
