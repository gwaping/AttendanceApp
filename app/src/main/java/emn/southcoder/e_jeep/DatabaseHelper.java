package emn.southcoder.e_jeep;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import emn.southcoder.e_jeep.model.EjeepTransaction;
import emn.southcoder.e_jeep.model.Users;

import static android.content.ContentValues.TAG;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    public static final String DATABASE_NAME = "MCC.db";

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + EjeepTransaction.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Users.TABLE_NAME);
        onCreate(db);
    }

    public long insertUser(Integer id, String mccnumber, String job) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(Users.COLUMN_ID, id);
        values.put(Users.COLUMN_MCCNUMBER, mccnumber);
        values.put(Users.COLUMN_JOB, job);

        // insert row
        long ret_id = db.insert(Users.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return ret_id;
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

        // close db connection
        db.close();

        // return newly inserted row id
        return ret_id;
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

        SQLiteDatabase db = this.getWritableDatabase();

        try {
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
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // close db connection
            db.close();
        }

        // return notes list
        return ejeepTransactions;
    }

    public void deleteUserAll() {
        SQLiteDatabase db = null;

        try {
            db = this.getWritableDatabase();
            db.delete(Users.TABLE_NAME, null, null);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public void deleteEjeepAll() {
        SQLiteDatabase db = null;

        try {
            db = this.getWritableDatabase();
            db.delete(EjeepTransaction.TABLE_NAME, null, null);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public boolean isValidLogin(String mccno, String access) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sqlQuery = "select * from " + Users.TABLE_NAME + " where MCCNumber='" + mccno + "' and Job LIKE '%" + access + "%'";

        try {
            Cursor res = db.rawQuery(sqlQuery,null);
            res.moveToFirst();
            int counter = 0;

            while(res.isAfterLast() == false) {
                counter++;
                res.moveToNext();
            }

            if (counter > 0)
                return true;
            else
                return false;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
           db.close();
        }

        return false;
    }

//    private String cursorToString(Cursor crs) {
//        JSONArray arr = new JSONArray();
//        crs.moveToFirst();
//        while (!crs.isAfterLast()) {
//            int nColumns = crs.getColumnCount();
//            JSONObject row = new JSONObject();
//            for (int i = 0 ; i < nColumns ; i++) {
//                String colName = crs.getColumnName(i);
//                if (colName != null) {
//                    String val = "";
//                    try {
//                        switch (crs.getType(i)) {
//                            case Cursor.FIELD_TYPE_BLOB   : row.put(colName, crs.getBlob(i).toString()); break;
//                            case Cursor.FIELD_TYPE_FLOAT  : row.put(colName, crs.getDouble(i))         ; break;
//                            case Cursor.FIELD_TYPE_INTEGER: row.put(colName, crs.getLong(i))           ; break;
//                            case Cursor.FIELD_TYPE_NULL   : row.put(colName, null)                     ; break;
//                            case Cursor.FIELD_TYPE_STRING : row.put(colName, crs.getString(i))         ; break;
//                        }
//                    } catch (JSONException e) {
//                    }
//                }
//            }
//            arr.put(row);
//            if (!crs.moveToNext())
//                break;
//        }
//        crs.close(); // close the cursor
//        return arr.toString();
//    }
//
//    public JSONArray cur2Json(Cursor cursor) {
//
//        JSONArray resultSet = new JSONArray();
//        cursor.moveToFirst();
//        while (cursor.isAfterLast() == false) {
//            int totalColumn = cursor.getColumnCount();
//            JSONObject rowObject = new JSONObject();
//            for (int i = 0; i < totalColumn; i++) {
//                if (cursor.getColumnName(i) != null) {
//                    try {
//                        rowObject.put(cursor.getColumnName(i),
//                                cursor.getString(i));
//                    } catch (Exception e) {
//                        Log.d(TAG, e.getMessage());
//                    }
//                }
//            }
//            resultSet.put(rowObject);
//            cursor.moveToNext();
//        }
//
//        cursor.close();
//        return resultSet;
//
//    }
}


