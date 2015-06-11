package cvnhan.android.calendarsample.model;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cvnhan.android.calendarsample.R;
import cvnhan.android.calendarsample.Utils;

/**
 * Created by cvnhan on 11-Jun-15.
 */
public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    public Context context;
    private List<StaffInfo> staffInfos;

    public StaffAdapter() {
        this.staffInfos = new ArrayList<>();
    }

    public StaffAdapter(List<StaffInfo> staffInfos) {
        this.staffInfos = staffInfos;
    }


    @Override
    public int getItemCount() {
        return staffInfos.size();
    }

    public StaffInfo getItemClicked() {
        for (StaffInfo staffInfo : staffInfos) {
            if (staffInfo.isClicked) return staffInfo;
        }
        return null;
    }

    public void setItemClicked(int index) {
        for (int i = 0; i < staffInfos.size(); i++) {
            StaffInfo staffInfo = staffInfos.get(i);
            if (i == index) {
                staffInfo.isClicked = !staffInfo.isClicked;
            } else {
                staffInfo.isClicked = false;
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final StaffViewHolder staffViewHolder, final int i) {
        final StaffInfo item = staffInfos.get(i);
        staffViewHolder.tvName.setText(item.name);
        LinearLayout staffInfoItem = staffViewHolder.staffInfoItem;
        if (item.isClicked) {
            staffViewHolder.tvName.setTextColor(Color.parseColor("#f6921e"));
            staffViewHolder.imgAvartar.setImageBitmap(Utils.circleBitmapSelected(item.avartar));
            staffInfoItem.setBackgroundColor(Color.parseColor("#FFFFCC"));
        } else {
            staffViewHolder.tvName.setTextColor(Color.BLACK);
            staffViewHolder.imgAvartar.setImageBitmap(Utils.circleBitmap(item.avartar));
            staffInfoItem.setBackgroundColor(Color.TRANSPARENT);
        }

        staffInfoItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setItemClicked(i);
            }
        });

    }

    @Override
    public StaffViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        View itemView = LayoutInflater.
                from(context).
                inflate(R.layout.staffinfo_layout, viewGroup, false);
        return new StaffViewHolder(itemView, context);
    }


    public static class StaffViewHolder extends RecyclerView.ViewHolder {

        Context context;
        @InjectView(R.id.imgAvartar)
        ImageView imgAvartar;
        @InjectView(R.id.tvName)
        TextView tvName;
        @InjectView(R.id.staffInfoItem)
        LinearLayout staffInfoItem;


        public StaffViewHolder(View v, Context context) {
            super(v);
            this.context = context;
            ButterKnife.inject(this, v);
        }
    }
}
