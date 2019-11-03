package emn.southcoder.attendance.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Attendances {
    @SerializedName("Attendances")
    ArrayList<Attendance> attendanceArrayList;

    public Attendances(ArrayList<Attendance> attendanceArrayList) {
        this.attendanceArrayList = attendanceArrayList;
    }

    public ArrayList<Attendance> getAttendanceArrayList() {
        return attendanceArrayList;
    }

    public void setAttendanceArrayList(ArrayList<Attendance> attendanceArrayList) {
        this.attendanceArrayList = attendanceArrayList;
    }
}
