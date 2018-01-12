package online.sniper.widget.recycler;

import android.view.View;

/**
 * 默认的ViewHolder
 * <p/>
 * Created by wangpeihe on 2016/6/22.
 */
public final class DefaultViewHolder extends RecyclerViewHolder<Object> {

    public DefaultViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onSetData(Object data, int position) {
    }
}
