package online.sniper.widget.square;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by wangpeihe on 2016/6/23.
 */
public class SquareRelativeLayout extends RelativeLayout {

    private final SquareHelper mHelper;

    public SquareRelativeLayout(Context context) {
        this(context, null);
    }

    public SquareRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHelper = new SquareHelper(this, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = mHelper.getMeasuredSize();
        int sizeSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(sizeSpec, sizeSpec);
    }

}
