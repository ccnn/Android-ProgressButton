
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * @author ccnn
 */
public class ProgressButton extends View{
    private static final int DEFAULT_MAX = 100;
    private static final int DEFAULT_PROGRESS = 0;
    private static final int DEFAULT_SECONDARY_PROGRESS = 0;

    private LayerDrawable mDrawable;
    private Drawable mBackground;
    private ClipDrawable mProgressDrawable;
    private ClipDrawable mSecondaryDrawble;
    private int mProgress;
    private int mSecondaryProgress;
    private int mMax;
    private String mText;
    private float mTextSize;
    private int mReachedTextColor;
    private int mUnReachedTextColor;

    private Paint mTextPaint;
    private long mUIThreadId;

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
        invalidate();
    }
    
    public void setText(int strResId){
    	setText(getContext().getString(strResId));
    }

    public ProgressButton(Context context) {
        super(context);
        init(null, 0);
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        mUIThreadId = Thread.currentThread().getId();
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ProgressButton, defStyle, 0);

        mDrawable =(LayerDrawable) a.getDrawable(R.styleable.ProgressButton_progressDrawable);
        if(null != mDrawable){
            mBackground = mDrawable.findDrawableByLayerId(android.R.id.background);
            mProgressDrawable = (ClipDrawable)mDrawable.findDrawableByLayerId(android.R.id.progress);
            mSecondaryDrawble = (ClipDrawable)mDrawable.findDrawableByLayerId(android.R.id.secondaryProgress);
        }
        mProgress = a.getInt(R.styleable.ProgressButton_progress, DEFAULT_PROGRESS);
        mSecondaryProgress = a.getInt(R.styleable.ProgressButton_secondaryProgress, DEFAULT_SECONDARY_PROGRESS);
        mMax = a.getInt(R.styleable.ProgressButton_max, DEFAULT_MAX);
        if(a.hasValue(R.styleable.ProgressButton_textSize)){
            mTextSize = a.getDimensionPixelSize(R.styleable.ProgressButton_textSize, 60);
            setMinimumHeight((int)mTextSize);
        }
        if(a.hasValue(R.styleable.ProgressButton_unReachedTextColor)){
            mUnReachedTextColor = a.getColor(R.styleable.ProgressButton_unReachedTextColor, Color.BLACK);
        }
        if(a.hasValue(R.styleable.ProgressButton_reachedTextColor)){
            mReachedTextColor = a.getColor(R.styleable.ProgressButton_reachedTextColor, Color.WHITE);
        }
        if(a.hasValue(R.styleable.ProgressButton_text)){
            mText = a.getString(R.styleable.ProgressButton_text);
        }
        a.recycle();
        
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;


        if(null != mBackground){
            mBackground.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mBackground.draw(canvas);
        }
        if(null != mProgressDrawable){
            mProgressDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mProgressDrawable.setLevel((int) (10000 * getProgressPercent()));
            mProgressDrawable.draw(canvas);
        }
        if(null != mSecondaryDrawble){
            mSecondaryDrawble.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mProgressDrawable.setLevel((int)(10000 * getSecondaryProgressPercent()));
            mSecondaryDrawble.draw(canvas);
        }
        if(!TextUtils.isEmpty(mText)){
            mTextPaint.setTextSize(mTextSize);
            float w = mTextPaint.measureText(mText);
            FontMetrics fontMetrics = mTextPaint.getFontMetrics();
            float h = fontMetrics.descent - fontMetrics.ascent;
            canvas.save();
            mTextPaint.setColor(mReachedTextColor);
            canvas.clipRect(0, 0, getWidth() * getProgressPercent(), getHeight());
            canvas.drawText(mText, (getWidth() - w)/2, (getHeight() + h) / 2 - fontMetrics.descent, mTextPaint);
            canvas.restore();
            canvas.save();
            mTextPaint.setColor(mUnReachedTextColor);
            canvas.clipRect(getWidth() * getProgressPercent(), 0, getWidth(), getHeight());
            canvas.drawText(mText, (getWidth() - w) / 2, (getHeight()+h)/2 - fontMetrics.descent, mTextPaint);
            canvas.restore();
        }
        Log.v("ProgressPercent", getProgressPercent() + "");
    }



    @Override
    protected void dispatchDraw(Canvas canvas) {

        super.dispatchDraw(canvas);
    }

    public int getProgress() {
        return mProgress;
    }

    public synchronized void setProgress(int mProgress) {
        this.mProgress = mProgress;
        invalidate();
    }

    public int getSecondaryProgress() {
        return mSecondaryProgress;
    }

    public synchronized void setSecondaryProgress(int mSecondaryProgress) {
        this.mSecondaryProgress = mSecondaryProgress;
        invalidate();
    }

    public int getMax() {
        return mMax;
    }

    public synchronized void setMax(int mMax) {
        this.mMax = mMax;
        invalidate();
    }

    public synchronized void inCrease(int increase){
        this.mProgress += increase;
        invalidate();
    }
    public float getProgressPercent(){
        if(mMax != 0){
            if(mProgress >= mMax)
                return 1.0f;
            return mProgress/(mMax+0.0f);
        }else
            return mProgress/(mMax + 1.0f);
    }
    public float getSecondaryProgressPercent(){
        if(mMax != 0)
            return mSecondaryProgress/(mMax+0.0f);
        else
            return mSecondaryProgress/(mMax + 1.0f);
    }

    @Override
    public void invalidate() {
        if(mUIThreadId == Thread.currentThread().getId())
            super.invalidate();
        else
            super.postInvalidate();
    }

    public void setTextSize(int textSize){
        this.mTextSize = textSize;
    }
    
}
