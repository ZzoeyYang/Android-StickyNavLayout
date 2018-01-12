package online.sniper.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * 类似{@link android.widget.ImageView.ScaleType#CENTER_INSIDE}的自动缩小的布局
 * <p>
 * Created by wangpeihe on 2017/11/8.
 */
public class InsideScaleLayout extends FrameLayout {
    public InsideScaleLayout(@NonNull Context context) {
        this(context, null);
    }

    public InsideScaleLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        if (count == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else if (count == 1) {
            // 恢复缩放
            View child = getChildAt(0);
            setChildScale(child, 1);
            // 测量
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            // 测量子视图
            int size = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            child.measure(size, size);
            float childWidth = child.getMeasuredWidth();
            float childHeight = child.getMeasuredHeight();
            float width = getMeasuredWidth();
            float height = getMeasuredHeight();
            // 计算缩放因子
            if (width == 0 || height == 0) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            } else if (childWidth > width || childHeight > height) {
                float scaleX = width / childWidth;
                float scaleY = height / childHeight;
                float scale = scaleX < scaleY ? scaleX : scaleY;
                setChildScale(child, scale);
                float scaleChildWidth = childWidth * scale;
                float scaleChildHeight = childHeight * scale;
                int widthMode = MeasureSpec.getMode(widthMeasureSpec);
                int heightMode = MeasureSpec.getMode(heightMeasureSpec);
                if (widthMode != MeasureSpec.EXACTLY) {
                    if (scaleChildWidth < width) {
                        width = scaleChildWidth;
                    }
                }
                if (heightMode != MeasureSpec.EXACTLY) {
                    if (scaleChildHeight < height) {
                        height = scaleChildHeight;
                    }
                }
                setMeasuredDimension((int) width, (int) height);
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        } else {
            throw new RuntimeException("Only one child.");
        }
    }

    private void setChildScale(View view, float scale) {
        view.setScaleX(scale);
        view.setScaleY(scale);
    }
}
