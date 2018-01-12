package online.sniper.widget.image;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by wangpeihe on 2017/7/28.
 */

public class XRoundedImageView extends RoundedImageView {
    private double mAspectRadio = 0;

    public XRoundedImageView(Context context) {
        this(context, null);
    }

    public XRoundedImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.CENTER_CROP);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height;
        if (mAspectRadio <= 0) {
            Drawable drawable = getDrawable();
            if (drawable == null) {
                return;
            }
            double w = drawable.getIntrinsicWidth();
            double h = drawable.getIntrinsicHeight();
            height = w <= 0 ? 0 : (int) (h * width / w + 0.5);
            setMeasuredDimension(width, height);
        } else {
            height = (int) (width * mAspectRadio + 0.5);
            setMeasuredDimension(width, height);
        }
    }

    /**
     * 设置高宽比
     */
    public void setAspectRadio(double width, double height) {
        mAspectRadio = height / width;
        invalidate();
    }

    /**
     * 设置高宽比
     */
    public void setAspectRadio(double radio) {
        mAspectRadio = radio;
        invalidate();
    }

    /**
     * 获取高宽比
     */
    public double getAspectRadio() {
        return mAspectRadio;
    }
}
