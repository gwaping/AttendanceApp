package emn.southcoder.attendance;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import emn.southcoder.attendance.model.EjeepTransaction;

public class EjeepApiResponse {
    @SerializedName("message")
    String message;

    @SerializedName("obj")
    ArrayList<EjeepTransaction> obj;
}
