package online.sniper.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * 依据X轴(宽度)等比缩放的RelativeLayout
 * <p>
 * Created by wangpeihe on 2017/5/2.
 */
public class XRCRelativeLayout extends RCRelativeLayout {
    private double mAspectRadio = 0;

    public XRCRelativeLayout(Context context) {
        this(context, null);
    }

    public XRCRelativeLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = (int) (width * mAspectRadio + 0.5);
        
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
