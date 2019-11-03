package emn.southcoder.attendance;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import emn.southcoder.attendance.model.Attendance;


public class AttendanceApiResponse {
    @SerializedName("message")
    String message;

    @SerializedName("obj")
    ArrayList<Attendance> obj;
}
