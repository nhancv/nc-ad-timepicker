package cvnhan.android.calendarsample.model;

/**
 * Created by cvnhan on 11-Jun-15.
 */
public class ServiceInfo {
    public String name;
    public String duration;
    public boolean isClicked=false;

    public ServiceInfo() {
    }

    public ServiceInfo(String name, String duration) {
        this.name = name;
        this.duration = duration;
        isClicked=false;
    }

}
