package emn.southcoder.attendance.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Events {
    @SerializedName("Events")
    ArrayList<Event> eventArrayList;

    public Events(ArrayList<Event> eventArrayList) {
        this.eventArrayList = eventArrayList;
    }

    public ArrayList<Event> getEventArrayList() {
        return eventArrayList;
    }

    public void setEventArrayList(ArrayList<Event> eventArrayList) {
        this.eventArrayList = eventArrayList;
    }
}
