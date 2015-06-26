package cvnhan.android.calendarsample.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.Toast;

/**
 * Created by cvnhan on 25-Jun-15.
 */
public class ServiceView extends RecyclerView {

    public ServiceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        addOnItemTouchListener(swipeTouchListener);
    }

    public ServiceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ServiceView(Context context) {
        this(context, null, 0);
    }
    SwipeableRecyclerViewTouchListener swipeTouchListener =
            new SwipeableRecyclerViewTouchListener(this,
                    new SwipeableRecyclerViewTouchListener.SwipeListener() {
                        @Override
                        public boolean canSwipe(int position) {
                            return true;
                        }

                        @Override
                        public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                            for (int position : reverseSortedPositions) {
                                    Toast.makeText(getContext(), position+ " swiped left", Toast.LENGTH_SHORT).show();

                            }
                        }

                        @Override
                        public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                            for (int position : reverseSortedPositions) {
                                    Toast.makeText(getContext(), position + " swiped right", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
}
