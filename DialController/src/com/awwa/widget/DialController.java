package com.awwa.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class DialController extends View implements OnTouchListener {
    @SuppressWarnings("unused")
    private static final String TAG = DialController.class.getSimpleName();
    private final DialController self = this;

    /**
     * Operate
     */
    public enum Operate {
        /** none **/
        NONE,
        /** click upper side **/
        UP,
        /** click down side **/
        DOWN,
        /** click left side **/
        LEFT,
        /** click right side **/
        RIGHT,
        /** rotate forward **/
        FORWARD,
        /** rotate backward **/
        BACKWARD,
        /** click center **/
        ENTER,
    }

    /** Frigidity : Ignore move event on outer ring for detect click operation **/
    private static final int FRIGIDITY = 5;
    /** Sensitivity : for rotate event **/
    private static final double SENSITIVITY = Math.PI / 8;

    /** Context **/
    private Context mContext;
    /** Radian of outer ring **/
    private double mRadianOuterRing = 0.0;
    /** Radian of outer ring(forward) **/
    private double mRadianOuterRingOld = 0.0;
    /** Radian of outer fing(init) **/
    private double mRadianStart = 0.0;
    /** Center X coordinate of ring **/
    private int mCx = 0;
    /** Center Y coordinate of ring **/
    private int mCy = 0;
    /** Radius of outer ring **/
    private int mRadiusOuterRing = 0;
    /** Radius for center of marker **/
    private int mRadiusMarker = 0;
    /** Radius of marker **/
    private int mRadiusMarkerCenter = 0;
    /** Radius of inner ring **/
    private int mRadiusInnerRing = 0;
    /** Background : outer ring **/
    private Drawable mBgOuterRing;
    /** Background : inner ring normal **/
    private Drawable mBgInnerRing;
    /** Paint : marker **/
    // private Paint mPMarker = new Paint();
    private Drawable mBgMarker;
    /** Paint : text **/
    private Paint mPText = new Paint();
    /** Paint : background of outer ring pressed **/
    private Paint mPBgPressed = new Paint();
    /** Inner ring press started **/
    private boolean mInnerRingPressStarted = false;
    /** Outer ring press started **/
    private Operate mKeyCrossPressStarted = Operate.NONE;
    /** Frigidity move count for detect click on outer ring **/
    private int mFrigidityMoveCount = 0;
    /** Operation listener **/
    private OnOperationListener mOnOperationListener = null;
    /** Initial clicked coordinate on outer ring for calculate rotate **/
    private PointF mPointStart;
    /** Text up **/
    private String mTextUp;
    /** Text down **/
    private String mTextDown;
    /** Text left **/
    private String mTextLeft;
    /** Text right **/
    private String mTextRight;
    /** Text enter **/
    private String mTextEnter;
    /** Text color **/
    private int mTextColor;
    /** Text size **/
    private float mTextSize;
    /** enable vibrate **/
    private boolean mEnableVibrate;
    /** Vibrator **/
    private Vibrator mVib;

    public DialController(Context context) {
        super(context);
        init(context, null);
    }

    public DialController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DialController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    /**
     * Initialize view
     */
    private void init(Context context, AttributeSet attrs) {

        mContext = context;
        TypedArray attributesArray = self.getContext().obtainStyledAttributes(
                attrs, R.styleable.DialController);
        mTextUp = attributesArray.getString(R.styleable.DialController_textUp);
        mTextDown = attributesArray
                .getString(R.styleable.DialController_textDown);
        mTextLeft = attributesArray
                .getString(R.styleable.DialController_textLeft);
        mTextRight = attributesArray
                .getString(R.styleable.DialController_textRight);
        mTextEnter = attributesArray
                .getString(R.styleable.DialController_textEnter);
        mTextColor = attributesArray.getColor(
                R.styleable.DialController_foreColor, Color.BLACK);
        mTextSize = attributesArray.getDimension(
                R.styleable.DialController_foreSize, 18f);
        mEnableVibrate = attributesArray.getBoolean(
                R.styleable.DialController_enableVibrate, false);
        setEnableVibrate(mEnableVibrate); // set vibrate

        int resIdBgOuterRing = attributesArray.getResourceId(
                R.styleable.DialController_backgroundOuterRing,
                R.drawable.bg_outer_ring);
        mBgOuterRing = self.getResources().getDrawable(resIdBgOuterRing);

        int resIdBgInnerRing = attributesArray.getResourceId(
                R.styleable.DialController_backgroundInnerRing,
                R.drawable.bg_inner_ring);
        mBgInnerRing = self.getResources().getDrawable(resIdBgInnerRing);

        int resIdBgMarker = attributesArray.getResourceId(
                R.styleable.DialController_backgroundMarker,
                R.drawable.bg_marker);
        mBgMarker = self.getResources().getDrawable(resIdBgMarker);

        mPText.setColor(mTextColor);
        mPText.setTextSize(mTextSize);
        mPText.setAntiAlias(true);
        mPBgPressed.setColor(self.getResources()
                .getColor(R.color.color_pressed));
        mPBgPressed.setAntiAlias(true);
        this.setOnTouchListener(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mCx = w / 2;
        mCy = h / 2;
        mRadiusOuterRing = (w < h ? w / 2 : h / 2);
        mRadiusInnerRing = 4 * mRadiusOuterRing / 9;
        mRadiusMarker = mRadiusOuterRing / 6;
        mRadiusMarkerCenter = mRadiusOuterRing * 3 / 4;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Background on Outer Ring
        drawBackgroundOuterRing(canvas);
        // Marker
        drawMarker(canvas, mRadianOuterRing);
        // Text on Outer Ring
        drawTextOnOuterRing(canvas);
        // BackGround on InnerRing
        drawBackgroundInnerRing(canvas);
        // Text on Inner Ring
        drawTextOnInnerRing(canvas);
    }

    /**
     * Background on Outer Ring
     * 
     * @param c
     */
    private void drawBackgroundOuterRing(Canvas c) {
        // Draw normal state
        Rect dst = new Rect(mCx - mRadiusOuterRing, mCy - mRadiusOuterRing, mCx
                + mRadiusOuterRing, mCy + mRadiusOuterRing);
        mBgOuterRing.setBounds(dst);
        if (mBgOuterRing instanceof GradientDrawable) {
            ((GradientDrawable) mBgOuterRing)
                    .setGradientRadius((float) mRadiusOuterRing * 1.5f);
        }
        mBgOuterRing.draw(c);

        // Draw pressed state
        RectF dstF = new RectF(mCx - mRadiusOuterRing, mCy - mRadiusOuterRing,
                mCx + mRadiusOuterRing, mCy + mRadiusOuterRing);
        if (mKeyCrossPressStarted == Operate.UP)
            c.drawArc(dstF, 225.0f, 90.0f, true, mPBgPressed);
        if (mKeyCrossPressStarted == Operate.DOWN)
            c.drawArc(dstF, 45.0f, 90.0f, true, mPBgPressed);
        if (mKeyCrossPressStarted == Operate.LEFT)
            c.drawArc(dstF, 135.0f, 90.0f, true, mPBgPressed);
        if (mKeyCrossPressStarted == Operate.RIGHT)
            c.drawArc(dstF, 315.0f, 90.0f, true, mPBgPressed);
    }

    /**
     * BackGround on InnerRing
     * 
     * @param c
     */
    private void drawBackgroundInnerRing(Canvas c) {
        Rect dst = new Rect(mCx - mRadiusInnerRing, mCy - mRadiusInnerRing, mCx
                + mRadiusInnerRing, mCy + mRadiusInnerRing);
        mBgInnerRing.setBounds(dst);
        // Draw normal state
        mBgInnerRing.draw(c);
        // Draw pressed state
        if (mInnerRingPressStarted) {
            c.drawCircle(mCx, mCy, mRadiusInnerRing, mPBgPressed);
        }
    }

    /**
     * Marker. Indicate rotation of outer ring.
     * 
     * @param c
     * @param radian
     */
    private void drawMarker(Canvas c, double radian) {

        Double x = mRadiusMarkerCenter * Math.cos(radian);
        Double y = mRadiusMarkerCenter * Math.sin(radian);

        // Draw marker
        Rect dst = new Rect(
                Double.valueOf(mCx + x.floatValue() - mRadiusMarker).intValue(),
                Double.valueOf(mCy + y.floatValue() - mRadiusMarker).intValue(),
                Double.valueOf(mCx + x.floatValue() + mRadiusMarker).intValue(),
                Double.valueOf(mCy + y.floatValue() + mRadiusMarker).intValue());
        mBgMarker.setBounds(dst);
        if (mBgMarker instanceof GradientDrawable)
            ((GradientDrawable) mBgMarker)
                    .setGradientRadius((float) mRadiusMarker * 2.0f);
        mBgMarker.draw(c);
    }

    /**
     * Text on Outer Ring
     * 
     * @param c
     */
    private void drawTextOnOuterRing(Canvas c) {
        // draw + on upper
        PointF p = getCoord4OuterRingText(
                (mRadiusOuterRing + mRadiusInnerRing) / 2, -Math.PI / 2,
                mPText, mTextUp);
        if (mKeyCrossPressStarted == Operate.UP && p != null)
            c.drawText(mTextUp, p.x, p.y, mPText);
        else
            c.drawText(mTextUp, p.x, p.y, mPText);

        // draw - on bottom
        p = getCoord4OuterRingText((mRadiusOuterRing + mRadiusInnerRing) / 2,
                Math.PI / 2, mPText, mTextDown);
        if (mKeyCrossPressStarted == Operate.DOWN && p != null)
            c.drawText(mTextDown, p.x, p.y, mPText);
        else
            c.drawText(mTextDown, p.x, p.y, mPText);

        // draw ++ on right
        p = getCoord4OuterRingText((mRadiusOuterRing + mRadiusInnerRing) / 2,
                0, mPText, mTextRight);
        if (mKeyCrossPressStarted == Operate.RIGHT && p != null)
            c.drawText(mTextRight, p.x, p.y, mPText);
        else
            c.drawText(mTextRight, p.x, p.y, mPText);

        // draw -- on left
        p = getCoord4OuterRingText((mRadiusOuterRing + mRadiusInnerRing) / 2,
                Math.PI, mPText, mTextLeft);
        if (mKeyCrossPressStarted == Operate.LEFT && p != null)
            c.drawText(mTextLeft, p.x, p.y, mPText);
        else
            c.drawText(mTextLeft, p.x, p.y, mPText);
    }

    /**
     * Text on Inner Ring
     * 
     * @param c
     */
    private void drawTextOnInnerRing(Canvas c) {
        // draw text for inner ring
        PointF p = getCoord4InnerRingText(mPText, mTextEnter);
        if (mInnerRingPressStarted && p != null)
            c.drawText(mTextEnter, p.x, p.y, mPText);
        else
            c.drawText(mTextEnter, p.x, p.y, mPText);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        boolean isInOuterRing = isInOuterRing(event.getX(), event.getY());
        boolean isInInnerRing = isInInnerRing(event.getX(), event.getY());
        Operate crossKey = Operate.NONE;
        if (isInOuterRing) {
            double r = calcRadian(new PointF(mCx + mRadiusOuterRing, mCy),
                    new PointF(event.getX(), event.getY()));
            if (r >= -3 * Math.PI / 4 && r < -Math.PI / 4)
                crossKey = Operate.UP;
            if (r >= Math.PI / 4 && r < 3 * Math.PI / 4)
                crossKey = Operate.DOWN;
            if (r >= -Math.PI / 4 && r < Math.PI / 4)
                crossKey = Operate.RIGHT;
            if (r >= 3 * Math.PI / 4 || r < -3 * Math.PI / 4)
                crossKey = Operate.LEFT;
        }

        // Reset initial rotate position when out of outer ring
        if (!isInOuterRing) {
            mRadianStart = mRadianOuterRing; // Save state for next rotate
        }
        // Reset initial rotate position when up
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mRadianStart = mRadianOuterRing; // Save state for next rotate
        }

        // Down event on outer ring
        if (isInOuterRing && event.getAction() == MotionEvent.ACTION_DOWN) {
            mPointStart = new PointF(event.getX(), event.getY());
            mKeyCrossPressStarted = crossKey;
            mFrigidityMoveCount = 0;
        }

        // Calculate rotation when move on outer ring
        if (mPointStart != null && isInOuterRing
                && event.getAction() == MotionEvent.ACTION_MOVE) {
            PointF now = new PointF(event.getX(), event.getY());
            double radianStartFromNow = calcRadian(mPointStart, now);
            mRadianOuterRing = mRadianStart + radianStartFromNow;
            // raise wheel event
            raiseWheelEvent(radianStartFromNow);

            // Reset 4way key press
            if (mFrigidityMoveCount < FRIGIDITY) {
                mFrigidityMoveCount++;
            } else {
                mFrigidityMoveCount = 0;
                mKeyCrossPressStarted = Operate.NONE;
            }
            invalidate();
        }

        // Reset 4way key press when out of outer ring
        if (!isInOuterRing && MotionEvent.ACTION_MOVE == event.getAction()) {
            mFrigidityMoveCount = 0;
            mKeyCrossPressStarted = Operate.NONE;
            invalidate();
        }

        // Click on outer ring
        if (isInOuterRing && crossKey != Operate.NONE
                && MotionEvent.ACTION_UP == event.getAction()) {
            if (mOnOperationListener != null
                    && mKeyCrossPressStarted != Operate.NONE) {
                mOnOperationListener.onOperation(mKeyCrossPressStarted);
                vibrate();
            }
            // reset status
            mKeyCrossPressStarted = Operate.NONE;
            crossKey = Operate.NONE;
            invalidate();
        }

        // Pressed on inner ring
        if (!mInnerRingPressStarted) {
            mInnerRingPressStarted = (isInInnerRing && MotionEvent.ACTION_DOWN == event
                    .getAction());
            invalidate();
        }

        // Clicked on inner ring
        if (isInInnerRing && event.getAction() == MotionEvent.ACTION_UP
                && mInnerRingPressStarted) {
            mInnerRingPressStarted = false;
            invalidate();
            if (mOnOperationListener != null) {
                mOnOperationListener.onOperation(Operate.ENTER);
                vibrate();
            }
        }

        // Reset press when out of inner ring
        if (!isInInnerRing && MotionEvent.ACTION_MOVE == event.getAction()) {
            mInnerRingPressStarted = false;
            invalidate();
        }

        return true;
    }

    /**
     * Check on outer ring
     * 
     * @param x
     * @param y
     * @return
     */
    private boolean isInOuterRing(float x, float y) {
        double distFromCenter = Math.sqrt(Math.pow(x - mCx, 2)
                + Math.pow(y - mCy, 2));
        return (distFromCenter < mRadiusOuterRing && distFromCenter > mRadiusInnerRing);
    }

    /**
     * Check on inner ring
     * 
     * @param x
     * @param y
     * @return
     */
    private boolean isInInnerRing(float x, float y) {
        double distFromCenter = Math.sqrt(Math.pow(x - mCx, 2)
                + Math.pow(y - mCy, 2));
        return (distFromCenter < mRadiusInnerRing);
    }

    /**
     * Calculate rotation
     * 
     * @param base
     * @param now
     * @return
     */
    private double calcRadian(PointF base, PointF now) {

        // Algebra ：A ・ B ≡ Ax * Bx + Ay * By
        double y = (base.x - mCx) * (now.x - mCx) + (base.y - mCy)
                * (now.y - mCy);
        // A × B ≡ Ax * By - Ay * Bx
        double x = (base.x - mCx) * (now.y - mCy) - (base.y - mCy)
                * (now.x - mCx);
        // θ ＝ atan2(A×B，A・B) (Unit is radian)
        return Math.atan2(x, y);
    }

    /**
     * Coordinate for outer ring text
     * 
     * @param radius
     * @param radian
     * @param paint
     * @param text
     * @return
     */
    private PointF getCoord4OuterRingText(int radius, double radian,
            Paint paint, String text) {

        if (text == null)
            return null;

        Double x = radius * Math.cos(radian);
        Double y = radius * Math.sin(radian);

        float textWidth = paint.measureText(text);

        FontMetrics fontMetrics = paint.getFontMetrics();
        float textHeight = (fontMetrics.ascent + fontMetrics.descent);

        PointF ret = new PointF(mCx + x.floatValue() - textWidth / 2, mCy
                + y.floatValue() - textHeight / 2);
        return ret;
    }

    /**
     * Coordinate for inner ring text
     * 
     * @param paint
     * @param text
     * @return
     */
    private PointF getCoord4InnerRingText(Paint paint, String text) {
        if (text == null)
            return null;

        float textWidth = paint.measureText(text);

        FontMetrics fontMetrics = paint.getFontMetrics();
        float textHeight = (fontMetrics.ascent + fontMetrics.descent);

        PointF ret = new PointF(mCx - textWidth / 2, mCy - textHeight / 2);
        return ret;
    }

    /**
     * Raise wheel event
     * 
     * @param radianDraw
     */
    private void raiseWheelEvent(double radianCalced) {

        if (mOnOperationListener == null)
            return;

        double delta = mRadianOuterRing - mRadianOuterRingOld;
        if (((delta > SENSITIVITY) && (delta < 2 * SENSITIVITY))
                || (delta < -2 * SENSITIVITY)) {
            // Forward rotation
            mOnOperationListener.onOperation(Operate.FORWARD);
            vibrate();
            mRadianOuterRingOld = mRadianOuterRing;
        }

        if (((delta < -SENSITIVITY) && (delta > -2 * SENSITIVITY))
                || (delta > 2 * SENSITIVITY)) {
            // Backward rotation
            mOnOperationListener.onOperation(Operate.BACKWARD);
            vibrate();
            mRadianOuterRingOld = mRadianOuterRing;
        }
    }

    /**
     * Operation event listener
     */
    public interface OnOperationListener {
        public void onOperation(Operate operate);
    }

    /**
     * Register operation event listener
     * 
     * @param l
     */
    public void setOnOperationListener(OnOperationListener l) {
        mOnOperationListener = l;
    }

    /**
     * Raise vibrate. Need permission 'android.permission.VIBRATE'
     */
    private void vibrate() {
        if (mVib != null)
            mVib.vibrate(50);
    }

    /**
     * Change text 'Up'
     * 
     * @param text
     */
    public void setTextUp(String text) {
        mTextUp = text;
        invalidate();
    }

    /**
     * Change text 'Down'
     * 
     * @param text
     */
    public void setTextDown(String text) {
        mTextDown = text;
        invalidate();
    }

    /**
     * Change text 'Left'
     * 
     * @param text
     */
    public void setTextLeft(String text) {
        mTextLeft = text;
        invalidate();
    }

    /**
     * Change text 'Right'
     * 
     * @param text
     */
    public void setTextRight(String text) {
        mTextRight = text;
        invalidate();
    }

    /**
     * Change text 'Enter'
     * 
     * @param text
     */
    public void setTextEnter(String text) {
        mTextEnter = text;
        invalidate();
    }

    /**
     * Enable vibrator
     * 
     * @param enableVibrate
     */
    public void setEnableVibrate(boolean enableVibrate) {
        try {
            if (enableVibrate)
                mVib = (Vibrator) mContext
                        .getSystemService(Context.VIBRATOR_SERVICE);
            else
                mVib = null;
            mEnableVibrate = enableVibrate;
        } catch (UnsupportedOperationException ex) {
            Log.w(TAG, ex.getMessage(), ex);
        }
    }

    /**
     * Get enable vibrator
     * 
     * @return
     */
    public boolean getEnableVibrate() {
        return mEnableVibrate;
    }

    /**
     * Change text size
     * 
     * @param value
     */
    public void setTextSize(float value) {
        mTextSize = value;
        mPText.setTextSize(mTextSize);
        invalidate();
    }

    /**
     * Change text color
     * 
     * @param value
     */
    public void setTextColor(int value) {
        mTextColor = value;
        mPText.setColor(mTextColor);
        invalidate();
    }

    /**
     * Change background drawable of outer ring
     * 
     * @param value
     */
    public void setBgOuterRing(Drawable value) {
        mBgOuterRing = value;
        invalidate();
    }

    /**
     * Change background drawable of inner ring
     * 
     * @param value
     */
    public void setBgInnerRing(Drawable value) {
        mBgInnerRing = value;
        invalidate();
    }

    /**
     * Change background drawable of marker
     * 
     * @param value
     */
    public void setBgMarker(Drawable value) {
        mBgMarker = value;
        invalidate();
    }
}
