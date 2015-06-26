package cvnhan.android.calendarsample.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Created by cvnhan on 26-Jun-15.
 */
public class SwipeDectectLayout extends ViewGroup {

    private static final String LOG_TAG = SwipeDectectLayout.class.getSimpleName();
    private View mTarget;
    private SwipeDectectLayout.OnRefreshListener mListener;
    private boolean mRefreshing;
    private int mTouchSlop;
    private float mTotalDragDistance;
    private int mMediumAnimationDuration;
    private int mCurrentTargetOffsetTop;
    private boolean mOriginalOffsetCalculated;
    private float mInitialMotionX;
    private float mInitialDownX;
    private boolean mIsBeingDragged;
    private int mActivePointerId;
    private boolean mScale;
    private boolean mReturningToStart;
    private final DecelerateInterpolator mDecelerateInterpolator;
    private static final int[] LAYOUT_ATTRS = new int[]{16842766};
    private CircleImageView mCircleView;
    private int mCircleViewIndex;
    protected int mFrom;
    private float mStartingScale;
    protected int mOriginalOffsetTop;
    private MaterialProgressDrawable mProgress;
    private Animation mScaleAnimation;
    private Animation mScaleDownAnimation;
    private Animation mAlphaStartAnimation;
    private Animation mAlphaMaxAnimation;
    private Animation mScaleDownToStartAnimation;
    private float mSpinnerFinalOffset;
    private boolean mNotify;
    private int mCircleWidth;
    private int mCircleHeight;
    private boolean mUsingCustomStart;
    private Animation.AnimationListener mRefreshListener;
    private int directionSwipe = 0; //0 left-right, 1 right-left;
    private final Animation mAnimateToCorrectPosition;
    private final Animation mAnimateToStartPosition;

    private void setColorViewAlpha(int targetAlpha) {
        this.mCircleView.getBackground().setAlpha(targetAlpha);
        this.mProgress.setAlpha(targetAlpha);
    }

    public void setProgressViewOffset(boolean scale, int start, int end) {
        this.mScale = scale;
        this.mCircleView.setVisibility(View.VISIBLE);
        this.mOriginalOffsetTop = this.mCurrentTargetOffsetTop = start;
        this.mSpinnerFinalOffset = (float) end;
        this.mUsingCustomStart = true;
        this.mCircleView.invalidate();
    }

    public void setProgressViewEndTarget(boolean scale, int end) {
        this.mSpinnerFinalOffset = (float) end;
        this.mScale = scale;
        this.mCircleView.invalidate();
    }

    public void setSize(int size) {
        if (size == 0 || size == 1) {
            DisplayMetrics metrics = this.getResources().getDisplayMetrics();
            if (size == 0) {
                this.mCircleHeight = this.mCircleWidth = (int) (56.0F * metrics.density);
            } else {
                this.mCircleHeight = this.mCircleWidth = (int) (40.0F * metrics.density);
            }

            this.mCircleView.setImageDrawable((Drawable) null);
            this.mProgress.updateSizes(size);
            this.mCircleView.setImageDrawable(this.mProgress);
        }
    }

    public SwipeDectectLayout(Context context) {
        this(context, (AttributeSet) null);
    }

    public SwipeDectectLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mRefreshing = false;
        this.mTotalDragDistance = -1.0F;
        this.mOriginalOffsetCalculated = false;
        this.mActivePointerId = -1;
        this.mCircleViewIndex = -1;
        this.mRefreshListener = new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (SwipeDectectLayout.this.mRefreshing) {
                    SwipeDectectLayout.this.mProgress.setAlpha(255);
                    SwipeDectectLayout.this.mProgress.start();
                    if (SwipeDectectLayout.this.mNotify && SwipeDectectLayout.this.mListener != null) {
                        if (directionSwipe == 0)
                            SwipeDectectLayout.this.mListener.onLefttoRight();
                        else
                            SwipeDectectLayout.this.mListener.onRighttoLeft();
                    }
                } else {
                    SwipeDectectLayout.this.mProgress.stop();
                    SwipeDectectLayout.this.mCircleView.setVisibility(View.VISIBLE);
                    SwipeDectectLayout.this.setColorViewAlpha(255);
                    if (SwipeDectectLayout.this.mScale) {
                        SwipeDectectLayout.this.setAnimationProgress(0.0F);
                    } else {
                        SwipeDectectLayout.this.setTargetOffsetTopAndBottom(SwipeDectectLayout.this.mOriginalOffsetTop - SwipeDectectLayout.this.mCurrentTargetOffsetTop, true);
                    }
                }

                SwipeDectectLayout.this.mCurrentTargetOffsetTop = SwipeDectectLayout.this.mCircleView.getTop();
            }
        };

        this.mAnimateToCorrectPosition = new Animation() {
            public void applyTransformation(float interpolatedTime, Transformation t) {
                boolean targetTop = false;
                boolean endTarget = false;
                int endTarget1;
                if (!SwipeDectectLayout.this.mUsingCustomStart) {
                    endTarget1 = (int) (SwipeDectectLayout.this.mSpinnerFinalOffset - (float) Math.abs(SwipeDectectLayout.this.mOriginalOffsetTop));
                } else {
                    endTarget1 = (int) SwipeDectectLayout.this.mSpinnerFinalOffset;
                }

                int targetTop1 = SwipeDectectLayout.this.mFrom + (int) ((float) (endTarget1 - SwipeDectectLayout.this.mFrom) * interpolatedTime);
                int offset = targetTop1 - SwipeDectectLayout.this.mCircleView.getTop();
                SwipeDectectLayout.this.setTargetOffsetTopAndBottom(offset, false);
                SwipeDectectLayout.this.mProgress.setArrowScale(1.0F - interpolatedTime);
            }
        };
        this.mAnimateToStartPosition = new Animation() {
            public void applyTransformation(float interpolatedTime, Transformation t) {
                SwipeDectectLayout.this.moveToStart(interpolatedTime);
            }
        };
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mMediumAnimationDuration = 2;
        this.setWillNotDraw(false);
        this.mDecelerateInterpolator = new DecelerateInterpolator(2.0F);
        TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        this.setEnabled(a.getBoolean(0, true));
        a.recycle();
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        this.mCircleWidth = (int) (40.0F * metrics.density);
        this.mCircleHeight = (int) (40.0F * metrics.density);
        this.createProgressView();
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        this.mSpinnerFinalOffset = 64.0F * metrics.density;
        this.mTotalDragDistance = this.mSpinnerFinalOffset;
    }

    protected int getChildDrawingOrder(int childCount, int i) {
        return this.mCircleViewIndex < 0 ? i : (i == childCount - 1 ? this.mCircleViewIndex : (i >= this.mCircleViewIndex ? i + 1 : i));
    }

    private void createProgressView() {
        this.mCircleView = new CircleImageView(this.getContext(), -328966, 20.0F);
        this.mProgress = new MaterialProgressDrawable(this.getContext(), this);
        this.mProgress.setBackgroundColor(-328966);
        this.mCircleView.setImageDrawable(this.mProgress);
        this.mCircleView.setVisibility(View.VISIBLE);
        this.addView(this.mCircleView);
    }

    public void setOnRefreshListener(SwipeDectectLayout.OnRefreshListener listener) {
        this.mListener = listener;
    }

    private boolean isAlphaUsedForScale() {
        return Build.VERSION.SDK_INT < 11;
    }

    public void setRefreshing(boolean refreshing) {
        if (refreshing && this.mRefreshing != refreshing) {
            this.mRefreshing = refreshing;
            boolean endTarget = false;
            int endTarget1;
            if (!this.mUsingCustomStart) {
                endTarget1 = (int) (this.mSpinnerFinalOffset + (float) this.mOriginalOffsetTop);
            } else {
                endTarget1 = (int) this.mSpinnerFinalOffset;
            }

            this.setTargetOffsetTopAndBottom(endTarget1 - this.mCurrentTargetOffsetTop, true);
            this.mNotify = false;
            this.startScaleUpAnimation(this.mRefreshListener);
        } else {
            this.setRefreshing(refreshing, false);
        }

    }

    private void startScaleUpAnimation(Animation.AnimationListener listener) {
        this.mCircleView.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= 11) {
            this.mProgress.setAlpha(255);
        }

        this.mScaleAnimation = new Animation() {
            public void applyTransformation(float interpolatedTime, Transformation t) {
                SwipeDectectLayout.this.setAnimationProgress(interpolatedTime);
            }
        };
        this.mScaleAnimation.setDuration((long) this.mMediumAnimationDuration);
        if (listener != null) {
            this.mCircleView.setAnimationListener(listener);
        }

        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(this.mScaleAnimation);
    }

    private void setAnimationProgress(float progress) {
        if (this.isAlphaUsedForScale()) {
            this.setColorViewAlpha((int) (progress * 255.0F));
        } else {
            ViewCompat.setScaleX(this.mCircleView, progress);
            ViewCompat.setScaleY(this.mCircleView, progress);
        }

    }

    private void setRefreshing(boolean refreshing, boolean notify) {
        if (this.mRefreshing != refreshing) {
            this.mNotify = notify;
            this.ensureTarget();
            this.mRefreshing = refreshing;
            if (this.mRefreshing) {
                this.animateOffsetToCorrectPosition(this.mCurrentTargetOffsetTop, this.mRefreshListener);
            } else {
                this.startScaleDownAnimation(this.mRefreshListener);
            }
        }

    }

    private void startScaleDownAnimation(Animation.AnimationListener listener) {
        this.mScaleDownAnimation = new Animation() {
            public void applyTransformation(float interpolatedTime, Transformation t) {
                SwipeDectectLayout.this.setAnimationProgress(1.0F - interpolatedTime);
            }
        };
        this.mScaleDownAnimation.setDuration(150L);
        this.mCircleView.setAnimationListener(listener);
        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(this.mScaleDownAnimation);
    }

    private void startProgressAlphaStartAnimation() {
        this.mAlphaStartAnimation = this.startAlphaAnimation(this.mProgress.getAlpha(), 76);
    }

    private void startProgressAlphaMaxAnimation() {
        this.mAlphaMaxAnimation = this.startAlphaAnimation(this.mProgress.getAlpha(), 255);
    }

    private Animation startAlphaAnimation(final int startingAlpha, final int endingAlpha) {
        if (this.mScale && this.isAlphaUsedForScale()) {
            return null;
        } else {
            Animation alpha = new Animation() {
                public void applyTransformation(float interpolatedTime, Transformation t) {
                    SwipeDectectLayout.this.mProgress.setAlpha((int) ((float) startingAlpha + (float) (endingAlpha - startingAlpha) * interpolatedTime));
                }
            };
            alpha.setDuration(300L);
            this.mCircleView.setAnimationListener((Animation.AnimationListener) null);
            this.mCircleView.clearAnimation();
            this.mCircleView.startAnimation(alpha);
            return alpha;
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setProgressBackgroundColor(int colorRes) {
        this.setProgressBackgroundColorSchemeResource(colorRes);
    }

    public void setProgressBackgroundColorSchemeResource(int colorRes) {
        this.setProgressBackgroundColorSchemeColor(this.getResources().getColor(colorRes));
    }

    public void setProgressBackgroundColorSchemeColor(int color) {
        this.mCircleView.setBackgroundColor(color);
        this.mProgress.setBackgroundColor(color);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setColorScheme(int... colors) {
        this.setColorSchemeResources(colors);
    }

    public void setColorSchemeResources(int... colorResIds) {
        Resources res = this.getResources();
        int[] colorRes = new int[colorResIds.length];

        for (int i = 0; i < colorResIds.length; ++i) {
            colorRes[i] = res.getColor(colorResIds[i]);
        }

        this.setColorSchemeColors(colorRes);
    }

    public void setColorSchemeColors(int... colors) {
        this.ensureTarget();
        this.mProgress.setColorSchemeColors(colors);
    }

    public boolean isRefreshing() {
        return this.mRefreshing;
    }

    private void ensureTarget() {
        if (this.mTarget == null) {
            for (int i = 0; i < this.getChildCount(); ++i) {
                View child = this.getChildAt(i);
                if (!child.equals(this.mCircleView)) {
                    this.mTarget = child;
                    break;
                }
            }
        }

    }

    public void setDistanceToTriggerSync(int distance) {
        this.mTotalDragDistance = (float) distance;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = this.getMeasuredWidth();
        int height = this.getMeasuredHeight();
        if (this.getChildCount() != 0) {
            if (this.mTarget == null) {
                this.ensureTarget();
            }

            if (this.mTarget != null) {
                View child = this.mTarget;
                int childLeft = this.getPaddingLeft();
                int childTop = this.getPaddingTop();
                int childWidth = width - this.getPaddingLeft() - this.getPaddingRight();
                int childHeight = height - this.getPaddingTop() - this.getPaddingBottom();
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
                int circleWidth = this.mCircleView.getMeasuredWidth();
                int circleHeight = this.mCircleView.getMeasuredHeight();
                this.mCircleView.layout(width / 2 - circleWidth / 2, this.mCurrentTargetOffsetTop, width / 2 + circleWidth / 2, this.mCurrentTargetOffsetTop + circleHeight);
            }
        }
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mTarget == null) {
            this.ensureTarget();
        }

        if (this.mTarget != null) {
            this.mTarget.measure(MeasureSpec.makeMeasureSpec(this.getMeasuredWidth() - this.getPaddingLeft() - this.getPaddingRight(), 1073741824), MeasureSpec.makeMeasureSpec(this.getMeasuredHeight() - this.getPaddingTop() - this.getPaddingBottom(), 1073741824));
            this.mCircleView.measure(MeasureSpec.makeMeasureSpec(this.mCircleWidth, 1073741824), MeasureSpec.makeMeasureSpec(this.mCircleHeight, 1073741824));
            if (!this.mUsingCustomStart && !this.mOriginalOffsetCalculated) {
                this.mOriginalOffsetCalculated = true;
                this.mCurrentTargetOffsetTop = this.mOriginalOffsetTop = -this.mCircleView.getMeasuredHeight();
            }

            this.mCircleViewIndex = -1;

            for (int index = 0; index < this.getChildCount(); ++index) {
                if (this.getChildAt(index) == this.mCircleView) {
                    this.mCircleViewIndex = index;
                    break;
                }
            }

        }
    }

    public int getProgressCircleDiameter() {
        return this.mCircleView != null ? this.mCircleView.getMeasuredHeight() : 0;
    }

    public boolean canChildScrollUp() {
        if (Build.VERSION.SDK_INT >= 14) {
            return ViewCompat.canScrollVertically(this.mTarget, -1);
        } else if (this.mTarget instanceof AbsListView) {
            AbsListView absListView = (AbsListView) this.mTarget;
            return absListView.getChildCount() > 0 && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0).getTop() < absListView.getPaddingTop());
        } else {
            return ViewCompat.canScrollVertically(this.mTarget, -1) || this.mTarget.getScrollY() > 0;
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        this.ensureTarget();
        int action = MotionEventCompat.getActionMasked(ev);
        if (this.mReturningToStart && action == 0) {
            this.mReturningToStart = false;
        }

//        if(this.isEnabled() && !this.mReturningToStart && !this.canChildScrollUp() && !this.mRefreshing) {
        if (this.isEnabled() && !this.mReturningToStart && !this.mRefreshing) {
            switch (action) {
                case 0:
                    this.setTargetOffsetTopAndBottom(this.mOriginalOffsetTop - this.mCircleView.getTop(), true);
                    this.mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    this.mIsBeingDragged = false;
                    float initialDownX = this.getMotionEventX(ev, this.mActivePointerId);
                    if (initialDownX == -1.0F) {
                        return false;
                    }
                    this.mInitialDownX = initialDownX;

                    break;
                case 1:
                case 3:
                    this.mIsBeingDragged = false;
                    this.mActivePointerId = -1;
                    break;
                case 2:
                    if (this.mActivePointerId == -1) {
                        Log.e(LOG_TAG, "Got ACTION_MOVE event but don\'t have an active pointer id.");
                        return false;
                    }

                    float x = this.getMotionEventX(ev, this.mActivePointerId);
                    if (x == -1.0F) {
                        return false;
                    }

                    if (originalWidth == 0) {
                        originalWidth = getWidth();
                        mCompressedParams = new LinearLayout.LayoutParams(
                                originalWidth, LinearLayout.LayoutParams.MATCH_PARENT);
                        setLayoutParams(mCompressedParams);
                    }
                    float xDiff = x - this.mInitialDownX;
                    if (Math.abs(xDiff) > (float) this.mTouchSlop && !this.mIsBeingDragged) {
                        if (xDiff > 0) {
                            this.mInitialMotionX = this.mInitialDownX + (float) this.mTouchSlop;
                            this.directionSwipe = 0;
                        } else {
                            this.mInitialMotionX = (this.mInitialDownX + (float) this.mTouchSlop) * -1;
                            this.directionSwipe = 1;
                        }
                        this.mIsBeingDragged = true;
                        this.mProgress.setAlpha(76);
                    }
                case 4:
                case 5:
                default:
                    break;
                case 6:
                    this.onSecondaryPointerUp(ev);
            }

            return this.mIsBeingDragged;
        } else {
            return false;
        }
    }


    private float getMotionEventX(MotionEvent ev, int activePointerId) {
        int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        return index < 0 ? -1.0F : MotionEventCompat.getX(ev, index);
    }

    public void requestDisallowInterceptTouchEvent(boolean b) {
    }

    private boolean isAnimationRunning(Animation animation) {
        return animation != null && animation.hasStarted() && !animation.hasEnded();
    }

    public boolean onTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        if (this.mReturningToStart && action == 0) {
            this.mReturningToStart = false;
        }
//        if(this.isEnabled() && !this.mReturningToStart && !this.canChildScrollUp()) {
        if (this.isEnabled() && !this.mReturningToStart) {
            int pointerIndex;
            float x;
            float overscrollTop;
            switch (action) {
                case 0:
                    this.mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    this.mIsBeingDragged = false;
                    break;
                case 1:
                case 3:
                    if (this.mActivePointerId == -1) {
                        if (action == 1) {
                            Log.e(LOG_TAG, "Got ACTION_UP event but don\'t have an active pointer id.");
                        }

                        return false;
                    }

                    pointerIndex = MotionEventCompat.findPointerIndex(ev, this.mActivePointerId);
                    x = MotionEventCompat.getX(ev, pointerIndex);
                    overscrollTop = (x - this.mInitialMotionX) * 0.5F;
                    this.mIsBeingDragged = false;
                    if (overscrollTop > this.mTotalDragDistance) {
                        this.setRefreshing(true, true);
                    } else {
                        this.mRefreshing = false;
                        this.mProgress.setStartEndTrim(0.0F, 0.0F);
                        Animation.AnimationListener listener1 = null;
                        if (!this.mScale) {
                            listener1 = new Animation.AnimationListener() {
                                public void onAnimationStart(Animation animation) {
                                }

                                public void onAnimationEnd(Animation animation) {
                                    if (!SwipeDectectLayout.this.mScale) {
                                        SwipeDectectLayout.this.startScaleDownAnimation((Animation.AnimationListener) null);
                                    }

                                }

                                public void onAnimationRepeat(Animation animation) {
                                }
                            };
                        }

                        this.animateOffsetToStartPosition(this.mCurrentTargetOffsetTop, listener1);
                        this.mProgress.showArrow(false);
                    }

                    this.mActivePointerId = -1;
                    return false;
                case 2:
                    pointerIndex = MotionEventCompat.findPointerIndex(ev, this.mActivePointerId);
                    if (pointerIndex < 0) {
                        Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    }

                    x = MotionEventCompat.getX(ev, pointerIndex);
                    overscrollTop = (x - this.mInitialMotionX) * 0.5F;
                    if (this.mIsBeingDragged) {
                        this.mProgress.showArrow(true);
                        float listener = overscrollTop / this.mTotalDragDistance;
                        if (listener < 0.0F) {
                            return false;
                        }

                        float dragPercent = Math.min(1.0F, Math.abs(listener));
                        float adjustedPercent = (float) Math.max((double) dragPercent - 0.4D, 0.0D) * 5.0F / 3.0F;
                        float extraOS = Math.abs(overscrollTop) - this.mTotalDragDistance;
                        float slingshotDist = this.mUsingCustomStart ? this.mSpinnerFinalOffset - (float) this.mOriginalOffsetTop : this.mSpinnerFinalOffset;
                        float tensionSlingshotPercent = Math.max(0.0F, Math.min(extraOS, slingshotDist * 2.0F) / slingshotDist);
                        float tensionPercent = (float) ((double) (tensionSlingshotPercent / 4.0F) - Math.pow((double) (tensionSlingshotPercent / 4.0F), 2.0D)) * 2.0F;
                        float extraMove = slingshotDist * tensionPercent * 2.0F;
                        int targetX = this.mOriginalOffsetTop + (int) (slingshotDist * dragPercent + extraMove);
                        if (this.mCircleView.getVisibility() != View.VISIBLE) {
                            this.mCircleView.setVisibility(View.VISIBLE);
                        }

                        if (!this.mScale) {
                            ViewCompat.setScaleX(this.mCircleView, 1.0F);
                            ViewCompat.setScaleY(this.mCircleView, 1.0F);
                        }

                        float rotation;
                        if (overscrollTop < this.mTotalDragDistance) {
                            if (this.mScale) {
                                this.setAnimationProgress(overscrollTop / this.mTotalDragDistance);
                            }

                            if (this.mProgress.getAlpha() > 76 && !this.isAnimationRunning(this.mAlphaStartAnimation)) {
                                this.startProgressAlphaStartAnimation();
                            }

                            rotation = adjustedPercent * 0.8F;
                            this.mProgress.setStartEndTrim(0.0F, Math.min(0.8F, rotation));
                            this.mProgress.setArrowScale(Math.min(1.0F, adjustedPercent));
                        } else if (this.mProgress.getAlpha() < 255 && !this.isAnimationRunning(this.mAlphaMaxAnimation)) {
                            this.startProgressAlphaMaxAnimation();
                        }

                        rotation = (-0.25F + 0.4F * adjustedPercent + tensionPercent * 2.0F) * 0.5F;
                        this.mProgress.setProgressRotation(rotation);
                        this.setTargetOffsetTopAndBottom(targetX - this.mCurrentTargetOffsetTop, true);
                    }
                case 4:
                default:
                    break;
                case 5:
                    pointerIndex = MotionEventCompat.getActionIndex(ev);
                    this.mActivePointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
                    break;
                case 6:
                    this.onSecondaryPointerUp(ev);
            }

            return true;
        } else {
            return false;
        }
    }

    public void restoreOriginalLayout(){
        if(originalWidth==0) originalWidth=100;
        if (mCompressedParams == null)
            mCompressedParams = new LinearLayout.LayoutParams(
                    originalWidth, LinearLayout.LayoutParams.MATCH_PARENT);
        setLayoutParams(mCompressedParams);
        requestLayout();
    }
    private void animateOffsetToCorrectPosition(int from, Animation.AnimationListener listener) {
        this.mFrom = from;
        this.mAnimateToCorrectPosition.reset();
        this.mAnimateToCorrectPosition.setDuration(200L);
        this.mAnimateToCorrectPosition.setInterpolator(this.mDecelerateInterpolator);
        if (listener != null) {
            this.mCircleView.setAnimationListener(listener);
        }

        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(this.mAnimateToCorrectPosition);
    }

    private void animateOffsetToStartPosition(int from, Animation.AnimationListener listener) {
        if (this.mScale) {
            this.startScaleDownReturnToStartAnimation(from, listener);
        } else {
            this.mFrom = from;
            this.mAnimateToStartPosition.reset();
            this.mAnimateToStartPosition.setDuration(200L);
            this.mAnimateToStartPosition.setInterpolator(this.mDecelerateInterpolator);
            if (listener != null) {
                this.mCircleView.setAnimationListener(listener);
            }

            this.mCircleView.clearAnimation();
            this.mCircleView.startAnimation(this.mAnimateToStartPosition);
        }

    }

    private void moveToStart(float interpolatedTime) {
        boolean targetTop = false;
        int targetTop1 = this.mFrom + (int) ((float) (this.mOriginalOffsetTop - this.mFrom) * interpolatedTime);
        int offset = targetTop1 - this.mCircleView.getTop();
        this.setTargetOffsetTopAndBottom(offset, false);
    }

    private void startScaleDownReturnToStartAnimation(int from, Animation.AnimationListener listener) {
        this.mFrom = from;
        if (this.isAlphaUsedForScale()) {
            this.mStartingScale = (float) this.mProgress.getAlpha();
        } else {
            this.mStartingScale = ViewCompat.getScaleX(this.mCircleView);
        }

        this.mScaleDownToStartAnimation = new Animation() {
            public void applyTransformation(float interpolatedTime, Transformation t) {
                float targetScale = SwipeDectectLayout.this.mStartingScale + -SwipeDectectLayout.this.mStartingScale * interpolatedTime;
                SwipeDectectLayout.this.setAnimationProgress(targetScale);
                SwipeDectectLayout.this.moveToStart(interpolatedTime);
            }
        };
        this.mScaleDownToStartAnimation.setDuration(150L);
        if (listener != null) {
            this.mCircleView.setAnimationListener(listener);
        }

        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(this.mScaleDownToStartAnimation);
    }

    private LinearLayout.LayoutParams mCompressedParams = null;

    private LinearLayout.LayoutParams mExpandedParams = new LinearLayout.LayoutParams(
            400, LinearLayout.LayoutParams.MATCH_PARENT);
    private int originalWidth = 0;

    private void setTargetOffsetTopAndBottom(int offset, boolean requiresUpdate) {

        this.mCircleView.bringToFront();
        this.mCircleView.offsetTopAndBottom(offset);
        this.mCurrentTargetOffsetTop = this.mCircleView.getTop();
        if (requiresUpdate && Build.VERSION.SDK_INT < 11) {
            this.invalidate();
        }

    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = MotionEventCompat.getActionIndex(ev);
        int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == this.mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            this.mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }

    }

    public interface OnRefreshListener {
        void onLefttoRight();

        void onRighttoLeft();
    }
}

class MaterialProgressDrawable extends Drawable implements Animatable {
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final float FULL_ROTATION = 1080.0F;
    static final int LARGE = 0;
    static final int DEFAULT = 1;
    private static final int CIRCLE_DIAMETER = 40;
    private static final float CENTER_RADIUS = 8.75F;
    private static final float STROKE_WIDTH = 2.5F;
    private static final int CIRCLE_DIAMETER_LARGE = 56;
    private static final float CENTER_RADIUS_LARGE = 12.5F;
    private static final float STROKE_WIDTH_LARGE = 3.0F;
    private final int[] COLORS = new int[]{-16777216};
    private static final float COLOR_START_DELAY_OFFSET = 0.75F;
    private static final float END_TRIM_START_DELAY_OFFSET = 0.5F;
    private static final float START_TRIM_DURATION_OFFSET = 0.5F;
    private static final int ANIMATION_DURATION = 1332;
    private static final float NUM_POINTS = 5.0F;
    private final ArrayList<Animation> mAnimators = new ArrayList();
    private final MaterialProgressDrawable.Ring mRing;
    private float mRotation;
    private static final int ARROW_WIDTH = 10;
    private static final int ARROW_HEIGHT = 5;
    private static final float ARROW_OFFSET_ANGLE = 5.0F;
    private static final int ARROW_WIDTH_LARGE = 12;
    private static final int ARROW_HEIGHT_LARGE = 6;
    private static final float MAX_PROGRESS_ARC = 0.8F;
    private Resources mResources;
    private View mParent;
    private Animation mAnimation;
    private float mRotationCount;
    private double mWidth;
    private double mHeight;
    boolean mFinishing;
    private final Callback mCallback = new Callback() {
        public void invalidateDrawable(Drawable d) {
            MaterialProgressDrawable.this.invalidateSelf();
        }

        public void scheduleDrawable(Drawable d, Runnable what, long when) {
            MaterialProgressDrawable.this.scheduleSelf(what, when);
        }

        public void unscheduleDrawable(Drawable d, Runnable what) {
            MaterialProgressDrawable.this.unscheduleSelf(what);
        }
    };

    public MaterialProgressDrawable(Context context, View parent) {
        this.mParent = parent;
        this.mResources = context.getResources();
        this.mRing = new MaterialProgressDrawable.Ring(this.mCallback);
        this.mRing.setColors(this.COLORS);
        this.updateSizes(1);
        this.setupAnimators();
    }

    private void setSizeParameters(double progressCircleWidth, double progressCircleHeight, double centerRadius, double strokeWidth, float arrowWidth, float arrowHeight) {
        MaterialProgressDrawable.Ring ring = this.mRing;
        DisplayMetrics metrics = this.mResources.getDisplayMetrics();
        float screenDensity = metrics.density;
        this.mWidth = progressCircleWidth * (double) screenDensity;
        this.mHeight = progressCircleHeight * (double) screenDensity;
        ring.setStrokeWidth((float) strokeWidth * screenDensity);
        ring.setCenterRadius(centerRadius * (double) screenDensity);
        ring.setColorIndex(0);
        ring.setArrowDimensions(arrowWidth * screenDensity, arrowHeight * screenDensity);
        ring.setInsets((int) this.mWidth, (int) this.mHeight);
    }

    public void updateSizes(@MaterialProgressDrawable.ProgressDrawableSize int size) {
        if (size == 0) {
            this.setSizeParameters(56.0D, 56.0D, 12.5D, 3.0D, 12.0F, 6.0F);
        } else {
            this.setSizeParameters(40.0D, 40.0D, 8.75D, 2.5D, 10.0F, 5.0F);
        }

    }

    public void showArrow(boolean show) {
        this.mRing.setShowArrow(show);
    }

    public void setArrowScale(float scale) {
        this.mRing.setArrowScale(scale);
    }

    public void setStartEndTrim(float startAngle, float endAngle) {
        this.mRing.setStartTrim(startAngle);
        this.mRing.setEndTrim(endAngle);
    }

    public void setProgressRotation(float rotation) {
        this.mRing.setRotation(rotation);
    }

    public void setBackgroundColor(int color) {
        this.mRing.setBackgroundColor(color);
    }

    public void setColorSchemeColors(int... colors) {
        this.mRing.setColors(colors);
        this.mRing.setColorIndex(0);
    }

    public int getIntrinsicHeight() {
        return (int) this.mHeight;
    }

    public int getIntrinsicWidth() {
        return (int) this.mWidth;
    }

    public void draw(Canvas c) {
        Rect bounds = this.getBounds();
        int saveCount = c.save();
        c.rotate(this.mRotation, bounds.exactCenterX(), bounds.exactCenterY());
        this.mRing.draw(c, bounds);
        c.restoreToCount(saveCount);
    }

    public void setAlpha(int alpha) {
        this.mRing.setAlpha(alpha);
    }

    public int getAlpha() {
        return this.mRing.getAlpha();
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mRing.setColorFilter(colorFilter);
    }

    void setRotation(float rotation) {
        this.mRotation = rotation;
        this.invalidateSelf();
    }

    private float getRotation() {
        return this.mRotation;
    }

    public int getOpacity() {
        return -3;
    }

    public boolean isRunning() {
        ArrayList animators = this.mAnimators;
        int N = animators.size();

        for (int i = 0; i < N; ++i) {
            Animation animator = (Animation) animators.get(i);
            if (animator.hasStarted() && !animator.hasEnded()) {
                return true;
            }
        }

        return false;
    }

    public void start() {
        this.mAnimation.reset();
        this.mRing.storeOriginals();
        if (this.mRing.getEndTrim() != this.mRing.getStartTrim()) {
            this.mFinishing = true;
            this.mAnimation.setDuration(666L);
            this.mParent.startAnimation(this.mAnimation);
        } else {
            this.mRing.setColorIndex(0);
            this.mRing.resetOriginals();
            this.mAnimation.setDuration(1332L);
            this.mParent.startAnimation(this.mAnimation);
        }

    }

    public void stop() {
        this.mParent.clearAnimation();
        this.setRotation(0.0F);
        this.mRing.setShowArrow(false);
        this.mRing.setColorIndex(0);
        this.mRing.resetOriginals();
    }

    private float getMinProgressArc(MaterialProgressDrawable.Ring ring) {
        return (float) Math.toRadians((double) ring.getStrokeWidth() / (6.283185307179586D * ring.getCenterRadius()));
    }

    private int evaluateColorChange(float fraction, int startValue, int endValue) {
        int startInt = Integer.valueOf(startValue).intValue();
        int startA = startInt >> 24 & 255;
        int startR = startInt >> 16 & 255;
        int startG = startInt >> 8 & 255;
        int startB = startInt & 255;
        int endInt = Integer.valueOf(endValue).intValue();
        int endA = endInt >> 24 & 255;
        int endR = endInt >> 16 & 255;
        int endG = endInt >> 8 & 255;
        int endB = endInt & 255;
        return startA + (int) (fraction * (float) (endA - startA)) << 24 | startR + (int) (fraction * (float) (endR - startR)) << 16 | startG + (int) (fraction * (float) (endG - startG)) << 8 | startB + (int) (fraction * (float) (endB - startB));
    }

    private void updateRingColor(float interpolatedTime, MaterialProgressDrawable.Ring ring) {
        if (interpolatedTime > 0.75F) {
            ring.setColor(this.evaluateColorChange((interpolatedTime - 0.75F) / 0.25F, ring.getStartingColor(), ring.getNextColor()));
        }

    }

    private void applyFinishTranslation(float interpolatedTime, MaterialProgressDrawable.Ring ring) {
        this.updateRingColor(interpolatedTime, ring);
        float targetRotation = (float) (Math.floor((double) (ring.getStartingRotation() / 0.8F)) + 1.0D);
        float minProgressArc = this.getMinProgressArc(ring);
        float startTrim = ring.getStartingStartTrim() + (ring.getStartingEndTrim() - minProgressArc - ring.getStartingStartTrim()) * interpolatedTime;
        ring.setStartTrim(startTrim);
        ring.setEndTrim(ring.getStartingEndTrim());
        float rotation = ring.getStartingRotation() + (targetRotation - ring.getStartingRotation()) * interpolatedTime;
        ring.setRotation(rotation);
    }

    private void setupAnimators() {
        final MaterialProgressDrawable.Ring ring = this.mRing;
        Animation animation = new Animation() {
            public void applyTransformation(float interpolatedTime, Transformation t) {
                if (MaterialProgressDrawable.this.mFinishing) {
                    MaterialProgressDrawable.this.applyFinishTranslation(interpolatedTime, ring);
                } else {
                    float minProgressArc = MaterialProgressDrawable.this.getMinProgressArc(ring);
                    float startingEndTrim = ring.getStartingEndTrim();
                    float startingTrim = ring.getStartingStartTrim();
                    float startingRotation = ring.getStartingRotation();
                    MaterialProgressDrawable.this.updateRingColor(interpolatedTime, ring);
                    float rotation;
                    float groupRotation;
                    if (interpolatedTime <= 0.5F) {
                        rotation = interpolatedTime / 0.5F;
                        groupRotation = startingTrim + (0.8F - minProgressArc) * MaterialProgressDrawable.MATERIAL_INTERPOLATOR.getInterpolation(rotation);
                        ring.setStartTrim(groupRotation);
                    }

                    if (interpolatedTime > 0.5F) {
                        rotation = 0.8F - minProgressArc;
                        groupRotation = (interpolatedTime - 0.5F) / 0.5F;
                        float endTrim = startingEndTrim + rotation * MaterialProgressDrawable.MATERIAL_INTERPOLATOR.getInterpolation(groupRotation);
                        ring.setEndTrim(endTrim);
                    }

                    rotation = startingRotation + 0.25F * interpolatedTime;
                    ring.setRotation(rotation);
                    groupRotation = 216.0F * interpolatedTime + 1080.0F * (MaterialProgressDrawable.this.mRotationCount / 5.0F);
                    MaterialProgressDrawable.this.setRotation(groupRotation);
                }

            }
        };
        animation.setRepeatCount(-1);
        animation.setRepeatMode(1);
        animation.setInterpolator(LINEAR_INTERPOLATOR);
        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
                MaterialProgressDrawable.this.mRotationCount = 0.0F;
            }

            public void onAnimationEnd(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
                ring.storeOriginals();
                ring.goToNextColor();
                ring.setStartTrim(ring.getEndTrim());
                if (MaterialProgressDrawable.this.mFinishing) {
                    MaterialProgressDrawable.this.mFinishing = false;
                    animation.setDuration(1332L);
                    ring.setShowArrow(false);
                } else {
                    MaterialProgressDrawable.this.mRotationCount = (MaterialProgressDrawable.this.mRotationCount + 1.0F) % 5.0F;
                }

            }
        });
        this.mAnimation = animation;
    }

    private static class Ring {
        private final RectF mTempBounds = new RectF();
        private final Paint mPaint = new Paint();
        private final Paint mArrowPaint = new Paint();
        private final Callback mCallback;
        private float mStartTrim = 0.0F;
        private float mEndTrim = 0.0F;
        private float mRotation = 0.0F;
        private float mStrokeWidth = 5.0F;
        private float mStrokeInset = 2.5F;
        private int[] mColors;
        private int mColorIndex;
        private float mStartingStartTrim;
        private float mStartingEndTrim;
        private float mStartingRotation;
        private boolean mShowArrow;
        private Path mArrow;
        private float mArrowScale;
        private double mRingCenterRadius;
        private int mArrowWidth;
        private int mArrowHeight;
        private int mAlpha;
        private final Paint mCirclePaint = new Paint(1);
        private int mBackgroundColor;
        private int mCurrentColor;

        public Ring(Callback callback) {
            this.mCallback = callback;
            this.mPaint.setStrokeCap(Paint.Cap.SQUARE);
            this.mPaint.setAntiAlias(true);
            this.mPaint.setStyle(Paint.Style.STROKE);
            this.mArrowPaint.setStyle(Paint.Style.FILL);
            this.mArrowPaint.setAntiAlias(true);
        }

        public void setBackgroundColor(int color) {
            this.mBackgroundColor = color;
        }

        public void setArrowDimensions(float width, float height) {
            this.mArrowWidth = (int) width;
            this.mArrowHeight = (int) height;
        }

        public void draw(Canvas c, Rect bounds) {
            RectF arcBounds = this.mTempBounds;
            arcBounds.set(bounds);
            arcBounds.inset(this.mStrokeInset, this.mStrokeInset);
            float startAngle = (this.mStartTrim + this.mRotation) * 360.0F;
            float endAngle = (this.mEndTrim + this.mRotation) * 360.0F;
            float sweepAngle = endAngle - startAngle;
            this.mPaint.setColor(this.mCurrentColor);
            c.drawArc(arcBounds, startAngle, sweepAngle, false, this.mPaint);
            this.drawTriangle(c, startAngle, sweepAngle, bounds);
            if (this.mAlpha < 255) {
                this.mCirclePaint.setColor(this.mBackgroundColor);
                this.mCirclePaint.setAlpha(255 - this.mAlpha);
                c.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), (float) (bounds.width() / 2), this.mCirclePaint);
            }

        }

        private void drawTriangle(Canvas c, float startAngle, float sweepAngle, Rect bounds) {
            if (this.mShowArrow) {
                if (this.mArrow == null) {
                    this.mArrow = new Path();
                    this.mArrow.setFillType(Path.FillType.EVEN_ODD);
                } else {
                    this.mArrow.reset();
                }

                float inset = (float) ((int) this.mStrokeInset / 2) * this.mArrowScale;
                float x = (float) (this.mRingCenterRadius * Math.cos(0.0D) + (double) bounds.exactCenterX());
                float y = (float) (this.mRingCenterRadius * Math.sin(0.0D) + (double) bounds.exactCenterY());
                this.mArrow.moveTo(0.0F, 0.0F);
                this.mArrow.lineTo((float) this.mArrowWidth * this.mArrowScale, 0.0F);
                this.mArrow.lineTo((float) this.mArrowWidth * this.mArrowScale / 2.0F, (float) this.mArrowHeight * this.mArrowScale);
                this.mArrow.offset(x - inset, y);
                this.mArrow.close();
                this.mArrowPaint.setColor(this.mCurrentColor);
                c.rotate(startAngle + sweepAngle - 5.0F, bounds.exactCenterX(), bounds.exactCenterY());
                c.drawPath(this.mArrow, this.mArrowPaint);
            }

        }

        public void setColors(@NonNull int[] colors) {
            this.mColors = colors;
            this.setColorIndex(0);
        }

        public void setColor(int color) {
            this.mCurrentColor = color;
        }

        public void setColorIndex(int index) {
            this.mColorIndex = index;
            this.mCurrentColor = this.mColors[this.mColorIndex];
        }

        public int getNextColor() {
            return this.mColors[this.getNextColorIndex()];
        }

        private int getNextColorIndex() {
            return (this.mColorIndex + 1) % this.mColors.length;
        }

        public void goToNextColor() {
            this.setColorIndex(this.getNextColorIndex());
        }

        public void setColorFilter(ColorFilter filter) {
            this.mPaint.setColorFilter(filter);
            this.invalidateSelf();
        }

        public void setAlpha(int alpha) {
            this.mAlpha = alpha;
        }

        public int getAlpha() {
            return this.mAlpha;
        }

        public void setStrokeWidth(float strokeWidth) {
            this.mStrokeWidth = strokeWidth;
            this.mPaint.setStrokeWidth(strokeWidth);
            this.invalidateSelf();
        }

        public float getStrokeWidth() {
            return this.mStrokeWidth;
        }

        public void setStartTrim(float startTrim) {
            this.mStartTrim = startTrim;
            this.invalidateSelf();
        }

        public float getStartTrim() {
            return this.mStartTrim;
        }

        public float getStartingStartTrim() {
            return this.mStartingStartTrim;
        }

        public float getStartingEndTrim() {
            return this.mStartingEndTrim;
        }

        public int getStartingColor() {
            return this.mColors[this.mColorIndex];
        }

        public void setEndTrim(float endTrim) {
            this.mEndTrim = endTrim;
            this.invalidateSelf();
        }

        public float getEndTrim() {
            return this.mEndTrim;
        }

        public void setRotation(float rotation) {
            this.mRotation = rotation;
            this.invalidateSelf();
        }

        public float getRotation() {
            return this.mRotation;
        }

        public void setInsets(int width, int height) {
            float minEdge = (float) Math.min(width, height);
            float insets;
            if (this.mRingCenterRadius > 0.0D && minEdge >= 0.0F) {
                insets = (float) ((double) (minEdge / 2.0F) - this.mRingCenterRadius);
            } else {
                insets = (float) Math.ceil((double) (this.mStrokeWidth / 2.0F));
            }

            this.mStrokeInset = insets;
        }

        public float getInsets() {
            return this.mStrokeInset;
        }

        public void setCenterRadius(double centerRadius) {
            this.mRingCenterRadius = centerRadius;
        }

        public double getCenterRadius() {
            return this.mRingCenterRadius;
        }

        public void setShowArrow(boolean show) {
            if (this.mShowArrow != show) {
                this.mShowArrow = show;
                this.invalidateSelf();
            }

        }

        public void setArrowScale(float scale) {
            if (scale != this.mArrowScale) {
                this.mArrowScale = scale;
                this.invalidateSelf();
            }

        }

        public float getStartingRotation() {
            return this.mStartingRotation;
        }

        public void storeOriginals() {
            this.mStartingStartTrim = this.mStartTrim;
            this.mStartingEndTrim = this.mEndTrim;
            this.mStartingRotation = this.mRotation;
        }

        public void resetOriginals() {
            this.mStartingStartTrim = 0.0F;
            this.mStartingEndTrim = 0.0F;
            this.mStartingRotation = 0.0F;
            this.setStartTrim(0.0F);
            this.setEndTrim(0.0F);
            this.setRotation(0.0F);
        }

        private void invalidateSelf() {
            this.mCallback.invalidateDrawable((Drawable) null);
        }
    }

    @Retention(RetentionPolicy.CLASS)
    public @interface ProgressDrawableSize {
    }
}

class CircleImageView extends ImageView {
    private static final int KEY_SHADOW_COLOR = 503316480;
    private static final int FILL_SHADOW_COLOR = 1023410176;
    private static final float X_OFFSET = 0.0F;
    private static final float Y_OFFSET = 1.75F;
    private static final float SHADOW_RADIUS = 3.5F;
    private static final int SHADOW_ELEVATION = 4;
    private Animation.AnimationListener mListener;
    private int mShadowRadius;

    public CircleImageView(Context context, int color, float radius) {
        super(context);
        float density = this.getContext().getResources().getDisplayMetrics().density;
        int diameter = (int) (radius * density * 2.0F);
        int shadowYOffset = (int) (density * 1.75F);
        int shadowXOffset = (int) (density * 0.0F);
        this.mShadowRadius = (int) (density * 3.5F);
        ShapeDrawable circle;
        if (this.elevationSupported()) {
            circle = new ShapeDrawable(new OvalShape());
            ViewCompat.setElevation(this, 4.0F * density);
        } else {
            CircleImageView.OvalShadow oval = new CircleImageView.OvalShadow(this.mShadowRadius, diameter);
            circle = new ShapeDrawable(oval);
            ViewCompat.setLayerType(this, 1, circle.getPaint());
            circle.getPaint().setShadowLayer((float) this.mShadowRadius, (float) shadowXOffset, (float) shadowYOffset, 503316480);
            int padding = this.mShadowRadius;
            this.setPadding(padding, padding, padding, padding);
        }

        circle.getPaint().setColor(color);
        this.setBackgroundDrawable(circle);
    }

    private boolean elevationSupported() {
        return Build.VERSION.SDK_INT >= 21;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!this.elevationSupported()) {
            this.setMeasuredDimension(this.getMeasuredWidth() + this.mShadowRadius * 2, this.getMeasuredHeight() + this.mShadowRadius * 2);
        }

    }

    public void setAnimationListener(Animation.AnimationListener listener) {
        this.mListener = listener;
    }

    public void onAnimationStart() {
        super.onAnimationStart();
        if (this.mListener != null) {
            this.mListener.onAnimationStart(this.getAnimation());
        }

    }

    public void onAnimationEnd() {
        super.onAnimationEnd();
        if (this.mListener != null) {
            this.mListener.onAnimationEnd(this.getAnimation());
        }

    }

    public void setBackgroundColorRes(int colorRes) {
        this.setBackgroundColor(this.getContext().getResources().getColor(colorRes));
    }

    public void setBackgroundColor(int color) {
        if (this.getBackground() instanceof ShapeDrawable) {
            ((ShapeDrawable) this.getBackground()).getPaint().setColor(color);
        }

    }

    private class OvalShadow extends OvalShape {
        private RadialGradient mRadialGradient;
        private Paint mShadowPaint = new Paint();
        private int mCircleDiameter;

        public OvalShadow(int shadowRadius, int circleDiameter) {
            CircleImageView.this.mShadowRadius = shadowRadius;
            this.mCircleDiameter = circleDiameter;
            this.mRadialGradient = new RadialGradient((float) (this.mCircleDiameter / 2), (float) (this.mCircleDiameter / 2), (float) CircleImageView.this.mShadowRadius, new int[]{1023410176, 0}, (float[]) null, Shader.TileMode.CLAMP);
            this.mShadowPaint.setShader(this.mRadialGradient);
        }

        public void draw(Canvas canvas, Paint paint) {
            int viewWidth = CircleImageView.this.getWidth();
            int viewHeight = CircleImageView.this.getHeight();
            canvas.drawCircle((float) (viewWidth / 2), (float) (viewHeight / 2), (float) (this.mCircleDiameter / 2 + CircleImageView.this.mShadowRadius), this.mShadowPaint);
            canvas.drawCircle((float) (viewWidth / 2), (float) (viewHeight / 2), (float) (this.mCircleDiameter / 2), paint);
        }
    }
}
