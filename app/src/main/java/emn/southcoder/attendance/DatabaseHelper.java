package emn.southcoder.attendance;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

// import models
import emn.southcoder.attendance.model.EjeepTransaction;
import emn.southcoder.attendance.model.Event;
import emn.southcoder.attendance.model.Users;
import emn.southcoder.attendance.model.Attendance;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "MCC.db";

    private static DatabaseHelper sInstance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Users.CREATE_TABLE);
        db.execSQL(EjeepTransaction.CREATE_TABLE);
        db.execSQL(Event.CREATE_TABLE);
        db.execSQL(Attendance.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + EjeepTransaction.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Event.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Users.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Attendance.TABLE_NAME);
        onCreate(db);
    }

    public long insertUser(Integer id, String mccnumber, String job) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            // `id` and `timestamp` will be inserted automatically.
            // no need to add them
            values.put(Users.COLUMN_ID, id);
            values.put(Users.COLUMN_MCCNUMBER, mccnumber);
            values.put(Users.COLUMN_JOB, job);

            // insert row
            long ret_id = db.insert(Users.TABLE_NAME, null, values);

            // return newly inserted row id
            return ret_id;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // close db connection
            db.close();
        }

        return 0;
    }

    public Users getUser(String mccnumber, String access) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor cursor = db.query(Users.TABLE_NAME,
                    new String[]{Users.COLUMN_ID, Users.COLUMN_MCCNUMBER, Users.COLUMN_JOB},
                    Users.COLUMN_MCCNUMBER + "=?" + " AND " + Users.COLUMN_JOB + " LIKE ?",
                    new String[]{mccnumber, "%" + access + "%"}, null, null, null, "1");

            if (cursor != null)
                cursor.moveToFirst();

            // prepare Users object
            Users user = new Users(
                    cursor.getInt(cursor.getColumnIndex(Users.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(Users.COLUMN_MCCNUMBER)),
                    cursor.getString(cursor.getColumnIndex(Users.COLUMN_JOB)));

            // close the db connection
            cursor.close();

            return user;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public long insertEjeepTransaction(String transctionid, String userid, String eftposserial, String cardserial, String mccnumber, Integer cardtype, Integer isexpired) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            // `id` and `timestamp` will be inserted automatically.
            // no need to add them
            values.put(EjeepTransaction.COLUMN_ID, transctionid);
            values.put(EjeepTransaction.COLUMN_USERID, userid);
            values.put(EjeepTransaction.COLUMN_EFTPOSSERIAL, eftposserial);
            values.put(EjeepTransaction.COLUMN_CARDSERIAL, cardserial);
            values.put(EjeepTransaction.COLUMN_MCCNUMBER, mccnumber);
            values.put(EjeepTransaction.COLUMN_CARDTYPE, cardtype);
            values.put(EjeepTransaction.COLUMN_ISEXPIRED, isexpired);

            // insert row
            long ret_id = db.insert(EjeepTransaction.TABLE_NAME, null, values);

            // return newly inserted row id
            return ret_id;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // close db connection
            db.close();
        }

        return 0;
    }

    public EjeepTransaction getEjeepTransaction(String mccnumber) {

        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor cursor = db.query(EjeepTransaction.TABLE_NAME,
                    new String[]{EjeepTransaction.COLUMN_ID, EjeepTransaction.COLUMN_USERID, EjeepTransaction.COLUMN_EFTPOSSERIAL, EjeepTransaction.COLUMN_CARDSERIAL, EjeepTransaction.COLUMN_MCCNUMBER,EjeepTransaction.COLUMN_CARDTYPE, EjeepTransaction.COLUMN_ISEXPIRED, EjeepTransaction.COLUMN_TRANSACTIONDATE},
                    Users.COLUMN_MCCNUMBER + "=?",
                    new String[]{mccnumber}, null, null, EjeepTransaction.COLUMN_TRANSACTIONDATE + " DESC", null);

            if (cursor != null)
                cursor.moveToFirst();

            // prepare EjeepTransaction object
            EjeepTransaction ejeepTransaction = new EjeepTransaction(
                    cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_CARDSERIAL)),
                    cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_MCCNUMBER)),
                    cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_EFTPOSSERIAL)),
                    cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_TRANSACTIONDATE)),
                    cursor.getInt(cursor.getColumnIndex(EjeepTransaction.COLUMN_CARDTYPE)),
                    cursor.getInt(cursor.getColumnIndex(EjeepTransaction.COLUMN_ISEXPIRED)),
                    cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_USERID)));


            // close the db connection
            cursor.close();

            return ejeepTransaction;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<EjeepTransaction> getAllEjeepTransactions() {
        ArrayList<EjeepTransaction> ejeepTransactions = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + EjeepTransaction.TABLE_NAME + " ORDER BY " + EjeepTransaction.COLUMN_TRANSACTIONDATE + " ASC";

        try (SQLiteDatabase db = this.getWritableDatabase()) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    EjeepTransaction ejeepTransaction = new EjeepTransaction();
                    ejeepTransaction.setId(cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_ID)));
                    ejeepTransaction.setUserId(cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_USERID)));
                    ejeepTransaction.setCardSerial(cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_CARDSERIAL)));
                    ejeepTransaction.setMCCNumber(cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_MCCNUMBER)));
                    ejeepTransaction.setEftposSerial(cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_EFTPOSSERIAL)));
                    ejeepTransaction.setTransactionDate(cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_TRANSACTIONDATE)));
                    ejeepTransaction.setCardType(cursor.getInt(cursor.getColumnIndex(EjeepTransaction.COLUMN_CARDTYPE)));
                    ejeepTransaction.setIsExpired(cursor.getInt(cursor.getColumnIndex(EjeepTransaction.COLUMN_ISEXPIRED)));

                    ejeepTransactions.add(ejeepTransaction);
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // close db connection

        // return notes list
        return ejeepTransactions;
    }

    public ArrayList<EjeepTransaction> getEjeepTransactions(int limitcount) {
        ArrayList<EjeepTransaction> ejeepTransactions = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + EjeepTransaction.TABLE_NAME + " ORDER BY " + EjeepTransaction.COLUMN_TRANSACTIONDATE + " ASC LIMIT " + limitcount;

        try (SQLiteDatabase db = this.getWritableDatabase()) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    EjeepTransaction ejeepTransaction = new EjeepTransaction();
                    ejeepTransaction.setId(cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_ID)));
                    ejeepTransaction.setUserId(cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_USERID)));
                    ejeepTransaction.setCardSerial(cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_CARDSERIAL)));
                    ejeepTransaction.setMCCNumber(cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_MCCNUMBER)));
                    ejeepTransaction.setEftposSerial(cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_EFTPOSSERIAL)));
                    ejeepTransaction.setTransactionDate(cursor.getString(cursor.getColumnIndex(EjeepTransaction.COLUMN_TRANSACTIONDATE)));
                    ejeepTransaction.setCardType(cursor.getInt(cursor.getColumnIndex(EjeepTransaction.COLUMN_CARDTYPE)));
                    ejeepTransaction.setIsExpired(cursor.getInt(cursor.getColumnIndex(EjeepTransaction.COLUMN_ISEXPIRED)));

                    ejeepTransactions.add(ejeepTransaction);
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // close db connection

        // return notes list
        return ejeepTransactions;
    }

    public int GetTransactionCount() {
        int retVal = 0;
        String selectQuery = "SELECT COUNT(*) AS totalTransaction FROM " + EjeepTransaction.TABLE_NAME;

        try (SQLiteDatabase db = this.getWritableDatabase()) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor != null)
                cursor.moveToFirst();

            retVal = cursor.getInt(cursor.getColumnIndex("totalTransaction"));

            cursor.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        // close db connection

        return retVal;
    }



    public int GetAttendanceCount() {
        int retVal = 0;
        String selectQuery = "SELECT COUNT(DISTINCT " + Attendance.COLUMN_CARDSERIAL + " ) AS totalTransaction FROM " + Attendance.TABLE_NAME;

        try (SQLiteDatabase db = this.getWritableDatabase()) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor != null)
                cursor.moveToFirst();

            retVal = cursor.getInt(cursor.getColumnIndex("totalTransaction"));

            cursor.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        // close db connection

        return retVal;
    }


    public String GetUserRole(String mccNumber) {
        String retVal = "";

        String selectQuery = "SELECT Job FROM " + Users.TABLE_NAME + " WHERE MCCNumber = '"+mccNumber+"'";

        try (SQLiteDatabase db = this.getWritableDatabase()) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor != null)
                cursor.moveToFirst();

            retVal = cursor.getString(cursor.getColumnIndex(Users.COLUMN_JOB));

            cursor.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        // close db connection

        return retVal;
    }

    public void deleteUserAll() {

        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete(Users.TABLE_NAME, null, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteEjeepTransaction(int limitcount) {
        // get readable database as we are not inserting anything
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            String delQuery;
            delQuery = "DELETE FROM " + EjeepTransaction.TABLE_NAME + " ORDER BY " + EjeepTransaction.COLUMN_TRANSACTIONDATE + " ASC LIMIT " + limitcount;
            db.execSQL(delQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteEjeepAll() {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete(EjeepTransaction.TABLE_NAME, null, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAttendanceAll() {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete(Attendance.TABLE_NAME, null, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isValidLogin(String mccno, String access) {

        try (SQLiteDatabase db = this.getWritableDatabase()) {
            String sqlQuery = "select * from " + Users.TABLE_NAME + " where MCCNumber='" + mccno + "' and Job LIKE '%" + access + "%'";
            Log.d ("GML SqlString : ", sqlQuery);
            Cursor res = db.rawQuery(sqlQuery, null);
            res.moveToFirst();
            int counter = 0;

            while (!res.isAfterLast()) {
                counter++;
                res.moveToNext();
            }

            res.close();

            return counter > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    // added by gio

    public void deleteEventAll() {

        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete(Event.TABLE_NAME, null, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long insertEvent(Integer id, String eventname, String description, String date, String numtaps, String threshholdtime1, String threshholdtime2) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            // `id` and `timestamp` will be inserted automatically.
            // no need to add them

            values.put(Event.COLUMN_ID, id);
            values.put(Event.COLUMN_EVENTNAME, eventname);
            values.put(Event.COLUMN_DESCRIPTION, description);
            values.put(Event.COLUMN_DATE, date);
            values.put(Event.COLUMN_NUMTAPS, numtaps);
            values.put(Event.COLUMN_THRESHOLDTIME1, threshholdtime1);
            values.put(Event.COLUMN_THRESHOLDTIME2, threshholdtime1);

            // insert row
            long ret_id = db.insert(Event.TABLE_NAME, null, values);

            // return newly inserted row id
            return ret_id;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // close db connection
            db.close();
        }

        return 0;
    }

    public Event getEvent(String eventName) {

        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor cursor = db.query(Event.TABLE_NAME,
                    new String[]{ Event.COLUMN_ID,
                                  Event.COLUMN_EVENTNAME,
                                  Event.COLUMN_DESCRIPTION,
                                  Event.COLUMN_DATE,
                                  Event.COLUMN_NUMTAPS,
                                  Event.COLUMN_THRESHOLDTIME1,
                                  Event.COLUMN_THRESHOLDTIME2},
                    Event.COLUMN_EVENTNAME + "=?",
                    new String[]{eventName}, null, null, Event.COLUMN_DATE + " DESC", null);

            if (cursor != null)
                cursor.moveToFirst();

            // prepare Event object
            Event event = new Event(
                    cursor.getInt(cursor.getColumnIndex(Event.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(Event.COLUMN_EVENTNAME)),
                    cursor.getString(cursor.getColumnIndex(Event.COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndex(Event.COLUMN_DATE)),
                    cursor.getString(cursor.getColumnIndex(Event.COLUMN_NUMTAPS)),
                    cursor.getString(cursor.getColumnIndex(Event.COLUMN_THRESHOLDTIME1)),
                    cursor.getString(cursor.getColumnIndex(Event.COLUMN_THRESHOLDTIME1)));

            // close the db connection
            cursor.close();

            return event;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Event> getAllEvents() {
        ArrayList<Event> eventList = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + Event.TABLE_NAME + " ORDER BY " + Event.COLUMN_DATE + " DESC";

        try (SQLiteDatabase db = this.getWritableDatabase()) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    Event event = new Event();
                    event.setId(cursor.getInt(cursor.getColumnIndex(Event.COLUMN_ID)));
                    event.setEventName(cursor.getString(cursor.getColumnIndex(Event.COLUMN_EVENTNAME)));
                    event.setDescription(cursor.getString(cursor.getColumnIndex(Event.COLUMN_DESCRIPTION)));
                    event.setDate(cursor.getString(cursor.getColumnIndex(Event.COLUMN_DATE)));
                    event.setNumTaps(cursor.getString(cursor.getColumnIndex(Event.COLUMN_NUMTAPS)));
                    event.setThreshholdTime1(cursor.getString(cursor.getColumnIndex(Event.COLUMN_THRESHOLDTIME1)));
                    event.setThreshholdTime2(cursor.getString(cursor.getColumnIndex(Event.COLUMN_THRESHOLDTIME2)));

                    Log.d("GML getAllEvent ", event.getEventName());
                    eventList.add(event);
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // close db connection

        // return event list

        return eventList;
    }

    // --------- Attendance  -------------------
    public long insertAttendance( String transctionid,
                                  String userid,
                                  String eftposserial,
                                  String cardserial,
                                  String mccnumber,
                                  String eventCode,
                                  String eventSession,
                                  String eventInOut,
                                  String eventTime ) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            // `id` and `timestamp` will be inserted automatically.
            // no need to add them
            values.put(Attendance.COLUMN_ID, transctionid);
            values.put(Attendance.COLUMN_USERID, userid);
            values.put(Attendance.COLUMN_EFTPOSSERIAL, eftposserial);
            values.put(Attendance.COLUMN_CARDSERIAL, cardserial);
            values.put(Attendance.COLUMN_MCCNUMBER, mccnumber);
            values.put(Attendance.COLUMN_EVENTCODE, eventCode);
            values.put(Attendance.COLUMN_EVENTSESSION, eventSession);
            values.put(Attendance.COLUMN_EVENTINOUT, eventInOut);
            values.put(Attendance.COLUMN_EVENTTIME, eventTime);

            // insert row
            long ret_id = db.insert(Attendance.TABLE_NAME, null, values);

            // return newly inserted row id
            return ret_id;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // close db connection
            db.close();
        }

        return 0;
    }

    public Attendance getAttendance(String mccnumber) {

        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor cursor = db.query(Attendance.TABLE_NAME,
                    new String[]{Attendance.COLUMN_ID,
                            Attendance.COLUMN_USERID,
                            Attendance.COLUMN_EFTPOSSERIAL,
                            Attendance.COLUMN_CARDSERIAL,
                            Attendance.COLUMN_MCCNUMBER,
                            Attendance.COLUMN_EVENTCODE,
                            Attendance.COLUMN_EVENTSESSION,
                            Attendance.COLUMN_EVENTINOUT,
                            Attendance.COLUMN_EVENTTIME,
                            Attendance.COLUMN_TRANSACTIONDATE},
                    Users.COLUMN_MCCNUMBER + "=?",
                    new String[]{mccnumber}, null, null, Attendance.COLUMN_TRANSACTIONDATE + " DESC", null);

            if (cursor != null)
                cursor.moveToFirst();

            // prepare Attendance object
            Attendance attendance = new Attendance(
                    cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_CARDSERIAL)),
                    cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_MCCNUMBER)),
                    cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_EFTPOSSERIAL)),
                    cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_TRANSACTIONDATE)),
                    cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_EVENTCODE)),
                    cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_EVENTSESSION)),
                    cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_EVENTINOUT)),
                    cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_EVENTTIME)),
                    cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_USERID)));


            // close the db connection
            cursor.close();

            return attendance;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Attendance> getAllAttendanceLogs() {
        ArrayList<Attendance> attendanceLogs = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + Attendance.TABLE_NAME + " ORDER BY " +  Attendance.COLUMN_TRANSACTIONDATE + " ASC";

        try (SQLiteDatabase db = this.getWritableDatabase()) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    Attendance attendance = new Attendance();
                    attendance.setId(cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_ID)));
                    attendance.setUserId(cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_USERID)));
                    attendance.setCardSerial(cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_CARDSERIAL)));
                    attendance.setMCCNumber(cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_MCCNUMBER)));
                    attendance.setEftposSerial(cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_EFTPOSSERIAL)));
                    attendance.setTransactionDate(cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_TRANSACTIONDATE)));
                    attendance.setEventCode(cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_EVENTCODE)));
                    attendance.setEventInOut(cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_EVENTINOUT)));
                    attendance.setEventSession(cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_EVENTSESSION)));
                    attendance.setEventTime(cursor.getString(cursor.getColumnIndex(Attendance.COLUMN_EVENTTIME)));

                    attendanceLogs.add(attendance);
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // close db connection

        // return notes list
        return attendanceLogs;
    }

    public int GetTotalTaps(String mccno, Integer eventId) {
        int retVal = 0;
        String selectQuery = "SELECT COUNT(*) AS Taps FROM " + Attendance.TABLE_NAME + " where MCCNumber='" + mccno.trim() + "' and EventCode ='" + eventId + "'";

        try (SQLiteDatabase db = this.getWritableDatabase()) {
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor != null)
                cursor.moveToFirst();

            retVal = cursor.getInt(cursor.getColumnIndex("Taps"));

            cursor.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        // close db connection

        return retVal;
    }



}


