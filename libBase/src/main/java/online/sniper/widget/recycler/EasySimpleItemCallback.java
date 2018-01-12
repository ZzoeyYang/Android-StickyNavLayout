package online.sniper.widget.recycler;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by Knero on 26/10/2016.
 */

class EasySimpleItemCallback extends ItemTouchHelper.Callback {

    private boolean mMovable = false;

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            int dragFlag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            if (viewHolder instanceof RecyclerViewHolder) {
                if (!((RecyclerViewHolder) viewHolder).isMovable()) {
                    dragFlag = 0;
                }
            }
            return makeMovementFlags(dragFlag, 0);
        } else if (layoutManager instanceof LinearLayoutManager) {
            int dragFlag;
            if (((LinearLayoutManager) layoutManager).getOrientation() == OrientationHelper.VERTICAL) {
                dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            } else {
                dragFlag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            }
            return makeMovementFlags(dragFlag, 0);
        }
        return 0;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
        boolean isMovable = true;
        if (source instanceof RecyclerViewHolder) {
            isMovable &= ((RecyclerViewHolder) source).isMovable();
        }
        if (target instanceof RecyclerViewHolder) {
            isMovable &= ((RecyclerViewHolder) target).isMovable();
        }
        if (isMovable) {
            ((RecyclerViewAdapter) recyclerView.getAdapter()).onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
        }
        return isMovable;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            ((RecyclerViewHolder) viewHolder).onSelectedChanged(actionState == 2);
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (viewHolder != null) {
            ((RecyclerViewHolder) viewHolder).onSelectedChanged(false);
        }
        super.clearView(recyclerView, viewHolder);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return mMovable;
    }


    public void setMovable(boolean movable) {
        mMovable = movable;
    }
}
