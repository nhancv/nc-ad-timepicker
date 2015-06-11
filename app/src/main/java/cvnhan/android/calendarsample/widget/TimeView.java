package cvnhan.android.calendarsample.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.OverScroller;

import cvnhan.android.calendarsample.MainActivity;
import cvnhan.android.calendarsample.R;

/**
 * Created by cvnhan on 09-Jun-15.
 */
public class TimeView extends View {
    private static final String TAG = "TimeView";

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

    private static final float AXIS_X_MIN = -1f;
    private static final float AXIS_X_MAX = 1f;
    private static final float AXIS_Y_MIN = -1f;
    private static final float AXIS_Y_MAX = 1f;

    public static RectF mCurrentViewport = new RectF(AXIS_X_MIN, AXIS_Y_MIN, AXIS_X_MAX, AXIS_Y_MAX);

    public static Rect mContentRect = new Rect();
    public static final int MINCHARTSIZE = 100;
    //Define custom
    private static float cellMaxWidth = 90;
    private static float cellMaxHeight = 80;

    private static int numColum = 1;
    private static int numRow = 32;
    private static int startHour = 540; //9:00
    private static int blockHour = 15; //15 minutes
    private static boolean hasHeaderRow = false;
    private static boolean hasHeaderColum = true;

    private static double minDeltaH = 1, maxDeltaH = 1;
    private static double minDeltaW = 1, maxDeltaW = 1;
    private float density = getResources().getDisplayMetrics().density;

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
    private float labelHeaderRowTextSize = 8;
    private int labelHeaderRowSeparation = 1;
    private int labelHeaderRowTextColor = 0x000000;
    private int labelHeaderRowHeight;
    private int maxLabelHeaderRowWidth;
    private Paint headerRowPaint;

    //HeaderCol
    private float labelHeaderColTextSize = 8;
    private int labelHeaderColSeparation = 1;
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

    // State objects and values related to gesture tracking.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetectorCompat gestureDetector;
    private OverScroller mScroller;
    private Zoomer mZoomer;
    private PointF zoomFocalPoint = new PointF();
    private RectF scrollerStartViewport = new RectF(); // Used only for zooms and flings.

    private float _downEventX, _downEventY, _moveEventX, _moveEventY,
            _distanceX, _distanceY, _spanX, _spanY, _velocityX, _velocityY, _showPressX, _showPressY, _longPressX, _longPressY, _deltaMove;
    private boolean FLAG_DOWN = false;
    private boolean FLAG_MOVE = false;
    private boolean FLAG_UP = false;
    private boolean FLAG_SCALE = false;
    private boolean FLAG_SCROLL = false;
    private boolean FLAG_DOUBLETAP = false;
    private boolean FLAG_FLING = false;
    private boolean FLAG_LONGPRESS = false;
    private boolean FLAG_SHOWPRESS = false;
    private boolean FLAG_RESTORE = false;

    private ObjectData objectData;

    // Edge effect / overscroll tracking objects.
    private EdgeEffectCompat edgeEffectTop;
    private EdgeEffectCompat edgeEffectBottom;
    private EdgeEffectCompat edgeEffectLeft;
    private EdgeEffectCompat edgeEffectRight;
    private boolean edgeEffectTopActive;
    private boolean edgeEffectBottomActive;
    private boolean edgeEffectLeftActive;
    private boolean edgeEffectRightActive;

    private Canvas canvas;
    public MainActivity mainActivity;
    public TimeView(Context context) {
        this(context, null, 0);
    }

    public TimeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.TimeView, defStyle, defStyle);

        try {
            gridThickness = a.getDimension(
                    R.styleable.TimeView_gridThickness, gridThickness);
            gridColor = a.getColor(
                    R.styleable.TimeView_gridColor, gridColor);

            dataThickness = a.getDimension(
                    R.styleable.TimeView_dataThickness, dataThickness);
            dataColor = a.getColor(
                    R.styleable.TimeView_dataColor, dataColor);

            labelHeaderColTextColor = a.getColor(
                    R.styleable.TimeView_labelHeaderColTextColor, labelHeaderColTextColor);
            labelHeaderColTextSize = a.getDimension(
                    R.styleable.TimeView_labelHeaderColTextSize, labelHeaderColTextSize);
            labelHeaderColSeparation = a.getDimensionPixelSize(
                    R.styleable.TimeView_labelHeaderColSeparation, labelHeaderColSeparation);

            labelHeaderRowTextColor = a.getColor(
                    R.styleable.TimeView_labelHeaderRowTextColor, labelHeaderRowTextColor);
            labelHeaderRowTextSize = a.getDimension(
                    R.styleable.TimeView_labelHeaderRowTextSize, labelHeaderRowTextSize);
            labelHeaderRowSeparation = a.getDimensionPixelSize(
                    R.styleable.TimeView_labelHeaderRowSeparation, labelHeaderRowSeparation);

            numColum = a.getInteger(R.styleable.TimeView_numColum, numColum);
            numRow = a.getInteger(R.styleable.TimeView_numRow, numRow);

            cellMaxWidth = a.getFloat(R.styleable.TimeView_cellMaxWidth, cellMaxWidth);
            cellMaxHeight = a.getFloat(R.styleable.TimeView_cellMaxHeight, cellMaxHeight);

            hasHeaderColum = a.getBoolean(R.styleable.TimeView_hasHeaderCol, hasHeaderColum);
            hasHeaderRow = a.getBoolean(R.styleable.TimeView_hasHeaderRow, hasHeaderRow);

        } finally {
            a.recycle();
        }

        initPaints();

        // Sets up interactions
        scaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureListener);
        gestureDetector = new GestureDetectorCompat(context, mGestureListener);

        mScroller = new OverScroller(context);
        mZoomer = new Zoomer(context);

        // Sets up edge effects
        edgeEffectLeft = new EdgeEffectCompat(context);
        edgeEffectTop = new EdgeEffectCompat(context);
        edgeEffectRight = new EdgeEffectCompat(context);
        edgeEffectBottom = new EdgeEffectCompat(context);
    }

    public void injectMainActivity(MainActivity mainActivity){
        this.mainActivity= mainActivity;
    }
    /**
     * (Re)initializes {@link Paint} objects based on current attribute values.
     */
    private void initPaints() {
        // Important for certain APIs
        mCurrentViewport = new RectF(AXIS_X_MIN, AXIS_Y_MIN, AXIS_X_MAX, AXIS_Y_MAX);
        mContentRect = new Rect();
        density = getResources().getDisplayMetrics().density;

        headerRowPaint = new Paint();
        headerRowPaint.setAntiAlias(true);
        headerRowPaint.setTextSize(labelHeaderRowTextSize);
        headerRowPaint.setColor(labelHeaderRowTextColor);
//        headerRowPaint.setTextAlign(Paint.Align.CENTER);
        labelHeaderRowHeight = (int) Math.abs(headerRowPaint.getFontMetrics().top);
        maxLabelHeaderRowWidth = (int) headerRowPaint.measureText("00:00");

        headerColPaint = new Paint();
        headerColPaint.setAntiAlias(true);
        headerColPaint.setTextSize(labelHeaderColTextSize);
        headerColPaint.setColor(labelHeaderColTextColor);
//        headerColPaint.setTextAlign(Paint.Align.RIGHT);
        labelHeaderColHeight = (int) Math.abs(headerColPaint.getFontMetrics().top);
        maxLabelHeaderColWidth = (int) headerColPaint.measureText("00:00");

        gridPaint = new Paint();
        gridPaint.setStrokeWidth(gridThickness);
        gridPaint.setColor(gridColor);
        gridPaint.setStyle(Paint.Style.STROKE);

        dataPaint = new Paint();
        dataPaint.setStrokeWidth(dataThickness);
        dataPaint.setColor(dataColor);
        dataPaint.setStyle(Paint.Style.FILL);
        dataPaint.setAntiAlias(true);
        dataPaint.setAlpha(50);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mContentRect.set(
                getPaddingLeft() + ((hasHeaderColum) ? (maxLabelHeaderColWidth + labelHeaderColSeparation) : 0),
                getPaddingTop() + ((hasHeaderRow) ? (+maxLabelHeaderRowWidth
                        + labelHeaderRowSeparation) : 0),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom());
        objectData = ObjectData.getInstance(this);
        initAxisStops(numRow, numColum);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minChartSize = MINCHARTSIZE;
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
        objectData.Draw(canvas);

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

        int i;
        // Draws X labels
        if (hasHeaderRow) {
            canvas.clipRect(new Rect(mContentRect.left, 0, mContentRect.right, mContentRect.top));

            for (i = 0; i < xStopsBuffer.axisLength; i++) {
                canvas.drawText(
                        AxisStops.getHHMM(xStopsBuffer.minutes[i]),
                        (i < xStopsBuffer.axisLength - 1) ? axisXPositionsBuffer[i] : (axisXPositionsBuffer[i] - maxLabelHeaderRowWidth),
                        mContentRect.top - labelHeaderRowHeight - labelHeaderRowSeparation,
                        headerRowPaint);
            }
            canvas.restoreToCount(clipRestoreCount);
        }

        // Draws Y labels
        if (hasHeaderColum) {
            clipRestoreCount = canvas.save();
            canvas.clipRect(new Rect(0, mContentRect.top, mContentRect.left, mContentRect.bottom));

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
    }

    private void drawTextY(Canvas canvas, int i) {
        canvas.drawText(AxisStops.getHHMM(yStopsBuffer.minutes[i]),
                mContentRect.left - labelHeaderColSeparation - maxLabelHeaderColWidth,
                (i > 0) ? axisYPositionsBuffer[i] : (axisYPositionsBuffer[i] + labelHeaderColHeight),
                headerColPaint);
    }

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
        //Draw axis X
        canvas.drawLines(axisXLinesBuffer, 0, xStopsBuffer.axisLength * 4, gridPaint);

        //Draw axis X
        //Draw hours
        if (mCurrentViewport.height() <= 2) {
            for (i = 0; i < yStopsBuffer.axisLength; i += 4) {
                canvas.drawLine(axisYLinesBuffer[i * 4 + 0], axisYLinesBuffer[i * 4 + 1], axisYLinesBuffer[i * 4 + 2], axisYLinesBuffer[i * 4 + 3], gridPaint);
            }
        }
        //Draw 30 unit (minutes)
        if (mCurrentViewport.height() <= 1) {
            for (i = 2; i < yStopsBuffer.axisLength; i += 4) {
                canvas.drawLine(axisYLinesBuffer[i * 4 + 0], axisYLinesBuffer[i * 4 + 1], axisYLinesBuffer[i * 4 + 2], axisYLinesBuffer[i * 4 + 3], gridPaint);
            }
        }
        //Draw 15 unit (minutes)
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
        outStops.numBlocks = steps;
        outStops.axisLength = steps + 1;
        if (outStops.stops.length < outStops.axisLength) {
            // Ensure stops contains at least numStops elements.
            outStops.stops = new float[outStops.axisLength];
            outStops.minutes = new int[outStops.axisLength];
        }

        for (f = first, i = 0; i < outStops.axisLength; f += interval, ++i) {
            outStops.stops[i] = (float) f;
            if (i == 0)
                outStops.minutes[i] = startHour;
            else
                outStops.minutes[i] = outStops.minutes[i - 1] + blockHour;
        }
    }


    private void drawEdgeEffectsUnclipped(Canvas canvas) {
        boolean needsInvalidate = false;

        if (!edgeEffectTop.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(mContentRect.left, mContentRect.top);
            edgeEffectTop.setSize(mContentRect.width(), mContentRect.height());
            if (edgeEffectTop.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (!edgeEffectBottom.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(2 * mContentRect.left - mContentRect.right, mContentRect.bottom);
            canvas.rotate(180, mContentRect.width(), 0);
            edgeEffectBottom.setSize(mContentRect.width(), mContentRect.height());
            if (edgeEffectBottom.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (!edgeEffectLeft.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(mContentRect.left, mContentRect.bottom);
            canvas.rotate(-90, 0, 0);
            edgeEffectLeft.setSize(mContentRect.height(), mContentRect.width());
            if (edgeEffectLeft.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (!edgeEffectRight.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(mContentRect.right, mContentRect.top);
            canvas.rotate(90, 0, 0);
            edgeEffectRight.setSize(mContentRect.height(), mContentRect.width());
            if (edgeEffectRight.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /*
     * ADAPTOR define
     */
    //viewport to contentRect
    private float getAxisx(float eventX) {
        return mCurrentViewport.left + (eventX - mContentRect.left) / mContentRect.width() * mCurrentViewport.width();
    }

    private float getAxisy(float eventY) {
        return mCurrentViewport.top + (eventY - mContentRect.top) / mContentRect.height() * mCurrentViewport.height();
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
        float top = getDrawY(yStopsBuffer.stops[row]);
        canvas.drawRect(Math.max(left, mContentRect.left), Math.max(top, mContentRect.top), Math.min(left + getBlockWidth(), mContentRect.right), Math.min(top + getBlockHeight(), mContentRect.bottom), dataPaint);
    }

    public void drawObjectbyEvent(float eventX, float eventY) {
        int row = getIndexyByEventY(eventY);
        int col = getIndexxByEventX(eventX);
        drawObjectbyCell(row, col);

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
        return mContentRect.top
                + mContentRect.height()
                * ((y - mCurrentViewport.top) / mCurrentViewport.height());
    }

    //END ADAPTOR


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and objects related to gesture handling
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
                        * (y - mContentRect.top) / mContentRect.height());
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case (MotionEvent.ACTION_DOWN):
//                Utils.e("ACTION_DOWN");
                FLAG_DOWN = true;
                _downEventX = event.getX();
                _downEventY = event.getY();
                break;
            case (MotionEvent.ACTION_MOVE):
//                Utils.e("ACTION_MOVE");
                FLAG_MOVE = true;
                _moveEventX = event.getX();
                _moveEventY = event.getY();
                handleOnMove(event);
                break;
            case (MotionEvent.ACTION_UP):
//                Utils.e("ACTION_UP");
                FLAG_UP = true;
                int index = getIndexy(objectData.getAxisy());
                if (index != -1) {
                    objectData.fixBlock(yStopsBuffer.stops[index]);
                }
                objectData.setFlagMove(false);
                invalidate();
                releaseFlagTouch();
                break;
            default:
                break;
        }

        boolean retVal = scaleGestureDetector.onTouchEvent(event);
//        if (!scaleGestureDetector.isInProgress()) {
        retVal = gestureDetector.onTouchEvent(event) || retVal;
//        }

        return retVal || super.onTouchEvent(event);
    }

    public void handleOnPress(MotionEvent event) {
        if (objectData.isTouched(_downEventY) == false) {
            objectData.setHeight(getBlockHeight());
            objectData.setAxisy(_downEventY);

            int index = getIndexy(objectData.getAxisy());
            if (index != -1)
                objectData.fixBlock(yStopsBuffer.stops[index]);

        } else {
            objectData.releaseObj();
        }
        invalidate();
    }

    private void handleOnMove(MotionEvent event) {
        if (FLAG_SHOWPRESS) {
            float axisytmp = Math.max(_moveEventY - _deltaMove, mContentRect.top);
            objectData.setAxisy(Math.min(axisytmp, mContentRect.bottom - objectData.getHeight()));
//            objectData.setAxisy(_moveEventY - _deltaMove);
        } else {
            objectData.setFlagMove(false);
        }
        invalidate();
    }


    private void releaseFlagTouch() {
        FLAG_DOWN = FLAG_MOVE = FLAG_SCROLL = FLAG_SCALE = FLAG_UP = FLAG_FLING = FLAG_DOUBLETAP = FLAG_LONGPRESS = FLAG_SHOWPRESS = false;
    }

    /**
     * The scale listener, used for handling multi-finger scale gestures.
     */
    private final ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener
            = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private PointF viewportFocus = new PointF();
        private float lastSpanX;
        private float lastSpanY;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            lastSpanX = ScaleGestureDetectorCompat.getCurrentSpanX(scaleGestureDetector);
            lastSpanY = ScaleGestureDetectorCompat.getCurrentSpanY(scaleGestureDetector);
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            float spanX = ScaleGestureDetectorCompat.getCurrentSpanX(scaleGestureDetector);
            float spanY = ScaleGestureDetectorCompat.getCurrentSpanY(scaleGestureDetector);
//            Utils.e("ACTION_SCALE");
            FLAG_SCALE = true;
            _spanX = spanX;
            _spanY = spanY;
            float newWidth = lastSpanX / spanX * mCurrentViewport.width();
            float newHeight = lastSpanY / spanY * mCurrentViewport.height();

            float focusX = scaleGestureDetector.getFocusX();
            float focusY = scaleGestureDetector.getFocusY();
            hitTest(focusX, focusY, viewportFocus);

            float viewportLeft = viewportFocus.x - newWidth * (focusX - mContentRect.left) / mContentRect.width();
            float viewportTop = viewportFocus.y - newHeight * (focusY - mContentRect.top) / mContentRect.height();
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
            ViewCompat.postInvalidateOnAnimation(TimeView.this);
            lastSpanX = spanX;
            lastSpanY = spanY;
            return true;
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
            scrollerStartViewport.set(mCurrentViewport);
            mScroller.forceFinished(true);
            ViewCompat.postInvalidateOnAnimation(TimeView.this);
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
//            Utils.e("onSingleTapUp");
            handleOnPress(e);
            invalidate();
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
//            Utils.e("onShowPress");
            if (objectData.isTouched(e.getY())) {
                FLAG_SHOWPRESS = true;
                _showPressX = e.getX();
                _showPressY = e.getY();
                _deltaMove = _showPressY - getDrawY(objectData.getAxisy());
                objectData.setFlagMove(true);
                invalidate();
            }
        }

        @Override
        public void onLongPress(MotionEvent e) {
//            Utils.e("ACTION_LONGPRESS");
            if (objectData.isTouched(e.getY())) {
                FLAG_LONGPRESS = true;
                _longPressX = e.getX();
                _longPressY = e.getY();
            }
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
//            Utils.e("ACTION_DOUBLETAP");
            FLAG_DOUBLETAP = true;
            //Disable double tap
            if (FLAG_DOUBLETAP) return false;
            mZoomer.forceFinished(true);
            if (hitTest(e.getX(), e.getY(), zoomFocalPoint)) {
                mZoomer.startZoom(ZOOM_AMOUNT);
            }
            ViewCompat.postInvalidateOnAnimation(TimeView.this);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            Utils.e("ACTION_SCROLL");
            FLAG_SCROLL = true;
            FLAG_LONGPRESS = FLAG_SHOWPRESS = false;
            _distanceX = distanceX;
            _distanceY = distanceY;
            float viewportOffsetX = distanceX * mCurrentViewport.width() / mContentRect.width();
            float viewportOffsetY = distanceY * mCurrentViewport.height() / mContentRect.height();
            computeScrollSurfaceSize(mSurfaceSizeBuffer);
            int scrolledX = (int) (mSurfaceSizeBuffer.x
                    * (mCurrentViewport.left + viewportOffsetX - AXIS_X_MIN)
                    / (AXIS_X_MAX - AXIS_X_MIN));
            int scrolledY = (int) (mSurfaceSizeBuffer.y
                    * (mCurrentViewport.top + viewportOffsetY - AXIS_Y_MIN)
                    / (AXIS_Y_MAX - AXIS_Y_MIN));
            boolean canScrollX = mCurrentViewport.left > AXIS_X_MIN
                    || mCurrentViewport.right < AXIS_X_MAX;
            boolean canScrollY = mCurrentViewport.top > AXIS_Y_MIN
                    || mCurrentViewport.bottom < AXIS_Y_MAX;
            setViewportTopLeft(
                    mCurrentViewport.left + viewportOffsetX,
                    mCurrentViewport.top + viewportOffsetY);

            if (canScrollX && scrolledX < 0) {
                edgeEffectLeft.onPull(scrolledX / (float) mContentRect.width());
                edgeEffectLeftActive = true;
            }
            if (canScrollY && scrolledY < 0) {
                edgeEffectTop.onPull(scrolledY / (float) mContentRect.height());
                edgeEffectTopActive = true;
            }
            if (canScrollX && scrolledX > mSurfaceSizeBuffer.x - mContentRect.width()) {
                edgeEffectRight.onPull((scrolledX - mSurfaceSizeBuffer.x + mContentRect.width())
                        / (float) mContentRect.width());
                edgeEffectRightActive = true;
            }
            if (canScrollY && scrolledY > mSurfaceSizeBuffer.y - mContentRect.height()) {
                edgeEffectBottom.onPull((scrolledY - mSurfaceSizeBuffer.y + mContentRect.height())
                        / (float) mContentRect.height());
                edgeEffectBottomActive = true;
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            Utils.e("ACTION_FLING");
            FLAG_FLING = true;
            _velocityX = velocityX;
            _velocityY = velocityY;
            fling((int) -velocityX, (int) -velocityY);
            return true;
        }
    };

    private void releaseEdgeEffects() {
        edgeEffectLeftActive
                = edgeEffectTopActive
                = edgeEffectRightActive
                = edgeEffectBottomActive
                = false;
        edgeEffectLeft.onRelease();
        edgeEffectTop.onRelease();
        edgeEffectRight.onRelease();
        edgeEffectBottom.onRelease();
    }

    private void fling(int velocityX, int velocityY) {
        releaseEdgeEffects();
        computeScrollSurfaceSize(mSurfaceSizeBuffer);
        scrollerStartViewport.set(mCurrentViewport);
        int startX = (int) (mSurfaceSizeBuffer.x * (scrollerStartViewport.left - AXIS_X_MIN) / (
                AXIS_X_MAX - AXIS_X_MIN));
        int startY = (int) (mSurfaceSizeBuffer.y * (scrollerStartViewport.top - AXIS_Y_MIN) / (
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
            computeScrollSurfaceSize(mSurfaceSizeBuffer);
            int currX = mScroller.getCurrX();
            int currY = mScroller.getCurrY();

            boolean canScrollX = (mCurrentViewport.left > AXIS_X_MIN
                    || mCurrentViewport.right < AXIS_X_MAX);
            boolean canScrollY = (mCurrentViewport.top > AXIS_Y_MIN
                    || mCurrentViewport.bottom < AXIS_Y_MAX);

            if (canScrollX
                    && currX < 0
                    && edgeEffectLeft.isFinished()
                    && !edgeEffectLeftActive) {
                edgeEffectLeft.onAbsorb((int) OverScrollerCompat.getCurrVelocity(mScroller));
                edgeEffectLeftActive = true;
                needsInvalidate = true;
            } else if (canScrollX
                    && currX > (mSurfaceSizeBuffer.x - mContentRect.width())
                    && edgeEffectRight.isFinished()
                    && !edgeEffectRightActive) {
                edgeEffectRight.onAbsorb((int) OverScrollerCompat.getCurrVelocity(mScroller));
                edgeEffectRightActive = true;
                needsInvalidate = true;
            }

            if (canScrollY
                    && currY < 0
                    && edgeEffectTop.isFinished()
                    && !edgeEffectTopActive) {
                edgeEffectTop.onAbsorb((int) OverScrollerCompat.getCurrVelocity(mScroller));
                edgeEffectTopActive = true;
                needsInvalidate = true;
            } else if (canScrollY
                    && currY > (mSurfaceSizeBuffer.y - mContentRect.height())
                    && edgeEffectBottom.isFinished()
                    && !edgeEffectBottomActive) {
                edgeEffectBottom.onAbsorb((int) OverScrollerCompat.getCurrVelocity(mScroller));
                edgeEffectBottomActive = true;
                needsInvalidate = true;
            }

            float currXRange = AXIS_X_MIN + (AXIS_X_MAX - AXIS_X_MIN)
                    * currX / mSurfaceSizeBuffer.x;
            float currYRange = AXIS_Y_MIN + (AXIS_Y_MAX - AXIS_Y_MIN)
                    * currY / mSurfaceSizeBuffer.y;
            setViewportTopLeft(currXRange, currYRange);
        }

        if (mZoomer.computeZoom()) {
            // Performs the zoom since a zoom is in progress (either programmatically or via
            // double-touch).
            float newWidth = (1f - mZoomer.getCurrZoom()) * scrollerStartViewport.width();
            float newHeight = (1f - mZoomer.getCurrZoom()) * scrollerStartViewport.height();
            float pointWithinViewportX = (zoomFocalPoint.x - scrollerStartViewport.left)
                    / scrollerStartViewport.width();
            float pointWithinViewportY = (zoomFocalPoint.y - scrollerStartViewport.top)
                    / scrollerStartViewport.height();

            float viewportLeft = zoomFocalPoint.x - newWidth * pointWithinViewportX;
            float viewportTop = zoomFocalPoint.y - newHeight * pointWithinViewportY;
            float viewportRight = zoomFocalPoint.x + newWidth * (1 - pointWithinViewportX);
            float viewportBottom = zoomFocalPoint.y + newHeight * (1 - pointWithinViewportY);
            // neu do rong cell lon hon do rong dinh nghia thi khong cho phong to nua
            if (isScaleYAvailable(viewportBottom, viewportTop)) {
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

    private void setViewportTopLeft(float x, float y) {
        float curWidth = mCurrentViewport.width();
        float curHeight = mCurrentViewport.height();
        x = Math.max(AXIS_X_MIN, Math.min(x, AXIS_X_MAX - curWidth));
        y = Math.max(AXIS_Y_MIN, Math.min(y, AXIS_Y_MAX - curHeight));
        mCurrentViewport.set(x, y, x + curWidth, y + curHeight);
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
    public void zoomMaximum() {
        scrollerStartViewport.set(mCurrentViewport);
        mZoomer.forceFinished(true);
        mZoomer.startZoom(ZOOM_AMOUNT * 3f);
        zoomFocalPoint.set(
                (mCurrentViewport.right + mCurrentViewport.left) / 2,
                (mCurrentViewport.bottom + mCurrentViewport.top) / 2);
        ViewCompat.postInvalidateOnAnimation(this);
    }


    /**
     * Smoothly zooms the chart in one step.
     */
    public void zoomIn() {
        scrollerStartViewport.set(mCurrentViewport);
        mZoomer.forceFinished(true);
        mZoomer.startZoom(ZOOM_AMOUNT);
        zoomFocalPoint.set(
                (mCurrentViewport.right + mCurrentViewport.left) / 2,
                (mCurrentViewport.bottom + mCurrentViewport.top) / 2);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * Smoothly zooms the chart out one step.
     */
    public void zoomOut() {
        scrollerStartViewport.set(mCurrentViewport);
        mZoomer.forceFinished(true);
        mZoomer.startZoom(-ZOOM_AMOUNT);
        zoomFocalPoint.set(
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

    public float getLabelHeaderColTextSize() {
        return labelHeaderColTextSize;
    }

    public void setLabelHeaderColTextSize(float labelHeaderColTextSize) {
        this.labelHeaderColTextSize = labelHeaderColTextSize;
        initPaints();
        ViewCompat.postInvalidateOnAnimation(this);
    }


    public float getGridThickness() {
        return gridThickness;
    }

    public void setGridThickness(float gridThickness) {
        this.gridThickness = gridThickness;
        initPaints();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public int getGridColor() {
        return gridColor;
    }

    public void setGridColor(int gridColor) {
        this.gridColor = gridColor;
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
        mCurrentViewport = ss.viewport;
        super.onRestoreInstanceState(ss.getSuperState());
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
            return "TimeView.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " viewport=" + viewport.toString() + "}";
        }

        public static final Creator<SavedState> CREATOR
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


/**
 * A simple class that animates double-touch zoom gestures. Functionally similar to a {@link
 * android.widget.Scroller}.
 */
class Zoomer {
    /**
     * The interpolator, used for making zooms animate 'naturally.'
     */
    private Interpolator mInterpolator;

    /**
     * The total animation duration for a zoom.
     */
    private int mAnimationDurationMillis;

    /**
     * Whether or not the current zoom has finished.
     */
    private boolean mFinished = true;

    /**
     * The current zoom value; computed by {@link #computeZoom()}.
     */
    private float mCurrentZoom;

    /**
     * The time the zoom started, computed using {@link SystemClock#elapsedRealtime()}.
     */
    private long mStartRTC;

    /**
     * The destination zoom factor.
     */
    private float mEndZoom;

    public Zoomer(Context context) {
        mInterpolator = new DecelerateInterpolator();
        mAnimationDurationMillis = context.getResources().getInteger(
                android.R.integer.config_shortAnimTime);
    }

    /**
     * Forces the zoom finished state to the given value. Unlike {@link #abortAnimation()}, the
     * current zoom value isn't set to the ending value.
     *
     * @see android.widget.Scroller#forceFinished(boolean)
     */
    public void forceFinished(boolean finished) {
        mFinished = finished;
    }

    /**
     * Aborts the animation, setting the current zoom value to the ending value.
     *
     * @see android.widget.Scroller#abortAnimation()
     */
    public void abortAnimation() {
        mFinished = true;
        mCurrentZoom = mEndZoom;
    }

    /**
     * Starts a zoom from 1.0 to (1.0 + endZoom). That is, to zoom from 100% to 125%, endZoom should
     * by 0.25f.
     *
     * @see android.widget.Scroller#startScroll(int, int, int, int)
     */
    public void startZoom(float endZoom) {
        mStartRTC = SystemClock.elapsedRealtime();
        mEndZoom = endZoom;

        mFinished = false;
        mCurrentZoom = 1f;
    }

    /**
     * Computes the current zoom level, returning true if the zoom is still active and false if the
     * zoom has finished.
     *
     * @see android.widget.Scroller#computeScrollOffset()
     */
    public boolean computeZoom() {
        if (mFinished) {
            return false;
        }

        long tRTC = SystemClock.elapsedRealtime() - mStartRTC;
        if (tRTC >= mAnimationDurationMillis) {
            mFinished = true;
            mCurrentZoom = mEndZoom;
            return false;
        }

        float t = tRTC * 1f / mAnimationDurationMillis;
        mCurrentZoom = mEndZoom * mInterpolator.getInterpolation(t);
        return true;
    }

    /**
     * Returns the current zoom level.
     *
     * @see android.widget.Scroller#getCurrX()
     */
    public float getCurrZoom() {
        return mCurrentZoom;
    }
}

/**
 * A utility class for using {@link OverScroller} in a backward-compatible fashion.
 */
class OverScrollerCompat {
    /**
     * Disallow instantiation.
     */
    private OverScrollerCompat() {
    }

    /**
     * @see ScaleGestureDetector#getCurrentSpanY()
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static float getCurrVelocity(OverScroller overScroller) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return overScroller.getCurrVelocity();
        } else {
            return 0;
        }
    }
}

/**
 * A utility class for using {@link ScaleGestureDetector} in a backward-compatible
 * fashion.
 */
class ScaleGestureDetectorCompat {
    /**
     * Disallow instantiation.
     */
    private ScaleGestureDetectorCompat() {
    }

    /**
     * @see ScaleGestureDetector#getCurrentSpanX()
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static float getCurrentSpanX(ScaleGestureDetector scaleGestureDetector) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return scaleGestureDetector.getCurrentSpanX();
        } else {
            return scaleGestureDetector.getCurrentSpan();
        }
    }

    /**
     * @see ScaleGestureDetector#getCurrentSpanY()
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static float getCurrentSpanY(ScaleGestureDetector scaleGestureDetector) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return scaleGestureDetector.getCurrentSpanY();
        } else {
            return scaleGestureDetector.getCurrentSpan();
        }
    }
}

/**
 * ObjectData class
 */
class ObjectData {
    private static final String TAG = "ObjectData";
    private static ObjectData objectData = null;
    private float height = 0, axisx = -1, axisy = -2;
    private float dataThickness = 1;
    private String dataColor = "#f6921e";
    private Paint dataPaint;

    private TimeView timeView;
    private Paint movePaint;
    private boolean flagMove = false;

    public static ObjectData getInstance(TimeView timeView) {
        if (objectData == null) return new ObjectData(timeView);
        return objectData;
    }

    private ObjectData(TimeView timeView) {
        this.timeView = timeView;
        initPaint();
    }

    private void initPaint() {

        dataPaint = new Paint();
        dataPaint.setStrokeWidth(dataThickness);
        dataPaint.setColor(Color.parseColor(dataColor));
        dataPaint.setStyle(Paint.Style.FILL);
        dataPaint.setAntiAlias(true);
        dataPaint.setAlpha(50);

        movePaint = new Paint();
        movePaint.setStrokeWidth(dataThickness * 2);
        movePaint.setColor(Color.parseColor(dataColor));
        movePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        movePaint.setAntiAlias(true);
        movePaint.setAlpha(100);
        movePaint.setStrokeJoin(Paint.Join.ROUND);
        timeView.setLayerType(View.LAYER_TYPE_SOFTWARE, movePaint);
        movePaint.setShadowLayer(5.0f, -5.0f, 5.0f, Color.BLACK);
    }

    public void Draw(Canvas canvas) {
        if (isCreated()) {
            timeView.mainActivity.updateHeaderTime(true);
//            int clipRestoreCount = canvas.save();
            canvas.drawRect(TimeView.mContentRect.left, Math.max(getDrawY(axisy), TimeView.mContentRect.top), TimeView.mContentRect.right, Math.min(getDrawY(axisy) + height, TimeView.mContentRect.bottom), dataPaint);
            if (flagMove) {
                canvas.drawRect(TimeView.mContentRect.left, Math.max(getDrawY(axisy), TimeView.mContentRect.top), TimeView.mContentRect.right, Math.min(getDrawY(axisy) + height, TimeView.mContentRect.bottom), movePaint);
            }
//            canvas.restoreToCount(clipRestoreCount);
        }else{
            timeView.mainActivity.updateHeaderTime(false);
        }
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    private float getDrawY(float y) {
        return TimeView.mContentRect.top
                + TimeView.mContentRect.height()
                * ((y - TimeView.mCurrentViewport.top) / TimeView.mCurrentViewport.height());
    }

    public void setAxisy(float eventY) {
        this.axisy = TimeView.mCurrentViewport.top + (eventY - TimeView.mContentRect.top) / TimeView.mContentRect.height() * TimeView.mCurrentViewport.height();
    }

    public boolean getFlagMove() {
        return flagMove;
    }

    public void setFlagMove(boolean move) {
        flagMove = move;
    }

    public boolean isTouched(float eventY) {
        if (isCreated()) {
            float drawY = getDrawY(axisy);
            if (eventY >= drawY && eventY <= (drawY + height)) return true;
        }
//        Log.e(TAG, "isTouched=false");
        return false;
    }

    public void releaseObj() {
        height = axisx = axisy = 0;
    }

    public float getAxisy() {
        return axisy;
    }

    public boolean isCreated() {
        if (height > 0 && axisy != -2) {
//            Log.e(TAG, "isCreated");
            return true;
        }
        return false;
    }

    public void fixBlock(float y) {
        axisy = y;
    }
}

