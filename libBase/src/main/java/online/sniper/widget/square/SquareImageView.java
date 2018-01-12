package online.sniper.widget.square;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by wangpeihe on 2016/6/23.
 */
public class SquareImageView extends AppCompatImageView {

    private final SquareHelper mHelper;

    public SquareImageView(Context context) {
        this(context, null);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHelper = new SquareHelper(this, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = mHelper.getMeasuredSize();
        setMeasuredDimension(size, size);
    }

}
