package emn.southcoder.e_jeep;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import emn.southcoder.e_jeep.model.Users;

public class UsersApiResponse {
    @SerializedName("message")
    String message;

    @SerializedName("obj")
    List<Users> obj;
}
