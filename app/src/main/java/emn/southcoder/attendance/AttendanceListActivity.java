package emn.southcoder.attendance;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import emn.southcoder.attendance.model.Attendance;
import emn.southcoder.attendance.DatabaseHelper;
import emn.southcoder.attendance.model.Attendances;

public class AttendanceListActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_list);
        InitDB();

        ListView AttendanceListView = findViewById(R.id.listView);

       // ArrayList<Attendance> _AttendanceList = dbHelper.getAllAttendanceLogs();
        ArrayList<String> _AttendanceList = dbHelper.viewAttendance();


        ArrayList<String> AttendanceList = new ArrayList<String>();

      for (String attendance : _AttendanceList) {
//          AttendanceList.add( attendance.getMCCNumber() + " - " +  attendance.getCardSerial());
           AttendanceList.add(attendance);
      }
//        AttendanceList.add("person1");
//        AttendanceList.add("person2");
//        AttendanceList.add("person3");
//        AttendanceList.add("person4");
//        AttendanceList.add("person5");
        //Attendances attendances = new Attendances(AttendanceList);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, AttendanceList);

        AttendanceListView.setAdapter(arrayAdapter);
    }

    private void InitDB() {
        dbHelper = DatabaseHelper.getInstance(this);
    }
}
