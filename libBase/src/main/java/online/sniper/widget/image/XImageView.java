package online.sniper.widget.image;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 依据X轴(宽度)等比缩放的ImageView
 * <p>
 * Created by wangpeihe on 2017/5/2.
 */
public class XImageView extends ImageView {
    private double mAspectRadio = 0;

    public XImageView(Context context) {
        this(context, null);
    }

    public XImageView(Context context, @Nullable AttributeSet attrs) {
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
