package emn.southcoder.e_jeep;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import emn.southcoder.e_jeep.model.EjeepTransaction;

public class EjeepApiResponse {
    @SerializedName("message")
    String message;

    @SerializedName("obj")
    ArrayList<EjeepTransaction> obj;
}
