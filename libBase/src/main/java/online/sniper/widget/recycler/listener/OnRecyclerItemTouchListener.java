package online.sniper.widget.recycler.listener;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnItemTouchListener;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.MotionEvent;

/**
 * Created by luozhaocheng on 6/23/16.
 */
public abstract class OnRecyclerItemTouchListener implements OnItemTouchListener {

    private RecyclerView mRecyclerView;
    private ItemTouchHelperGestureListener mGestureListener;
    private GestureDetectorCompat mGestureDetector = null;

    public OnRecyclerItemTouchListener(Context context, RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mGestureListener = new ItemTouchHelperGestureListener(mRecyclerView) {

            @Override
            public void onItemClick(ViewHolder viewHolder) {
                OnRecyclerItemTouchListener.this.onItemClick(viewHolder);
            }

            @Override
            public void onLongClick(ViewHolder viewHolder) {
                OnRecyclerItemTouchListener.this.onLongClick(viewHolder);
            }
        };
        this.mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        this.mGestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        this.mGestureDetector.onTouchEvent(e);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    public abstract void onItemClick(RecyclerView.ViewHolder viewHolder);

    public abstract void onLongClick(RecyclerView.ViewHolder viewHolder);
}
