package emn.southcoder.attendance.model;

import com.google.gson.annotations.SerializedName;

public class Users {
    public static final String TABLE_NAME = "users";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MCCNUMBER = "MCCNumber";
    public static final String COLUMN_JOB = "Job";

    @SerializedName("id") private int id;
    @SerializedName("MCCNumber") private String MCCNumber;
    @SerializedName("Job") private String Job;

    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_MCCNUMBER + " TEXT,"
                    + COLUMN_JOB + " TEXT"
                    + ")";

    public Users() {
    }

    public Users(int id, String mccnumber, String job) {
        this.id = id;
        this.MCCNumber = mccnumber;
        this.Job = job;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMCCNumber() {
        return MCCNumber;
    }

    public void setMCCNumber(String MCCNumber) {
        this.MCCNumber = MCCNumber;
    }

    public String getJob() {
        return Job;
    }

    public void setJob(String job) {
        Job = job;
    }
}
