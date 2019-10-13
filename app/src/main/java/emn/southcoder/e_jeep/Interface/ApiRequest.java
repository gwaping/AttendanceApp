package emn.southcoder.e_jeep.Interface;

import com.google.gson.JsonObject;

import emn.southcoder.e_jeep.ApiResponse;
import emn.southcoder.e_jeep.EjeepApiResponse;
import emn.southcoder.e_jeep.UsersApiResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiRequest {
    @GET("mobileuser")
    Call<UsersApiResponse> getUsers();

    @POST("androiddevices/verifyid")
    @Headers({
            "Content-Type: application/json;charset=UTF-8"
    })
    Call<ApiResponse> verifyDevice(@Body JsonObject device);

    @POST("ejeeptransaction/bulkcreate")
    @Headers({
            "Content-Type: application/json;charset=UTF-8"
    })
    Call<EjeepApiResponse> postEJeepTransactions(@Body JsonObject EJeepTransactions);
}
