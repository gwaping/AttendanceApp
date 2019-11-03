package emn.southcoder.attendance.model;

public class Attendance {
    public static final String TABLE_NAME = "attendance";

    public static final String COLUMN_ID = "transactionid";
    public static final String COLUMN_CARDSERIAL = "cardserial";
    public static final String COLUMN_MCCNUMBER = "mccnumber";
    public static final String COLUMN_EFTPOSSERIAL = "EftposSerial";
    public static final String COLUMN_TRANSACTIONDATE = "transactiondate";
    public static final String COLUMN_EVENTCODE = "eventcode";
    public static final String COLUMN_EVENTSESSION = "eventsession";
    public static final String COLUMN_EVENTINOUT = "eventinout";
    public static final String COLUMN_EVENTTIME = "eventtime";
    public static final String COLUMN_USERID = "userid";


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " TEXT PRIMARY KEY,"
                    + COLUMN_CARDSERIAL + " TEXT,"
                    + COLUMN_MCCNUMBER + " TEXT,"
                    + COLUMN_EFTPOSSERIAL + " TEXT,"
                    + COLUMN_TRANSACTIONDATE + " DATETIME DEFAULT (datetime('now','localtime')),"
                    + COLUMN_EVENTCODE + " TEXT,"
                    + COLUMN_EVENTSESSION + " TEXT,"
                    + COLUMN_EVENTINOUT + " TEXT,"
                    + COLUMN_EVENTTIME + " DATETIME,"
                    + COLUMN_USERID + " TEXT"
                    + ")";

    private String TransactionId;
    private String CardSerial;
    private String MCCNumber;
    private String EftposSerial;
    private String TransactionDate;
    private String EventCode;
    private String EventSession;
    private String EventInOut;
    private String EventTime;
    private String UserId;

    public Attendance() {}

    public Attendance(String transactionid,
                      String cardSerial,
                      String mCCNumber,
                      String eftposSerial,
                      String transactionDate,
                      String eventCode,
                      String eventSession,
                      String eventInOut,
                      String eventTime,
                      String userId) {
        TransactionId = transactionid;
        CardSerial = cardSerial;
        MCCNumber = mCCNumber;
        EftposSerial = eftposSerial;
        TransactionDate = transactionDate;
        EventCode = eventCode;
        EventSession = eventSession;
        EventInOut = eventInOut;
        EventTime = eventTime;
        UserId = userId;
    }

    public String getId() {
        return TransactionId;
    }

    public void setId(String id) {
        TransactionId = id;
    }

    public String getUserId() {
        return UserId;
    }
    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getCardSerial() {
        return CardSerial;
    }
    public void setCardSerial(String cardSerial) {
        CardSerial = cardSerial;
    }

    public String getMCCNumber() {
        return MCCNumber;
    }
    public void setMCCNumber(String MCCNumber) {
        this.MCCNumber = MCCNumber;
    }

    public String getEftposSerial() {
        return EftposSerial;
    }
    public void setEftposSerial(String eftposSerial) {
        EftposSerial = eftposSerial;
    }

    public String getTransactionDate() {
        return TransactionDate;
    }
    public void setTransactionDate(String transactionDate) {
        TransactionDate = transactionDate;
    }

    public String getEventCode() {
        return EventCode;
    }
    public void setEventCode(String eventCode) { EventCode = eventCode; }

    public String getEventSession() { return EventSession; }
    public void setEventSession(String eventSession) { EventSession = eventSession; }

    public String getEventInOut() {
        return EventInOut;
    }
    public void setEventInOut(String eventInOut) { EventInOut = eventInOut; }

    public String getEventTime() {
        return EventTime;
    }
    public void setEventTime(String eventTime) { EventTime = eventTime; }

}
