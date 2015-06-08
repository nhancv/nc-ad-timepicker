package cvnhan.android.calendarsample.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

import cvnhan.android.calendarsample.R;
import cvnhan.android.calendarsample.interactivechart.OverScrollerCompat;
import cvnhan.android.calendarsample.interactivechart.ScaleGestureDetectorCompat;
import cvnhan.android.calendarsample.interactivechart.Zoomer;

/**
 * Created by cvnhan on 04-Jun-15.
 */

public class Calendar extends View {
    private static final String TAG = "Calendar";

    /**
     * Initial fling velocity for pan operations, in screen widths (or heights) per second.
     *
     * @see #panLeft()
     * @see #panRight()
     * @see #panUp()
     * @see #panDown()
     */
    private static final float PAN_VELOCITY_FACTOR = 2f;

    /**
     * The scaling factor for a single zoom 'step'.
     *
     * @see #zoomIn()
     * @see #zoomOut()
     */
    private static final float ZOOM_AMOUNT = 0.25f;

    // Viewport extremes. See mCurrentViewport for a discussion of the viewport.
    private static final float AXIS_X_MIN = -1f;
    private static final float AXIS_X_MAX = 1f;
    private static final float AXIS_Y_MIN = -1f;
    private static final float AXIS_Y_MAX = 1f;

    /**
     * The current viewport. This rectangle represents the currently visible chart domain
     * and range. The currently visible chart X values are from this rectangle's left to its right.
     * The currently visible chart Y values are from this rectangle's top to its bottom.
     * <p/>
     * Note that this rectangle's top is actually the smaller Y value, and its bottom is the larger
     * Y value. Since the chart is drawn onscreen in such a way that chart Y values increase
     * towards the top of the screen (decreasing pixel Y positions), this rectangle's "top" is drawn
     * above this rectangle's "bottom" value.
     *
     * @see #mContentRect
     */
    private RectF mCurrentViewport = new RectF(AXIS_X_MIN, AXIS_Y_MIN, AXIS_X_MAX, AXIS_Y_MAX);

    /**
     * The current destination rectangle (in pixel coordinates) into which the chart data should
     * be drawn. Chart labels are drawn outside this area.
     *
     * @see #mCurrentViewport
     */
    private Rect mContentRect = new Rect();

    //Define custom
    private static float cellMaxWidth=90;
    private static float cellMaxHeight=80;

    private static int numColum;
    private static int numRow;
    private static boolean hasHeaderRow=true;
    private static boolean hasHeaderColum=true;

    private static double minDelta = 1, maxDelta = 1;
    private float density = getResources().getDisplayMetrics().density;
    public boolean isScale=false;

    //Array data
    private final AxisStops xStopsBuffer = new AxisStops();
    private final AxisStops yStopsBuffer = new AxisStops();

    private float[] axisXPositionsBuffer = new float[]{};
    private float[] axisYPositionsBuffer = new float[]{};
    private float[] axisXLinesBuffer = new float[]{};
    private float[] axisYLinesBuffer = new float[]{};
    private String[] headerRowData = new String[]{};
    private String[] headerColData = new String[]{};

    private Point mSurfaceSizeBuffer = new Point();

    //Define Paint
    //HeaderRow
    private float labelHeaderRowTextSize;
    private int labelHeaderRowSeparation;
    private int labelHeaderRowTextColor;
    private int mMaxLabelHeaderRowWidth;
    Paint headerRowPaint;
    
    //HeaderCol
    private float labelHeaderColTextSize;
    private int labelHeaderColSeparation;
    private int labelHeaderColTextColor;
    private int maxLabelHeaderColWidth;
    Paint headerColPaint;

    //Data
    private float mDataThickness;
    private int mDataColor;
    private Paint mDataPaint;

    //Grid
    private float gridThickness;
    private int gridColor;
    private Paint gridPaint;


    private float mLabelTextSize;
    private int mLabelSeparation;
    private int mLabelTextColor;
    private Paint mLabelTextPaint;
    private int mMaxLabelWidth;
    private int mLabelHeight;
    private float mAxisThickness;
    private int mAxisColor;
    private Paint mAxisPaint;
    private Paint mObjPaint;

    public int cellHeightObj = 80;

    // State objects and values related to gesture tracking.
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetectorCompat mGestureDetector;
    private OverScroller mScroller;
    private Zoomer mZoomer;
    private PointF mZoomFocalPoint = new PointF();
    private RectF mScrollerStartViewport = new RectF(); // Used only for zooms and flings.

    // Edge effect / overscroll tracking objects.
    private EdgeEffectCompat mEdgeEffectTop;
    private EdgeEffectCompat mEdgeEffectBottom;
    private EdgeEffectCompat mEdgeEffectLeft;
    private EdgeEffectCompat mEdgeEffectRight;
    private boolean mEdgeEffectTopActive;
    private boolean mEdgeEffectBottomActive;
    private boolean mEdgeEffectLeftActive;
    private boolean mEdgeEffectRightActive;

    private Canvas canvas;


    public Calendar(Context context) {
        this(context, null, 0);
    }

    public Calendar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Calendar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.Calendar, defStyle, defStyle);

        try {
            mLabelTextColor = a.getColor(
                    R.styleable.Calendar_cal_labelTextColor, mLabelTextColor);
            mLabelTextSize = a.getDimension(
                    R.styleable.Calendar_cal_labelTextSize, mLabelTextSize);
            mLabelSeparation = a.getDimensionPixelSize(
                    R.styleable.Calendar_cal_labelSeparation, mLabelSeparation);

            gridThickness = a.getDimension(
                    R.styleable.Calendar_cal_gridThickness, gridThickness);
            gridColor = a.getColor(
                    R.styleable.Calendar_cal_gridColor, gridColor);

            mAxisThickness = a.getDimension(
                    R.styleable.Calendar_cal_axisThickness, mAxisThickness);
            mAxisColor = a.getColor(
                    R.styleable.Calendar_cal_axisColor, mAxisColor);

            mDataThickness = a.getDimension(
                    R.styleable.Calendar_cal_dataThickness, mDataThickness);
            mDataColor = a.getColor(
                    R.styleable.Calendar_cal_dataColor, mDataColor);
        } finally {
            a.recycle();
        }

        initPaints();

        // Sets up interactions
        mScaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureListener);
        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);

        mScroller = new OverScroller(context);
        mZoomer = new Zoomer(context);

        // Sets up edge effects
        mEdgeEffectLeft = new EdgeEffectCompat(context);
        mEdgeEffectTop = new EdgeEffectCompat(context);
        mEdgeEffectRight = new EdgeEffectCompat(context);
        mEdgeEffectBottom = new EdgeEffectCompat(context);
    }

    /**
     * (Re)initializes {@link Paint} objects based on current attribute values.
     */
    private void initPaints() {
        mLabelTextPaint = new Paint();
        mLabelTextPaint.setAntiAlias(true);
        mLabelTextPaint.setTextSize(mLabelTextSize);
        mLabelTextPaint.setColor(mLabelTextColor);
        mLabelHeight = (int) Math.abs(mLabelTextPaint.getFontMetrics().top);
        mMaxLabelWidth = (int) mLabelTextPaint.measureText("00:00");

        gridPaint = new Paint();
        gridPaint.setStrokeWidth(gridThickness);
        gridPaint.setColor(gridColor);
        gridPaint.setStyle(Paint.Style.STROKE);

        mAxisPaint = new Paint();
        mAxisPaint.setStrokeWidth(mAxisThickness);
        mAxisPaint.setColor(mAxisColor);
        mAxisPaint.setStyle(Paint.Style.STROKE);

        mDataPaint = new Paint();
        mDataPaint.setStrokeWidth(mDataThickness);
        mDataPaint.setColor(mDataColor);
        mDataPaint.setStyle(Paint.Style.STROKE);
        mDataPaint.setAntiAlias(true);

        mObjPaint = new Paint();
        mObjPaint.setStrokeWidth(mAxisThickness);
        mObjPaint.setColor(Color.BLUE);
        mObjPaint.setStyle(Paint.Style.FILL);
        mObjPaint.setAlpha(50);


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mContentRect.set(
                getPaddingLeft() + mMaxLabelWidth + mLabelSeparation,
                getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom() - mLabelHeight - mLabelSeparation);
        initAxisStops(1,50);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minChartSize = getResources().getDimensionPixelSize(R.dimen.min_chart_size);
        setMeasuredDimension(
                Math.max(getSuggestedMinimumWidth(),
                        resolveSize(minChartSize + getPaddingLeft() + mMaxLabelWidth
                                        + mLabelSeparation + getPaddingRight(),
                                widthMeasureSpec)),
                Math.max(getSuggestedMinimumHeight(),
                        resolveSize(minChartSize + getPaddingTop() + mLabelHeight
                                        + mLabelSeparation + getPaddingBottom(),
                                heightMeasureSpec)));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and objects related to drawing
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        // Draws axes and text labels
        drawAxes(canvas);

        // Clips the next few drawing operations to the content area
        int clipRestoreCount = canvas.save();
        canvas.clipRect(mContentRect);
        drawObject(1);
        drawEdgeEffectsUnclipped(canvas);
        // Removes clipping rectangle
        canvas.restoreToCount(clipRestoreCount);
//        canvas.drawRect(mContentRect, mAxisPaint);
    }


    public void drawObject(int indexBlock) {
        if (indexBlock == -1) return;
        float y = yStopsBuffer.stops[yStopsBuffer.numStops - indexBlock - 1];
        float Y = getDrawY(y);
//        Log.e(TAG, "Block=" + indexBlock + " y=" + y + " Y=" + Y + " mContentRect.top=" + mContentRect.top + " mContentRect.bottom = " + mContentRect.bottom);
        canvas.drawRect(mContentRect.left, Y, mContentRect.right, Y + getBlockHeight(), mObjPaint);

    }

    //phai cong them mot khoang delta khi scale
    public float gety(float Y) {
        float re = (AXIS_Y_MAX-mCurrentViewport.bottom) - (mCurrentViewport.top-AXIS_Y_MIN) + mCurrentViewport.top + (Y - mContentRect.top) / mContentRect.height() * mCurrentViewport.height();
//        Log.e(TAG, "gety=" + re + " mCurrentViewport.top=" + mCurrentViewport.top + " mCurrentViewport.bottom=" + mCurrentViewport.bottom);
        return re;
    }

    public int getIndexinYStopsArr(float y) {
        for (int i = 0; i < yStopsBuffer.numStops - 1; i++) {
            if (y >= yStopsBuffer.stops[i] && y <= yStopsBuffer.stops[i + 1]) {
                return i - 1;
            }
        }
        return -1;
    }

    public float getBlockHeight() {
        float h = (float) (minDelta * ((AXIS_Y_MAX - AXIS_Y_MIN) / (mCurrentViewport.bottom - mCurrentViewport.top)) * mContentRect.height());
//        Log.e(TAG, h + "");
        return h;
    }

    public void initAxisStops(int col, int row) {
        maxDelta = (cellHeightObj * density) / mContentRect.height();
        computeAxisStops(
                mCurrentViewport.left,
                mCurrentViewport.right,
                col,
                xStopsBuffer);
        computeAxisStops(
                mCurrentViewport.top,
                mCurrentViewport.bottom,
                row,
                yStopsBuffer);

    }


    /**
     * Draws the chart axes and labels onto the canvas.
     */
    private void drawAxes(Canvas canvas) {
        int i;
        if (axisXPositionsBuffer.length < xStopsBuffer.numStops) {
            axisXPositionsBuffer = new float[xStopsBuffer.numStops];
        }
        if (axisYPositionsBuffer.length < yStopsBuffer.numStops) {
            axisYPositionsBuffer = new float[yStopsBuffer.numStops];
        }
        if (axisXLinesBuffer.length < xStopsBuffer.numStops * 4) {
            axisXLinesBuffer = new float[xStopsBuffer.numStops * 4];
        }
        if (axisYLinesBuffer.length < yStopsBuffer.numStops * 4) {
            axisYLinesBuffer = new float[yStopsBuffer.numStops * 4];
        }

        // Compute positions
        for (i = 0; i < xStopsBuffer.numStops; i++) {
            axisXPositionsBuffer[i] = getDrawX(xStopsBuffer.stops[i]);
        }
        for (i = 0; i < yStopsBuffer.numStops; i++) {
            axisYPositionsBuffer[i] = getDrawY(yStopsBuffer.stops[i]);
        }

        // Draws grid lines using drawLines (faster than individual drawLine calls)
        for (i = 0; i < xStopsBuffer.numStops; i++) {
            axisXLinesBuffer[i * 4 + 0] = (float) Math.floor(axisXPositionsBuffer[i]);
            axisXLinesBuffer[i * 4 + 1] = mContentRect.top;
            axisXLinesBuffer[i * 4 + 2] = (float) Math.floor(axisXPositionsBuffer[i]);
            axisXLinesBuffer[i * 4 + 3] = mContentRect.bottom;
        }
        canvas.drawLines(axisXLinesBuffer, 0, xStopsBuffer.numStops * 4, gridPaint);

        for (i = 0; i < yStopsBuffer.numStops; i++) {
            axisYLinesBuffer[i * 4 + 0] = mContentRect.left;
            axisYLinesBuffer[i * 4 + 1] = (float) Math.floor(axisYPositionsBuffer[i]);
            axisYLinesBuffer[i * 4 + 2] = mContentRect.right;
            axisYLinesBuffer[i * 4 + 3] = (float) Math.floor(axisYPositionsBuffer[i]);
        }
//        canvas.drawLines(axisYLinesBuffer, 0, yStopsBuffer.numStops * 4, gridPaint);

        // Draws X labels
        mLabelTextPaint.setTextAlign(Paint.Align.CENTER);
        for (i = 0; i < xStopsBuffer.numStops; i++) {
            canvas.drawText(
                    AxisStops.getHHMM(xStopsBuffer.minutes[i]),
                    axisXPositionsBuffer[i],
                    mContentRect.top + mLabelHeight + mLabelSeparation,
                    mLabelTextPaint);
        }

        // Draws Y labels
        mLabelTextPaint.setTextAlign(Paint.Align.RIGHT);
        if (mCurrentViewport.height() <= 2) {
            for (i = 0; i < yStopsBuffer.numStops; i += 4) {
                canvas.drawText(AxisStops.getHHMM(yStopsBuffer.minutes[i]),
                        mContentRect.left - mLabelSeparation,
                        axisYPositionsBuffer[i],
                        mLabelTextPaint);
                canvas.drawLine(axisYLinesBuffer[i * 4 + 0], axisYLinesBuffer[i * 4 + 1], axisYLinesBuffer[i * 4 + 2], axisYLinesBuffer[i * 4 + 3], gridPaint);
            }
        }
        if (mCurrentViewport.height() <= 1) {
            for (i = 2; i < yStopsBuffer.numStops; i += 4) {
                canvas.drawText(AxisStops.getHHMM(yStopsBuffer.minutes[i]),
                        mContentRect.left - mLabelSeparation,
                        axisYPositionsBuffer[i],
                        mLabelTextPaint);
                canvas.drawLine(axisYLinesBuffer[i * 4 + 0], axisYLinesBuffer[i * 4 + 1], axisYLinesBuffer[i * 4 + 2], axisYLinesBuffer[i * 4 + 3], gridPaint);
            }
        }
        if (mCurrentViewport.height() <= 0.7) {
            for (i = 1; i < yStopsBuffer.numStops; i += 2) {

                canvas.drawText(AxisStops.getHHMM(yStopsBuffer.minutes[i]),
                        mContentRect.left - mLabelSeparation,
                        axisYPositionsBuffer[i],
                        mLabelTextPaint);
                canvas.drawLine(axisYLinesBuffer[i * 4 + 0], axisYLinesBuffer[i * 4 + 1], axisYLinesBuffer[i * 4 + 2], axisYLinesBuffer[i * 4 + 3], gridPaint);
            }
        }
//        Log.e(TAG, mCurrentViewport.bottom + " " + mCurrentViewport.height() + " " + mContentRect.height() + " " + mContentRect.width() + " " + mContentRect.bottom);
    }

    private static void computeAxisStops(float start, float stop, int steps, AxisStops outStops) {
        double range = stop - start;
        if (steps == 0 || range <= 0) {
            outStops.stops = new float[]{};
            outStops.numStops = 0;
            return;
        }

        double interval = range / steps;

        double first = Math.ceil(start / interval) * interval;
        double last = Math.nextUp(Math.floor(stop / interval) * interval);

        minDelta = interval / (AXIS_Y_MAX - AXIS_Y_MIN);
        maxDelta = Math.max(minDelta, maxDelta);
        double f;
        int i;
        int n = 0;
        for (f = first; f <= last; f += interval) {
            ++n;
        }

        outStops.numStops = n;

        if (outStops.stops.length < n) {
            // Ensure stops contains at least numStops elements.
            outStops.stops = new float[n];
            outStops.minutes = new int[n];
        }

        for (f = first, i = 0; i < n; f += interval, ++i) {
            outStops.stops[i] = (float) f;
            if (i == 0)
                outStops.minutes[i] = 1020;
            else
                outStops.minutes[i] = outStops.minutes[i - 1] - 15;

        }
        if (interval < 1) {
            outStops.decimals = (int) Math.ceil(-Math.log10(interval));
        } else {
            outStops.decimals = 0;
        }


    }

    /**
     * Computes the pixel offset for the given X chart value. This may be outside the view bounds.
     */
    private float getDrawX(float x) {
        return mContentRect.left
                + mContentRect.width()
                * (x - mCurrentViewport.left) / mCurrentViewport.width();
    }

    /**
     * Computes the pixel offset for the given Y chart value. This may be outside the view bounds.
     */
    private float getDrawY(float y) {
        return mContentRect.bottom
                - mContentRect.height()
                * ((y - mCurrentViewport.top) / mCurrentViewport.height());
    }

    private void drawEdgeEffectsUnclipped(Canvas canvas) {
        // The methods below rotate and translate the canvas as needed before drawing the glow,
        // since EdgeEffectCompat always draws a top-glow at 0,0.

        boolean needsInvalidate = false;

        if (!mEdgeEffectTop.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(mContentRect.left, mContentRect.top);
            mEdgeEffectTop.setSize(mContentRect.width(), mContentRect.height());
            if (mEdgeEffectTop.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (!mEdgeEffectBottom.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(2 * mContentRect.left - mContentRect.right, mContentRect.bottom);
            canvas.rotate(180, mContentRect.width(), 0);
            mEdgeEffectBottom.setSize(mContentRect.width(), mContentRect.height());
            if (mEdgeEffectBottom.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (!mEdgeEffectLeft.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(mContentRect.left, mContentRect.bottom);
            canvas.rotate(-90, 0, 0);
            mEdgeEffectLeft.setSize(mContentRect.height(), mContentRect.width());
            if (mEdgeEffectLeft.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (!mEdgeEffectRight.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(mContentRect.right, mContentRect.top);
            canvas.rotate(90, 0, 0);
            mEdgeEffectRight.setSize(mContentRect.height(), mContentRect.width());
            if (mEdgeEffectRight.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }
    public boolean isScaleAvailable(float viewportBottom, float viewportTop){
        return ((minDelta * ((AXIS_Y_MAX - AXIS_Y_MIN) / (viewportBottom - viewportTop))) <= maxDelta);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and objects related to gesture handling
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Finds the chart point (i.e. within the chart's domain and range) represented by the
     * given pixel coordinates, if that pixel is within the chart region described by
     * {@link #mContentRect}. If the point is found, the "dest" argument is set to the point and
     * this function returns true. Otherwise, this function returns false and "dest" is unchanged.
     */
    private boolean hitTest(float x, float y, PointF dest) {
        if (!mContentRect.contains((int) x, (int) y)) {
            return false;
        }

        dest.set(
                mCurrentViewport.left
                        + mCurrentViewport.width()
                        * (x - mContentRect.left) / mContentRect.width(),
                mCurrentViewport.top
                        + mCurrentViewport.height()
                        * (y - mContentRect.bottom) / -mContentRect.height());
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean retVal = mScaleGestureDetector.onTouchEvent(event);
        retVal = mGestureDetector.onTouchEvent(event) || retVal;
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case (MotionEvent.ACTION_DOWN):
//                Log.e(TAG, "Action was DOWN event.getY()=" + event.getY());
                break;
            case (MotionEvent.ACTION_MOVE):
                Log.d(TAG, "Action was MOVE");
                break;
            case (MotionEvent.ACTION_UP):
                isScale=false;
                Log.d(TAG, "Action was UP");
                break;
            case (MotionEvent.ACTION_CANCEL):
                Log.d(TAG, "Action was CANCEL");
                break;
            case (MotionEvent.ACTION_OUTSIDE):
                Log.d(TAG, "Movement occurred outside bounds " +
                        "of current screen element");
                break;
            default:
                break;
        }
        return retVal || super.onTouchEvent(event);
    }

    /**
     * The scale listener, used for handling multi-finger scale gestures.
     */
    private final ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener
            = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        /**
         * This is the active focal point in terms of the viewport. Could be a local
         * variable but kept here to minimize per-frame allocations.
         */
        private PointF viewportFocus = new PointF();
        private float lastSpanX;
        private float lastSpanY;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            lastSpanX = ScaleGestureDetectorCompat.getCurrentSpanX(scaleGestureDetector);
            lastSpanY = ScaleGestureDetectorCompat.getCurrentSpanY(scaleGestureDetector);
            isScale=true;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            float spanX = ScaleGestureDetectorCompat.getCurrentSpanX(scaleGestureDetector);
            float spanY = ScaleGestureDetectorCompat.getCurrentSpanY(scaleGestureDetector);

            float newWidth = lastSpanX / spanX * mCurrentViewport.width();
            float newHeight = lastSpanY / spanY * mCurrentViewport.height();

            float focusX = scaleGestureDetector.getFocusX();
            float focusY = scaleGestureDetector.getFocusY();
            hitTest(focusX, focusY, viewportFocus);

            float viewportLeft = viewportFocus.x - newWidth * (focusX - mContentRect.left) / mContentRect.width();
            float viewportTop = viewportFocus.y - newHeight * (mContentRect.bottom - focusY) / mContentRect.height();
            float viewportRight = viewportLeft + newWidth;
            float viewportBottom = viewportTop + newHeight;
            if (isScaleAvailable(viewportBottom,viewportTop)) { // neu do rong cell lon hon do rong dinh nghia thi khong cho phong to nua
//            if ((viewportBottom - viewportTop) > 0.5) { // neu viewport height duoc scale 4 lan thi khong cho phong to nua
                mCurrentViewport.set(viewportLeft, viewportTop, viewportRight, viewportBottom);
            }

            constrainViewport();
            ViewCompat.postInvalidateOnAnimation(Calendar.this);
            lastSpanX = spanX;
            lastSpanY = spanY;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isScale=false;
            super.onScaleEnd(detector);
        }
    };

    /**
     * Ensures that current viewport is inside the viewport extremes defined by {@link #AXIS_X_MIN},
     * {@link #AXIS_X_MAX}, {@link #AXIS_Y_MIN} and {@link #AXIS_Y_MAX}.
     */
    private void constrainViewport() {
        mCurrentViewport.left = Math.max(AXIS_X_MIN, mCurrentViewport.left);
        mCurrentViewport.top = Math.max(AXIS_Y_MIN, mCurrentViewport.top);
        mCurrentViewport.bottom = Math.max(Math.nextUp(mCurrentViewport.top),
                Math.min(AXIS_Y_MAX, mCurrentViewport.bottom));
        mCurrentViewport.right = Math.max(Math.nextUp(mCurrentViewport.left),
                Math.min(AXIS_X_MAX, mCurrentViewport.right));
    }

    /**
     * The gesture listener, used for handling simple gestures such as double touches, scrolls,
     * and flings.
     */
    private final GestureDetector.SimpleOnGestureListener mGestureListener
            = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            if(isScale==false)
                drawObject(getIndexinYStopsArr(gety(e.getY())));

            releaseEdgeEffects();
            mScrollerStartViewport.set(mCurrentViewport);
            mScroller.forceFinished(true);
            ViewCompat.postInvalidateOnAnimation(Calendar.this);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
//            if(isScaleAvailable(mCurrentViewport.bottom, mCurrentViewport.top)) {
//                mZoomer.forceFinished(true);
//                if (hitTest(e.getX(), e.getY(), mZoomFocalPoint)) {
//                    mZoomer.startZoom(ZOOM_AMOUNT);
//                }
//                ViewCompat.postInvalidateOnAnimation(Calendar.this);
//            }
//            return true;
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Scrolling uses math based on the viewport (as opposed to math using pixels).
            float viewportOffsetX = distanceX * mCurrentViewport.width() / mContentRect.width();
            float viewportOffsetY = -distanceY * mCurrentViewport.height() / mContentRect.height();
            computeScrollSurfaceSize(mSurfaceSizeBuffer);
            int scrolledX = (int) (mSurfaceSizeBuffer.x
                    * (mCurrentViewport.left + viewportOffsetX - AXIS_X_MIN)
                    / (AXIS_X_MAX - AXIS_X_MIN));
            int scrolledY = (int) (mSurfaceSizeBuffer.y
                    * (AXIS_Y_MAX - mCurrentViewport.bottom - viewportOffsetY)
                    / (AXIS_Y_MAX - AXIS_Y_MIN));
            boolean canScrollX = mCurrentViewport.left > AXIS_X_MIN
                    || mCurrentViewport.right < AXIS_X_MAX;
            boolean canScrollY = mCurrentViewport.top > AXIS_Y_MIN
                    || mCurrentViewport.bottom < AXIS_Y_MAX;
            setViewportBottomLeft(
                    mCurrentViewport.left + viewportOffsetX,
                    mCurrentViewport.bottom + viewportOffsetY);

            if (canScrollX && scrolledX < 0) {
                mEdgeEffectLeft.onPull(scrolledX / (float) mContentRect.width());
                mEdgeEffectLeftActive = true;
            }
            if (canScrollY && scrolledY < 0) {
                mEdgeEffectTop.onPull(scrolledY / (float) mContentRect.height());
                mEdgeEffectTopActive = true;
            }
            if (canScrollX && scrolledX > mSurfaceSizeBuffer.x - mContentRect.width()) {
                mEdgeEffectRight.onPull((scrolledX - mSurfaceSizeBuffer.x + mContentRect.width())
                        / (float) mContentRect.width());
                mEdgeEffectRightActive = true;
            }
            if (canScrollY && scrolledY > mSurfaceSizeBuffer.y - mContentRect.height()) {
                mEdgeEffectBottom.onPull((scrolledY - mSurfaceSizeBuffer.y + mContentRect.height())
                        / (float) mContentRect.height());
                mEdgeEffectBottomActive = true;
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            fling((int) -velocityX, (int) -velocityY);
            isScale=true;
            return true;
        }
    };

    private void releaseEdgeEffects() {
        mEdgeEffectLeftActive
                = mEdgeEffectTopActive
                = mEdgeEffectRightActive
                = mEdgeEffectBottomActive
                = false;
        mEdgeEffectLeft.onRelease();
        mEdgeEffectTop.onRelease();
        mEdgeEffectRight.onRelease();
        mEdgeEffectBottom.onRelease();
    }

    private void fling(int velocityX, int velocityY) {
        releaseEdgeEffects();
        // Flings use math in pixels (as opposed to math based on the viewport).
        computeScrollSurfaceSize(mSurfaceSizeBuffer);
        mScrollerStartViewport.set(mCurrentViewport);
        int startX = (int) (mSurfaceSizeBuffer.x * (mScrollerStartViewport.left - AXIS_X_MIN) / (
                AXIS_X_MAX - AXIS_X_MIN));
        int startY = (int) (mSurfaceSizeBuffer.y * (AXIS_Y_MAX - mScrollerStartViewport.bottom) / (
                AXIS_Y_MAX - AXIS_Y_MIN));
        mScroller.forceFinished(true);
        mScroller.fling(
                startX,
                startY,
                velocityX,
                velocityY,
                0, mSurfaceSizeBuffer.x - mContentRect.width(),
                0, mSurfaceSizeBuffer.y - mContentRect.height(),
                mContentRect.width() / 2,
                mContentRect.height() / 2);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * Computes the current scrollable surface size, in pixels. For example, if the entire chart
     * area is visible, this is simply the current size of {@link #mContentRect}. If the chart
     * is zoomed in 200% in both directions, the returned size will be twice as large horizontally
     * and vertically.
     */
    private void computeScrollSurfaceSize(Point out) {
        out.set(
                (int) (mContentRect.width() * (AXIS_X_MAX - AXIS_X_MIN)
                        / mCurrentViewport.width()),
                (int) (mContentRect.height() * (AXIS_Y_MAX - AXIS_Y_MIN)
                        / mCurrentViewport.height()));
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        boolean needsInvalidate = false;

        if (mScroller.computeScrollOffset()) {
            // The scroller isn't finished, meaning a fling or programmatic pan operation is
            // currently active.

            computeScrollSurfaceSize(mSurfaceSizeBuffer);
            int currX = mScroller.getCurrX();
            int currY = mScroller.getCurrY();

            boolean canScrollX = (mCurrentViewport.left > AXIS_X_MIN
                    || mCurrentViewport.right < AXIS_X_MAX);
            boolean canScrollY = (mCurrentViewport.top > AXIS_Y_MIN
                    || mCurrentViewport.bottom < AXIS_Y_MAX);

            if (canScrollX
                    && currX < 0
                    && mEdgeEffectLeft.isFinished()
                    && !mEdgeEffectLeftActive) {
                mEdgeEffectLeft.onAbsorb((int) OverScrollerCompat.getCurrVelocity(mScroller));
                mEdgeEffectLeftActive = true;
                needsInvalidate = true;
            } else if (canScrollX
                    && currX > (mSurfaceSizeBuffer.x - mContentRect.width())
                    && mEdgeEffectRight.isFinished()
                    && !mEdgeEffectRightActive) {
                mEdgeEffectRight.onAbsorb((int) OverScrollerCompat.getCurrVelocity(mScroller));
                mEdgeEffectRightActive = true;
                needsInvalidate = true;
            }

            if (canScrollY
                    && currY < 0
                    && mEdgeEffectTop.isFinished()
                    && !mEdgeEffectTopActive) {
                mEdgeEffectTop.onAbsorb((int) OverScrollerCompat.getCurrVelocity(mScroller));
                mEdgeEffectTopActive = true;
                needsInvalidate = true;
            } else if (canScrollY
                    && currY > (mSurfaceSizeBuffer.y - mContentRect.height())
                    && mEdgeEffectBottom.isFinished()
                    && !mEdgeEffectBottomActive) {
                mEdgeEffectBottom.onAbsorb((int) OverScrollerCompat.getCurrVelocity(mScroller));
                mEdgeEffectBottomActive = true;
                needsInvalidate = true;
            }

            float currXRange = AXIS_X_MIN + (AXIS_X_MAX - AXIS_X_MIN)
                    * currX / mSurfaceSizeBuffer.x;
            float currYRange = AXIS_Y_MAX - (AXIS_Y_MAX - AXIS_Y_MIN)
                    * currY / mSurfaceSizeBuffer.y;
            setViewportBottomLeft(currXRange, currYRange);
        }

        if (mZoomer.computeZoom()) {
            // Performs the zoom since a zoom is in progress (either programmatically or via
            // double-touch).
            float newWidth = (1f - mZoomer.getCurrZoom()) * mScrollerStartViewport.width();
            float newHeight = (1f - mZoomer.getCurrZoom()) * mScrollerStartViewport.height();
            float pointWithinViewportX = (mZoomFocalPoint.x - mScrollerStartViewport.left)
                    / mScrollerStartViewport.width();
            float pointWithinViewportY = (mZoomFocalPoint.y - mScrollerStartViewport.top)
                    / mScrollerStartViewport.height();
            mCurrentViewport.set(
                    mZoomFocalPoint.x - newWidth * pointWithinViewportX,
                    mZoomFocalPoint.y - newHeight * pointWithinViewportY,
                    mZoomFocalPoint.x + newWidth * (1 - pointWithinViewportX),
                    mZoomFocalPoint.y + newHeight * (1 - pointWithinViewportY));
            constrainViewport();
            needsInvalidate = true;
        }

        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * Sets the current viewport (defined by {@link #mCurrentViewport}) to the given
     * X and Y positions. Note that the Y value represents the topmost pixel position, and thus
     * the bottom of the {@link #mCurrentViewport} rectangle. For more details on why top and
     * bottom are flipped, see {@link #mCurrentViewport}.
     */
    private void setViewportBottomLeft(float x, float y) {
        /**
         * Constrains within the scroll range. The scroll range is simply the viewport extremes
         * (AXIS_X_MAX, etc.) minus the viewport size. For example, if the extrema were 0 and 10,
         * and the viewport size was 2, the scroll range would be 0 to 8.
         */

        float curWidth = mCurrentViewport.width();
        float curHeight = mCurrentViewport.height();
        x = Math.max(AXIS_X_MIN, Math.min(x, AXIS_X_MAX - curWidth));
        y = Math.max(AXIS_Y_MIN + curHeight, Math.min(y, AXIS_Y_MAX));

        mCurrentViewport.set(x, y - curHeight, x + curWidth, y);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods for programmatically changing the viewport
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the current viewport (visible extremes for the chart domain and range.)
     */
    public RectF getCurrentViewport() {
        return new RectF(mCurrentViewport);
    }

    /**
     * Sets the chart's current viewport.
     *
     * @see #getCurrentViewport()
     */
    public void setCurrentViewport(RectF viewport) {
        mCurrentViewport = viewport;
        constrainViewport();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * Smoothly zooms the chart in one step.
     */
    public void zoomIn() {
        mScrollerStartViewport.set(mCurrentViewport);
        mZoomer.forceFinished(true);
        mZoomer.startZoom(ZOOM_AMOUNT);
        mZoomFocalPoint.set(
                (mCurrentViewport.right + mCurrentViewport.left) / 2,
                (mCurrentViewport.bottom + mCurrentViewport.top) / 2);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * Smoothly zooms the chart out one step.
     */
    public void zoomOut() {
        mScrollerStartViewport.set(mCurrentViewport);
        mZoomer.forceFinished(true);
        mZoomer.startZoom(-ZOOM_AMOUNT);
        mZoomFocalPoint.set(
                (mCurrentViewport.right + mCurrentViewport.left) / 2,
                (mCurrentViewport.bottom + mCurrentViewport.top) / 2);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * Smoothly pans the chart left one step.
     */
    public void panLeft() {
        fling((int) (-PAN_VELOCITY_FACTOR * getWidth()), 0);
    }

    /**
     * Smoothly pans the chart right one step.
     */
    public void panRight() {
        fling((int) (PAN_VELOCITY_FACTOR * getWidth()), 0);
    }

    /**
     * Smoothly pans the chart up one step.
     */
    public void panUp() {
        fling(0, (int) (-PAN_VELOCITY_FACTOR * getHeight()));
    }

    /**
     * Smoothly pans the chart down one step.
     */
    public void panDown() {
        fling(0, (int) (PAN_VELOCITY_FACTOR * getHeight()));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods related to custom attributes
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public float getLabelTextSize() {
        return mLabelTextSize;
    }

    public void setLabelTextSize(float labelTextSize) {
        mLabelTextSize = labelTextSize;
        initPaints();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public int getLabelTextColor() {
        return mLabelTextColor;
    }

    public void setLabelTextColor(int labelTextColor) {
        mLabelTextColor = labelTextColor;
        initPaints();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public float getGridThickness() {
        return gridThickness;
    }

    public void setGridThickness(float gridThickness) {
        gridThickness = gridThickness;
        initPaints();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public int getGridColor() {
        return gridColor;
    }

    public void setGridColor(int gridColor) {
        gridColor = gridColor;
        initPaints();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public float getAxisThickness() {
        return mAxisThickness;
    }

    public void setAxisThickness(float axisThickness) {
        mAxisThickness = axisThickness;
        initPaints();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public int getAxisColor() {
        return mAxisColor;
    }

    public void setAxisColor(int axisColor) {
        mAxisColor = axisColor;
        initPaints();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public float getDataThickness() {
        return mDataThickness;
    }

    public void setDataThickness(float dataThickness) {
        mDataThickness = dataThickness;
    }

    public int getDataColor() {
        return mDataColor;
    }

    public void setDataColor(int dataColor) {
        mDataColor = dataColor;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and classes related to view state persistence.
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.viewport = mCurrentViewport;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mCurrentViewport = ss.viewport;
    }

    /**
     * Persistent state that is saved by InteractiveLineGraphView.
     */
    public static class SavedState extends BaseSavedState {
        private RectF viewport;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(viewport.left);
            out.writeFloat(viewport.top);
            out.writeFloat(viewport.right);
            out.writeFloat(viewport.bottom);
        }

        @Override
        public String toString() {
            return "Calendar.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " viewport=" + viewport.toString() + "}";
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        });

        SavedState(Parcel in) {
            super(in);
            viewport = new RectF(in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
        }
    }

    /**
     * A simple class representing axis label values.
     *
     * @see #computeAxisStops
     */
    private static class AxisStops {
        float[] stops = new float[]{};
        int[] minutes = new int[]{};
        int numStops;
        int decimals;

        public static String getHHMM(int minutes) {
            return String.format("%02d:%02d", minutes / 60, minutes % 60);
        }
    }
}