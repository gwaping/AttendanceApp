package emn.southcoder.e_jeep.Interface;

import emn.southcoder.e_jeep.DeviceApiResponse;
import emn.southcoder.e_jeep.UsersApiResponse;
import emn.southcoder.e_jeep.model.Device;
import emn.southcoder.e_jeep.model.EjeepTransaction;
import emn.southcoder.e_jeep.model.EjeepTransactions;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface MCCPlaceHolderApi {
    @GET("mobileuser")
    Call<UsersApiResponse> getUsers();

    @POST("androiddevices/verifyid")
    Call<DeviceApiResponse> verifyDevice(@Body Device device);

    @POST("ejeeptransaction")
    Call<EjeepTransaction> createPost(@Body EjeepTransaction ejeepTransaction);

    @POST("ejeeptransaction/bulkcreate")
    Call<EjeepTransactions> createPost(@Body EjeepTransactions ejeepTransactions);
}
