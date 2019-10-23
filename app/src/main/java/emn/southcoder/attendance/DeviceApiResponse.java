package emn.southcoder.attendance;

import com.google.gson.annotations.SerializedName;
import emn.southcoder.attendance.model.Device;

public class DeviceApiResponse {
    @SerializedName("message")
    String message;

    @SerializedName("obj")
    Device obj;
}
