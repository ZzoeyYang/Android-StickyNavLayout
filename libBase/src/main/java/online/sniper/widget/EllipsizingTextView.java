package online.sniper.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by luozhaocheng on 04/01/2017.
 */
public class EllipsizingTextView extends TextView {

    private static final String ELLIPSIS = "\u2026";
    private static final Pattern DEFAULT_END_PUNCTUATION = Pattern.compile("[\\.,\u2026;\\:\\s]*$", Pattern.DOTALL);

    public interface EllipsizeListener {

        void ellipsizeStateChanged(boolean ellipsized);
    }

    private final List<EllipsizeListener> ellipsizeListeners = new ArrayList<EllipsizeListener>();
    private boolean isEllipsized;
    private boolean isStale;
    private boolean programmaticChange;
    private CharSequence fullText;
    private int maxLines;
    private float lineSpacingMultiplier = 1.0f;
    private float lineAdditionalVerticalPadding = 0.0f;
    /**
     * The end punctuation which will be removed when appending #ELLIPSIS.
     */
    private Pattern endPunctuationPattern;

    public EllipsizingTextView(Context context) {
        this(context, null);
    }

    public EllipsizingTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EllipsizingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.maxLines});
        setMaxLines(a.getInt(0, Integer.MAX_VALUE));
        a.recycle();
        setEndPunctuationPattern(DEFAULT_END_PUNCTUATION);
    }

    public void setEndPunctuationPattern(Pattern pattern) {
        this.endPunctuationPattern = pattern;
    }

    public void addEllipsizeListener(EllipsizeListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        ellipsizeListeners.add(listener);
    }

    public void removeEllipsizeListener(EllipsizeListener listener) {
        ellipsizeListeners.remove(listener);
    }

    public boolean isEllipsized() {
        return isEllipsized;
    }

    @Override
    public void setMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
        this.maxLines = maxLines;
        isStale = true;
    }

    @SuppressLint("Override")
    public int getMaxLines() {
        return maxLines;
    }

    public boolean ellipsizingLastFullyVisibleLine() {
        return maxLines == Integer.MAX_VALUE;
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        this.lineAdditionalVerticalPadding = add;
        this.lineSpacingMultiplier = mult;
        super.setLineSpacing(add, mult);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int before,
                                 int after) {
        super.onTextChanged(text, start, before, after);
        if (!programmaticChange) {
            fullText = text;
            isStale = true;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (ellipsizingLastFullyVisibleLine()) {
            isStale = true;
        }
    }

    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        if (ellipsizingLastFullyVisibleLine()) {
            isStale = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isStale) {
            resetText();
        }
        super.onDraw(canvas);
    }

    private void resetText() {
        CharSequence workingText = fullText;
        boolean ellipsized = false;
        Layout layout = createWorkingLayout(workingText);
        int linesCount = getLinesCount();
        if (layout.getLineCount() > linesCount) {
            // We have more lines of text than we are allowed to display.
            int textCount = layout.getLineEnd(linesCount - 1);
            if (getEllipsize() == TruncateAt.END) {
                workingText = fullText.subSequence(0, textCount);
            } else if (getEllipsize() == TruncateAt.START) {
                int length = fullText.length();
                workingText = fullText.subSequence(length - textCount - 1, length);
            } else {
                int subCount = textCount / 2;
                workingText = fullText.subSequence(0, subCount) + fullText.subSequence(fullText.length() - subCount - 1, fullText.length()).toString();
            }

            while (createWorkingLayout(getResultString(workingText)).getLineCount() > linesCount) {
                TruncateAt textEllipsize = getEllipsize();
                if (textEllipsize == TruncateAt.END) {
                    workingText = workingText.subSequence(0, workingText.length() - 2);
                } else if (textEllipsize == TruncateAt.MIDDLE) {
                    int middleLength = workingText.length() / 2;
                    workingText = workingText.subSequence(0, middleLength - 1) + workingText.subSequence(middleLength + 1, workingText.length()).toString();
                } else {
                    workingText = workingText.subSequence(1, workingText.length() - 1);
                }
            }
            workingText = endPunctuationPattern.matcher(workingText).replaceFirst("");
            workingText = getResultString(workingText);
            ellipsized = true;
        }
        if (!workingText.equals(getText())) {
            programmaticChange = true;
            try {
                setText(workingText);
            } finally {
                programmaticChange = false;
            }
        }
        isStale = false;
        if (ellipsized != isEllipsized) {
            isEllipsized = ellipsized;
            for (EllipsizeListener listener : ellipsizeListeners) {
                listener.ellipsizeStateChanged(ellipsized);
            }
        }
    }

    private CharSequence getResultString(CharSequence workText) {
        if (getEllipsize() == TruncateAt.START) {
            return ELLIPSIS + workText;
        } else if (getEllipsize() == TruncateAt.MIDDLE) {
            int middleLength = workText.length() / 2;
            return workText.subSequence(0, middleLength) + ELLIPSIS + workText.subSequence(middleLength, workText.length());
        } else {
            return workText + ELLIPSIS;
        }
    }

    /**
     * Get how many lines of text we are allowed to display.
     */
    private int getLinesCount() {
        if (ellipsizingLastFullyVisibleLine()) {
            int fullyVisibleLinesCount = getFullyVisibleLinesCount();
            if (fullyVisibleLinesCount == -1) {
                return 1;
            } else {
                return fullyVisibleLinesCount;
            }
        } else {
            return maxLines;
        }
    }

    /**
     * Get how many lines of text we can display so their full height is visible.
     */
    private int getFullyVisibleLinesCount() {
        Layout layout = createWorkingLayout("");
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        int lineHeight = layout.getLineBottom(0);
        return height / lineHeight;
    }

    private Layout createWorkingLayout(CharSequence workingText) {
        return new StaticLayout(workingText, getPaint(),
                getWidth() - getPaddingLeft() - getPaddingRight(),
                Alignment.ALIGN_NORMAL, lineSpacingMultiplier,
                lineAdditionalVerticalPadding, false /* includepad */);
    }
}