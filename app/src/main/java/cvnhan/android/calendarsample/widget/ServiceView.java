package cvnhan.android.calendarsample.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by cvnhan on 25-Jun-15.
 */
public class ServiceView extends RecyclerView {

    public ServiceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public ServiceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ServiceView(Context context) {
        this(context, null, 0);
    }
}
