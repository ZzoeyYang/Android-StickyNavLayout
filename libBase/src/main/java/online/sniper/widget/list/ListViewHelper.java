package online.sniper.widget.list;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ListView;

public final class ListViewHelper {
    private ListViewHelper() {
    }

    @SuppressLint("NewApi")
    public static void smoothScrollToPositionFromTop(final ListView listView, final int position, final int offset) {
        if (listView == null) {
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= 11) {
            listView.smoothScrollToPositionFromTop(position, offset);
        } else {
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.setSelectionFromTop(position, offset);
                }
            });
        }
    }

    @SuppressLint("NewApi")
    public static void smoothScrollToPositionFromTop(final ListView listView, final int position, final int offset, int duration) {
        if (listView == null) {
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= 11) {
            listView.smoothScrollToPositionFromTop(position, offset, duration);
        } else {
            listView.post(new Runnable() {

                @Override
                public void run() {
                    listView.setSelectionFromTop(position, offset);
                }
            });
        }
    }

    /**
     * 判断列表是否可以向下拉
     *
     * @param listView
     * @return
     */
    public static boolean canPullDown(ListView listView) {
        // 无数据可以向下滚动
        if (listView == null || listView.getVisibility() != View.VISIBLE) {
            return false;
        }

        if (listView.getFirstVisiblePosition() > 0) {
            return true;
        }

        View firstView = listView.getChildAt(0);
        if (firstView == null) {
            return false;
        }

        return firstView.getTop() < 0;
    }

    /**
     * 判断列表是否可以向上拉
     *
     * @param listView
     * @return
     */
    public static boolean canPullUp(ListView listView) {
        if (listView == null || listView.getVisibility() != View.VISIBLE) {
            return false;
        }

        if (listView.getCount() == 0) {
            return false;
        } else if (listView.getLastVisiblePosition() == (listView.getCount() - 1)) {
            View lastView = listView.getChildAt(listView.getLastVisiblePosition() - listView.getFirstVisiblePosition());
            if (lastView.getBottom() <= listView.getMeasuredHeight()) {
                return false;
            }
        }

        return true;
    }

}
