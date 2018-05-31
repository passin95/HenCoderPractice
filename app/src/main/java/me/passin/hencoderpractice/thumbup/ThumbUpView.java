package me.passin.hencoderpractice.thumbup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import me.passin.hencoderpractice.R;
import me.passin.hencoderpractice.Utils;

/**
 * <pre>
 * @author : passin
 * Contact me : https://github.com/passin95
 * Date: 2018/5/29 9:57
 * </pre>
 */
public class ThumbUpView extends View implements View.OnClickListener {

    /**
     *  波动圆圈颜色
     */
    private static final int START_COLOR = Color.parseColor("#00e24d3d");
    private static final int END_COLOR = Color.parseColor("#88e24d3d");
    /**
     *  缩放动画的时间
     */
    private static final int SCALE_DURING = 150;
    /**
     * 圆圈扩散动画的时间
     */
    private static final int RADIUS_DURING = 100;
    /**
     * 缩放比例
     */
    private static final float SCALE_MIN = 0.9f;
    private static final float SCALE_MAX = 1f;
    /**
     * 默认值
     */
    private static final float DEFAULT_DRAWABLE_PADDING = Utils.dpToPixel(8);
    private static final String DEFAULT_TEXT_COLOR = "#cccccc";
    private static final int DEFAULT_TEXT_SIZE = 15;

    //这个相对位置是在布局中试出来的
    private static final float APART_BITMAP_TOP = Utils.dpToPixel(8);
    private static final float APART_BITMAP_RIGHT = Utils.dpToPixel(2);


    /**
     * 左边部分
     */
    private Paint mBitmapPaint;
    private Paint mCirclePaint;
    private Path mClipPath;

    private Bitmap mThumbUpBitmap;
    private Bitmap mShiningBitmap;
    private Bitmap mThumbNormalBitmap;

    private float mThumbWidth;
    private float mThumbHeight;
    private float mShiningWidth;
    private float mShiningHeight;

    private TuvPoint mShiningPoint;
    private TuvPoint mThumbPoint;
    private TuvPoint mCirclePoint;

    private float mRadiusMax;
    private float mRadiusMin;
    private float mRadius;


    /**
     * 右边部分
     */
    private Paint mTextPaint;

    private float mTextSize;
    private int mTextColor;
    private float mDrawablePadding;
    private int mEndTextColor;

    private String[] mNumbers;//num[0]是不变的部分，nums[1]原来的部分，nums[2]变化后的部分
    private TuvPoint[] mTextPoints;//表示各部分的坐标

    private float mMaxOffsetY;
    private float mMinOffsetY;

    private float mOldOffsetY;
    private float mNewOffsetY;
    private float mFraction;

    private int mCount;
    private int mUpCount;


    private ThumbUpClickListener mThumbUpClickListener;
    private boolean mIsThumbUp;


    public ThumbUpView(Context context) {
        this(context, null);
    }

    public ThumbUpView(Context context,
            @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThumbUpView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ThumbUpView);
        mDrawablePadding = typedArray.getDimension(R.styleable.ThumbUpView_tuv_drawable_padding, Utils.dpToPixel(DEFAULT_DRAWABLE_PADDING));
        mIsThumbUp = typedArray.getBoolean(R.styleable.ThumbUpView_tuv_isThumbUp, false);
        mTextColor = typedArray.getColor(R.styleable.ThumbUpView_tuv_text_color, Color.parseColor(DEFAULT_TEXT_COLOR));
        mTextSize = typedArray.getDimension(R.styleable.ThumbUpView_tuv_text_size, Utils.spToPixel(DEFAULT_TEXT_SIZE));
        if (mIsThumbUp) {
            mUpCount = typedArray.getInt(R.styleable.ThumbUpView_tuv_count, 0);
            mCount = mUpCount - 1;
        } else {
            mCount = typedArray.getInt(R.styleable.ThumbUpView_tuv_count, 0);
            mUpCount = mCount + 1;
        }

        init();
    }

    private void init() {
        initBitmapInfo();
        initCountInfo();

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(Utils.dpToPixel(2));
        mFraction = 1;
        calculateNumbers();

        mCirclePoint = new TuvPoint();

        setOnClickListener(this);
    }


    private void initBitmapInfo() {
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mThumbUpBitmap = BitmapFactory
                .decodeResource(getResources(), R.drawable.ic_messages_like_selected);
        mThumbNormalBitmap = BitmapFactory
                .decodeResource(getResources(), R.drawable.ic_messages_like_unselected);
        mShiningBitmap = BitmapFactory
                .decodeResource(getResources(), R.drawable.ic_messages_like_selected_shining);

        mThumbWidth = mThumbUpBitmap.getWidth();
        mThumbHeight = mThumbUpBitmap.getHeight();

        mShiningWidth = mShiningBitmap.getWidth();
        mShiningHeight = mShiningBitmap.getHeight();

        mShiningPoint = new TuvPoint();
        mThumbPoint = new TuvPoint();

        mClipPath = new Path();

    }

    private void initCountInfo() {
        mNumbers = new String[3];
        mTextPoints = new TuvPoint[3];
        mTextPoints[0] = new TuvPoint();
        mTextPoints[1] = new TuvPoint();
        mTextPoints[2] = new TuvPoint();

        mMinOffsetY = 0;
        mMaxOffsetY = mTextSize;

        mEndTextColor = Color
                .argb(0, Color.red(mTextColor), Color.green(mTextColor), Color.blue(mTextColor));

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
    }

    /**
     * 计算不变，原来，和改变后各部分的数字
     * 这里是只针对加一和减一去计算的算法，因为直接设置的时候没有动画
     */
    public void calculateNumbers() {

        String oldNum = String.valueOf(mCount);
        String newNum = String.valueOf(mUpCount);

        for (int i = 0; i < oldNum.length(); i++) {
            char oldC = oldNum.charAt(i);
            char newC = newNum.charAt(i);
            if (oldC != newC) {
                mNumbers[0] = i == 0 ? "" : newNum.substring(0, i);
                mNumbers[1] = oldNum.substring(i);
                mNumbers[2] = newNum.substring(i);
                break;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateNumberLocation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制图片
        if (mIsThumbUp) {
            canvas.save();
            canvas.clipPath(mClipPath);
            canvas.drawBitmap(mShiningBitmap, mShiningPoint.x, mShiningPoint.y, mBitmapPaint);
            canvas.restore();
            canvas.drawCircle(mCirclePoint.x, mCirclePoint.y, mRadius, mCirclePaint);
            canvas.drawBitmap(mThumbUpBitmap, mThumbPoint.x, mThumbPoint.y, mBitmapPaint);
        } else {
            canvas.drawBitmap(mThumbNormalBitmap, mThumbPoint.x, mThumbPoint.y, mBitmapPaint);
        }

        //绘制数字
        //绘制不变部分
        mTextPaint.setColor(mTextColor);
        canvas.drawText(String.valueOf(mNumbers[0]), mTextPoints[0].x, mTextPoints[0].y,
                mTextPaint);
        //绘制未点赞部分
        mTextPaint.setColor((Integer) Utils.evaluate(mFraction, mEndTextColor, mTextColor));
        canvas.drawText(String.valueOf(mNumbers[1]), mTextPoints[1].x, mTextPoints[1].y,
                mTextPaint);
        //绘制已点赞部分
        mTextPaint.setColor((Integer) Utils.evaluate(mFraction, mTextColor, mEndTextColor));
        canvas.drawText(String.valueOf(mNumbers[2]), mTextPoints[2].x, mTextPoints[2].y,
                mTextPaint);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mThumbPoint.x = getPaddingLeft();
        mThumbPoint.y = getMeasuredHeight() / 2 - mThumbHeight / 2;
        mShiningPoint.x = getPaddingLeft() + APART_BITMAP_RIGHT;
        mShiningPoint.y = getMeasuredHeight() / 2 - mThumbHeight / 2 - APART_BITMAP_TOP;
        mCirclePoint.x = mThumbPoint.x + mThumbWidth / 2;
        mCirclePoint.y = mThumbPoint.y + mThumbHeight / 2;

        mRadiusMax = Math.max(mCirclePoint.x - getPaddingLeft(), mCirclePoint.y - getPaddingTop());
        mRadiusMin = Utils.dpToPixel(8);//这个值是根据点击效果调整得到的
        mClipPath.addCircle(mCirclePoint.x, mCirclePoint.y, mRadiusMax, Path.Direction.CW);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) (
                getSrcContentWidth() + getPaddingRight() + Math
                        .ceil(mTextPaint.measureText(String.valueOf(mCount))));
        int height = (int) (Math
                .max(mThumbHeight + mShiningHeight + APART_BITMAP_TOP,
                        mTextSize)) + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(resolveSize(width, widthMeasureSpec),
                resolveSize(height, heightMeasureSpec));
    }

    private int getSrcContentWidth() {
        return (int) (Math.max(mThumbWidth, mShiningWidth + APART_BITMAP_RIGHT)
                + getPaddingLeft() + mDrawablePadding);
    }

    private void calculateNumberLocation() {
        String text = String.valueOf(mCount);
        float textWidth = mTextPaint.measureText(text) / text.length();
        float unChangeWidth = textWidth * mNumbers[0].length();

        Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
        float x = getSrcContentWidth();
        float y = getMeasuredHeight() / 2 + mMaxOffsetY - (fontMetrics.bottom - fontMetrics.top) / 2;
        mTextPoints[0].x = x;
        mTextPoints[1].x = x + unChangeWidth;
        mTextPoints[2].x = x + unChangeWidth;

        mTextPoints[0].y = y;
        mTextPoints[1].y = y - mOldOffsetY;
        mTextPoints[2].y = y + mNewOffsetY;
    }

    @Override
    public void onClick(View v) {
        if (mIsThumbUp) {
            startThumbDownAnim();
        } else {
            startThumbUpAnim();
        }
    }

    private void startThumbUpAnim() {
        ObjectAnimator notThumbUpScale = ObjectAnimator
                .ofFloat(this, "notThumbUpScale", SCALE_MAX, SCALE_MIN);
        notThumbUpScale.setDuration(SCALE_DURING);

        ObjectAnimator thumbUpScale = ObjectAnimator
                .ofFloat(this, "thumbUpScale", SCALE_MIN, SCALE_MAX);
        thumbUpScale.setDuration(SCALE_DURING);
        thumbUpScale.setInterpolator(new OvershootInterpolator());

        ObjectAnimator circleScale = ObjectAnimator
                .ofFloat(this, "circleScale", mRadiusMin, mRadiusMax);
        circleScale.setDuration(RADIUS_DURING);

        ObjectAnimator textOffsetY = ObjectAnimator
                .ofFloat(this, "textOffsetY", mMinOffsetY, mMaxOffsetY);
        textOffsetY.setDuration(SCALE_DURING + RADIUS_DURING);

        AnimatorSet thumbUpAnim = new AnimatorSet();
        thumbUpAnim.play(circleScale).with(thumbUpScale).with(textOffsetY).before(notThumbUpScale);
        thumbUpAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsThumbUp = true;
            }
        });
        thumbUpAnim.start();
    }

    private void startThumbDownAnim() {
        ObjectAnimator textOffsetY = ObjectAnimator
                .ofFloat(this, "textOffsetY", mMaxOffsetY, mMinOffsetY);
        textOffsetY.setDuration(SCALE_DURING);

        ObjectAnimator thumbUpScale = ObjectAnimator
                .ofFloat(this, "thumbUpScale", SCALE_MAX, SCALE_MIN);
        thumbUpScale.setDuration(SCALE_DURING);
        thumbUpScale.start();

        AnimatorSet thumbDowmAnim = new AnimatorSet();
        thumbDowmAnim.play(textOffsetY).with(thumbUpScale);
        thumbDowmAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsThumbUp = false;
                setNotThumbUpScale(SCALE_MAX);
            }
        });
        thumbDowmAnim.start();
    }

    private void setNotThumbUpScale(float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        mThumbNormalBitmap = BitmapFactory
                .decodeResource(getResources(), R.drawable.ic_messages_like_unselected);
        mThumbNormalBitmap = Bitmap
                .createBitmap(mThumbNormalBitmap, 0, 0, mThumbNormalBitmap.getWidth(),
                        mThumbNormalBitmap
                                .getHeight(),
                        matrix, true);
        invalidate();
    }

    private void setThumbUpScale(float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        mThumbUpBitmap = BitmapFactory
                .decodeResource(getResources(), R.drawable.ic_messages_like_selected);
        mThumbUpBitmap = Bitmap
                .createBitmap(mThumbUpBitmap, 0, 0, mThumbUpBitmap.getWidth(), mThumbUpBitmap
                                .getHeight(),
                        matrix, true);
        invalidate();
    }

    private void setShiningScale(float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        mShiningBitmap = BitmapFactory
                .decodeResource(getResources(), R.drawable.ic_messages_like_selected_shining);
        mShiningBitmap = Bitmap
                .createBitmap(mShiningBitmap, 0, 0, mShiningBitmap.getWidth(), mShiningBitmap
                                .getHeight(),
                        matrix, true);
        invalidate();
    }

    public void setCircleScale(float radius) {
        mRadius = radius;
        mClipPath = new Path();
        mClipPath.addCircle(mCirclePoint.x, mCirclePoint.y, mRadius, Path.Direction.CW);
        float fraction = (mRadiusMax - radius) / (mRadiusMax - mRadiusMin);
        mCirclePaint.setColor((int) Utils.evaluate(fraction, START_COLOR, END_COLOR));
        invalidate();
    }


    public void setTextOffsetY(float offsetY) {
        mOldOffsetY = offsetY;
        mNewOffsetY =  mMaxOffsetY-offsetY;
        mFraction = (mMaxOffsetY - Math.abs(mOldOffsetY)) / (mMaxOffsetY - mMinOffsetY);
        calculateNumberLocation();
        invalidate();
    }

    public boolean getThumbUpBitmap() {
        return mIsThumbUp;
    }

    public void setThumbUpClickListener(ThumbUpClickListener thumbUpClickListener) {
        this.mThumbUpClickListener = thumbUpClickListener;
    }


    public void setCount(int count) {
        boolean isRequestLayout = String.valueOf(mCount).length() == String.valueOf(count).length()?false:true;
        mCount = count;
        mUpCount = mCount + 1;
        mIsThumbUp = false;
        calculateNumbers();
        if (isRequestLayout) {
            requestLayout();
        }
        setTextOffsetY(0);
    }



    public interface ThumbUpClickListener {

        //点赞回调
        void thumbUpFinish();

        //取消点赞回调
        void thumbDownFinish();

    }


    private static class TuvPoint {

        float x;
        float y;

        TuvPoint() {
        }

        public TuvPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        @Override
        public int hashCode() {
            int result = Float.floatToIntBits(x);
            result = 31 * result + Float.floatToIntBits(y);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Point point = (Point) o;

            if (x != point.x) {
                return false;
            }
            if (y != point.y) {
                return false;
            }

            return true;
        }

        @Override
        public String toString() {
            return "Point(" + x + ", " + y + ")";
        }
    }


}
