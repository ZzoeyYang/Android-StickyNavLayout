package online.sniper.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 自动启动动画的View
 * Created by wangpeihe on 2016/11/16.
 */
public class AnimationImageView extends ImageView {

    private boolean isStarting = false;

    public AnimationImageView(Context context) {
        super(context);
    }

    public AnimationImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable d = getDrawable();
        if (d instanceof AnimationDrawable) {
            if (!isStarting) {
                ((AnimationDrawable) d).start();
                isStarting = true;
            }
        } else {
            isStarting = false;
        }
        super.onDraw(canvas);
    }
}
