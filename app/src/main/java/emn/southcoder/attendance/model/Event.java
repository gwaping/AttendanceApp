package emn.southcoder.attendance.model;

import com.google.gson.annotations.SerializedName;

public class Event {
    public static final String TABLE_NAME = "events";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_EVENTNAME = "EventName";
    public static final String COLUMN_DESCRIPTION = "Description";
    public static final String COLUMN_DATE = "EventDate";

    @SerializedName("id") private int id;
    @SerializedName("EventName") private String EventName;
    @SerializedName("Description") private String Description;
    @SerializedName("EventDate") private String EventDate;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_EVENTNAME + " TEXT,"
                    + COLUMN_DESCRIPTION + " TEXT,"
                    + COLUMN_DATE + " TEXT"
                    + ")";

    public Event() {
    }

    public Event(int id,
                 String eventname,
                 String description,
                 String date) {
        this.id = id;
        this.EventName = eventname;
        this.Description = description;
        this.EventDate = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEventName() {
        return EventName;
    }

    public void setEventName(String _eventname) {
        this.EventName = _eventname;
    }

    public String getDescription() {
        return Description;
    }
    public void setDescription(String _description) {
        Description = _description;
    }

    public String getDate() {
        return EventDate;
    }
    public void setDate(String _date) {
        EventDate = _date;
    }
}
