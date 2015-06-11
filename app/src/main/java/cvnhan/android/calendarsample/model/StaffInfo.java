package cvnhan.android.calendarsample.model;

import android.graphics.Bitmap;

/**
 * Created by cvnhan on 11-Jun-15.
 */
public class StaffInfo {
    protected String name;
    protected Bitmap avartar;
    protected boolean isClicked=false;

    public StaffInfo() {

    }

    public StaffInfo(String name, Bitmap avartar) {
        this.name = name;
        this.avartar = avartar;
        isClicked=false;
    }

}
