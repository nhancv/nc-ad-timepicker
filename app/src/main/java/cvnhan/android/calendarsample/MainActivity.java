package cvnhan.android.calendarsample;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import cvnhan.android.calendarsample.calendar.Calendar;


public class MainActivity  extends Activity {
    private Calendar mGraphView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        mGraphView = (Calendar) findViewById(R.id.chart);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


}