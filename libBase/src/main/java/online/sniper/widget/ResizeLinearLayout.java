package online.sniper.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ResizeLinearLayout extends LinearLayout {
    private OnKeyboardListener onKeyboardListener;
    private boolean isKeyboardPopup = false;

    public ResizeLinearLayout(Context context) {
        super(context);
    }

    public ResizeLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnKeyboardListener(OnKeyboardListener listener) {
        onKeyboardListener = listener;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (isKeyboardPopup) {
//            if (event.getAction() == MotionEvent.ACTION_UP) {
//                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                if (imm != null) {
//                    View decorView = ((Activity) getContext()).getWindow().getDecorView();
//                    imm.hideSoftInputFromWindow(decorView.getWindowToken(), 0);
//                }
//                if (onKeyboardListener != null) {
//                    onKeyboardListener.onKeyboardTouchEvent();
//                }
//                return false;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        if (isKeyboardPopup) {
//            return true;
//        }
//        return super.onInterceptTouchEvent(ev);
//    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (onKeyboardListener != null) {
            if (oldh > h && oldh - h > oldh / 4) {
                onKeyboardListener.onKeyboardPopup();
                isKeyboardPopup = true;
            }

            if (h > oldh && h - oldh > h / 4) {
                onKeyboardListener.onKeyboardHide();
                isKeyboardPopup = false;
            }
        }
    }

    public interface OnKeyboardListener {

        public void onKeyboardPopup();

        public void onKeyboardHide();

        public void onKeyboardTouchEvent();
    }
}
