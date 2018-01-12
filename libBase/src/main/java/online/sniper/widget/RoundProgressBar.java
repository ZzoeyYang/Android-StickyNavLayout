package online.sniper.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

import online.sniper.R;


/**
 * 圆环进度条
 */
public class RoundProgressBar extends View {

    private static final int ANIMATION_DURATION = 500; // 动画持续时间为0.5秒
    private static final int FRAME_DURATION = 1000 / 60; // 一帧动画的持续时间

    private final RectF mCircle = new RectF();
    private final Paint mPaint = new Paint();

    private int mBackgroundColor = 0xFFFF902A;
    private int mProgressColor = 0xFFFFFFFF;

    private double mMax = 100;
    private double mProgress = 0;

    private boolean mAnimating = false;
    private long mStartAnimationTime = 0;
    private Interpolator mInterpolator = new AccelerateInterpolator();
    private Handler mHandler = new Handler();
    private Runnable mPerformAnimation = new Runnable() {

        @Override
        public void run() {
            if (mAnimating) {
                float t = (SystemClock.uptimeMillis() - mStartAnimationTime) / (float) ANIMATION_DURATION;
                if (t > 1.0) {
                    mAnimating = false;
                }
            }

            invalidate();

            if (mAnimating) {
                mHandler.postDelayed(mPerformAnimation, FRAME_DURATION);
            }
        }
    };

    public RoundProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBar);
        try {
            int count = a.getIndexCount();
            for (int i = 0; i < count; i++) {
                int attr = a.getIndex(i);
                if (attr == R.styleable.RoundProgressBar_backgroundColor) {
                    mBackgroundColor = a.getColor(attr, mBackgroundColor);
                } if (attr == R.styleable.RoundProgressBar_progressColor) {
                    mProgressColor = a.getColor(attr, mProgressColor);
                }
            }
        } finally {
            a.recycle();
        }

        mPaint.setStyle(Style.FILL); // 描边
        mPaint.setStrokeCap(Cap.ROUND); // 画笔为圆笔
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    /**
     * 设置最大值
     */
    public void setMax(double max) {
        if (max <= 0) {
            throw new RuntimeException("max must be > 0");
        }

        mMax = max;
        if (mProgress > max) {
            mProgress = max;
        }

        invalidate();
    }

    /**
     * 获取最大值
     */
    public double getMax() {
        return mMax;
    }

    /**
     * 设置当前进度，默认启动动画
     *
     * @param progress 进度
     */
    public void setProgress(double progress) {
        setProgress(progress, true);
    }

    /***
     * 设置当前进度
     *
     * @param progress
     *            进度
     * @param animation
     *            是否启动动画
     */
    public void setProgress(double progress, boolean animation) {
        mProgress = Math.max(0, Math.min(progress, mMax));
        mAnimating = animation;
        if (mAnimating) {
            mStartAnimationTime = SystemClock.uptimeMillis();
        } else {
            mStartAnimationTime = 0;
        }

        mHandler.removeCallbacks(mPerformAnimation);
        mHandler.postDelayed(mPerformAnimation, FRAME_DURATION);
    }

    /**
     * 获取当前进度
     */
    public double getProgress() {
        return mProgress;
    }

    /**
     * 判断是否在绘制动画
     */
    public boolean isAnimating() {
        return mAnimating;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        double p;
        if (mAnimating) {
            float t = (SystemClock.uptimeMillis() - mStartAnimationTime) / (float) ANIMATION_DURATION;
            if (t > 1.0) {
                mAnimating = false;
                p = mProgress;
            } else {
                p = mProgress * mInterpolator.getInterpolation(t);
            }
        } else {
            p = mProgress;
        }
        float degree = (float) (360 * p / mMax);
        drawCircle(canvas, degree);
    }

    /**
     * 绘制圆环
     */
    private void drawCircle(Canvas canvas, float degree) {
        final float width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        final float height = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        final float paddingLeft = getPaddingLeft();
        final float paddingTop = getPaddingTop();
        final float size = Math.min(width, height);
        final float radius = size / 2;

        canvas.save();
        canvas.translate(paddingLeft, paddingTop);
        canvas.translate(width / 2, height / 2);

        mPaint.setStyle(Style.FILL);
        mPaint.setColor(mBackgroundColor);
        mCircle.set(-radius, -radius, radius, radius);
        canvas.drawOval(mCircle, mPaint);

        mPaint.setStyle(Style.FILL);
        mPaint.setColor(mProgressColor);
        mCircle.set(-radius, -radius, radius, radius);
        canvas.drawArc(mCircle, -90, degree, true, mPaint);

        canvas.restore();
    }
}
