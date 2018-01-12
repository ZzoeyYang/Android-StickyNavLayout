package online.sniper.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.Checkable;

public class CheckedImageView extends AppCompatImageView implements Checkable {
    private boolean isChecked = false;

    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };

    public CheckedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CheckedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckedImageView(Context context) {
        super(context);
    }

    public void setChecked(boolean check) {
        isChecked = check;
        refreshDrawableState();
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void toggle() {
        setChecked(!isChecked);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }
} 