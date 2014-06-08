package com.ms.square.android.glassview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

/**
 * GlassView.java
 *
 * @author Manabu-GT on 6/8/14.
 */
public class GlassView extends RelativeLayout {

    private static final String TAG = GlassView.class.getSimpleName();

    private static final int DEFAULT_DOWN_SAMPLING = 3;

    private static final float DEFAULT_BLUR_RADIUS = 5f;
    private static final float MAX_BLUR_RADIUS = 25f;

    private RenderScript mRenderScript;
    private ScriptIntrinsicBlur mBlur;
    private Allocation mIn;
    private Allocation mOut;

    private Bitmap mOrigBitmap;
    private Bitmap mBlurredBitmap;

    private float mBlurRadius = DEFAULT_BLUR_RADIUS;
    private Canvas mBlurCanvas;
    private Rect mDestRect;

    private int mDownSampling;
    private float mScaleFactor;

    private boolean mParentViewDrawn;

    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;

    private ViewTreeObserver.OnScrollChangedListener mScrollChangedListener;

    public GlassView(Context context) {
        super(context);
        init();
    }

    public GlassView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GlassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        initAttributes(attrs);
    }

    private void init() {
        setWillNotDraw(false);
    }

    private void initAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.GlassView);
        mDownSampling = typedArray.getInt(R.styleable.GlassView_downSampling, DEFAULT_DOWN_SAMPLING);
        setBlurRadius(typedArray.getFloat(R.styleable.GlassView_blurRadius, DEFAULT_BLUR_RADIUS));
        typedArray.recycle();
    }

    private void prepBitmaps() {
        cleanUpBitmaps();
        mScaleFactor = 1f / mDownSampling;
        mOrigBitmap = Bitmap.createBitmap((int) (getMeasuredWidth() * mScaleFactor), (int) (getMeasuredHeight() * mScaleFactor),
                Bitmap.Config.ARGB_8888);
        mBlurredBitmap = mOrigBitmap.copy(mOrigBitmap.getConfig(), true);
        mBlurCanvas = new Canvas(mOrigBitmap);
    }

    public void setBlurRadius(float blurRadius) {
        if (0f < blurRadius && blurRadius <= MAX_BLUR_RADIUS) {
            mBlurRadius = blurRadius;
            invalidate();
        }
    }

    public float getBlurRadius() {
        return mBlurRadius;
    }

    private void applyBlur() {
        mIn = Allocation.createFromBitmap(mRenderScript, mOrigBitmap);
        mOut = Allocation.createTyped(mRenderScript, mIn.getType());
        mBlur.setRadius(mBlurRadius);
        mBlur.setInput(mIn);
        mBlur.forEach(mOut);
        mOut.copyTo(mBlurredBitmap);
    }

    private void drawParentToBitmap(View parent) {
        mBlurCanvas.save();
        if (mDownSampling > 1) {
            mBlurCanvas.translate(-getLeft() * mScaleFactor, -getTop() * mScaleFactor);
            mBlurCanvas.scale(mScaleFactor, mScaleFactor);
        } else {
            mBlurCanvas.translate(-getLeft(), -getTop());
        }
        parent.draw(mBlurCanvas);
        mBlurCanvas.restore();
    }

    private void cleanUpBitmaps() {
        if (mOrigBitmap != null) {
            mOrigBitmap.recycle();
            mOrigBitmap = null;
        }
        if (mBlurredBitmap != null) {
            mBlurredBitmap.recycle();
            mBlurredBitmap = null;
        }
    }

    private ViewTreeObserver.OnGlobalLayoutListener getGlobalLayoutListener() {
        if (mGlobalLayoutListener == null) {
            mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                /**
                 * Callback method to be invoked when the global layout state or the visibility of views
                 * within the view tree changes
                 */
                @Override
                public void onGlobalLayout() {
                    //Log.d(TAG, "onGlobalLayout() called");
                    invalidate();
                }
            };
        }
        return mGlobalLayoutListener;
    }

    private ViewTreeObserver.OnScrollChangedListener getScrollChangedListener() {
        if (mScrollChangedListener == null) {
            mScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
                /**
                 * Callback method to be invoked when something in the view tree
                 * has been scrolled.
                 */
                @Override
                public void onScrollChanged() {
                    //Log.d(TAG, "onScrollChanged() called");
                    invalidate();
                }
            };
        }
        return mScrollChangedListener;
    }

    private static boolean isPostHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mRenderScript = RenderScript.create(getContext());
        mBlur = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
        if (isPostHoneycomb() && isHardwareAccelerated()) {
            getViewTreeObserver().addOnGlobalLayoutListener(getGlobalLayoutListener());
            getViewTreeObserver().addOnScrollChangedListener(getScrollChangedListener());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // clean up
        if (mBlur != null) {
            mBlur.destroy();
            mBlur = null;
        }
        if (mRenderScript != null) {
            mRenderScript.destroy();
            mRenderScript = null;
        }
        cleanUpBitmaps();

        if (mGlobalLayoutListener != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
            }
        }
        if (mScrollChangedListener != null) {
            getViewTreeObserver().removeOnScrollChangedListener(mScrollChangedListener);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        prepBitmaps();
        mDestRect = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    public void draw(Canvas canvas)  {
        View parent = (View) getParent();

        // prevent draw() from being recursively called
        if (mParentViewDrawn) {
            return;
        }

        mParentViewDrawn = true;

        drawParentToBitmap(parent);
        applyBlur();

        canvas.drawBitmap(mBlurredBitmap, null, mDestRect, null);
        super.draw(canvas);
        mParentViewDrawn = false;
    }
}