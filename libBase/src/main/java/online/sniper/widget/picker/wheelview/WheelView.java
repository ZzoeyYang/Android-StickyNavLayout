/*
 *  Android Wheel Control.
 *  https://code.google.com/p/android-wheel/
 *  
 *  Copyright 2010 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package online.sniper.widget.picker.wheelview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import java.util.LinkedList;
import java.util.List;

import online.sniper.AppConfig;
import online.sniper.R;
import online.sniper.utils.DisplayUtils;

/**
 * Numeric wheel view.
 */
public class WheelView extends View {

    /**
     * Minimum delta for scrolling
     */
    private static final int MIN_DELTA_FOR_SCROLLING = 1;

    /**
     * Top and bottom shadows colors
     */
    private static final int[] SHADOWS_COLORS = new int[]{0xFF111111, 0x00AAAAAA, 0x00AAAAAA};

    /**
     * Additional width for items layout
     */
    private static final int ADDITIONAL_ITEMS_SPACE = DisplayUtils.dip2px(AppConfig.getContext(), 5);

    /**
     * Label offset
     */
    private static final int LABEL_OFFSET = DisplayUtils.dip2px(AppConfig.getContext(), 4);

    // Wheel Values
    private WheelAdapter adapter = null;
    private int currentItem = 0;

    // Widths
    private int itemsWidth = 0;
    private int labelWidth = 0;

    // Item height
    private int itemHeight = 0;

    // Text paints
    private TextPaint itemsPaint;
    private TextPaint valuePaint;
    private Paint dividerPaint;

    // Layouts
    private StaticLayout itemsLayout;
    private StaticLayout labelLayout;
    private StaticLayout valueLayout;

    // Label & background
    private String label;
    private Drawable centerDrawable;

    // Shadows drawables
    private GradientDrawable topShadow;
    private GradientDrawable bottomShadow;

    // Scrolling
    private boolean isScrollingPerformed;
    private int scrollingOffset;

    // Scrolling animation
    private Scroller scroller;
    private int lastScrollY;
    private VelocityTracker mVelocityTracker;

    private int mMinimumFlingVelocity;
    private int mMaximumFlingVelocity;

    // Listeners
    private List<OnWheelChangedListener> changingListeners = new LinkedList<OnWheelChangedListener>();
    private List<OnWheelScrollListener> scrollingListeners = new LinkedList<OnWheelScrollListener>();

    private WheelStyleFactory.WheelStyle mStyle = WheelStyleFactory.getDefaultStyle();

    public int type = 1;

    private int mMaxTextCount = 0;

    /**
     * Constructor
     */
    public WheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initData(context);
    }

    /**
     * Constructor
     */
    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData(context);
    }

    /**
     * Constructor
     */
    public WheelView(Context context) {
        super(context);
        initData(context);
    }

    public void setWheelStyle(WheelStyleFactory.WheelStyle style) {
        if (style == null)
            return;
        this.mStyle = style;
    }

    /**
     * Initializes class data
     *
     * @param context the context
     */
    @SuppressWarnings("deprecation")
    private void initData(Context context) {
        scroller = new Scroller(context);
        if (context == null) {
            // noinspection deprecation
            mMinimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity();
            mMaximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();
        } else {
            final ViewConfiguration configuration = ViewConfiguration.get(context);
            mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
            mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        }
    }

    /**
     * Gets wheel adapter
     *
     * @return the adapter
     */
    public WheelAdapter getAdapter() {
        return adapter;
    }

    /**
     * Sets wheel adapter
     *
     * @param adapter the new wheel adapter
     */
    public void setAdapter(WheelAdapter adapter) {
        this.adapter = adapter;
        invalidateLayouts();
        invalidate();
    }

    /**
     * Set the the specified scrolling interpolator
     *
     * @param interpolator the interpolator
     */
    public void setInterpolator(Interpolator interpolator) {
        scroller.forceFinished(true);
        scroller = new Scroller(getContext(), interpolator);
    }

    /**
     * Gets count of visible items
     *
     * @return the count of visible items
     */
    public int getVisibleItems() {
        return mStyle.countVisibleItems;
    }

    /**
     * Sets count of visible items
     *
     * @param count the new count
     */
    public void setVisibleItems(int count) {
        if (count <= 0)
            return;
        // visibleItems = count;
        mStyle.countVisibleItems = count;
        invalidate();
    }

    /**
     * Gets label
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets label
     *
     * @param newLabel the label to set
     */
    public void setLabel(String newLabel) {
        newLabel = newLabel != null ? "\n" + newLabel + "\n" : null;
        if (label == null || !label.equals(newLabel)) {
            label = newLabel;
            labelLayout = null;
            invalidate();
        }
    }

    /**
     * Adds wheel changing listener
     *
     * @param listener the listener
     */
    public void addChangingListener(OnWheelChangedListener listener) {
        changingListeners.add(listener);
    }

    /**
     * Removes wheel changing listener
     *
     * @param listener the listener
     */
    public void removeChangingListener(OnWheelChangedListener listener) {
        changingListeners.remove(listener);
    }

    /**
     * Notifies changing listeners
     *
     * @param oldValue the old wheel value
     * @param newValue the new wheel value
     */
    protected void notifyChangingListeners(int oldValue, int newValue) {
        for (OnWheelChangedListener listener : changingListeners) {
            listener.onChanged(this, oldValue, newValue);
        }
    }

    /**
     * Adds wheel scrolling listener
     *
     * @param listener the listener
     */
    public void addScrollingListener(OnWheelScrollListener listener) {
        scrollingListeners.add(listener);
    }

    /**
     * Removes wheel scrolling listener
     *
     * @param listener the listener
     */
    public void removeScrollingListener(OnWheelScrollListener listener) {
        scrollingListeners.remove(listener);
    }

    /**
     * Notifies listeners about starting scrolling
     */
    protected void notifyScrollingListenersAboutStart() {
        for (OnWheelScrollListener listener : scrollingListeners) {
            listener.onScrollingStarted(this);
        }
    }

    /**
     * Notifies listeners about ending scrolling
     */
    protected void notifyScrollingListenersAboutEnd() {
        for (OnWheelScrollListener listener : scrollingListeners) {
            listener.onScrollingFinished(this);
        }
    }

    /**
     * Gets current value
     *
     * @return the current value
     */
    public int getCurrentItem() {
        return currentItem;
    }

    /**
     * Sets the current item. Does nothing when index is wrong.
     *
     * @param index    the item index
     * @param animated the animation flag
     */
    public void setCurrentItem(int index, boolean animated) {
        if (adapter == null || adapter.getItemsCount() == 0) {
            return; // throw?
        }
        if (index < 0 || index >= adapter.getItemsCount()) {
            if (mStyle.isCycle) {
                while (index < 0) {
                    index += adapter.getItemsCount();
                }
                index %= adapter.getItemsCount();
            } else {
                return; // throw?
            }
        }
        if (index != currentItem) {
            if (animated) {
                scroll(index - currentItem, mStyle.scrollingDuration);
            } else {
                invalidateLayouts();

                int old = currentItem;
                currentItem = index;

                notifyChangingListeners(old, currentItem);

                invalidate();
            }
        }
    }

    /**
     * Sets the current item w/o animation. Does nothing when index is wrong.
     *
     * @param index the item index
     */
    public void setCurrentItem(int index) {
        setCurrentItem(index, false);
    }

    /**
     * Tests if wheel is cyclic. That means before the 1st item there is shown the last one
     *
     * @return true if wheel is cyclic
     */
    public boolean isCyclic() {
        return mStyle.isCycle;
    }

    /**
     * Set wheel cyclic flag
     *
     * @param isCyclic the flag to set
     */
    public void setCyclic(boolean isCyclic) {
        this.mStyle.isCycle = isCyclic;

        invalidate();
        invalidateLayouts();
    }

    /**
     * Invalidates layouts
     */
    private void invalidateLayouts() {
        itemsLayout = null;
        valueLayout = null;
        scrollingOffset = 0;
    }

    /**
     * Initializes resources
     */
    private void initResourcesIfNecessary() {
        if (itemsPaint == null) {
            itemsPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            // itemsPaint.density = getResources().getDisplayMetrics().density;
            itemsPaint.setTextSize(mStyle.sizeItemText);
        }

        if (valuePaint == null) {
            valuePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            // valuePaint.density = getResources().getDisplayMetrics().density;
            valuePaint.setTextSize(mStyle.sizeValueText);
            valuePaint.setShadowLayer(0.1f, 0, 0.1f, 0xFFC0C0C0);
        }

        if (centerDrawable == null) {
            centerDrawable = getContext().getResources().getDrawable(R.drawable.bg_time_selected);
        }

        if (topShadow == null) {
            topShadow = new GradientDrawable(Orientation.TOP_BOTTOM, SHADOWS_COLORS);
        }

        if (bottomShadow == null) {
            bottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP, SHADOWS_COLORS);
        }

        if (dividerPaint == null) {
            dividerPaint = new Paint();
            dividerPaint.setColor(Color.parseColor("#d8d8d8"));
        }

        // setBackgroundResource(R.drawable.wheel_bg);
    }

    /**
     * Calculates desired height for layout
     *
     * @param layout the source layout
     * @return the desired layout height
     */
    private int getDesiredHeight(Layout layout) {
        if (layout == null) {
            return 0;
        }

        int desired = getItemHeight() * mStyle.countVisibleItems - mStyle.sizeFadeEdge * 2;

        // Check against our minimum height
        desired = Math.max(desired, getSuggestedMinimumHeight());

        return desired;
    }

    /**
     * Returns text item by index
     *
     * @param index the item index
     * @return the item or null
     */
    private String getTextItem(int index) {
        if (adapter == null || adapter.getItemsCount() == 0) {
            return null;
        }
        int count = adapter.getItemsCount();
        if ((index < 0 || index >= count) && !mStyle.isCycle) {
            return null;
        } else {
            while (index < 0) {
                index = count + index;
            }
        }

        index %= count;
        return adapter.getItem(index);
    }

    /**
     * Builds text depending on current value
     *
     * @param useCurrentValue
     * @return the text
     */
    private String buildText(boolean useCurrentValue) {
        StringBuilder itemsText = new StringBuilder();
        int addItems = mStyle.countVisibleItems / 2 + 1;

        for (int i = currentItem - addItems; i <= currentItem + addItems; i++) {
            // if (useCurrentValue || i != currentItem) {
            String text = getTextItem(i);
            if (text != null) {
                text = dealSingleLineStr(text);
                itemsText.append(text);
            }
            // }
            if (i < currentItem + addItems) {
                itemsText.append("\n");
            }
        }

        return itemsText.toString();
    }

    /**
     * Returns the max item length that can be present
     *
     * @return the max length
     */
    private int getMaxTextLength() {
        WheelAdapter adapter = getAdapter();
        if (adapter == null) {
            return 0;
        }

        int adapterLength = adapter.getMaximumLength();
        if (adapterLength > 0) {
            return adapterLength;
        }

        String maxText = null;
        // int addItems = mStyle.countVisibleItems / 2;
        // for (int i = Math.max(currentItem - addItems, 0); i < Math.min(currentItem + mStyle.countVisibleItems, adapter.getItemsCount());
        // i++) {
        for (int i = 0; i < adapter.getItemsCount(); i++) {
            String text = adapter.getItem(i);
            if (text != null && (maxText == null || maxText.length() < text.length())) {
                maxText = text;
            }
        }

        return maxText != null ? maxText.length() : 0;
    }

    /**
     * Returns height of wheel item
     *
     * @return the item height
     */
    private int getItemHeight() {
        if (itemHeight != 0) {
            return itemHeight;
        } else if (itemsLayout != null && itemsLayout.getLineCount() > 2) {
            itemHeight = itemsLayout.getLineTop(2) - itemsLayout.getLineTop(1);
            return itemHeight;
        }

        return getHeight() / mStyle.countVisibleItems;
    }

    /**
     * Calculates control width and creates text layouts
     *
     * @param widthSize the input layout width
     * @param mode      the layout mode
     * @return the calculated control width
     */
    private int calculateLayoutWidth(int widthSize, int mode) {
        initResourcesIfNecessary();

        int width = widthSize;

        int maxLength = getMaxTextLength();
        if (type == 1)
            if (maxLength > 0) {
                float textWidth = (float) Math.ceil(Layout.getDesiredWidth("0", itemsPaint));
                itemsWidth = (int) (maxLength * textWidth);
            } else {
                itemsWidth = 0;
            }
        itemsWidth += ADDITIONAL_ITEMS_SPACE; // make it some more
        if (type == 1)
            Log.d("abc", "itemsWidth = " + itemsWidth);

        labelWidth = 0;
        if (label != null && label.length() > 0) {
            labelWidth = (int) Math.ceil(Layout.getDesiredWidth(label, valuePaint));
        }

        boolean recalculate = false;
        if (mode == MeasureSpec.EXACTLY) {
            width = widthSize;
            recalculate = true;
        } else {
            width = itemsWidth + labelWidth + 2 * mStyle.sizeHorizontalPadding;
            if (labelWidth > 0) {
                width += LABEL_OFFSET;
            }

            // Check against our minimum width
            width = Math.max(width, getSuggestedMinimumWidth());

            if (mode == MeasureSpec.AT_MOST && widthSize < width) {
                width = widthSize;
                recalculate = true;
            }
        }

        if (recalculate) {
            // recalculate width
            int pureWidth = width - LABEL_OFFSET - 2 * mStyle.sizeHorizontalPadding;
            if (pureWidth <= 0) {
                itemsWidth = labelWidth = 0;
            }
            if (labelWidth > 0) {
                double newWidthItems = (double) itemsWidth * pureWidth / (itemsWidth + labelWidth);
                itemsWidth = (int) newWidthItems;
                labelWidth = pureWidth - itemsWidth;
            } else {
                itemsWidth = pureWidth + LABEL_OFFSET; // no label
            }
        }

        if (itemsWidth > 0) {
            createLayouts(itemsWidth, labelWidth);
        }

        return width;
    }

    /**
     * Creates layouts
     *
     * @param widthItems width of items layout
     * @param widthLabel width of label layout
     */
    private void createLayouts(int widthItems, int widthLabel) {
        if (itemsLayout == null || itemsLayout.getWidth() > widthItems) {
            itemsLayout = new StaticLayout(buildText(isScrollingPerformed), itemsPaint, widthItems, mStyle.textAlignment, 1, mStyle.sizeExtraItemHeight, false);
        } else {
            itemsLayout.increaseWidthTo(widthItems);
        }

        String text = null;
        if (getAdapter() != null) {
            int preId = currentItem - 1;
            int nextId = currentItem + 1;
            String preString = "";
            String nextString = "";
            if (mStyle.isCycle) {
                preId = (preId + getAdapter().getItemsCount()) % getAdapter().getItemsCount();
                nextId = nextId % getAdapter().getItemsCount();
                preString = getAdapter().getItem(preId);
                nextString = getAdapter().getItem(nextId);
            } else {
                preString = preId < 0 ? "" : getAdapter().getItem(preId);
                nextString = nextId >= adapter.getItemsCount() ? "" : getAdapter().getItem(nextId);
            }
            text = dealSingleLineStr(preString) + "\n"
                    + dealSingleLineStr(getAdapter().getItem(currentItem)) + "\n"
                    + dealSingleLineStr(nextString);
        }
        if (!isScrollingPerformed && (valueLayout == null || valueLayout.getWidth() > widthItems)) {
            valueLayout = new StaticLayout(text != null ? text : "", valuePaint, widthItems, mStyle.textAlignment, 1, mStyle.sizeExtraItemHeight, false);
        } else if (isScrollingPerformed) {
            valueLayout = new StaticLayout(text != null ? text : "", valuePaint, widthItems, mStyle.textAlignment, 1, mStyle.sizeExtraItemHeight, false);
        } else {
            valueLayout.increaseWidthTo(widthItems);
        }

        if (widthLabel > 0) {
            if (labelLayout == null || labelLayout.getWidth() > widthLabel) {
                labelLayout = new StaticLayout(label, valuePaint, widthLabel, Alignment.ALIGN_NORMAL, 1, mStyle.sizeExtraItemHeight, false);
            } else {
                labelLayout.increaseWidthTo(widthLabel);
            }
        }
    }

    private void calcMaxTextCount() {
        // 计算一个字的宽度
        valuePaint.setColor(mStyle.colorValueText);
        valuePaint.drawableState = getDrawableState();
        float length = valuePaint.measureText("裁");
        mMaxTextCount = (int) (getWidth() / length);
    }

    private String dealSingleLineStr(String string) {
        if (mMaxTextCount <= 0) {
            calcMaxTextCount();
        }

        if (mMaxTextCount > 0 && string.length() > mMaxTextCount) {
            return string.substring(0, mMaxTextCount - 1) + "...";
        }
        return string;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = calculateLayoutWidth(widthSize, widthMode);

        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = getDesiredHeight(itemsLayout);

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (itemsLayout == null) {
            if (itemsWidth == 0) {
                calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);
            } else {
                createLayouts(itemsWidth, labelWidth);
            }
        }

        if (itemsWidth > 0) {
            canvas.save();
            // Skip padding space and hide a part of top and bottom items
            canvas.translate(0, -mStyle.sizeFadeEdge);
            drawItems(canvas);
            drawValue(canvas);
            if (mStyle.isDrawDivider)
                drawDividers(canvas);
            canvas.restore();
        }

        drawCenterRect(canvas);
        if (mStyle.isDrawShadows)
            // 上下阴影
            drawShadows(canvas);
    }

    private void drawDividers(Canvas canvas) {
        if (itemsLayout != null && mStyle.countVisibleItems > 1) {
            int center = getHeight() / 2;
            int itemHeight = getItemHeight();
            boolean isOdd = mStyle.countVisibleItems % 2 == 1;
            int yAbove = 0, yBelow = 0;
            for (int i = 0; i < mStyle.countVisibleItems / 2; i++) {
                if (i == 0) {
                    if (isOdd) {
                        yAbove = center - itemHeight / 2 - mStyle.sizeExtraItemHeight / 2;
                        yBelow = center + itemHeight / 2 - mStyle.sizeExtraItemHeight / 2;
                    } else {
                        yAbove = center;
                        yBelow = -1;
                    }
                    continue;
                } else {
                    yAbove -= itemHeight;
                    yBelow += itemHeight;
                }
                if (yAbove > 0) {
                    canvas.drawLine(0, yAbove, getWidth(), yAbove, dividerPaint);
                }
                if (yBelow > 0) {
                    canvas.drawLine(0, yBelow, getWidth(), yBelow, dividerPaint);
                }
            }
        }
    }

    /**
     * Draws shadows on top and bottom of control
     *
     * @param canvas the canvas for drawing
     */
    private void drawShadows(Canvas canvas) {
        topShadow.setBounds(0, 0, getWidth(), getHeight() / mStyle.countVisibleItems);
        topShadow.draw(canvas);

        bottomShadow.setBounds(0, getHeight() - getHeight() / mStyle.countVisibleItems, getWidth(), getHeight());
        bottomShadow.draw(canvas);
    }

    /**
     * Draws value and label layout
     *
     * @param canvas the canvas for drawing
     */
    private void drawValue(Canvas canvas) {
        valuePaint.setColor(mStyle.colorValueText);
        valuePaint.drawableState = getDrawableState();
        int itemHeight = getItemHeight();

        // draw current value
        if (valueLayout != null) {
            int baseLine = (valueLayout.getLineBaseline(0) + valueLayout.getLineBaseline(1) + 1) / 2;
            int top = baseLine + DisplayUtils.dip2px(getContext(), 2) + (valueLayout.getLineTop(1) - itemHeight) / 2;

            Bitmap bitmap = Bitmap.createBitmap(getWidth(), itemHeight, Bitmap.Config.ARGB_8888);
            Canvas valueCanvas = new Canvas(bitmap);
            valueCanvas.drawColor(mStyle.colorBg);
            int dx = 0;
            if (mStyle.textAlignment == Alignment.ALIGN_NORMAL)
                dx = mStyle.sizeHorizontalPadding;
            else if (mStyle.textAlignment == Alignment.ALIGN_OPPOSITE)
                dx = mStyle.sizeHorizontalPadding;
            valueCanvas.translate(dx, -top + scrollingOffset + mStyle.sizeExtraItemHeight / 2);
            valueLayout.draw(valueCanvas);

            canvas.save();
            canvas.drawBitmap(bitmap, 0, (mStyle.countVisibleItems / 2) * itemHeight, null);
            canvas.restore();
        }

        // draw label
        if (labelLayout != null) {
            int baseLine = (labelLayout.getLineBaseline(0) + labelLayout.getLineBaseline(1) + 1) / 2;
            int top = baseLine + DisplayUtils.dip2px(getContext(), 2) + (labelLayout.getLineTop(1) - itemHeight) / 2;

            canvas.save();
            canvas.translate(itemsLayout.getWidth() + LABEL_OFFSET + mStyle.sizeHorizontalPadding, (mStyle.countVisibleItems / 2) * itemHeight);
            canvas.translate(0, -top + mStyle.sizeExtraItemHeight / 2);
            labelLayout.draw(canvas);
            canvas.restore();

//            Bitmap bitmap = Bitmap.createBitmap(labelLayout.getWidth(), itemHeight, Bitmap.Config.ARGB_8888);
//            Canvas labelCanvas = new Canvas(bitmap);
//            labelCanvas.drawColor(mStyle.colorBg);
//            labelCanvas.translate(0, -top + mStyle.sizeExtraItemHeight / 2);
//            labelLayout.draw(labelCanvas);
//
//            canvas.save();
//            canvas.drawBitmap(bitmap, itemsLayout.getWidth() + LABEL_OFFSET + mStyle.sizeHorizontalPadding, (mStyle.countVisibleItems / 2) * itemHeight, null);
//            canvas.restore();
        }
    }

    /**
     * Draws items
     *
     * @param canvas the canvas for drawing
     */
    private void drawItems(Canvas canvas) {
        canvas.save();

        int baseLine = (itemsLayout.getLineBaseline(0) + itemsLayout.getLineBaseline(1) + 1) / 2;
        int top = baseLine + DisplayUtils.dip2px(getContext(), 2);
        int dx = 0;
        if (mStyle.textAlignment == Alignment.ALIGN_NORMAL)
            dx = mStyle.sizeHorizontalPadding;
        else if (mStyle.textAlignment == Alignment.ALIGN_OPPOSITE)
            dx = mStyle.sizeHorizontalPadding;
        canvas.translate(dx, -top + scrollingOffset + mStyle.sizeExtraItemHeight / 2);

        itemsPaint.setColor(mStyle.colorItemText);
        itemsPaint.drawableState = getDrawableState();
        itemsLayout.draw(canvas);
        canvas.restore();
    }

    /**
     * Draws rect for current value
     *
     * @param canvas the canvas for drawing
     */
    private void drawCenterRect(Canvas canvas) {
        int center = getHeight() / 2;
        int offset = getItemHeight() / 2;
        centerDrawable.setBounds(0, center - offset, getWidth(), center + offset);
        centerDrawable.draw(canvas);
    }

    int orgY = 0;
    int curY = 0;
    int distanceY = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        WheelAdapter adapter = getAdapter();
        if (adapter == null) {
            return true;
        }
        final int action = event.getAction();
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                orgY = curY = (int) event.getY();
                if (isScrollingPerformed) {
                    forceScrolling();
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                curY = (int) event.getY();
                distanceY = curY - orgY;
                orgY = curY;
                if (!(isReachTop() && distanceY > 0) && !(isReachBottom() && distanceY < 0)) {
                    startScrolling();
                    doScroll(distanceY);
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!isReachTop() && !isReachBottom()) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    final int pointerId = event.getPointerId(0);
                    velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                    final float velocityY = velocityTracker.getYVelocity(pointerId);
                    final float velocityX = velocityTracker.getXVelocity(pointerId);
                    if ((Math.abs(velocityY) > mMinimumFlingVelocity) || (Math.abs(velocityX) > mMinimumFlingVelocity)) {
                        onFling((int) velocityY);
                    } else
                        justify();
                    if (mVelocityTracker != null) {
                        // This may have been cleared when we called out to the
                        // application above.
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                } else
                    justify();
                return true;
        }
        return true;
    }

    private boolean isReachTop() {
        if (mStyle.isCycle)
            return false;
        if (getCurrentItem() == 0)
            return true;
        return false;
    }

    private boolean isReachBottom() {
        if (mStyle.isCycle)
            return false;
        if (getCurrentItem() == adapter.getItemsCount() - 1)
            return true;
        return false;
    }

    /**
     * Scrolls the wheel
     *
     * @param delta the scrolling value
     */
    private void doScroll(int delta) {
        scrollingOffset += delta;

        int count = scrollingOffset / getItemHeight();
        int pos = currentItem - count;
        if (mStyle.isCycle && adapter.getItemsCount() > 0) {
            // fix position by rotating
            while (pos < 0) {
                pos += adapter.getItemsCount();
            }
            pos %= adapter.getItemsCount();
        } else if (isScrollingPerformed) {
            //
            if (pos < 0) {
                count = currentItem;
                pos = 0;
            } else if (pos >= adapter.getItemsCount()) {
                count = currentItem - adapter.getItemsCount() + 1;
                pos = adapter.getItemsCount() - 1;
            }
        } else {
            // fix position
            pos = Math.max(pos, 0);
            pos = Math.min(pos, adapter.getItemsCount() - 1);
        }

        int offset = scrollingOffset;
        if (pos != currentItem) {
            setCurrentItem(pos, false);
        } else {
            invalidate();
        }

        // update offset
        scrollingOffset = offset - count * getItemHeight();
        if (scrollingOffset > getHeight()) {
            scrollingOffset = scrollingOffset % getHeight() + getHeight();
        }
    }

    // Messages
    private final int MESSAGE_SCROLL = 0;
    private final int MESSAGE_JUSTIFY = 1;

    /**
     * Set next message to queue. Clears queue before.
     *
     * @param message the message to set
     */
    private void setNextMessage(int message) {
        clearMessages();
        animationHandler.sendEmptyMessage(message);
    }

    /**
     * Clears messages from queue
     */
    private void clearMessages() {
        animationHandler.removeMessages(MESSAGE_SCROLL);
        animationHandler.removeMessages(MESSAGE_JUSTIFY);
    }

    // animation handler
    @SuppressLint("HandlerLeak")
    private Handler animationHandler = new Handler() {
        public void handleMessage(Message msg) {
            scroller.computeScrollOffset();
            int currY = scroller.getCurrY();
            int delta = lastScrollY - currY;
            lastScrollY = currY;
            if (delta != 0) {
                doScroll(delta);
            }
            // scrolling is not finished when it comes to final Y
            // so, finish it manually
            if (Math.abs(currY - scroller.getFinalY()) < MIN_DELTA_FOR_SCROLLING) {
                currY = scroller.getFinalY();
                scroller.forceFinished(true);
            }
            if (!scroller.isFinished()) {
                animationHandler.sendEmptyMessage(msg.what);
            } else if (msg.what == MESSAGE_SCROLL) {
                justify();
            } else {
                finishScrolling();
            }
        }
    };

    /**
     * Justifies wheel
     */
    private void justify() {
        if (adapter == null) {
            return;
        }

        lastScrollY = 0;
        int offset = scrollingOffset;
        int itemHeight = getItemHeight();
        boolean needToIncrease = offset > 0 ? currentItem < adapter.getItemsCount() : currentItem > 0;
        if ((mStyle.isCycle || needToIncrease) && Math.abs((float) offset) > (float) itemHeight / 2) {
            if (offset < 0)
                offset += itemHeight + MIN_DELTA_FOR_SCROLLING;
            else
                offset -= itemHeight + MIN_DELTA_FOR_SCROLLING;
        }
        if (Math.abs(offset) > MIN_DELTA_FOR_SCROLLING) {
            scroller.startScroll(0, 0, 0, offset, mStyle.scrollingDuration);
            setNextMessage(MESSAGE_JUSTIFY);
        } else {
            finishScrolling();
        }
    }

    /**
     * Starts scrolling
     */
    private void startScrolling() {
        if (!isScrollingPerformed) {
            isScrollingPerformed = true;
            notifyScrollingListenersAboutStart();
        }
    }

    /**
     * Finishes scrolling
     */
    void finishScrolling() {
        if (isScrollingPerformed) {
            notifyScrollingListenersAboutEnd();
            isScrollingPerformed = false;
        }
        invalidateLayouts();
        invalidate();
    }

    /**
     * Scroll the wheel
     *
     * @param itemsToScroll items to scroll
     * @param time        scrolling duration
     */
    public void scroll(int itemsToScroll, int time) {
        scroller.forceFinished(true);

        lastScrollY = scrollingOffset;
        int offset = itemsToScroll * getItemHeight();

        scroller.startScroll(0, lastScrollY, 0, offset - lastScrollY, time);
        setNextMessage(MESSAGE_SCROLL);

        startScrolling();
    }

    /**
     * force scroll called when touch down
     */
    private void forceScrolling() {
        scroller.forceFinished(true);
        clearMessages();
    }

    /**
     * @param velocityY y velocity when touch up/cancle
     */
    private void onFling(int velocityY) {
        lastScrollY = currentItem * getItemHeight() - scrollingOffset;
        int maxY = 0x7FFFFFFF;
        int minY = -maxY;
        if (mStyle.isCycle) {
            maxY = 0x7FFFFFFF;
            minY = -maxY;
        } else {
            if (velocityY > 0) {// 下拉
                maxY = lastScrollY - getItemHeight();
                minY = 0;
            } else {// 上拽
                maxY = (adapter.getItemsCount() - 1) * getItemHeight();
                // minY = maxY;
                // minY = -maxY;
            }
        }
        scroller.fling(0, lastScrollY, 0, (int) -velocityY / 2, 0, 0, minY, maxY);
        setNextMessage(MESSAGE_SCROLL);
    }
}