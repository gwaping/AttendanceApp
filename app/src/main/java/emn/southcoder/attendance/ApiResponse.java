package emn.southcoder.attendance;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    @SerializedName("message")
    String message;

    @SerializedName("obj")
    String obj;
}
