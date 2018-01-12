package online.sniper.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * 有最大高度限制的ScrollView
 * <p>
 * Created by wangpeihe on 2017/4/23.
 */
public class MaxHeightScrollView extends ScrollView {

    private final MaxSizeHelper mHelper = new MaxSizeHelper();

    public MaxHeightScrollView(Context context) {
        this(context, null);
    }

    public MaxHeightScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        mHelper.init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mHelper.getMaxHeight(), MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setMaxHeight(int maxHeight) {
        mHelper.setMaxHeight(maxHeight);
    }
}
