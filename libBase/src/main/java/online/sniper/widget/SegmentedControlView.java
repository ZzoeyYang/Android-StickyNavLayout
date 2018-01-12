/*
 * NOTICE
 *
 * This is the copyright work of The MITRE Corporation, and was produced for
 * the U. S. Government under Contract Number DTFAWA-10-C-00080, and is subject
 * to Federal Aviation Administration Acquisition Management System Clause
 * 3.5-13, Rights In Data-General, Alt. III and Alt. IV (Oct. 1996).  No other
 * use other than that granted to the U. S. Government, or to those acting on
 * behalf of the U. S. Government, under that Clause is authorized without the
 * express written permission of The MITRE Corporation. For further information,
 * please contact The MITRE Corporation, Contracts Office,7515 Colshire Drive,
 * McLean, VA  22102-7539, (703) 983-6000.
 *
 * Approved for Public Release; Distribution Unlimited. Case Number 14-4045
 */

package online.sniper.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import online.sniper.R;
import online.sniper.utils.DisplayUtils;

/**
 * Created by KLONG on 7/2/2014.
 */
public class SegmentedControlView extends RadioGroup implements RadioGroup.OnCheckedChangeListener {
    // Helpers
    private Context mContext;

    // Interaction
    private OnSegmentChangedListener mListener;

    // UI
    private int mSelectedColor = Color.parseColor("#0099CC");
    private int mUnselectedColor = Color.TRANSPARENT;

    private int mSelectedTextColor = Color.WHITE;
    private int mUnselectedTextColor = Color.parseColor("#0099CC");
    private ColorStateList mTextColorStateList;
    private int mTextSize = 12;

    private int mLeftRightPadding = 10;
    private int mTopBottomPadding = 5;
    private int mRadius = 5;
    private int mStrokeWidth = 2;

    private boolean isStretch = true;
    private boolean isEqualWidth = true;
    private int mDefaultSelection = -1;

    // Item organization
    private final LinkedHashMap<String, String> mItemMap = new LinkedHashMap<String, String>();
    private final List<RadioButton> mOptions = new ArrayList<>();

    public SegmentedControlView(Context context) {
        this(context, null);
    }

    public SegmentedControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        // 初始化默认值
        mLeftRightPadding = DisplayUtils.dip2px(context, mLeftRightPadding);
        mTopBottomPadding = DisplayUtils.dip2px(context, mTopBottomPadding);
        mRadius = DisplayUtils.dip2px(context, mRadius);
        mStrokeWidth = DisplayUtils.dip2px(context, mStrokeWidth);
        mTextSize = DisplayUtils.sp2px(context, mTextSize);

        // Here's where overwrite the defaults with the values from the xml attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SegmentedControlView, 0, 0);
        try {
            mSelectedColor = a.getColor(R.styleable.SegmentedControlView_scv_selectedColor, mSelectedColor);
            mUnselectedColor = a.getColor(R.styleable.SegmentedControlView_scv_unselectedColor, mUnselectedColor);

            // Set text selectedColor state list
            mSelectedTextColor = a.getColor(R.styleable.SegmentedControlView_scv_selectedTextColor, mSelectedTextColor);
            mUnselectedTextColor = a.getColor(R.styleable.SegmentedControlView_scv_unselectedTextColor, mSelectedColor);
            mTextColorStateList = new ColorStateList(new int[][]{
                    {-android.R.attr.state_checked}, {android.R.attr.state_checked}},
                    new int[]{mUnselectedTextColor, mSelectedTextColor}
            );
            mTextSize = a.getDimensionPixelSize(R.styleable.SegmentedControlView_scv_textSize, mTextSize);

            mLeftRightPadding = a.getDimensionPixelSize(R.styleable.SegmentedControlView_scv_leftRightPadding, mLeftRightPadding);
            mTopBottomPadding = a.getDimensionPixelSize(R.styleable.SegmentedControlView_scv_topBottomPadding, mTopBottomPadding);
            mRadius = a.getDimensionPixelSize(R.styleable.SegmentedControlView_scv_radius, mRadius);
            mStrokeWidth = a.getDimensionPixelSize(R.styleable.SegmentedControlView_scv_strokeWidth, mStrokeWidth);

            isStretch = a.getBoolean(R.styleable.SegmentedControlView_scv_stretch, isStretch);
            isEqualWidth = a.getBoolean(R.styleable.SegmentedControlView_scv_equalWidth, isEqualWidth);
            mDefaultSelection = a.getInt(R.styleable.SegmentedControlView_scv_defaultSelection, mDefaultSelection);

            // 初始化item
            CharSequence[] itemArray = a.getTextArray(R.styleable.SegmentedControlView_scv_items);
            CharSequence[] valueArray = a.getTextArray(R.styleable.SegmentedControlView_scv_values);
            // TODO: Need to look into better setting up the item for the preview view
            if (isInEditMode()) {
                itemArray = new CharSequence[]{"YES", "NO", "MAYBE", "DON'T KNOW"};
            }
            // Item and value arrays need to be of the same length
            if (itemArray != null && valueArray != null) {
                if (itemArray.length != valueArray.length) {
                    throw new RuntimeException("Item labels and value arrays must be the same size");
                }
            }
            if (itemArray != null) {
                if (valueArray != null) {
                    for (int i = 0; i < itemArray.length; i++) {
                        mItemMap.put(itemArray[i].toString(), valueArray[i].toString());
                    }
                } else {
                    for (CharSequence item : itemArray) {
                        mItemMap.put(item.toString(), item.toString());
                    }
                }
            }
        } finally {
            a.recycle();
        }

        // Setup the view
        updateUi();
    }

    /**
     * Does the setup and re-setup of the view based on the currently set options
     */
    private void updateUi() {
        // Ensure orientation is horizontal
        setOrientation(RadioGroup.HORIZONTAL);
        setOnCheckedChangeListener(null);
        // Remove all views...
        removeAllViews();
        mOptions.clear();

        Iterator<Map.Entry<String, String>> it = mItemMap.entrySet().iterator();
        float textWidth = 0;
        int i = 0;
        while (it.hasNext()) {
            Map.Entry<String, String> item = it.next();
            RadioButton radioButton = new RadioButton(mContext);
            radioButton.setTextColor(mTextColorStateList);

            // 布局大小
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            if (isStretch) {
                lp.weight = 1.0f;
            }
            if (i > 0) {
                lp.setMargins(-mStrokeWidth, 0, 0, 0);
            }
            radioButton.setLayoutParams(lp);
            radioButton.setPadding(mLeftRightPadding, mTopBottomPadding, mLeftRightPadding, mTopBottomPadding);

            // Clear out button drawable (text only)
            radioButton.setButtonDrawable(new StateListDrawable());
            final StateListDrawable background;
            // Create state list for background
            if (i == 0) {
                if (mItemMap.size() == 1) {
                    // single
                    background = getSingleDrawable();
                } else {
                    // Left
                    background = getLeftDrawable();
                }
            } else if (i == (mItemMap.size() - 1)) {
                // Right
                background = getRightDrawable();
            } else {
                // Middle
                background = getMiddleDrawable();
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                radioButton.setBackgroundDrawable(background);
            } else {
                radioButton.setBackground(background);
            }

            radioButton.setMinWidth(mStrokeWidth * 10);
            radioButton.setGravity(Gravity.CENTER);
            radioButton.setTypeface(null, Typeface.BOLD);
            radioButton.setText(item.getKey());
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
            textWidth = Math.max(radioButton.getPaint().measureText(item.getKey()), textWidth);
            mOptions.add(radioButton);
            i++;
        }

        // We do this to make all the segments the same width
        for (RadioButton option : mOptions) {
            if (isEqualWidth) {
                option.setWidth((int) (textWidth + (mLeftRightPadding * 2)));
            }
            addView(option);
        }

        setOnCheckedChangeListener(this);
        int childCount = getChildCount();
        if (childCount > 0) {
            if (mDefaultSelection < 0 || mDefaultSelection >= childCount) {
                mDefaultSelection = 0;
            }
            check(getChildAt(mDefaultSelection).getId());
        } else {
            mDefaultSelection = -1;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (mListener != null) {
            RadioButton radioButton = (RadioButton) group.findViewById(checkedId);
            if (radioButton != null) {
                String item = radioButton.getText().toString();
                mListener.onSegmentChanged(item, mItemMap.get(item));
            }
        }
    }

    private StateListDrawable getSingleDrawable() {
        GradientDrawable selected = getSelectedDrawable(mStrokeWidth);
        selected.setCornerRadii(getRadii(mRadius, mRadius, mRadius, mRadius));

        GradientDrawable unselected = getUnselectedDrawable(mStrokeWidth);
        unselected.setCornerRadii(getRadii(mRadius, mRadius, mRadius, mRadius));

        return getStateListDrawable(selected, unselected);
    }

    public StateListDrawable getLeftDrawable() {
        GradientDrawable selected = getSelectedDrawable(mStrokeWidth);
        selected.setCornerRadii(getRadii(mRadius, 0, 0, mRadius));

        GradientDrawable unselected = getUnselectedDrawable(mStrokeWidth);
        unselected.setCornerRadii(getRadii(mRadius, 0, 0, mRadius));

        return getStateListDrawable(selected, unselected);
    }

    public StateListDrawable getMiddleDrawable() {
        GradientDrawable selected = getSelectedDrawable(mStrokeWidth);

        GradientDrawable unselected = getUnselectedDrawable(mStrokeWidth);
        unselected.setDither(true);
        return getStateListDrawable(selected, unselected);
    }

    public StateListDrawable getRightDrawable() {
        GradientDrawable selected = getSelectedDrawable(mStrokeWidth);
        selected.setCornerRadii(getRadii(0, mRadius, mRadius, 0));

        GradientDrawable unselected = getUnselectedDrawable(mStrokeWidth);
        unselected.setCornerRadii(getRadii(0, mRadius, mRadius, 0));

        return getStateListDrawable(selected, unselected);
    }

    private GradientDrawable getSelectedDrawable(int strokeWidth) {
        GradientDrawable selected = new GradientDrawable();
        selected.setColor(mSelectedColor);
        selected.setStroke(strokeWidth, mSelectedColor);
        return selected;
    }

    private GradientDrawable getUnselectedDrawable(int strokeWidth) {
        GradientDrawable unselected = new GradientDrawable();
        unselected.setColor(mUnselectedColor);
        unselected.setStroke(strokeWidth, mSelectedColor);
        return unselected;
    }

    private StateListDrawable getStateListDrawable(GradientDrawable selected, GradientDrawable unselected) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_checked}, selected);
        stateListDrawable.addState(new int[]{-android.R.attr.state_checked}, unselected);
        return stateListDrawable;
    }

    private static float[] getRadii(int topLeftRadius, int topRightRadius, int bottomRightRadius, int bottomLeftRadius) {
        return new float[]{
                topLeftRadius, topLeftRadius,
                topRightRadius, topRightRadius,
                bottomRightRadius, bottomRightRadius,
                bottomLeftRadius, bottomLeftRadius
        };
    }

    /**
     * Get currently selected segment
     *
     * @return value of currently selected segment
     */
    public String getChecked() {
        int checkedId = getCheckedRadioButtonId();
        if (checkedId == -1) {
            return null;
        }
        RadioButton radioButton = (RadioButton) findViewById(checkedId);
        if (radioButton == null) {
            return null;
        }
        String item = radioButton.getText().toString();
        return mItemMap.get(item);
    }

    /**
     * Sets the items and values for each segments.
     *
     * @param itemArray
     * @param valueArray
     */
    public void setItems(String[] itemArray, String[] valueArray) {
        mItemMap.clear();
        if (itemArray != null && valueArray != null) {
            if (itemArray.length != valueArray.length) {
                throw new RuntimeException("Item labels and value arrays must be the same size");
            }
        }
        if (itemArray != null) {
            if (valueArray != null) {
                for (int i = 0; i < itemArray.length; i++) {
                    mItemMap.put(itemArray[i].toString(), valueArray[i].toString());
                }
            } else {
                for (CharSequence item : itemArray) {
                    mItemMap.put(item.toString(), item.toString());
                }
            }
        }

        updateUi();
    }

    /**
     * Sets the items and values for each segments. Also provides a helper to setting the
     * default selection
     *
     * @param items
     * @param values
     * @param defaultSelection
     */
    public void setItems(String[] items, String[] values, int defaultSelection) {
        if (defaultSelection > (items.length - 1)) {
            throw new RuntimeException("Default selection cannot be greater than the number of items");
        } else {
            mDefaultSelection = defaultSelection;
            setItems(items, values);
        }
    }

    /**
     * Sets the item that is selected by default. Must be greater than -1.
     *
     * @param defaultSelection
     */
    public void setDefaultSelection(int defaultSelection) {
        if (defaultSelection > (mItemMap.size() - 1)) {
            throw new RuntimeException("Default selection cannot be greater than the number of items");
        } else {
            mDefaultSelection = defaultSelection;
            updateUi();
        }
    }

    /**
     * Sets the colors used when drawing the view. The primary color will be used for selected color
     * and unselected text color, while the secondary color will be used for unselected color
     * and selected text color.
     *
     * @param primaryColor
     * @param secondaryColor
     */
    public void setColors(int primaryColor, int secondaryColor) {
        mSelectedColor = primaryColor;
        mUnselectedColor = secondaryColor;

        mSelectedTextColor = secondaryColor;
        mUnselectedTextColor = primaryColor;

        //Set text selectedColor state list
        mTextColorStateList = new ColorStateList(new int[][]{
                {-android.R.attr.state_checked}, {android.R.attr.state_checked}},
                new int[]{mUnselectedTextColor, mSelectedTextColor}
        );

        updateUi();
    }

    /**
     * Sets the colors used when drawing the view
     *
     * @param selectedColor
     * @param unselectedColor
     * @param selectedTextColor
     * @param unselectedTextColor
     */
    public void setColors(int selectedColor, int unselectedColor, int selectedTextColor, int unselectedTextColor) {
        mSelectedColor = selectedColor;
        mUnselectedColor = unselectedColor;

        mSelectedTextColor = selectedTextColor;
        mUnselectedTextColor = unselectedTextColor;
        // Set text selectedColor state list
        mTextColorStateList = new ColorStateList(new int[][]{
                {-android.R.attr.state_checked}, {android.R.attr.state_checked}},
                new int[]{unselectedTextColor, selectedTextColor}
        );

        updateUi();
    }

    /**
     * Used to set the selected value based on the value (not the visible text) provided in the
     * value array provided via xml or code
     *
     * @param value
     */
    public void setByValue(String value) {
        String buttonText = "";
        if (mItemMap.containsValue(value)) {
            for (String entry : mItemMap.keySet()) {
                if (mItemMap.get(entry).equalsIgnoreCase(value)) {
                    buttonText = entry;
                }
            }
        }

        for (RadioButton option : mOptions) {
            if (option.getText().toString().equalsIgnoreCase(buttonText)) {
                check(option.getId());
            }
        }
    }

    /**
     * Sets the mListener that gets called when a selection is changed
     *
     * @param listener
     */
    public void setOnSelectionChangedListener(OnSegmentChangedListener listener) {
        mListener = listener;
    }

    /**
     * Set to true if you want each segment to be equal width
     *
     * @param equalWidth
     */
    public void setEqualWidth(boolean equalWidth) {
        isEqualWidth = equalWidth;
        updateUi();
    }

    /**
     * Set to true if the view should be stretched to fill it's parent view
     *
     * @param stretch
     */
    public void setStretch(boolean stretch) {
        isStretch = stretch;
        updateUi();
    }

    /**
     * Interface for for the selection change event
     */
    public interface OnSegmentChangedListener {
        public void onSegmentChanged(String item, String value);
    }
}
