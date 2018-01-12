package online.sniper.utils;

import android.graphics.Paint;
import android.widget.TextView;

/**
 * Created by wangling-ob on 2016/11/12.
 */

public class TextViewUtils {
    /**
     * 给TextView内的文字添加中划线
     *
     * @param textView
     */
    public static void addStrike(TextView textView) {
        textView.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        textView.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
    }
}
