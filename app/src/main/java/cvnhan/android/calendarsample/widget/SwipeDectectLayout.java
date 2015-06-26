package cvnhan.android.calendarsample.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
    private int mCurrentTargetOffsetLeft;
    private boolean mOriginalOffsetCalculated;
    private float mInitialMotionX;
    private float mInitialDownX;
    private boolean mIsBeingDragged;
    private int mActivePointerId;
    private boolean mReturningToStart;
    private final DecelerateInterpolator mDecelerateInterpolator;
    private static final int[] LAYOUT_ATTRS = new int[]{16842766};
    private CircleImageView mCircleView;
    private int mCircleViewIndex;
    protected int mFrom;
    protected int mOriginalOffsetLeft;
    private Animation mScaleAnimation;
    private Animation mScaleDownAnimation;
    private float mSpinnerFinalOffset;
    private boolean mNotify;
    private int mCircleWidth;
    private int mCircleHeight;
    private Animation.AnimationListener mSwipeListener;
    private int directionSwipe = 0; //0 left-right, 1 right-left;
    private final Animation mAnimateToCorrectPosition;
    private final Animation mAnimateToStartPosition;

    private LinearLayout.LayoutParams mCompressedParams = null;
    private int originalWidth = 0;

    private void setColorViewAlpha(int targetAlpha) {
        this.mCircleView.getBackground().setAlpha(targetAlpha);
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
        this.mSwipeListener = new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (SwipeDectectLayout.this.mRefreshing) {
                    if (SwipeDectectLayout.this.mNotify && SwipeDectectLayout.this.mListener != null) {
                        if (directionSwipe == 0)
                            SwipeDectectLayout.this.mListener.onLefttoRight();
                        else
                            SwipeDectectLayout.this.mListener.onRighttoLeft();
                    }
                } else {
                    SwipeDectectLayout.this.mCircleView.setVisibility(View.GONE);
                    SwipeDectectLayout.this.setTargetOffsetLeftAndRight(SwipeDectectLayout.this.mOriginalOffsetLeft - SwipeDectectLayout.this.mCurrentTargetOffsetLeft, true);

                }

                SwipeDectectLayout.this.mCurrentTargetOffsetLeft = SwipeDectectLayout.this.mCircleView.getLeft();
            }
        };

        this.mAnimateToCorrectPosition = new Animation() {
            public void applyTransformation(float interpolatedTime, Transformation t) {
                int endTarget1 = (int) SwipeDectectLayout.this.mSpinnerFinalOffset;


                int targetTop1 = SwipeDectectLayout.this.mFrom + (int) ((float) (endTarget1 - SwipeDectectLayout.this.mFrom) * interpolatedTime);
                int offset = targetTop1 - SwipeDectectLayout.this.mCircleView.getLeft();
                SwipeDectectLayout.this.setTargetOffsetLeftAndRight(offset, false);
            }
        };
        this.mAnimateToStartPosition = new Animation() {
            public void applyTransformation(float interpolatedTime, Transformation t) {
                SwipeDectectLayout.this.moveToStart(interpolatedTime);
            }
        };
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mMediumAnimationDuration = 0;
        this.setWillNotDraw(false);
        this.mDecelerateInterpolator = new DecelerateInterpolator(1.0F);
        TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        this.setEnabled(a.getBoolean(0, true));
        a.recycle();
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        this.mCircleWidth = (int) (40.0F * metrics.density);
        this.mCircleHeight = (int) (40.0F * metrics.density);
        this.createProgressView();
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        this.mSpinnerFinalOffset = 50.0F * metrics.density;
        this.mTotalDragDistance = this.mSpinnerFinalOffset;
    }

    protected int getChildDrawingOrder(int childCount, int i) {
        return this.mCircleViewIndex < 0 ? i : (i == childCount - 1 ? this.mCircleViewIndex : (i >= this.mCircleViewIndex ? i + 1 : i));
    }

    private void createProgressView() {
        this.mCircleView = new CircleImageView(this.getContext(), -328966, 20.0F);
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
            int endTarget1 = (int) this.mSpinnerFinalOffset;
            this.setTargetOffsetLeftAndRight(endTarget1 - this.mCurrentTargetOffsetLeft, true);
            this.mNotify = false;
            this.startScaleUpAnimation(this.mSwipeListener);
        } else {
            this.setRefreshing(refreshing, false);
        }

    }

    private void startScaleUpAnimation(Animation.AnimationListener listener) {
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
                this.animateOffsetToCorrectPosition(this.mCurrentTargetOffsetLeft, this.mSwipeListener);
            } else {
                this.startScaleDownAnimation(this.mSwipeListener);
            }
        }

    }

    private void startScaleDownAnimation(Animation.AnimationListener listener) {
        this.mScaleDownAnimation = new Animation() {
            public void applyTransformation(float interpolatedTime, Transformation t) {
                SwipeDectectLayout.this.setAnimationProgress(1.0F - interpolatedTime);
            }
        };
        this.mScaleDownAnimation.setDuration(0);
        this.mCircleView.setAnimationListener(listener);
        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(this.mScaleDownAnimation);
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
                this.mCircleView.layout(-circleWidth / 2 + this.mCurrentTargetOffsetLeft, height / 2, circleWidth / 2 + this.mCurrentTargetOffsetLeft + circleHeight, height / 2 + circleHeight / 2);
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
            if (!this.mOriginalOffsetCalculated) {
                this.mOriginalOffsetCalculated = true;
                this.mCurrentTargetOffsetLeft = this.mOriginalOffsetLeft = -this.mCircleView.getMeasuredWidth();
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
                    this.setTargetOffsetLeftAndRight(this.mOriginalOffsetLeft - this.mCircleView.getLeft(), true);
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
                            this.mInitialMotionX = -this.mInitialDownX + (float) this.mTouchSlop;
                            this.directionSwipe = 1;
                        }
                        this.mIsBeingDragged = true;
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
            float overscrollLeft;
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
                    overscrollLeft = (x - this.mInitialMotionX) * 0.5F;
                    this.mIsBeingDragged = false;
                    if (overscrollLeft > this.mTotalDragDistance) {
                        this.setRefreshing(true, true);
                    } else {
                        this.mRefreshing = false;
                        Animation.AnimationListener listener1 = null;
                        this.animateOffsetToStartPosition(this.mCurrentTargetOffsetLeft, listener1);
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
                    overscrollLeft = (x - this.mInitialMotionX) * 0.5F;
                    if (this.mIsBeingDragged) {
                        float listener = overscrollLeft / this.mTotalDragDistance;
                        if (listener < 0.0F) {
                            return false;
                        }

                        float dragPercent = Math.min(1.0F, Math.abs(listener));
                        float extraOS = Math.abs(overscrollLeft) - this.mTotalDragDistance;
                        float slingshotDist = this.mSpinnerFinalOffset;
                        float tensionSlingshotPercent = Math.max(0.0F, Math.min(extraOS, slingshotDist * 2.0F) / slingshotDist);
                        float tensionPercent = (float) ((double) (tensionSlingshotPercent / 4.0F) - Math.pow((double) (tensionSlingshotPercent / 4.0F), 2.0D)) * 2.0F;
                        float extraMove = slingshotDist * tensionPercent * 2.0F;
                        int targetX = this.mOriginalOffsetLeft + (int) (slingshotDist * dragPercent + extraMove);
                        this.setTargetOffsetLeftAndRight(targetX - this.mCurrentTargetOffsetLeft, true);
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

    private void animateOffsetToCorrectPosition(int from, Animation.AnimationListener listener) {
        this.mFrom = from;
        this.mAnimateToCorrectPosition.reset();
        this.mAnimateToCorrectPosition.setDuration(0);
        this.mAnimateToCorrectPosition.setInterpolator(this.mDecelerateInterpolator);
        if (listener != null) {
            this.mCircleView.setAnimationListener(listener);
        }

        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(this.mAnimateToCorrectPosition);
    }

    private void animateOffsetToStartPosition(int from, Animation.AnimationListener listener) {
        this.mFrom = from;
        this.mAnimateToStartPosition.reset();
        this.mAnimateToStartPosition.setDuration(0L);
        this.mAnimateToStartPosition.setInterpolator(this.mDecelerateInterpolator);
        if (listener != null) {
            this.mCircleView.setAnimationListener(listener);
        }

        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(this.mAnimateToStartPosition);

    }

    private void moveToStart(float interpolatedTime) {
        int targetTop1 = this.mFrom + (int) ((float) (this.mOriginalOffsetLeft - this.mFrom) * interpolatedTime);
        int offset = targetTop1 - this.mCircleView.getLeft();
        this.setTargetOffsetLeftAndRight(offset, false);
    }


    private void setTargetOffsetLeftAndRight(int offset, boolean requiresUpdate) {
        this.mCircleView.bringToFront();
        this.mCircleView.offsetLeftAndRight(offset);
        this.mCurrentTargetOffsetLeft = this.mCircleView.getLeft();
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

    private void resizeWidthwidAnimation(int width) {
        ResizeWidthAnimation anim = new ResizeWidthAnimation(this, width);
        this.startAnimation(anim);
    }

    private void restoreOriginalLayout() {
        if (originalWidth == 0) originalWidth = 100;
        if (mCompressedParams == null)
            mCompressedParams = new LinearLayout.LayoutParams(
                    originalWidth, LinearLayout.LayoutParams.MATCH_PARENT);
        setLayoutParams(mCompressedParams);
    }

    private void restoreOriginalLayoutwithAnimation() {
        if (originalWidth == 0) originalWidth = 100;
        ResizeWidthAnimation anim = new ResizeWidthAnimation(this, originalWidth);
        this.startAnimation(anim);
    }

    public void expandWidth() {
        setRefreshing(false);
        int width = this.getResources().getDisplayMetrics().widthPixels;
        resizeWidthwidAnimation(width * 8 / 9);
    }

    public void collapseWidth() {
        setRefreshing(false);
        restoreOriginalLayoutwithAnimation();
    }


    public interface OnRefreshListener {
        void onLefttoRight();

        void onRighttoLeft();
    }

    public class ResizeWidthAnimation extends Animation {
        private int mWidth;
        private int mStartWidth;
        private View mView;

        public ResizeWidthAnimation(View view, int width) {
            mView = view;
            mWidth = width;
            mStartWidth = view.getWidth();
            setDuration(500);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int newWidth = mStartWidth + (int) ((mWidth - mStartWidth) * interpolated(interpolatedTime));

            mView.getLayoutParams().width = newWidth;
            mView.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }

        private float interpolated(float t){
            return (float)Math.pow(t-1, 5) + 1;
        }
    }
}

class CircleImageView extends ImageView {
    private Animation.AnimationListener mListener;

    public CircleImageView(Context context, int color, float radius) {
        super(context);
        setVisibility(View.INVISIBLE);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
}


