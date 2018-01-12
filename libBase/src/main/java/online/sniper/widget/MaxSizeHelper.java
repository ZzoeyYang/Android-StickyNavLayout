package online.sniper.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import online.sniper.R;
import online.sniper.utils.DisplayUtils;

/**
 * Created by wangpeihe on 2017/10/30.
 */

public class MaxSizeHelper {
    private int maxWidth;
    private int maxHeight;

    public MaxSizeHelper() {
    }

    public void init(Context context, AttributeSet attrs) {
        int defaultWidth = DisplayUtils.getScreenWidth(context) / 2;
        int defaultHeight = DisplayUtils.getScreenHeight(context) / 2;
        if (attrs != null) {
            TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.MaxSize);
            maxWidth = styledAttrs.getDimensionPixelSize(R.styleable.MaxSize_maxWidth, defaultWidth);
            maxHeight = styledAttrs.getDimensionPixelSize(R.styleable.MaxSize_maxHeight, defaultHeight);
            styledAttrs.recycle();
        } else {
            maxWidth = defaultWidth;
            maxHeight = defaultHeight;
        }
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }
}
