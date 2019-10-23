package emn.southcoder.attendance.Interface;

import emn.southcoder.attendance.DeviceApiResponse;
import emn.southcoder.attendance.EventsApiResponse;
import emn.southcoder.attendance.UsersApiResponse;
import emn.southcoder.attendance.model.Attendance;
import emn.southcoder.attendance.model.Attendances;
import emn.southcoder.attendance.model.Device;
import emn.southcoder.attendance.model.EjeepTransaction;
import emn.southcoder.attendance.model.EjeepTransactions;

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


    // Event
    @GET("event")
    Call<EventsApiResponse> getEvents();

    @POST("attendance")
    Call<Attendance> createPost(@Body Attendance attendance);

    @POST("attendance/bulkcreate")
    Call<Attendances> createPost(@Body Attendances attendances);

}
