package emn.southcoder.e_jeep;

import com.google.gson.annotations.SerializedName;
import emn.southcoder.e_jeep.model.Device;

public class DeviceApiResponse {
    @SerializedName("message")
    String message;

    @SerializedName("obj")
    Device obj;
}
