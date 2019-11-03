package emn.southcoder.attendance.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Event {
    public static final String TABLE_NAME = "events";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_EVENTNAME = "EventName";
    public static final String COLUMN_DESCRIPTION = "Description";
    public static final String COLUMN_DATE = "EventDate";
    public static final String COLUMN_NUMTAPS = "NumTaps";
    public static final String COLUMN_THRESHOLDTIME1 = "ThreshholdTime1";
    public static final String COLUMN_THRESHOLDTIME2 = "ThreshholdTime2";

    @SerializedName("id") private int id;
    @SerializedName("EventName") private String EventName;
    @SerializedName("Description") private String Description;
    @SerializedName("EventDate") private String EventDate;
    @SerializedName("NumTaps") private String NumTaps;
    @SerializedName("ThreshholdTime1") private String ThreshholdTime1;
    @SerializedName("ThreshholdTime2") private String ThreshholdTime2;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_EVENTNAME + " TEXT,"
                    + COLUMN_DESCRIPTION + " TEXT,"
                    + COLUMN_DATE + " TEXT,"
                    + COLUMN_NUMTAPS + " TEXT,"
                    + COLUMN_THRESHOLDTIME1 + " TEXT,"
                    + COLUMN_THRESHOLDTIME2 + " TEXT"
                    + ")";

    public Event() {
    }

    public Event(int id,
                 String eventname,
                 String description,
                 String date,
                 String numtaps,
                 String threshholdtime1,
                 String threshholdtime2 ) {
        this.id = id;
        this.EventName = eventname;
        this.Description = description;
        this.EventDate = date;
        this.NumTaps = numtaps;
        this.ThreshholdTime1 = threshholdtime1;
        this.ThreshholdTime2 = threshholdtime2;
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

    public String getNumTaps() {
        if (NumTaps == null) {
            NumTaps = "0";
        }
        return NumTaps;
    }
    public void setNumTaps(String numtaps) {
        NumTaps = numtaps;
    }

    public String getThreshholdTime1() {
        if (ThreshholdTime1 == null) {
            ThreshholdTime1 = "";
        }
        return ThreshholdTime1;
    }
    public void setThreshholdTime1(String threshholdTime1) {
        ThreshholdTime1 = threshholdTime1;
    }

    public String getThreshholdTime2() {
        if (ThreshholdTime2 == null) {
            ThreshholdTime2 = "";
        }
        return ThreshholdTime2;
    }
    public void setThreshholdTime2(String threshholdTime2) {
        ThreshholdTime2 = threshholdTime2;
    }
}
