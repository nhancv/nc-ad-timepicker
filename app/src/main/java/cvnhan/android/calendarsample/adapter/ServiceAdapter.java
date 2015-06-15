package cvnhan.android.calendarsample.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cvnhan.android.calendarsample.MainActivity;
import cvnhan.android.calendarsample.R;
import cvnhan.android.calendarsample.model.ServiceInfo;

/**
 * Created by cvnhan on 11-Jun-15.
 */
public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    public Context context;
    private List<ServiceInfo> serviceInfos;
    public MainActivity mainActivity;

    public ServiceAdapter() {
        this.serviceInfos = new ArrayList<>();
    }

    public ServiceAdapter(List<ServiceInfo> serviceInfos, MainActivity mainActivity) {
        this.serviceInfos = serviceInfos;
        this.mainActivity = mainActivity;
    }


    @Override
    public int getItemCount() {
        return serviceInfos.size();
    }

    public ServiceInfo getItemClicked() {
        for (ServiceInfo serviceInfo : serviceInfos) {
            if (serviceInfo.isClicked) return serviceInfo;
        }
        return null;
    }

    public void setItemClicked(int index) {
        for (int i = 0; i < serviceInfos.size(); i++) {
            ServiceInfo serviceInfo = serviceInfos.get(i);
            if (i == index) {
                serviceInfo.isClicked = !serviceInfo.isClicked;
                if(serviceInfo.isClicked){
                    mainActivity.createObj(serviceInfo.duration);
                }else{
                    mainActivity.releaseObj();
                }
            } else {
                serviceInfo.isClicked = false;
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final ServiceViewHolder serviceViewHolder, final int i) {
        final ServiceInfo item = serviceInfos.get(i);
        serviceViewHolder.tvName.setText(item.name);
        serviceViewHolder.tvDuration.setText("("+item.duration+" m)");

        LinearLayout serviceInfoItem = serviceViewHolder.serviceInfoItem;
        if (item.isClicked) {
            serviceViewHolder.tvName.setTextColor(Color.parseColor("#f6921e"));
            serviceViewHolder.tvDuration.setTextColor(Color.parseColor("#f6921e"));
            serviceInfoItem.setBackgroundColor(Color.parseColor("#FFFFCC"));
        } else {
            serviceViewHolder.tvName.setTextColor(Color.BLACK);
            serviceViewHolder.tvDuration.setTextColor(Color.BLACK);
            serviceInfoItem.setBackgroundColor(Color.TRANSPARENT);
        }

        serviceInfoItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setItemClicked(i);
                if (getItemClicked() != null)
                    mainActivity.updateHeaderService(true);
                else
                    mainActivity.updateHeaderService(false);
            }
        });

    }

    @Override
    public ServiceViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        View itemView = LayoutInflater.
                from(context).
                inflate(R.layout.serviceinfo_layout, viewGroup, false);
        return new ServiceViewHolder(itemView, context);
    }


    public static class ServiceViewHolder extends RecyclerView.ViewHolder {

        Context context;
        @InjectView(R.id.tvName)
        TextView tvName;
        @InjectView(R.id.tvDuration)
        TextView tvDuration;
        @InjectView(R.id.serviceInfoItem)
        LinearLayout serviceInfoItem;


        public ServiceViewHolder(View v, Context context) {
            super(v);
            this.context = context;
            ButterKnife.inject(this, v);
        }
    }
}
