package online.sniper.utils;

import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ScrollView;

public class ScrollUtils {
    /**
     * 判断视图是否可以向上滚动
     */
    public static boolean canScrollUp(View targetView) {
        return canScrollVertically(targetView, -1);
    }

    /**
     * 判断视图是否可以向下滚动
     */
    public static boolean canScrollDown(View targetView) {
        return canScrollVertically(targetView, 1);
    }

    @SuppressWarnings("deprecation")
    public static boolean canScrollVertically(View targetView, int direction) {
        if (Build.VERSION.SDK_INT < 14) {
            if (direction < 0) {
                if (targetView instanceof AbsListView) {
                    final AbsListView absListView = (AbsListView) targetView;
                    return absListView.getChildCount() > 0
                            && (absListView.getFirstVisiblePosition() > 0
                            || absListView.getChildAt(0).getTop() < absListView.getPaddingTop());
                } else {
                    return ViewCompat.canScrollVertically(targetView, direction) || targetView.getScrollY() > 0;
                }
            } else {
                if (targetView instanceof AbsListView) {
                    final AbsListView absListView = (AbsListView) targetView;
                    if (absListView.getCount() <= 0) {
                        return false;
                    } else if (absListView.getLastVisiblePosition() < (absListView.getCount() - 1)) {
                        return true;
                    } else {
                        View lastView = absListView.getChildAt(absListView.getLastVisiblePosition() - absListView.getFirstVisiblePosition());
                        return !(lastView != null && lastView.getBottom() <= absListView.getMeasuredHeight());
                    }
                } else if (targetView instanceof ScrollView) {
                    ScrollView scrollView = (ScrollView) targetView;
                    View child = scrollView.getChildAt(0);
                    return child != null && scrollView.getScrollY() < (child.getHeight() - scrollView.getMeasuredHeight());
                } else if (targetView instanceof WebView) {
                    WebView webView = (WebView) targetView;
                    return webView.getScrollY() < (webView.getContentHeight() * webView.getScale() - webView.getMeasuredHeight());
                } else {
                    return ViewCompat.canScrollVertically(targetView, direction);
                }
            }
        } else {
            return ViewCompat.canScrollVertically(targetView, direction);
        }
    }

    public static void scrollToUp(ScrollView scrollView) {
        scrollView.scrollTo(0, 0);
    }

    public static void scrollToDown(ScrollView scrollView) {
        int y = scrollView.getHeight();
        int count = scrollView.getChildCount();
        if (count > 0) {
            View view = scrollView.getChildAt(count - 1);
            y = view.getBottom() + scrollView.getPaddingBottom();
        }
        scrollView.scrollTo(0, y);
    }

    public static void smoothScrollToUp(ScrollView scrollView) {
        scrollView.smoothScrollTo(0, 0);
    }

    public static void smoothScrollToDown(ScrollView scrollView) {
        int y = scrollView.getHeight();
        int count = scrollView.getChildCount();
        if (count > 0) {
            View view = scrollView.getChildAt(count - 1);
            y = view.getBottom() + scrollView.getPaddingBottom();
        }
        scrollView.smoothScrollTo(0, y);
    }
}