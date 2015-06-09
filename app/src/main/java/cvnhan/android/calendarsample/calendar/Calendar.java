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
    private static float cellMaxWidth = 90;
    private static float cellMaxHeight = 80;

    private static int numColum = 10;
    private static int numRow = 50;
    private static boolean hasHeaderRow = true;
    private static boolean hasHeaderColum = true;

    private static double minDeltaH = 1, maxDeltaH = 1;
    private static double minDeltaW = 1, maxDeltaW = 1;
    private float density = getResources().getDisplayMetrics().density;
    public boolean isScale = false;

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
    private float labelHeaderRowTextSize = 14;
    private int labelHeaderRowSeparation = 10;
    private int labelHeaderRowTextColor = 0x000000;
    private int labelHeaderRowHeight;
    private int maxLabelHeaderRowWidth;
    private Paint headerRowPaint;

    //HeaderCol
    private float labelHeaderColTextSize = 14;
    private int labelHeaderColSeparation = 10;
    private int labelHeaderColTextColor = 0x000000;
    private int labelHeaderColHeight;
    private int maxLabelHeaderColWidth;
    private Paint headerColPaint;

    //Data
    private float dataThickness;
    private int dataColor;
    private Paint dataPaint;

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
    private Paint mObjPaint;

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

            dataThickness = a.getDimension(
                    R.styleable.Calendar_cal_dataThickness, dataThickness);
            dataColor = a.getColor(
                    R.styleable.Calendar_cal_dataColor, dataColor);

            labelHeaderColTextColor = a.getColor(
                    R.styleable.Calendar_cal_labelTextColor, mLabelTextColor);
            labelHeaderColTextSize = a.getDimension(
                    R.styleable.Calendar_cal_labelTextSize, mLabelTextSize);
            labelHeaderColSeparation = a.getDimensionPixelSize(
                    R.styleable.Calendar_cal_labelSeparation, mLabelSeparation);

            labelHeaderRowTextColor = a.getColor(
                    R.styleable.Calendar_cal_labelTextColor, mLabelTextColor);
            labelHeaderRowTextSize = a.getDimension(
                    R.styleable.Calendar_cal_labelTextSize, mLabelTextSize);
            labelHeaderRowSeparation = a.getDimensionPixelSize(
                    R.styleable.Calendar_cal_labelSeparation, mLabelSeparation);
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
        headerRowPaint = new Paint();
        headerRowPaint.setAntiAlias(true);
        headerRowPaint.setTextSize(labelHeaderRowTextSize);
        headerRowPaint.setColor(labelHeaderRowTextColor);
        labelHeaderRowHeight = (int) Math.abs(headerRowPaint.getFontMetrics().top);
        maxLabelHeaderRowWidth = (int) headerRowPaint.measureText("00:00");

        headerColPaint = new Paint();
        headerColPaint.setAntiAlias(true);
        headerColPaint.setTextSize(labelHeaderColTextSize);
        headerColPaint.setColor(labelHeaderColTextColor);
        labelHeaderColHeight = (int) Math.abs(headerColPaint.getFontMetrics().top);
        maxLabelHeaderColWidth = (int) headerColPaint.measureText("00:00");

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

        dataPaint = new Paint();
        dataPaint.setStrokeWidth(dataThickness);
        dataPaint.setColor(dataColor);
        dataPaint.setStyle(Paint.Style.STROKE);
        dataPaint.setAntiAlias(true);

        mObjPaint = new Paint();
        mObjPaint.setStrokeWidth(dataThickness);
        mObjPaint.setColor(Color.BLUE);
        mObjPaint.setStyle(Paint.Style.FILL);
        mObjPaint.setAlpha(50);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mContentRect.set(
                getPaddingLeft() + maxLabelHeaderColWidth + labelHeaderColSeparation,
                getPaddingTop() + getPaddingTop() + maxLabelHeaderRowWidth
                        + labelHeaderRowSeparation,
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom());
        initAxisStops(numRow, numColum);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minChartSize = getResources().getDimensionPixelSize(R.dimen.min_chart_size);
        setMeasuredDimension(
                Math.max(getSuggestedMinimumWidth(),
                        resolveSize(minChartSize + getPaddingLeft() + maxLabelHeaderColWidth
                                        + labelHeaderColSeparation + getPaddingRight(),
                                widthMeasureSpec)),
                Math.max(getSuggestedMinimumHeight(),
                        resolveSize(minChartSize + getPaddingTop() + maxLabelHeaderRowWidth
                                        + labelHeaderRowSeparation + getPaddingBottom(),
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
        computeAxis();
        // Draw Header
        drawHeader(canvas);
        int clipRestoreCount = canvas.save();
        canvas.clipRect(mContentRect);
        // Draws axes
        drawAxes(canvas);
        drawEdgeEffectsUnclipped(canvas);
        // Removes clipping rectangle
        canvas.restoreToCount(clipRestoreCount);
        canvas.drawRect(mContentRect, gridPaint);


    }

    private void computeAxis() {
        int i;
        if (axisXPositionsBuffer.length < xStopsBuffer.axisLength) {
            axisXPositionsBuffer = new float[xStopsBuffer.axisLength];
        }
        if (axisYPositionsBuffer.length < yStopsBuffer.axisLength) {
            axisYPositionsBuffer = new float[yStopsBuffer.axisLength];
        }
        if (axisXLinesBuffer.length < xStopsBuffer.axisLength * 4) {
            axisXLinesBuffer = new float[xStopsBuffer.axisLength * 4];
        }
        if (axisYLinesBuffer.length < yStopsBuffer.axisLength * 4) {
            axisYLinesBuffer = new float[yStopsBuffer.axisLength * 4];
        }

        // Compute positions
        for (i = 0; i < xStopsBuffer.axisLength; i++) {
            axisXPositionsBuffer[i] = getDrawX(xStopsBuffer.stops[i]);
        }
        for (i = 0; i < yStopsBuffer.axisLength; i++) {
            axisYPositionsBuffer[i] = getDrawY(yStopsBuffer.stops[i]);
        }

        for (i = 0; i < xStopsBuffer.axisLength; i++) {
            axisXLinesBuffer[i * 4 + 0] = (float) Math.floor(axisXPositionsBuffer[i]);
            axisXLinesBuffer[i * 4 + 1] = mContentRect.top;
            axisXLinesBuffer[i * 4 + 2] = (float) Math.floor(axisXPositionsBuffer[i]);
            axisXLinesBuffer[i * 4 + 3] = mContentRect.bottom;
        }
        for (i = 0; i < yStopsBuffer.axisLength; i++) {
            axisYLinesBuffer[i * 4 + 0] = mContentRect.left;
            axisYLinesBuffer[i * 4 + 1] = (float) Math.floor(axisYPositionsBuffer[i]);
            axisYLinesBuffer[i * 4 + 2] = mContentRect.right;
            axisYLinesBuffer[i * 4 + 3] = (float) Math.floor(axisYPositionsBuffer[i]);
        }
    }

    private void drawHeader(Canvas canvas) {
        int clipRestoreCount = canvas.save();
        canvas.clipRect(new Rect(mContentRect.left, 0, mContentRect.right, mContentRect.top));
        int i;
        // Draws X labels
        mLabelTextPaint.setTextAlign(Paint.Align.LEFT);
        for (i = 0; i < xStopsBuffer.axisLength; i++) {
            canvas.drawText(
                    AxisStops.getHHMM(xStopsBuffer.minutes[i]),
                    (i < xStopsBuffer.axisLength - 1) ? axisXPositionsBuffer[i] : (axisXPositionsBuffer[i] - maxLabelHeaderRowWidth),
                    mContentRect.top - labelHeaderRowHeight - labelHeaderRowSeparation,
                    headerRowPaint);
        }
        canvas.restoreToCount(clipRestoreCount);

        // Draws Y labels
        clipRestoreCount = canvas.save();
        canvas.clipRect(new Rect(0, mContentRect.top, mContentRect.left, mContentRect.bottom));
        mLabelTextPaint.setTextAlign(Paint.Align.RIGHT);
        if (mCurrentViewport.height() <= 2) {
            for (i = 0; i < yStopsBuffer.axisLength; i += 4) {
                drawTextY(canvas, i);
            }
        }
        if (mCurrentViewport.height() <= 1) {
            for (i = 2; i < yStopsBuffer.axisLength; i += 4) {
                drawTextY(canvas, i);
            }
        }
        if (mCurrentViewport.height() <= 0.6) {
            for (i = 1; i < yStopsBuffer.axisLength; i += 2) {
                drawTextY(canvas, i);
            }
        }
        canvas.restoreToCount(clipRestoreCount);
    }

    private void drawTextY(Canvas canvas, int i) {
        canvas.drawText(AxisStops.getHHMM(yStopsBuffer.minutes[i]),
                mContentRect.left - labelHeaderColSeparation - maxLabelHeaderColWidth,
                (i < yStopsBuffer.axisLength - 1) ? axisYPositionsBuffer[i] : (axisYPositionsBuffer[i] + labelHeaderColHeight),
                headerColPaint);
    }

    /**
     * ADAPTOR define
     */
//    (AXIS_X_MAX - mCurrentViewport.right) - (mCurrentViewport.left - AXIS_X_MIN) -
    //viewport to contentRect
    private float getAxisx(float eventX) {
        return mCurrentViewport.left + (eventX - mContentRect.left) / mContentRect.width() * mCurrentViewport.width();
    }

    private float getAxisy(float eventY) {
        //do toa do viewport bottom tuong ung voi contentRect top nen phai cong them 1 khoang delta
        return (AXIS_Y_MAX - mCurrentViewport.bottom) - (mCurrentViewport.top - AXIS_Y_MIN) + mCurrentViewport.top + (eventY - mContentRect.top) / mContentRect.height() * mCurrentViewport.height();
    }

    public int getIndexy(float y) {
        for (int i = 0; i < yStopsBuffer.axisLength - 1; i++) {
            if (y >= yStopsBuffer.stops[i] && y < yStopsBuffer.stops[i + 1]) {
                return i;
            }
        }
        return -1;
    }

    public int getIndexyByEventY(final float eventY) {
        return getIndexy(getAxisy(eventY));
    }

    public int getIndexx(float x) {
        for (int i = 0; i < xStopsBuffer.axisLength - 1; i++) {
            if (x >= xStopsBuffer.stops[i] && x < xStopsBuffer.stops[i + 1]) {
                return i;
            }
        }
        return -1;
    }

    public int getIndexxByEventX(float eventX) {
        return getIndexx(getAxisx(eventX));
    }

    public void drawObjectbyCell(int row, int col) {
        if (row == -1 || col == -1) return;
        float left = getDrawX(xStopsBuffer.stops[col]);
        float top = getDrawY(yStopsBuffer.stops[yStopsBuffer.axisLength - row - 1]);
        canvas.drawRect(Math.max(left, mContentRect.left), Math.max(top, mContentRect.top), Math.min(left + getBlockWidth(), mContentRect.right), Math.min(top + getBlockHeight(), mContentRect.bottom), mObjPaint);
    }

    public void drawObjectbyEvent(float eventX, float eventY) {
        int row = getIndexyByEventY(eventY);
        int col = getIndexxByEventX(eventX);
        Log.e(TAG, "row=" + row + " col=" + col + " eventX=" + eventX + " eventY=" + eventY);
        drawObjectbyCell(row, col);

    }

    public int getIndexinYStopsArr(float y) {
        for (int i = 0; i < yStopsBuffer.axisLength - 1; i++) {
            if (y >= yStopsBuffer.stops[i] && y <= yStopsBuffer.stops[i + 1]) {
                return i - 1;
            }
        }
        return -1;
    }

    public float getBlockHeight() {
        return (float) (minDeltaH * ((AXIS_Y_MAX - AXIS_Y_MIN) / (mCurrentViewport.bottom - mCurrentViewport.top)) * mContentRect.height());
    }

    public float getBlockWidth() {
        return (float) (minDeltaW * ((AXIS_X_MAX - AXIS_X_MIN) / (mCurrentViewport.right - mCurrentViewport.left)) * mContentRect.width());
    }

    public boolean isScaleYAvailable(float viewportBottom, float viewportTop) {
        return ((minDeltaH * ((AXIS_Y_MAX - AXIS_Y_MIN) / (viewportBottom - viewportTop))) <= maxDeltaH);
    }

    public boolean isScaleXAvailable(float viewportRight, float viewportLeft) {
        return ((minDeltaW * ((AXIS_X_MAX - AXIS_X_MIN) / (viewportRight - viewportLeft))) <= maxDeltaW);
    }

    //contentRect to viewport

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

    //END ADAPTOR


    public void initAxisStops(int row, int col) {
        maxDeltaH = (cellMaxHeight * density) / mContentRect.height();
        maxDeltaW = (cellMaxWidth * density) / mContentRect.width();
        computeAxisStops(
                mCurrentViewport.left,
                mCurrentViewport.right,
                col,
                xStopsBuffer, false);
        computeAxisStops(
                mCurrentViewport.top,
                mCurrentViewport.bottom,
                row,
                yStopsBuffer, true);

    }

    /**
     * Draws the chart axes onto the canvas.
     */
    private void drawAxes(Canvas canvas) {
        int i;
        canvas.drawLines(axisXLinesBuffer, 0, xStopsBuffer.axisLength * 4, gridPaint);

        mLabelTextPaint.setTextAlign(Paint.Align.RIGHT);
        if (mCurrentViewport.height() <= 2) {
            for (i = 0; i < yStopsBuffer.axisLength; i += 4) {
                canvas.drawLine(axisYLinesBuffer[i * 4 + 0], axisYLinesBuffer[i * 4 + 1], axisYLinesBuffer[i * 4 + 2], axisYLinesBuffer[i * 4 + 3], gridPaint);
            }
        }
        if (mCurrentViewport.height() <= 1) {
            for (i = 2; i < yStopsBuffer.axisLength; i += 4) {
                canvas.drawLine(axisYLinesBuffer[i * 4 + 0], axisYLinesBuffer[i * 4 + 1], axisYLinesBuffer[i * 4 + 2], axisYLinesBuffer[i * 4 + 3], gridPaint);
            }
        }
        if (mCurrentViewport.height() <= 0.6) {
            for (i = 1; i < yStopsBuffer.axisLength; i += 2) {
                canvas.drawLine(axisYLinesBuffer[i * 4 + 0], axisYLinesBuffer[i * 4 + 1], axisYLinesBuffer[i * 4 + 2], axisYLinesBuffer[i * 4 + 3], gridPaint);
            }
        }
    }

    private static void computeAxisStops(double first, double last, int steps, AxisStops outStops, boolean isYAxis) {
        double range = last - first;
        if (steps == 0 || range <= 0) {
            outStops.stops = new float[]{};
            outStops.numBlocks = 0;
            return;
        }
        double interval = range / steps;
        if (isYAxis) {
            minDeltaH = interval / (AXIS_Y_MAX - AXIS_Y_MIN);
            maxDeltaH = Math.max(minDeltaH, maxDeltaH);
        } else {
            minDeltaW = interval / (AXIS_Y_MAX - AXIS_Y_MIN);
            maxDeltaW = Math.max(minDeltaW, maxDeltaW);
        }
        double f;
        int i;
        int n = 0;
        for (f = first; f <= last; f += interval) {
            ++n;
        }
        outStops.numBlocks = n;
        outStops.axisLength = n + 1;
        if (outStops.stops.length < outStops.axisLength) {
            // Ensure stops contains at least numStops elements.
            outStops.stops = new float[outStops.axisLength];
            outStops.minutes = new int[outStops.axisLength];
        }

        for (f = first, i = 0; i < outStops.axisLength; f += interval, ++i) {
            outStops.stops[i] = (float) Math.min(f, last);
            if (i == 0)
                outStops.minutes[i] = 1020;
            else
                outStops.minutes[i] = outStops.minutes[i - 1] - 15;
        }
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
                if (isScale == false) {
                    drawObjectbyEvent(event.getX(), event.getY());
                }
                break;
            case (MotionEvent.ACTION_MOVE):
                break;
            case (MotionEvent.ACTION_UP):
                isScale = false;
                break;
            case (MotionEvent.ACTION_CANCEL):
                break;
            case (MotionEvent.ACTION_OUTSIDE):
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
            isScale = true;
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
            if (isScaleYAvailable(viewportBottom, viewportTop)) { // neu do rong cell lon hon do rong dinh nghia thi khong cho phong to nua
                mCurrentViewport.top = viewportTop;
                mCurrentViewport.bottom = viewportBottom;
            }
            if (isScaleXAvailable(viewportRight, viewportLeft)) {
                mCurrentViewport.left = viewportLeft;
                mCurrentViewport.right = viewportRight;
            }

            constrainViewport();
            ViewCompat.postInvalidateOnAnimation(Calendar.this);
            lastSpanX = spanX;
            lastSpanY = spanY;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isScale = false;
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
            releaseEdgeEffects();
            mScrollerStartViewport.set(mCurrentViewport);
            mScroller.forceFinished(true);
            ViewCompat.postInvalidateOnAnimation(Calendar.this);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mZoomer.forceFinished(true);
            if (hitTest(e.getX(), e.getY(), mZoomFocalPoint)) {
                mZoomer.startZoom(ZOOM_AMOUNT);
            }
            ViewCompat.postInvalidateOnAnimation(Calendar.this);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
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
            isScale = true;
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

            float viewportLeft = mZoomFocalPoint.x - newWidth * pointWithinViewportX;
            float viewportTop = mZoomFocalPoint.y - newHeight * pointWithinViewportY;
            float viewportRight = mZoomFocalPoint.x + newWidth * (1 - pointWithinViewportX);
            float viewportBottom = mZoomFocalPoint.y + newHeight * (1 - pointWithinViewportY);
            if (isScaleYAvailable(viewportBottom, viewportTop)) { // neu do rong cell lon hon do rong dinh nghia thi khong cho phong to nua
                mCurrentViewport.top = viewportTop;
                mCurrentViewport.bottom = viewportBottom;
            }
            if (isScaleXAvailable(viewportRight, viewportLeft)) {
                mCurrentViewport.left = viewportLeft;
                mCurrentViewport.right = viewportRight;
            }
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

    public float getDataThickness() {
        return dataThickness;
    }

    public void setDataThickness(float dataThickness) {
        dataThickness = dataThickness;
    }

    public int getDataColor() {
        return dataColor;
    }

    public void setDataColor(int dataColor) {
        dataColor = dataColor;
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
        int numBlocks;
        int axisLength; //length of arr = numStops+1 because it has a last line

        public static String getHHMM(int minutes) {
            return String.format("%02d:%02d", minutes / 60, minutes % 60);
        }
    }
}