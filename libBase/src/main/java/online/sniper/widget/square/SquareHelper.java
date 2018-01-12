package online.sniper.widget.square;

import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import online.sniper.R;

/**
 * 方形视图帮助类
 * Created by wangpeihe on 2016/6/23.
 */
public class SquareHelper {

    public static final int BASE_WIDTH = 0;
    public static final int BASE_HEIGHT = 1;
    public static final int BASE_MIN_EDGE = 2;
    public static final int BASE_MAX_EDGE = 3;

    @IntDef({BASE_WIDTH, BASE_HEIGHT, BASE_MAX_EDGE, BASE_MIN_EDGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SquareViewEdge {

    }

    private View mView;
    private int mBaseEdge = BASE_WIDTH;

    public SquareHelper(View view, AttributeSet attrs) {
        mView = view;
        TypedArray a = view.getContext().obtainStyledAttributes(attrs, R.styleable.SquareView);
        try {
            mBaseEdge = a.getInt(R.styleable.SquareView_baseEdge, BASE_WIDTH);
        } finally {
            a.recycle();
        }
    }

    public void setBaseEdge(@SquareViewEdge int baseEdge) {
        mBaseEdge = baseEdge;
    }

    public int getMeasuredSize() {
        switch (mBaseEdge) {
            case BASE_WIDTH:
                return mView.getMeasuredWidth();
            case BASE_HEIGHT:
                return mView.getMeasuredHeight();
            case BASE_MIN_EDGE:
                return Math.min(mView.getMeasuredWidth(), mView.getMeasuredHeight());
            case BASE_MAX_EDGE:
                return Math.max(mView.getMeasuredWidth(), mView.getMeasuredHeight());
            default:
                return mView.getMeasuredWidth();
        }
    }

}
