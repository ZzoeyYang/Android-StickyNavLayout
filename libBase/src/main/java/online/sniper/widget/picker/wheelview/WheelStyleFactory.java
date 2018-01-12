package online.sniper.widget.picker.wheelview;

import android.graphics.Color;
import android.text.Layout.Alignment;

import online.sniper.AppConfig;
import online.sniper.utils.DisplayUtils;

/**
 * @author qigaosheng
 *         <p>
 *         滚轮文本显示样式
 */
public class WheelStyleFactory {

    /**
     * Scrolling duration
     */
    private static final int SCROLLING_DURATION = 400;

    /**
     * Current value & label text color
     */
    private static final int VALUE_TEXT_COLOR = Color.BLACK;

    /**
     * Items text color
     */
    private static final int ITEMS_TEXT_COLOR = Color.parseColor("#999999");

    /**
     * wheel background color
     */
    private static final int DEFAULT_BG_COLOR = Color.WHITE;

    /**
     * Text size
     */
    private static final int TEXT_SIZE = DisplayUtils.dip2px(AppConfig.getContext(), 14);

    /**
     * Additional items height (is added to standard text item height)
     */
    private static final int ADDITIONAL_ITEM_HEIGHT = DisplayUtils.dip2px(AppConfig.getContext(), 14);

    /**
     * Top and bottom items offset (to hide that)
     */
    private static final int ITEM_OFFSET = DisplayUtils.dip2px(AppConfig.getContext(), 7);

    /**
     * Left and right padding value
     */
    private static final int PADDING = DisplayUtils.dip2px(AppConfig.getContext(), 14);

    /**
     * Default count of visible items
     */
    private static final int DEF_VISIBLE_ITEMS = 5;

    /**
     * 是否刻画分割线
     */
    private static final boolean IS_DRAW_DIVIDERS = false;

    /**
     * 是否刻画阴影
     */
    private static final boolean IS_DRAW_SHADOWS = false;

    /**
     * 列表是否循环显示
     */
    private static final boolean IS_CYCLE = true;

    public static class WheelStyle {
        /**
         * 滚动时间
         */
        public int scrollingDuration = SCROLLING_DURATION;
        /**
         * 选中文字颜色
         */
        public int colorValueText = VALUE_TEXT_COLOR;

        /**
         * 默认背景色
         */
        public int colorBg = DEFAULT_BG_COLOR;
        /**
         * 未选中文字颜色
         */
        public int colorItemText = ITEMS_TEXT_COLOR;
        /**
         * 选中文字大小
         */
        public int sizeValueText = TEXT_SIZE;
        /**
         * 未选中文字大小
         */
        public int sizeItemText = TEXT_SIZE;
        /**
         * 上下边缘渐隐效果高度
         */
        public int sizeFadeEdge = ITEM_OFFSET;
        /**
         * 每一行文本额外高度
         */
        public int sizeExtraItemHeight = ADDITIONAL_ITEM_HEIGHT;
        /**
         * 文本左右padding
         */
        public int sizeHorizontalPadding = PADDING;
        /**
         * 完整显示文本行数
         */
        public int countVisibleItems = DEF_VISIBLE_ITEMS;

        /**
         * 文本对齐方式
         */
        public Alignment textAlignment = Alignment.ALIGN_CENTER;

        /**
         * 是否刻画分割线
         */
        public boolean isDrawDivider = IS_DRAW_DIVIDERS;

        public boolean isDrawShadows = IS_DRAW_SHADOWS;

        public boolean isCycle = IS_CYCLE;
    }

    /**
     * 获取默认样式
     */
    public static WheelStyle getDefaultStyle() {
        return new WheelStyle();
    }

    public static WheelStyle getMyStyle() {
        WheelStyle style = getDefaultStyle();
        style.isDrawDivider = true;
        style.sizeFadeEdge = 0;
        style.sizeExtraItemHeight = DisplayUtils.dip2px(AppConfig.getContext(), 14);
        style.sizeValueText = style.sizeItemText + DisplayUtils.dip2px(AppConfig.getContext(), 2);
        return style;
    }
}
