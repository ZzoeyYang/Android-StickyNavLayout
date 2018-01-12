package online.sniper.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 两端对齐的TextView
 * 
 * @author wangpeihe
 *
 */
public class JustifyTextView extends TextView {

    private int mLineY;
    private int mViewWidth;
    public static final String TWO_CHINESE_BLANK = "  ";

    public JustifyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Layout layout = getLayout();
        // getLayout()在4.4.3出现NullPointerException
        if (layout == null) {
            super.onDraw(canvas);
            return;
        }

        TextPaint paint = getPaint();
        paint.setColor(getCurrentTextColor());
        paint.drawableState = getDrawableState();
        mViewWidth = getMeasuredWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight();
        CharSequence text = getText();
        mLineY = getCompoundPaddingTop();
        mLineY += getTextSize();

        Paint.FontMetrics fm = paint.getFontMetrics();
        int textHeight = (int) (Math.ceil(fm.descent - fm.ascent));
        textHeight = (int) (textHeight * layout.getSpacingMultiplier() + layout.getSpacingAdd());

        for (int i = 0; i < layout.getLineCount(); i++) {
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            float width = StaticLayout.getDesiredWidth(text, lineStart, lineEnd, getPaint());
            CharSequence line = text.subSequence(lineStart, lineEnd);
            if (needScale(line)) {
                drawScaledText(canvas, lineStart, line, width);
            } else {
                canvas.drawText(line, 0, line.length(), getCompoundPaddingLeft(), mLineY, paint);
            }
            mLineY += textHeight;
        }
    }

    /** 绘制缩放文字 */
    private void drawScaledText(Canvas canvas, int lineStart, CharSequence line, float lineWidth) {
        float x = getCompoundPaddingLeft();
        if (isFirstLineOfParagraph(lineStart, line)) {
            canvas.drawText(TWO_CHINESE_BLANK, x, mLineY, getPaint());
            float bw = StaticLayout.getDesiredWidth(TWO_CHINESE_BLANK, getPaint());
            x += bw;
            line = line.subSequence(3, line.length());
        }

        int gapCount = line.length() - 1;
        int i = 0;
        if (line.length() > 2 && line.charAt(0) == 12288 && line.charAt(1) == 12288) {
            CharSequence substring = line.subSequence(0, 2);
            float cw = StaticLayout.getDesiredWidth(substring, getPaint());
            canvas.drawText(substring, 0, substring.length(), x, mLineY, getPaint());
            x += cw;
            i += 2;
        }

        float d = (mViewWidth - lineWidth) / gapCount;
        for (; i < line.length(); i++) {
            String c = String.valueOf(line.charAt(i));
            float cw = StaticLayout.getDesiredWidth(c, getPaint());
            canvas.drawText(c, x, mLineY, getPaint());
            x += cw + d;
        }
    }

    /** 判断是否为段落的首行 */
    private boolean isFirstLineOfParagraph(int lineStart, CharSequence line) {
        return line.length() > 3 && line.charAt(0) == ' ' && line.charAt(1) == ' ';
    }

    /** 判断是否需要缩放 */
    private boolean needScale(CharSequence line) {
        if (line == null || line.length() == 0) {
            return false;
        } else {
            return line.charAt(line.length() - 1) != '\n';
        }
    }

}
