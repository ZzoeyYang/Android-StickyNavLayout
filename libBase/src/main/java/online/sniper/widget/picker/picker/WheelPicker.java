package online.sniper.widget.picker.picker;

import android.app.Activity;
import android.view.View;

import online.sniper.widget.picker.popup.ConfirmPopup;


/**
 * 滑轮选择器
 *
 * @author 李玉江[QQ:1032694760]
 * @since 2015/12/22
 */
public abstract class WheelPicker extends ConfirmPopup<View> {

    private View contentView;

    public WheelPicker(Activity activity) {
        super(activity);
    }

    /**
     * 得到选择器视图，可内嵌到其他视图容器
     */
    @Override
    public View getContentView() {
        if (null == contentView) {
            contentView = makeCenterView();
        }
        return contentView;
    }

}
