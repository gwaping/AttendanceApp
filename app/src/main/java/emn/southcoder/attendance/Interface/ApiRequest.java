package emn.southcoder.attendance.Interface;

import com.google.gson.JsonObject;

import emn.southcoder.attendance.ApiResponse;
import emn.southcoder.attendance.AttendanceApiResponse;
import emn.southcoder.attendance.EjeepApiResponse;
import emn.southcoder.attendance.UsersApiResponse;
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

    @POST("attendance/bulkcreate")
    @Headers({
            "Content-Type: application/json;charset=UTF-8"
    })
    Call<AttendanceApiResponse> postAttendances (@Body JsonObject Attendances);


}
