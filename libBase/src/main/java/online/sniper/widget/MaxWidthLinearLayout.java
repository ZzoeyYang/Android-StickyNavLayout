package online.sniper.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 有最大宽度限制的LinearLayout
 * <p>
 * Created by wangpeihe on 2017/4/23.
 */
public class MaxWidthLinearLayout extends LinearLayout {

    private final MaxSizeHelper mHelper = new MaxSizeHelper();

    public MaxWidthLinearLayout(Context context) {
        this(context, null);
    }

    public MaxWidthLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        mHelper.init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(mHelper.getMaxWidth(), MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setMaxWidth(int maxWidth) {
        mHelper.setMaxWidth(maxWidth);
    }
}
