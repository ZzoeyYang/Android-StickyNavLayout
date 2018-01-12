package online.sniper.widget.list;

import android.view.View;

/**
 * 默认的ViewHolder
 * <p/>
 * Created by wangpeihe on 2016/6/22.
 */
public final class DefaultListViewHolder extends RecyclerListViewHolder<Object> {
    public DefaultListViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onSetData(Object data, int position) {
    }
}
