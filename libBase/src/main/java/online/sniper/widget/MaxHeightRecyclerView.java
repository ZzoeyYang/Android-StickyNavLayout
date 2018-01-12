package online.sniper.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * 有最大高度限制的RecyclerView
 * Created by pingyongxia on 2017/12/11.
 */
public class MaxHeightRecyclerView extends RecyclerView {

    private final MaxSizeHelper mHelper = new MaxSizeHelper();

    public MaxHeightRecyclerView(Context context) {
        this(context, null);
    }

    public MaxHeightRecyclerView(Context context, AttributeSet attrs) {
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
