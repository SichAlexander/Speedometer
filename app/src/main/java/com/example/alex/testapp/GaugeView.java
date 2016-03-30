package com.example.alex.testapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class GaugeView extends View {

    protected final String SUPER_STATE              = "superState";
    protected final String NEEDLE_INITIALIZED       = "needleInitialized";
    protected final String NEEDLE_VELOCITY          = "needleVelocity";
    protected final String NEEDLE_ACCELERATION      = "needleAcceleration";
    protected final String NEEDLE_LAST_MOVED        = "needleLastMoved";
    protected final String NEEDLE_CURRENT_VALUE     = "currentValue";
    protected final String NEEDLE_TARGET_VALUE      = "targetValue";

    public static final int SIZE = 300;
    public static final float TOP = 0.0f;
    public static final float LEFT = 0.0f;
    public static final float RIGHT = 1.0f;
    public static final float BOTTOM = 1.0f;
    public static final float CENTER = 0.5f;
    public static final boolean SHOW_OUTER_BORDER = true;
    public static final boolean SHOW_NEEDLE = true;
    public static final boolean SHOW_SCALE = false;
    public static final boolean SHOW_RANGES = true;

    public static final float OUTER_BORDER_WIDTH = 0.06f;

    public static final float NEEDLE_WIDTH = 0.025f;
    public static final float NEEDLE_HEIGHT = 0.32f;
    public static final float SCALE_POSITION = 0.015f;
    public static final float SCALE_START_VALUE = 0.0f;
    public static final float SCALE_END_VALUE = 100.0f;
    public static final float SCALE_START_ANGLE = 60.0f;
    public static final int SCALE_DIVISIONS = 5;
    public static final int SCALE_SUBDIVISIONS = 5;

    public static final float[] RANGE_VALUES = {16.0f, 25.0f, 40.0f, 100.0f};
    public static final int[] RANGE_COLORS = {Color.rgb(0, 0, 0), Color.rgb(0, 0, 0), Color.rgb(0, 0, 0),
            Color.rgb(0, 0, 0)};

    public static final int TEXT_SHADOW_COLOR = Color.argb(100, 0, 0, 0);
    public static final int TEXT_VALUE_COLOR = Color.WHITE;
    public static final int TEXT_UNIT_COLOR = Color.GREEN;
    public static final float TEXT_VALUE_SIZE = 0.12f;
    public static final float TEXT_UNIT_SIZE = 0.12f;
    public static final float TEXT_OFFSET_Y = 20f;


    // *--------------------------------------------------------------------- *//
    // Customizable properties
    // *--------------------------------------------------------------------- *//

    private boolean mShowOuterBorder;

    private boolean mShowScale;
    private boolean mShowRanges;
    private boolean mShowNeedle;
    private boolean mShowText = true;

    private float mOuterBorderWidth;
    private float mNeedleWidth;
    private float mNeedleHeight;


    private float mScalePosition;
    private float mScaleStartValue;
    private float mScaleEndValue;
    private float mScaleStartAngle;
    private float[] mRangeValues;

    private int[] mRangeColors;
    private int mDivisions;
    private int mSubdivisions;

    private RectF mOuterShadowRect;
    private RectF mOuterBorderRect;
    private RectF mOuterRimRect;

    private RectF mFaceRect;
    private RectF mScaleRect;

    private Bitmap mBackground;
    private Paint mBackgroundPaint;

    private Paint mOuterBorderPaint;

    private Paint mFacePaint;

    private Paint[] mRangePaints;

    private Paint mNeedleLeftPaint;
    private Paint mNeedleScrewPaint;

    private Paint mTextValuePaint;
    private Paint mTextUnitPaint;

    private String mTextValue;
    private String mTextUnit;
    private int mTextValueColor;
    private int mTextUnitColor;
    private int mTextShadowColor;
    private float mTextValueSize;
    private float mTextUnitSize;

    private Path mNeedleRightPath;
    private Path mNeedleLeftPath;

    // *--------------------------------------------------------------------- *//

    private float mScaleRotation;
    private float mDivisionValue;
    private float mSubdivisionValue;
    private float mSubdivisionAngle;

    private float mTargetValue;
    private float mCurrentValue;

    private float mNeedleVelocity;
    private float mNeedleAcceleration;
    private long mNeedleLastMoved = -1;
    private boolean mNeedleInitialized;

    public GaugeView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        readAttrs(context, attrs, defStyle);
        init();
    }

    public GaugeView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GaugeView(final Context context) {
        this(context, null, 0);
    }

    private void readAttrs(final Context context, final AttributeSet attrs, final int defStyle) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GaugeView, defStyle, 0);
        mShowOuterBorder = a.getBoolean(R.styleable.GaugeView_showOuterBorder, SHOW_OUTER_BORDER);

        mShowNeedle = a.getBoolean(R.styleable.GaugeView_showNeedle, SHOW_NEEDLE);
        mShowScale = a.getBoolean(R.styleable.GaugeView_showScale, SHOW_SCALE);
        mShowRanges = a.getBoolean(R.styleable.GaugeView_showRanges, SHOW_RANGES);

        mOuterBorderWidth = mShowOuterBorder ? a.getFloat(R.styleable.GaugeView_outerBorderWidth, OUTER_BORDER_WIDTH) : 0.0f;


        mNeedleWidth = a.getFloat(R.styleable.GaugeView_needleWidth, NEEDLE_WIDTH);
        mNeedleHeight = a.getFloat(R.styleable.GaugeView_needleHeight, NEEDLE_HEIGHT);


        mScalePosition = (mShowScale || mShowRanges) ? a.getFloat(R.styleable.GaugeView_scalePosition, SCALE_POSITION) : 0.0f;
        mScaleStartValue = a.getFloat(R.styleable.GaugeView_scaleStartValue, SCALE_START_VALUE);
        mScaleEndValue = a.getFloat(R.styleable.GaugeView_scaleEndValue, SCALE_END_VALUE);
        mScaleStartAngle = a.getFloat(R.styleable.GaugeView_scaleStartAngle, SCALE_START_ANGLE);

        mDivisions = a.getInteger(R.styleable.GaugeView_divisions, SCALE_DIVISIONS);
        mSubdivisions = a.getInteger(R.styleable.GaugeView_subdivisions, SCALE_SUBDIVISIONS);

        if (mShowRanges) {
            mTextShadowColor = a.getColor(R.styleable.GaugeView_textShadowColor, TEXT_SHADOW_COLOR);
            final int rangesId = a.getResourceId(R.styleable.GaugeView_rangeValues, 0);
            final int colorsId = a.getResourceId(R.styleable.GaugeView_rangeColors, 0);
            readRanges(context.getResources(), rangesId, colorsId);
        }

        if (mShowText) {
            final int textValueId = a.getResourceId(R.styleable.GaugeView_textValue, 0);
            final String textValue = a.getString(R.styleable.GaugeView_textValue);
            mTextValue = (0 < textValueId) ? context.getString(textValueId) : (null != textValue) ? textValue : "";

            final int textUnitId = a.getResourceId(R.styleable.GaugeView_textUnit, 0);
            final String textUnit = a.getString(R.styleable.GaugeView_textUnit);
            mTextUnit = (0 < textUnitId) ? context.getString(textUnitId) : (null != textUnit) ? textUnit : "";
            mTextValueColor = a.getColor(R.styleable.GaugeView_textValueColor, TEXT_VALUE_COLOR);
            mTextUnitColor = a.getColor(R.styleable.GaugeView_textUnitColor, TEXT_UNIT_COLOR);
            mTextShadowColor = a.getColor(R.styleable.GaugeView_textShadowColor, TEXT_SHADOW_COLOR);

            mTextValueSize = a.getFloat(R.styleable.GaugeView_textValueSize, TEXT_VALUE_SIZE);
            mTextUnitSize = a.getFloat(R.styleable.GaugeView_textUnitSize, TEXT_UNIT_SIZE);
        }

        a.recycle();
    }

    private void readRanges(final Resources res, final int rangesId, final int colorsId) {
        if (rangesId > 0 && colorsId > 0) {
            final String[] ranges = res.getStringArray(R.array.ranges);
            final String[] colors = res.getStringArray(R.array.rangeColors);
            if (ranges.length != colors.length) {
                throw new IllegalArgumentException(
                        "The ranges and colors arrays must have the same length.");
            }

            final int length = ranges.length;
            mRangeValues = new float[length];
            mRangeColors = new int[length];
            for (int i = 0; i < length; i++) {
                mRangeValues[i] = Float.parseFloat(ranges[i]);
                mRangeColors[i] = Color.parseColor(colors[i]);
            }
        } else {
            mRangeValues = RANGE_VALUES;
            mRangeColors = RANGE_COLORS;
        }
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        initDrawingRects();
        initDrawingTools();
        if (mShowRanges) {
            initScale();
        }
    }

    public void initDrawingRects() {
        // The drawing area is a rectangle of width 1 and height 1,
        // where (0,0) is the top left corner of the canvas.
        // Note that on Canvas X axis points to right, while the Y axis points downwards.
        mOuterShadowRect = new RectF(LEFT, TOP, RIGHT, BOTTOM);

        mOuterBorderRect = new RectF(mOuterShadowRect.left + OUTER_BORDER_WIDTH/2 , mOuterShadowRect.top + OUTER_BORDER_WIDTH/2,
                mOuterShadowRect.right - OUTER_BORDER_WIDTH/2 , mOuterShadowRect.bottom - OUTER_BORDER_WIDTH/2);

        mOuterRimRect = new RectF(mOuterBorderRect.left + mOuterBorderWidth, mOuterBorderRect.top + mOuterBorderWidth,
                mOuterBorderRect.right - mOuterBorderWidth, mOuterBorderRect.bottom - mOuterBorderWidth);

        mFaceRect = new RectF(mOuterRimRect.left , mOuterRimRect.top ,
                mOuterRimRect.right , mOuterRimRect.bottom );

        mScaleRect = new RectF(mFaceRect.left + mScalePosition, mFaceRect.top + mScalePosition, mFaceRect.right - mScalePosition,
                mFaceRect.bottom - mScalePosition);
    }

    private void initDrawingTools() {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setFilterBitmap(true);

        if (mShowOuterBorder) {
            mOuterBorderPaint = getDefaultOuterBorderPaint();
        }

        if (mShowRanges) {
            setDefaultScaleRangePaints();
        }
        if (mShowNeedle) {
            setDefaultNeedlePaths();
            mNeedleLeftPaint = getDefaultNeedleLeftPaint();
            mNeedleScrewPaint = getDefaultNeedleScrewPaint();
        }
//        if (mShowText) {
            mTextValuePaint = getDefaultTextValuePaint();
            mTextUnitPaint = getDefaultTextUnitPaint();
//        }

        mFacePaint = getDefaultFacePaint();

    }



    private Paint getDefaultOuterBorderPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(245, 0, 0, 0));
        return paint;
    }


    public Paint getDefaultFacePaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(255, 255, 255));
        return paint;
    }


    public void setDefaultNeedlePaths() {
        final float x = 0.5f, y = 0.5f;
        mNeedleLeftPath = new Path();
        mNeedleLeftPath.moveTo(x, y);
        mNeedleLeftPath.lineTo(x - mNeedleWidth, y);
        mNeedleLeftPath.lineTo(x, y - mNeedleHeight);
        mNeedleLeftPath.lineTo(x, y);
        mNeedleLeftPath.lineTo(x - mNeedleWidth, y);

        mNeedleRightPath = new Path();
        mNeedleRightPath.moveTo(x, y);
        mNeedleRightPath.lineTo(x + mNeedleWidth, y);
        mNeedleRightPath.lineTo(x, y - mNeedleHeight);
        mNeedleRightPath.lineTo(x, y);
        mNeedleRightPath.lineTo(x + mNeedleWidth, y);
    }

    public Paint getDefaultNeedleLeftPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(0, 0, 0));
        return paint;
    }


    public Paint getDefaultNeedleScrewPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(0, 0, 0));
        return paint;
    }

    public void setDefaultScaleRangePaints() {
        final int length = mRangeValues.length;
        mRangePaints = new Paint[length];
        for (int i = 0; i < length; i++) {
            mRangePaints[i] = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            mRangePaints[i].setColor(mRangeColors[i]);
            mRangePaints[i].setStyle(Paint.Style.STROKE);
            mRangePaints[i].setStrokeWidth(0.005f);
            mRangePaints[i].setTextSize(0.05f);
            mRangePaints[i].setTypeface(Typeface.SANS_SERIF);
            mRangePaints[i].setTextAlign(Align.CENTER);
            mRangePaints[i].setShadowLayer(0.005f, 0.002f, 0.002f, mTextShadowColor);
        }
    }

    public Paint getDefaultTextValuePaint() {
        final Paint paint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(0.005f);
        paint.setTextSize(mTextValueSize);
        paint.setTextAlign(Align.CENTER);
        paint.setTypeface(Typeface.SANS_SERIF);
        paint.setShadowLayer(0.01f, 0.002f, 0.002f, mTextShadowColor);
        return paint;
    }

    public Paint getDefaultTextUnitPaint() {
        final Paint paint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(0.005f);
        paint.setTextSize(mTextUnitSize);
        paint.setTextAlign(Align.CENTER);
        paint.setShadowLayer(0.01f, 0.002f, 0.002f, mTextShadowColor);
        return paint;
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        final Bundle bundle = (Bundle) state;
        final Parcelable superState = bundle.getParcelable(SUPER_STATE);
        super.onRestoreInstanceState(superState);

        mNeedleInitialized = bundle.getBoolean(NEEDLE_INITIALIZED);
        mNeedleVelocity = bundle.getFloat(NEEDLE_VELOCITY);
        mNeedleAcceleration = bundle.getFloat(NEEDLE_ACCELERATION);
        mNeedleLastMoved = bundle.getLong(NEEDLE_LAST_MOVED);
        mCurrentValue = bundle.getFloat(NEEDLE_CURRENT_VALUE);
        mTargetValue = bundle.getFloat(NEEDLE_TARGET_VALUE);
    }

    private void initScale() {
        mScaleRotation = (mScaleStartAngle + 180) % 360;
        mDivisionValue = (mScaleEndValue - mScaleStartValue) / mDivisions;
        Log.d("mDivisionValue:", String.valueOf(mDivisionValue));
        mSubdivisionValue = mDivisionValue / mSubdivisions;
        mSubdivisionAngle = (360 - 2 * mScaleStartAngle) / (mDivisions * mSubdivisions);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        final Bundle state = new Bundle();
        state.putParcelable(SUPER_STATE, superState);
        state.putBoolean(NEEDLE_INITIALIZED, mNeedleInitialized);
        state.putFloat(NEEDLE_VELOCITY, mNeedleVelocity);
        state.putFloat(NEEDLE_ACCELERATION, mNeedleAcceleration);
        state.putLong(NEEDLE_LAST_MOVED, mNeedleLastMoved);
        state.putFloat(NEEDLE_CURRENT_VALUE, mCurrentValue);
        state.putFloat(NEEDLE_TARGET_VALUE, mTargetValue);
        return state;
    }


    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        // Loggable.log.debug(String.format("widthMeasureSpec=%s, heightMeasureSpec=%s",
        // View.MeasureSpec.toString(widthMeasureSpec),
        // View.MeasureSpec.toString(heightMeasureSpec)));

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int chosenWidth = chooseDimension(widthMode, widthSize);
        final int chosenHeight = chooseDimension(heightMode, heightSize);
        setMeasuredDimension(chosenWidth, chosenHeight);
    }

    private int chooseDimension(final int mode, final int size) {
        switch (mode) {
            case View.MeasureSpec.AT_MOST:
            case View.MeasureSpec.EXACTLY:
                return size;
            case View.MeasureSpec.UNSPECIFIED:
            default:
                return getDefaultDimension();
        }
    }

    private int getDefaultDimension() {
        return SIZE;
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        drawGauge();
    }

    private void drawGauge() {
        if (null != mBackground) {
            // Let go of the old background
            mBackground.recycle();
        }
        // Create a new background according to the new width and height
        mBackground = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(mBackground);
        final float scale = Math.min(getWidth(), getHeight());
        canvas.scale(scale, scale);
        canvas.translate((scale == getHeight()) ? ((getWidth() - scale) / 2) / scale : 0
                , (scale == getWidth()) ? ((getHeight() - scale) / 2) / scale : 0);

        drawRim(canvas);
        drawFace(canvas);

        if (mShowRanges) {
            drawScale(canvas);
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        drawBackground(canvas);

        final float scale = Math.min(getWidth(), getHeight());
        canvas.scale(scale, scale);
        canvas.translate((scale == getHeight()) ? ((getWidth() - scale) / 2) / scale : 0
                , (scale == getWidth()) ? ((getHeight() - scale) / 2) / scale : 0);

        if (mShowNeedle) {
            drawNeedle(canvas);
        }

        if (mShowText) {
            drawText(canvas);
        }

        computeCurrentValue();
    }

    private void drawBackground(final Canvas canvas) {
        if (null != mBackground) {
            canvas.drawBitmap(mBackground, 0, 0, mBackgroundPaint);
        }
    }

    private void drawRim(final Canvas canvas) {
        Path path = new Path();
        path.addArc(mOuterBorderRect,130, 280);
        mOuterBorderPaint.setColor(Color.BLACK);
        mOuterBorderPaint.setStyle(Paint.Style.STROKE);
        mOuterBorderPaint.setStrokeWidth(OUTER_BORDER_WIDTH);
        canvas.drawPath(path, mOuterBorderPaint);
    }

    private void drawFace(final Canvas canvas) {
        // Draw the face gradient
        mFacePaint.setColor(Color.WHITE);
//        canvas.drawArc(mFaceRect, 130, 280, false, mFacePaint);

    }

    private void drawText(final Canvas canvas) {
        final String textValue = !TextUtils.isEmpty(mTextValue) ? mTextValue : valueString(mCurrentValue);
        final float textValueWidth = mTextValuePaint.measureText(textValue);
        final float textUnitWidth = !TextUtils.isEmpty(mTextUnit) ? mTextUnitPaint.measureText(mTextUnit) : 0;

        final float startX = CENTER - textUnitWidth / 2;
        final float startY = CENTER + 0.1f;

        drawText(canvas,textValue, CENTER, startY, mTextValuePaint);
//        mTextUnit = "MHP";
//        if (!TextUtils.isEmpty(mTextUnit)) {
            drawText(canvas,mTextUnit, CENTER , startY + 0.12f, mTextValuePaint);
//        }
    }

    private void drawScale(final Canvas canvas) {
        canvas.save();
        // On canvas, North is 0 degrees, East is 90 degrees, South is 180 etc.
        // We start the scale somewhere South-West so we need to first rotate the canvas.
        canvas.rotate(mScaleRotation, 0.5f, 0.5f);
        Log.d("mScaleRotation: ", String.valueOf(mScaleRotation));

        final int totalTicks = mDivisions * mSubdivisions + 1;
        for (int i = 0; i < totalTicks; i++) {
            final float y1 = mScaleRect.top;
            Log.d("mScaleRect.top: ", String.valueOf(mScaleRect.top));

            final float y3 = y1 + 0.090f; // height of subdivision

            final float value = getValueForTick(i);
            final Paint paint = getRangePaint(value);

            float div = mScaleEndValue / (float) mDivisions;
            float mod = value % div;
            if ((Math.abs(mod - 0) < 0.001) || (Math.abs(mod - div) < 0.001)) {
                // Draw a division tick
                paint.setStrokeWidth(0.01f);
                paint.setColor(Color.rgb(87,97,114));
                canvas.drawLine(0.5f, y1 - 0.015f, 0.5f, y3 - 0.03f, paint);
                Log.d("TEXT:",valueString(value));
            }
            canvas.rotate(mSubdivisionAngle, 0.5f, 0.5f);
            Log.d("mSubdivisionAngle: ", String.valueOf(mSubdivisionAngle));
        }
        canvas.restore();
    }


    private void drawText(Canvas canvas, String value, float x, float y, Paint paint)
    {
        //Save original font size
        float originalTextSize = paint.getTextSize();

        // set a magnification factor
        final float magnifier = 100f;

        // Scale the canvas
        canvas.save();
        canvas.scale(1f / magnifier, 1f / magnifier);

        // increase the font size
        paint.setTextSize(originalTextSize * magnifier);

        canvas.drawText(value, x*magnifier, (y * magnifier) + TEXT_OFFSET_Y,paint );

//        canvas.drawTextOnPath(value, textPath, 0.0f, 0.0f, paint);

        // bring everything back to normal
        canvas.restore();
        paint.setTextSize(originalTextSize);
    }


    private String valueString(final float value) {
        return String.format("%d", (int) value);
    }

    private float getValueForTick(final int tick) {
        return tick * (mDivisionValue / mSubdivisions);
    }

    private Paint getRangePaint(final float value) {
        final int length = mRangeValues.length;
        for (int i = 0; i < length - 1; i++) {
            if (value < mRangeValues[i]) return mRangePaints[i];
        }
        if (value <= mRangeValues[length - 1]) return mRangePaints[length - 1];
        throw new IllegalArgumentException("Value " + value + " out of range!");
    }

    private void drawNeedle(final Canvas canvas) {
        if (mNeedleInitialized) {
            final float angle = getAngleForValue(mCurrentValue);
            canvas.save();
            canvas.rotate(angle, 0.5f, 0.5f);
            canvas.drawPath(mNeedleLeftPath, mNeedleLeftPaint);
            canvas.restore();
            canvas.drawCircle(0.5f, 0.5f, 0.025f, mNeedleScrewPaint);

        }
    }

    private float getAngleForValue(final float value) {
        return (mScaleRotation + (value / mSubdivisionValue) * mSubdivisionAngle) % 360;
    }

    private void computeCurrentValue() {
        if (!(Math.abs(mCurrentValue - mTargetValue) > 0.01f)) {
            return;
        }
        if (-1 != mNeedleLastMoved) {
            final float time = (System.currentTimeMillis() - mNeedleLastMoved) / 1000.0f;
            final float direction = Math.signum(mNeedleVelocity);
            if (Math.abs(mNeedleVelocity) < 90.0f) {
                mNeedleAcceleration = 5.0f * (mTargetValue - mCurrentValue);
            } else {
                mNeedleAcceleration = 0.0f;
            }

            mNeedleAcceleration = 5.0f * (mTargetValue - mCurrentValue);
            mCurrentValue += mNeedleVelocity * time;
            mNeedleVelocity += mNeedleAcceleration * time;

            if ((mTargetValue - mCurrentValue) * direction < 0.01f * direction) {
                mCurrentValue = mTargetValue;
                mNeedleVelocity = 0.0f;
                mNeedleAcceleration = 0.0f;
                mNeedleLastMoved = -1L;
            } else {
                mNeedleLastMoved = System.currentTimeMillis();
            }

            invalidate();

        } else {
            mNeedleLastMoved = System.currentTimeMillis();
            computeCurrentValue();
        }
    }

    public void setTargetValue(final float value) {
        if (mShowScale || mShowRanges) {
            if (value < mScaleStartValue) {
                mTargetValue = mScaleStartValue;
            } else if (value > mScaleEndValue) {
                mTargetValue = mScaleEndValue;
            } else {
                mTargetValue = value;
            }
        } else {
            mTargetValue = value;
        }
        mNeedleInitialized = true;
        invalidate();
    }

}


