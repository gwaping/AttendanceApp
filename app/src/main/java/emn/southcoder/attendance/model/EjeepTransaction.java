package emn.southcoder.attendance.model;

public class EjeepTransaction {
    public static final String TABLE_NAME = "ejeep";

    public static final String COLUMN_ID = "transactionid";
    public static final String COLUMN_CARDSERIAL = "cardserial";
    public static final String COLUMN_MCCNUMBER = "mccnumber";
    public static final String COLUMN_EFTPOSSERIAL = "EftposSerial";
    public static final String COLUMN_TRANSACTIONDATE = "transactiondate";
    public static final String COLUMN_CARDTYPE = "cardtype";
    public static final String COLUMN_ISEXPIRED = "isexpired";
    public static final String COLUMN_USERID = "userid";


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " TEXT PRIMARY KEY,"
                    + COLUMN_CARDSERIAL + " TEXT,"
                    + COLUMN_MCCNUMBER + " TEXT,"
                    + COLUMN_EFTPOSSERIAL + " TEXT,"
                    + COLUMN_TRANSACTIONDATE + " DATETIME DEFAULT (datetime('now','localtime')),"
                    + COLUMN_CARDTYPE + " TINYINT,"
                    + COLUMN_ISEXPIRED + " TINYINT,"
                    + COLUMN_USERID + " TEXT"
                    + ")";

    private String TransactionId;
    private String CardSerial;
    private String MCCNumber;
    private String EftposSerial;
    private String TransactionDate;
    private int CardType;
    private int IsExpired;
    private String UserId;

    public EjeepTransaction () {}

    public EjeepTransaction(String transactionid,
                            String cardSerial,
                            String mCCNumber,
                            String eftposSerial,
                            String transactionDate,
                            int cardType,
                            int isExpired, String userId) {
        TransactionId = transactionid;
        CardSerial = cardSerial;
        MCCNumber = mCCNumber;
        EftposSerial = eftposSerial;
        TransactionDate = transactionDate;
        CardType = cardType;
        IsExpired = isExpired;
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

    public int getCardType() {
        return CardType;
    }

    public void setCardType(int cardType) {
        CardType = cardType;
    }

    public int getIsExpired() {
        return IsExpired;
    }

    public void setIsExpired(int isExpired) {
        IsExpired = isExpired;
    }

    public String getTransactionDate() {
        return TransactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        TransactionDate = transactionDate;
    }
}
