package cvnhan.android.calendarsample;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cvnhan.android.calendarsample.adapter.ServiceAdapter;
import cvnhan.android.calendarsample.adapter.StaffAdapter;
import cvnhan.android.calendarsample.model.ServiceInfo;
import cvnhan.android.calendarsample.model.StaffInfo;
import cvnhan.android.calendarsample.widget.OnSwipeTouchListener;
import cvnhan.android.calendarsample.widget.TimeView;


public class MainActivity extends Activity {

    @InjectView(R.id.serviceView)
    RecyclerView serviceView;

    @InjectView(R.id.staffView)
    RecyclerView staffView;

    @InjectView(R.id.timeView)
    TimeView mGraphView;

    @InjectView(R.id.tvServiceCircle)
    public TextView tvServiceCircle;
    @InjectView(R.id.tvService)
    public TextView tvService;

    @InjectView(R.id.tvStaffCircle)
    public TextView tvStaffCircle;
    @InjectView(R.id.tvStaff)
    public TextView tvStaff;

    @InjectView(R.id.tvTimeCircle)
    public TextView tvTimeCircle;
    @InjectView(R.id.tvTime)
    public TextView tvTime;


    private ServiceAdapter serviceAdapter;
    private StaffAdapter staffAdapter;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        initView();
    }

    private void initView() {
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        serviceView.setHasFixedSize(true);
        serviceView.setLayoutManager(llm);
        serviceAdapter = new ServiceAdapter(createServiceList(), this);
        serviceView.setAdapter(serviceAdapter);
        serviceView.getItemAnimator().setSupportsChangeAnimations(true);
        serviceView.setItemAnimator(new DefaultItemAnimator());

        llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        staffView.setHasFixedSize(true);
        staffView.setLayoutManager(llm);
        staffAdapter = new StaffAdapter(createStaffList(), this);
        staffView.setAdapter(staffAdapter);
        staffView.getItemAnimator().setSupportsChangeAnimations(true);

        mGraphView.injectMainActivity(this);
        mGraphView.setupTimeView(240, 79);
        mGraphView.initInvalidAreas();
        mGraphView.zoomMaximum();

        serviceView.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            public void onSwipeTop() {
                Toast.makeText(MainActivity.this, "top", Toast.LENGTH_SHORT).show();

            }

            public void onSwipeRight() {
                Toast.makeText(MainActivity.this, "right", Toast.LENGTH_SHORT).show();
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(600, LinearLayout.LayoutParams.WRAP_CONTENT);
                serviceView.setLayoutParams(lp);
            }

            public void onSwipeLeft() {
                Toast.makeText(MainActivity.this, "left", Toast.LENGTH_SHORT).show();
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT);
                serviceView.setLayoutParams(lp);
            }

            public void onSwipeBottom() {
                Toast.makeText(MainActivity.this, "bottom", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<ServiceInfo> createServiceList() {
        List<ServiceInfo> result = new ArrayList<>();
        ServiceInfo item = new ServiceInfo("Massage", 10);
        result.add(item);
        item = new ServiceInfo("Shampoo, blow dry", 14);
        result.add(item);
        item = new ServiceInfo("Spa", 90);
        result.add(item);
        item = new ServiceInfo("Nail", 45);
        result.add(item);
        item = new ServiceInfo("Washing, Conditioning & Softening", 90);
        result.add(item);
        item = new ServiceInfo("Tia long", 30);
        result.add(item);
        item = new ServiceInfo("Nho toc ngua", 45);
        result.add(item);
        item = new ServiceInfo("Suong", 45);
        result.add(item);
        item = new ServiceInfo("Hehe", 60);
        result.add(item);
        return result;
    }

    private List<StaffInfo> createStaffList() {
        List<StaffInfo> result = new ArrayList<>();
        StaffInfo
                item = new StaffInfo("Anyone", BitmapFactory.decodeResource(this.getResources(), R.drawable.avartar0));
        result.add(item);
        item = new StaffInfo("Mr. Free", BitmapFactory.decodeResource(this.getResources(), R.drawable.test));
        result.add(item);
        item = new StaffInfo("Jackit", BitmapFactory.decodeResource(this.getResources(), R.drawable.avartar1));
        result.add(item);
        item = new StaffInfo("Jonny", BitmapFactory.decodeResource(this.getResources(), R.drawable.avartar2));
        result.add(item);
        item = new StaffInfo("A Phuc", BitmapFactory.decodeResource(this.getResources(), R.drawable.avartar3));
        result.add(item);
        item = new StaffInfo("A Binh", BitmapFactory.decodeResource(this.getResources(), R.drawable.avartar4));
        result.add(item);
        item = new StaffInfo("A Lam", BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher));
        result.add(item);
        item = new StaffInfo("A Hung", BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher));
        result.add(item);
        item = new StaffInfo("A Thang", BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher));
        result.add(item);
        item = new StaffInfo("A Vuong", BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher));
        result.add(item);
        item = new StaffInfo("A Son", BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher));
        result.add(item);
        item = new StaffInfo("A Sang", BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher));
        result.add(item);
        item = new StaffInfo("C Thuy", BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher));
        result.add(item);
        return result;
    }

    public void createObj(long minutes) {
        mGraphView.createObj(minutes);
    }

    public void releaseObj() {
        mGraphView.releaseObj();
    }

    public void updateHeaderService(boolean status) {
        if (status) {
            tvServiceCircle.setBackgroundResource(R.drawable.circle_header_press);
            tvService.setTextColor(getResources().getColor(R.color.main_green));
        } else {
            tvServiceCircle.setBackgroundResource(R.drawable.circle_header);
            tvService.setTextColor(getResources().getColor(R.color.main_color));
        }
    }

    public void updateHeaderStaff(boolean status) {
        if (status) {
            tvStaffCircle.setBackgroundResource(R.drawable.circle_header_press);
            tvStaff.setTextColor(getResources().getColor(R.color.main_green));
        } else {
            tvStaffCircle.setBackgroundResource(R.drawable.circle_header);
            tvStaff.setTextColor(getResources().getColor(R.color.main_color));
        }
    }

    public void updateHeaderTime(boolean status) {
        if (status) {
            tvTimeCircle.setBackgroundResource(R.drawable.circle_header_press);
            tvTime.setTextColor(getResources().getColor(R.color.main_green));
        } else {
            tvTimeCircle.setBackgroundResource(R.drawable.circle_header);
            tvTime.setTextColor(getResources().getColor(R.color.main_color));
        }
    }

    @OnClick(R.id.lvCalendar)
    public void CalendarPopup() {
        Toast.makeText(getApplicationContext(), "Calendar popup", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_zoom_in:
                mGraphView.zoomIn();
                return true;

            case R.id.action_zoom_out:
                mGraphView.zoomOut();
                return true;

            case R.id.action_pan_left:
                mGraphView.panLeft();
                return true;

            case R.id.action_pan_right:
                mGraphView.panRight();
                return true;

            case R.id.action_pan_up:
                mGraphView.panUp();
                return true;

            case R.id.action_pan_down:
                mGraphView.panDown();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}