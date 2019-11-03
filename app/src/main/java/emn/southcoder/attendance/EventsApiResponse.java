package emn.southcoder.attendance;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import emn.southcoder.attendance.model.Event;

public class EventsApiResponse {
    @SerializedName("message")
     String message;

    @SerializedName("obj")
    List<Event> obj;
}
