package online.sniper.widget.picker.wheelview;

import java.util.List;

/**
 * Created by knero on 4/17/17.
 */

public class ListWheelAdapter<T> implements WheelAdapter {
    private List<T> mItems;
    // length
    private int length;
    /** The default items length */
    public static final int DEFAULT_LENGTH = -1;

    public ListWheelAdapter(List<T> items) {
        this(items, items == null ? 0 : items.size());
    }

    public ListWheelAdapter(List<T> items, int length) {
        mItems = items;
        this.length = length;
    }

    @Override
    public String getItem(int index) {
        if (index >= 0 && index < mItems.size()) {
            return mItems.get(index).toString();
        }
        return null;
    }

    @Override
    public int getItemsCount() {
        return mItems.size();
    }

    @Override
    public int getMaximumLength() {
        return length;
    }
}
