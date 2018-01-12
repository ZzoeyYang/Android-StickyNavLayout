package com.zhy.stickynavlayout.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 展示轮播图的ViewPager
 */
public class BannerViewPager extends ViewPager {

    /**
     * 定时器控制器
     */
    public interface TimerController {

        void startTimer();

        void cancelTimer();
    }

    private TimerController mTimerController = null;

    private boolean isMoving = false;

    public BannerViewPager(Context mContext) {
        super(mContext);
    }

    public BannerViewPager(Context mContext, AttributeSet attrs) {
        super(mContext, attrs);
    }

    public void setTimerController(TimerController controller) {
        mTimerController = controller;
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEventCompat.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                // 暂停定时器
                if (!isMoving && mTimerController != null) {
                    mTimerController.cancelTimer();
                    isMoving = true;
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                // 唤醒定时器
                if (isMoving && mTimerController != null) {
                    //滑动完成，唤醒自动轮播线程
                    mTimerController.startTimer();
                    isMoving = false;
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }
}
