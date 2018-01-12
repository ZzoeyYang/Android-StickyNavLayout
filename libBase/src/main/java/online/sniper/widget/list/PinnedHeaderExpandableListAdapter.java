package online.sniper.widget.list;

import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

public abstract class PinnedHeaderExpandableListAdapter extends BaseExpandableListAdapter implements PinnedHeaderExpandableListView.PinnedHeaderAdapter {
    @Override
    public long getGroupId(int groupPosition) {
        return ExpandableListView.getPackedPositionForGroup(groupPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return ExpandableListView.getPackedPositionForChild(groupPosition, childPosition);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
