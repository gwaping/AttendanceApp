package emn.southcoder.e_jeep.model;

import com.google.gson.annotations.SerializedName;

public class Device {
    @SerializedName("DeviceId") private String DeviceId;
    @SerializedName("Apps") private String Apps;

    public Device() {
    }

    public Device(String deviceid, String apps) {
        this.DeviceId = deviceid;
        this.Apps = apps;
    }

    public String getDeviceId() {
        return DeviceId;
    }

    public void setDeviceId(String deviceId) {
        DeviceId = deviceId;
    }

    public String getApps() {
        return Apps;
    }

    public void setApps(String apps) {
        Apps = apps;
    }
}
