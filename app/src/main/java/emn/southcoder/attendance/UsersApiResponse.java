package emn.southcoder.attendance;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import emn.southcoder.attendance.model.Users;

public class UsersApiResponse {
    @SerializedName("message")
    String message;

    @SerializedName("obj")
    List<Users> obj;
}
