package emn.southcoder.e_jeep;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import emn.southcoder.e_jeep.Interface.MCCPlaceHolderApi;
import emn.southcoder.e_jeep.model.Device;
import emn.southcoder.e_jeep.model.EjeepTransaction;
import emn.southcoder.e_jeep.model.EjeepTransactions;
import emn.southcoder.e_jeep.model.Users;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private TextView textViewBlock, textViewGreetings, textViewLogTimeLabel;
    //TextView textViewMccNo, textViewSerialNo, textViewFName, textViewMName, textViewLName, textViewIssueDate, textViewExpiryDate, textViewInfo, textViewTagInfo;

    private boolean isNFCSupported = false;
    private boolean loggedIn = false;
    private boolean loginAlertShown = false;
    private AlertDialog loginAlert;
    private Tag tag;
    private MifareClassic mifareClassicTag;
//    Timer timerObj;
//    TimerTask timerTaskObj;

    private Handler handler;
//    private int delay = 2*1000;
    private Animation animBlink;
    private View vwLogin;
    private Spinner spinnerTimeAllowance;
    private Button btnVerifyDevice, btnSyncUserList, btnUploadRidersLogs;
    private TextView status;
    private TelephonyManager tm;
    private DatabaseHelper dbHelper;
    private String deviceID = null;
    private int minuteThreshold = 2; //-- Default value
    private String mode;

    private String userMCCNo;
    private MCCPlaceHolderApi mccPlaceHolderApi;

//    private Handler mHandler;
//    private Runnable mRunnable;
//    private int mInterval = 300; // milliseconds
//    private boolean initialState = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter != null)
        {
            isNFCSupported = true;

            if (!nfcAdapter.isEnabled()) {
                Toast.makeText(this, "NFC is not enabled!", Toast.LENGTH_LONG).show();
                showNFCSettings();
            }
        }
        else {
            Toast.makeText(this, "NFC is not supported on this device!", Toast.LENGTH_LONG).show();
            finish();
        }

        Gson gson = new GsonBuilder().serializeNulls().create();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://mcc.alcagroupholdings.com:3500/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();

        mccPlaceHolderApi = retrofit.create(MCCPlaceHolderApi.class);

        textViewGreetings = findViewById(R.id.txt_greetings);
        textViewBlock = findViewById(R.id.block);

        showLoginAlert(this);

        //-- Get unique device ID
        tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 100);
        } else {
            deviceID = tm.getDeviceId();
            status.setText("Device ID: " + deviceID);

            if (doesDatabaseExist(this, "MCC.db"))
                InitDB();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100: { //--Read phone state
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission grznted!", Toast.LENGTH_LONG).show();

                    if (ShowDeviceID())
                        InitDB();

                } else {
                    this.CloseMainActivity();
                }

                return;
            }
        }
    }

    @Override
    protected void onResume() {
//        handler.postDelayed( runnable = new Runnable() {
//            public void run() {
//                clearMessage();
//                handler.postDelayed(runnable, delay);
//            }
//        }, delay);
//        btnSyncUserList = findViewById(R.id.btn_sync_user_list);
//        btnUploadRidersLogs = findViewById(R.id.btn_upload_data);

        super.onResume();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (isNFCSupported)
            setupForegroundDispatch(this, nfcAdapter);

//        if (!loggedIn) {
//            showLoginAlert(this);
//            //OpenLoginActivityDialog();
//        }
    }

    @Override
    protected void onPause() {
        //handler.removeCallbacks(runnable); //stop handler when activity not visible

        super.onPause();

        if (isNFCSupported)
            stopForegroundDispatch(this, nfcAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    private void InitDB() {
        dbHelper = DatabaseHelper.getInstance(this);
    }

    private boolean ShowDeviceID() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            deviceID = tm.getDeviceId();
            status.setText("Device ID: " + deviceID);

            return true;
        }

        return false;
    }

    public void VerifyUser(String mccno, String access) {
        if (dbHelper.isValidLogin(mccno, access)) {
            loggedIn = true; //-- Flag for user login status
            this.loginAlert.dismiss();
        }
        else Toast.makeText(this, "Invalid user", Toast.LENGTH_LONG).show();
    }

    public void CloseMainActivity() {
        finish();
    }

//    public void OpenLoginActivityDialog() {
//        loginActivity = new LoginActivity();
//        loginActivity.show(getSupportFragmentManager(), "Login");
//    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();

        //Toast.makeText(this, action, Toast.LENGTH_SHORT).show();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            //Toast.makeText(this, "ACTION_TECH_DISCOVERED", Toast.LENGTH_SHORT).show();
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            if (tag != null) {
                //clearMessage();
                readMifareClassic(tag);
            }

//            if (tag == null) {
//                textViewInfo.setText("tag == null");
//            }
//            else {
//                String tagInfo = tag.toString() + "\n";
//
//                tagInfo += "\nTag Id: \n";
//                byte[] tagId = tag.getId();
//                tagInfo += "length = " + tagId.length +"\n";
//
//                for(int i=0; i<tagId.length; i++){
//                    tagInfo += String.format("%02X", tagId[i] & 0xff) + " ";
//                }
//
//                tagInfo += "\n";
//
//                String[] techList = tag.getTechList();
//                tagInfo += "\nTech List\n";
//                tagInfo += "length = " + techList.length +"\n";
//
//                for(int i=0; i<techList.length; i++) {
//                    tagInfo += techList[i] + "\n ";
//                }
//
//                textViewInfo.setText(tagInfo);
//
//                //Only android.nfc.tech.MifareClassic specified in nfc_tech_filter.xml,
//                //so must be MifareClassic
//
                //nfcAdapter.ignore(tag, 1000, NfcAdapter.OnTagRemovedListener, null);
//            }
        }
        else {
            Toast.makeText(this, "onResume() : " + action, Toast.LENGTH_SHORT).show();
        }
    }

    public void readMifareClassic(Tag tag) {
        mifareClassicTag = MifareClassic.get(tag);

//        String typeInfoString = "--- MifareClassic tag ---\n";
//        int type = mifareClassicTag.getType();
//        switch(type){
//            case MifareClassic.TYPE_PLUS:
//                typeInfoString += "MifareClassic.TYPE_PLUS\n";
//                break;
//            case MifareClassic.TYPE_PRO:
//                typeInfoString += "MifareClassic.TYPE_PRO\n";
//                break;
//            case MifareClassic.TYPE_CLASSIC:
//                typeInfoString += "MifareClassic.TYPE_CLASSIC\n";
//                break;
//            case MifareClassic.TYPE_UNKNOWN:
//                typeInfoString += "MifareClassic.TYPE_UNKNOWN\n";
//                break;
//            default:
//                typeInfoString += "unknown...!\n";
//        }
//
//        int size = mifareClassicTag.getSize();
//        switch(size){
//            case MifareClassic.SIZE_1K:
//                typeInfoString += "MifareClassic.SIZE_1K\n";
//                break;
//            case MifareClassic.SIZE_2K:
//                typeInfoString += "MifareClassic.SIZE_2K\n";
//                break;
//            case MifareClassic.SIZE_4K:
//                typeInfoString += "MifareClassic.SIZE_4K\n";
//                break;
//            case MifareClassic.SIZE_MINI:
//                typeInfoString += "MifareClassic.SIZE_MINI\n";
//                break;
//            default:
//                typeInfoString += "unknown size...!\n";
//        }

//        int blockCount = mifareClassicTag.getBlockCount();
//        typeInfoString += "BlockCount \t= " + blockCount + "\n";
//        int sectorCount = mifareClassicTag.getSectorCount();
//        typeInfoString += "SectorCount \t= " + sectorCount + "\n";
//
//        textViewTagInfo.setText(typeInfoString);

        new ReadMifareClassicTask(mifareClassicTag, this).execute();
    }

    private class ReadMifareClassicTask extends AsyncTask<Void, Void, Void> {
        /*
        MIFARE Classic tags are divided into sectors, and each sector is sub-divided into blocks.
        Block size is always 16 bytes (BLOCK_SIZE). Sector size varies.
        MIFARE Classic 1k are 1024 bytes (SIZE_1K), with 16 sectors each of 4 blocks.
        */

        MifareClassic mifareClassic;
        boolean success;
        final int numOfSector = 16;
        final int numOfBlockInSector = 4;
        byte[][][] buffer = new byte[numOfSector][numOfBlockInSector][MifareClassic.BLOCK_SIZE];
        byte[] accessKeyA = new byte[] { 0x45, 0x52, 0x57, 0x49, 0x4E, 0x00 };
        //byte[] accessKeyA = new byte[] { 0xD3, 0xF7, 0xD3, 0xF7, 0xD3, 0xF7 };
        byte[] accessKeyB = new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
        byte[] mccNo = new byte[MifareClassic.BLOCK_SIZE];
        byte[] cardSerial = new byte[MifareClassic.BLOCK_SIZE];
        byte[] fName = new byte[MifareClassic.BLOCK_SIZE];
        byte[] mName = new byte[MifareClassic.BLOCK_SIZE];
        byte[] lName = new byte[MifareClassic.BLOCK_SIZE];
        byte[] issueDate = new byte[MifareClassic.BLOCK_SIZE];
        byte[] expiryDate = new byte[MifareClassic.BLOCK_SIZE];
        byte[] access1 = new byte[MifareClassic.BLOCK_SIZE];
        byte[] access2 = new byte[MifareClassic.BLOCK_SIZE];
        byte[] access3 = new byte[MifareClassic.BLOCK_SIZE];
        byte[] southcoderTag = new byte[MifareClassic.BLOCK_SIZE];
        private Context mContext;

        ReadMifareClassicTask(MifareClassic tag, Context context){
            mifareClassic = tag;
            success = false;
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            if (loggedIn)
                textViewBlock.setText("Reading Tag, do not remove...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mifareClassic.connect();

                success = true;
                //tagScanned = true;

                //-- Read values from sector 0
                if (mifareClassic.authenticateSectorWithKeyB(0, accessKeyB)) {
                    cardSerial = mifareClassic.readBlock(1);
                }
                else success = false;

                //-- Read values from sector 1
                if (mifareClassic.authenticateSectorWithKeyB(1, accessKeyB)) {
                    mccNo = mifareClassic.readBlock(4);
                    issueDate = mifareClassic.readBlock(5);
                    expiryDate = mifareClassic.readBlock(6);
                }
                else success = false;
//
                //-- Read values from sector 2
                if (mifareClassic.authenticateSectorWithKeyB(2, accessKeyB)) {
                    fName = mifareClassic.readBlock(8);
                    mName = mifareClassic.readBlock(9);
                    lName = mifareClassic.readBlock(10);
                }
                else success = false;

                //-- Read values from sector 3
                if (mifareClassic.authenticateSectorWithKeyB(3, accessKeyB)) {
                    access1 = mifareClassic.readBlock(12);
                    access2 = mifareClassic.readBlock(13);
                    access3 = mifareClassic.readBlock(14);
                }
                else success = false;

                //-- Read values from sector 4
                if (mifareClassic.authenticateSectorWithKeyB(4, accessKeyB)) {
                    southcoderTag = mifareClassic.readBlock(16);
                }
                else success = false;

//                for(int s=0; s<numOfSector; s++) {
//                    if(mifareClassic.authenticateSectorWithKeyA(s, accessKeyA)) {
//                        for(int b=0; b<numOfBlockInSector; b++) {
//                            int blockIndex = (s * numOfBlockInSector) + b;
//                            buffer[s][b] = mifareClassic.readBlock(blockIndex);
//                        }
//                    }
//                }

                //acknowledgeBeep();
            } catch (IOException e) {
                clearMessage();
                e.printStackTrace();
            } finally {
                if(mifareClassic != null){
                    try {
                        mifareClassic.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //display block

            if (success) {
//                try {
//                    //-- Serial
//                    textViewSerialNo.setText("Card Serial: " + hexToAscii(byteToString(cardNo, MifareClassic.BLOCK_SIZE)));
//                    //-- MCC Number
//                    textViewMccNo.setText("MCC Number: " + hexToAscii(byteToString(mccNo, MifareClassic.BLOCK_SIZE)));
//                    //-- First Name
//                    textViewFName.setText("First Name: " + hexToAscii(byteToString(fName, MifareClassic.BLOCK_SIZE)));
//                    //-- Middle Name
//                    textViewMName.setText("Middle Name: " + hexToAscii(byteToString(mName, MifareClassic.BLOCK_SIZE)));
//                    //-- Last Name
//                    textViewLName.setText("Last Name: " + hexToAscii(byteToString(lName, MifareClassic.BLOCK_SIZE)));
//                    //-- Issue Date
//                    textViewIssueDate.setText("Issue Date: " + hexToAscii(byteToString(issueDate, MifareClassic.BLOCK_SIZE)));
//                    //-- Expiry Date
//                    textViewExpiryDate.setText("Expiry Date: " + hexToAscii(byteToString(expiryDate, MifareClassic.BLOCK_SIZE)));
//
//                    textViewBlock.setText("");
//
////                StringBuilder stringBlock = new StringBuilder();
////                for(int i=0; i<numOfSector; i++){
////                    stringBlock.append(i + " :\n");
////                    for(int j=0; j<numOfBlockInSector; j++){
////                        for(int k=0; k<MifareClassic.BLOCK_SIZE; k++){
////                            stringBlock.append(String.format("%02X", buffer[i][j][k] & 0xff) + " ");
////                        }
////                        stringBlock.append("\n");
////                    }
////                    stringBlock.append("\n");
////                }
////                textViewBlock.setText(stringBlock);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                try {
                    String cardserial = hexToAscii(byteToString(cardSerial, MifareClassic.BLOCK_SIZE));
                    String mccno = hexToAscii(byteToString(mccNo, MifareClassic.BLOCK_SIZE));
                    String name = hexToAscii(byteToString(fName, MifareClassic.BLOCK_SIZE));
                    String access = hexToAscii(byteToString(access1, MifareClassic.BLOCK_SIZE)) +
                            hexToAscii(byteToString(access2, MifareClassic.BLOCK_SIZE)) +
                            hexToAscii(byteToString(access3, MifareClassic.BLOCK_SIZE));
                    String scTag = hexToAscii(byteToString(southcoderTag, MifareClassic.BLOCK_SIZE)); //-- To know if the card went through our system
                    Integer cardtype = Integer.parseInt(mccno.substring(0, 1));

                    if (loggedIn) {
                        try {
                            if (mccno != "") {
                                textViewGreetings.setBackground(getResources().getDrawable(R.drawable.rounded_corner_tv_blue));
                                textViewGreetings.setTextColor(getResources().getColor(R.color.colorWhite));
                                textViewGreetings.setText("Welcome " + name + "!\n Enjoy your free ride.");

                                insertEjeepLog(name, userMCCNo, deviceID, cardserial, mccno.substring(0, 7), cardtype, 0);
                            }
                            else {
                                textViewGreetings.setBackground(getResources().getDrawable(R.drawable.rounded_corner_tv_red));
                                textViewGreetings.setTextColor(getResources().getColor(R.color.colorWhite));
                                textViewGreetings.setText("Your may have an invalid card.");
                            }

                            textViewBlock.setText("Reading NFC card successful.");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        //-- Check if user have access to the app
                        try {
                            if (mode == "UPLOAD") {
                                Users user = dbHelper.getUser(mccno.substring(0, 7), "upload");

                                if (user != null)
                                    UploadRiderLogs();
                                else
                                    status.setText("User not allowed to execute task.");

                                //UploadRiderLogs();
                                mode = "";
                            } else if (mode == "VERIFY") {
                                VerifyDevice();
                                mode = "";
                            }
                            else {
                                userMCCNo = mccno.substring(0, 7);
                                VerifyUser(userMCCNo, "ejeep");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Toast.makeText(mContext, "Invalid User or Card", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    textViewGreetings.setBackground(getResources().getDrawable(R.drawable.rounded_corner_tv_red));
                    textViewGreetings.setTextColor(getResources().getColor(R.color.colorWhite));
                    textViewGreetings.setText("Your may have an invalid card.");
                }
            }
            else {
                if (loggedIn) {
                    textViewGreetings.setBackground(getResources().getDrawable(R.drawable.rounded_corner_tv_red));
                    textViewGreetings.setTextColor(getResources().getColor(R.color.colorWhite));
                    textViewGreetings.setText("Unable to read your card. Please try again.");
                    textViewBlock.setText("Failed to read NFC card!");
                }
            }
        }
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
        adapter.enableForegroundDispatch(activity, pendingIntent, null, null);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    private void insertEjeepLog(String name, String userid, String deviceid, String cardserial, String mccno, Integer cardtype, Integer isexpired) {
        //-- Check if log is exceeds time threshold before inserting
        EjeepTransaction ejeepTransaction = dbHelper.getEjeepTransaction(mccno);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String currentDateTime = dateFormat.format(new Date()); // Find todays date
        String logid = "ejeep" + currentDateTime;

        if (ejeepTransaction != null) {
            Integer timeThreshold = 1000 * 60 * minuteThreshold; //--2 minutes

            if (Math.abs(System.currentTimeMillis() - Timestamp.valueOf(ejeepTransaction.getTransactionDate()).getTime()) > timeThreshold)
                dbHelper.insertEjeepTransaction(logid, userid, deviceid, cardserial, mccno, cardtype, isexpired);
            else {
                textViewGreetings.setBackground(getResources().getDrawable(R.drawable.rounded_corner_tv_blue));
                textViewGreetings.setTextColor(getResources().getColor(R.color.colorWhite));
                textViewGreetings.setText("Hi " + name + "! You have already tapped your card.");
            }
        } else dbHelper.insertEjeepTransaction(logid, userid, deviceid, cardserial, mccno, cardtype, isexpired);
    }

    private void showNFCSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
        startActivity(intent);
    }

    private String byteToString(byte[] b, int byteLen) {
        String retVal = "";

        try {
            for (int i = 0; i < byteLen; i++) {
                retVal += String.format("%02X", b[i] & 0xff);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retVal;
    }

    private String hexToAscii(String s) {
        StringBuilder sb = null;

        try {
            if (s.length() <= 0) return "";

            int n = s.length();
            sb = new StringBuilder(n / 2);

            for (int i = 0; i < n; i += 2) {
                char a = s.charAt(i);
                char b = s.charAt(i + 1);
                sb.append((char) ((hexToInt(a) << 4) | hexToInt(b)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return sb == null ? "" : sb.toString();
    }

    private static int hexToInt(char ch) {
        if ('a' <= ch && ch <= 'f') { return ch - 'a' + 10; }
        if ('A' <= ch && ch <= 'F') { return ch - 'A' + 10; }
        if ('0' <= ch && ch <= '9') { return ch - '0'; }

        throw new IllegalArgumentException(String.valueOf(ch));
    }

    private void acknowledgeBeep() {
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 500);

        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500);
    }

    private void showLoginAlert(Activity activity) {
        if (!loginAlertShown) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            LayoutInflater inflater = activity.getLayoutInflater();
            vwLogin = inflater.inflate(R.layout.activity_login, null);

            builder.setView(vwLogin)
                    .setTitle("EJRF Login")
                    .setCancelable(false)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            CloseMainActivity();
                        }
                    });
//                    .setPositiveButton("Login", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                        }
//                    });

            loginAlert = builder.create();
            loginAlert.show();
            loginAlertShown = true;

            btnVerifyDevice = vwLogin.findViewById(R.id.btn_verify_device);
            btnSyncUserList = vwLogin.findViewById(R.id.btn_sync_user_list);
            btnUploadRidersLogs = vwLogin.findViewById(R.id.btn_upload_data);
            spinnerTimeAllowance = vwLogin.findViewById(R.id.spinner_time_allowance);
            status = vwLogin.findViewById(R.id.tvStatus);
            textViewLogTimeLabel = vwLogin.findViewById(R.id.tvLogTimeAllowanceLabel);

            ArrayList<String> arrTimeAllowanceList = new ArrayList<>();
            arrTimeAllowanceList.add("2 MINUTES");
            arrTimeAllowanceList.add("3 MINUTES");
            arrTimeAllowanceList.add("4 MINUTES");
            arrTimeAllowanceList.add("5 MINUTES");
            arrTimeAllowanceList.add("6 MINUTES");
            arrTimeAllowanceList.add("7 MINUTES");

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(vwLogin.getContext(), R.layout.support_simple_spinner_dropdown_item, arrTimeAllowanceList);
            arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            spinnerTimeAllowance.setAdapter(arrayAdapter);

            spinnerTimeAllowance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String timeAllowance = parent.getItemAtPosition(position).toString();
                    SetTimeAllowance(timeAllowance);
                    //Toast.makeText(parent.getContext(), "Selected: " + timeAllowance, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if (!doesDatabaseExist(this, "MCC.db")) {
                btnSyncUserList.setVisibility(View.INVISIBLE);
                btnUploadRidersLogs.setVisibility(View.INVISIBLE);
                textViewLogTimeLabel.setVisibility(View.INVISIBLE);
                spinnerTimeAllowance.setVisibility(View.INVISIBLE);
            } else {
                btnVerifyDevice.setVisibility(View.INVISIBLE);
                btnSyncUserList.setVisibility(View.VISIBLE);
                btnUploadRidersLogs.setVisibility(View.VISIBLE);
                textViewLogTimeLabel.setVisibility(View.VISIBLE);
                spinnerTimeAllowance.setVisibility(View.VISIBLE);
            }

//            // Initialize the Handler
//            mHandler = new Handler();

            btnVerifyDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mode = "VERIFY";
                    status.setText("To Verify, Please tap your card to continue.");
                }
            });

            btnSyncUserList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //mode = "SYNC";
                    SyncUserList();
                }
            });

            btnUploadRidersLogs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mode = "UPLOAD";
                    status.setText("To Upload, Please tap your card to continue.");
//                    status.setAnimation(AnimationUtils.loadAnimation(btnUploadRidersLogs.getContext(), R.anim.textblink));
//                    UploadRiderLogs(retrofit);

//                    mRunnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            doBlink();
//                        }
//                    };
//
//                    mHandler.postDelayed(mRunnable, mInterval);
                }
            });
        }
    }

//    protected void doBlink(){
//        if(initialState){
//            // Reverse the boolean
//            initialState = false;
//            // Set the TextView color to red
//            status.setTextColor(Color.WHITE);
//        } else {
//            // Reverse the boolean
//            initialState = true;
//            // Change the TextView color to initial State
//            status.setTextColor(Color.BLUE);
//        }
//
//        // Schedule the task
//        mHandler.postDelayed(mRunnable,mInterval);
//    }

    private void SetTimeAllowance(String itemSelected) {
        switch (itemSelected) {
            case "2 MINUTES":
                minuteThreshold = 2;
                break;
            case "3 MINUTES":
                minuteThreshold = 2;
                break;
            case "4 MINUTES":
                minuteThreshold = 2;
                break;
            case "5 MINUTES":
                minuteThreshold = 2;
                break;
            case "6 MINUTES":
                minuteThreshold = 2;
                break;
            case "7 MINUTES":
                minuteThreshold = 2;
                break;
        }
    }

    private void VerifyDevice() {
        final ProgressDialog dialogVerify = new ProgressDialog(btnVerifyDevice.getContext());

        if (IsConnectedToTheInternet()) {
            status.setText("");
            dialogVerify.setTitle("Verify Device");
            dialogVerify.setMessage("Verifying this device. Please wait...");
            dialogVerify.setCancelable(false);
            dialogVerify.show();

            Device device = new Device(deviceID, "ejeep");
            Call<DeviceApiResponse> call = mccPlaceHolderApi.verifyDevice(device);
            call.enqueue(new Callback<DeviceApiResponse>() {
                @Override
                public void onResponse(Call<DeviceApiResponse> call, Response<DeviceApiResponse> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(btnSyncUserList.getContext(), "Code: " + response.code(), Toast.LENGTH_LONG).show();
                        dialogVerify.cancel();
                        return;
                    }

                    //-- ToDo: Check the route reply
                    DeviceApiResponse resp = response.body();

                    if ("Success".equals(resp.message)) {
                        btnVerifyDevice.setVisibility(View.INVISIBLE);
                        btnSyncUserList.setVisibility(View.VISIBLE);
                        btnUploadRidersLogs.setVisibility(View.VISIBLE);

                        if (ShowDeviceID())
                            InitDB();
                    } else {
                        status.setText("This device is not allowed to used the app.");
                    }

                    dialogVerify.cancel();
                }

                @Override
                public void onFailure(Call<DeviceApiResponse> call, Throwable t) {
                    //status.setText(t.getMessage());
                    Toast.makeText(btnSyncUserList.getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                    dialogVerify.cancel();
                }
            });
        }
    }

    private void SyncUserList() {
        final ProgressDialog dialogSynching = new ProgressDialog(btnSyncUserList.getContext());

        if (IsConnectedToTheInternet()) {
            status.setText("");
            dialogSynching.setTitle("User List");
            dialogSynching.setMessage("Synching. Please wait...");
            dialogSynching.setCancelable(false);
            dialogSynching.show();

            Call<UsersApiResponse> call = mccPlaceHolderApi.getUsers();
            call.enqueue(new Callback<UsersApiResponse>() {
                @Override
                public void onResponse(Call<UsersApiResponse> call, Response<UsersApiResponse> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(btnSyncUserList.getContext(), "Code: " + response.code(), Toast.LENGTH_LONG).show();
                        dialogSynching.cancel();
                        return;
                    }

                    UsersApiResponse users = response.body();

                    //if (users.message == "Success") {
                    if (!users.obj.isEmpty()) {
                        dbHelper.deleteUserAll();

                        for (Users user : users.obj) {
                            int id = user.getId();
                            String mccno = user.getMCCNumber();
                            String job = user.getJob();
                            dbHelper.insertUser(id, mccno, job);
                        }

                        status.setText("User list successfully synced!");
                        Toast.makeText(btnSyncUserList.getContext(), "User List Synced", Toast.LENGTH_LONG).show();
                    } else {
                        status.setText("User list not synced! Please try again.");
                    }

                    dialogSynching.cancel();
                }

                @Override
                public void onFailure(Call<UsersApiResponse> call, Throwable t) {
                    status.setText(t.getMessage());
                    dialogSynching.cancel();
                }
            });
        }
        else
            Toast.makeText(btnSyncUserList.getContext(), "You need internet to Sync User list.", Toast.LENGTH_LONG).show();
    }

    private void UploadRiderLogs() {
        final ProgressDialog dialogUploading = new ProgressDialog(btnSyncUserList.getContext());

        if (IsConnectedToTheInternet()) {
            status.setText("");
            dialogUploading.setTitle("Ejeep Logs");
            dialogUploading.setMessage("Uploading. Please wait...");
            dialogUploading.setCancelable(false);
            dialogUploading.show();

            ArrayList<EjeepTransaction> ejeepTransactionList = dbHelper.getAllEjeepTransactions();
            EjeepTransactions ejeepTransactions = new EjeepTransactions(ejeepTransactionList);
            Call<EjeepTransactions> ejeepTransactionsCall = mccPlaceHolderApi.createPost(ejeepTransactions);
            ejeepTransactionsCall.enqueue(new Callback<EjeepTransactions>() {
                @Override
                public void onResponse(Call<EjeepTransactions> call, Response<EjeepTransactions> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(btnSyncUserList.getContext(), "Code: " + response.code(), Toast.LENGTH_LONG).show();
                        dialogUploading.cancel();
                        return;
                    }

                    EjeepTransactions resp = response.body();

                    if (response.code() == 201) { //--successful
                        //-- Remove uploaded records
                        dbHelper.deleteEjeepAll();

                        status.setText("Ejeep logs was successfully uploaded!");
                        Toast.makeText(btnSyncUserList.getContext(), "Ejeep transactions uploaded", Toast.LENGTH_LONG).show();
                    } else {
                        status.setText("Ejeep logs uploaded failed!");
                    }

                    dialogUploading.cancel();
                }

                @Override
                public void onFailure(Call<EjeepTransactions> call, Throwable t) {
                    status.setText(t.getMessage());
                    dialogUploading.cancel();
                }
            });

            dialogUploading.cancel();
            Toast.makeText(btnUploadRidersLogs.getContext(), "Upload complete", Toast.LENGTH_LONG).show();
        } else Toast.makeText(btnSyncUserList.getContext(), "You need internet to Sync User list.", Toast.LENGTH_LONG).show();
    }

//    private void checkIfCardIsStillInRange() {
//        timerObj = new Timer();
//        timerTaskObj = new TimerTask() {
//            public void run() {
//                readMifareClassic(tag);
//            }
//        };
//        timerObj.schedule(timerTaskObj, 0, 1000);
//    }

    private void clearMessage() {
        textViewGreetings.setBackground(getResources().getDrawable(R.drawable.rounded_corner_tv));
        textViewGreetings.setTextColor(getResources().getColor(R.color.colorYellow));
        textViewGreetings.setText("Welcome!\n Enjoy your free ride.");
        textViewBlock.setText("");
    }

    private boolean IsConnectedToTheInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private static boolean doesDatabaseExist(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }

//    private class JsonTask extends AsyncTask<String, String, String> {
//
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//            pdialog = new ProgressDialog(MainActivity.this);
//            pdialog.setMessage("Syncing User list please wait...");
//            pdialog.setCancelable(false);
//            pdialog.show();
//        }
//
//        protected String doInBackground(String... params) {
//            HttpURLConnection connection = null;
//            BufferedReader reader = null;
//
//            try {
//                URL url = new URL(params[0]);
//                connection = (HttpURLConnection) url.openConnection();
//                connection.connect();
//
//                InputStream stream = connection.getInputStream();
//
//                reader = new BufferedReader(new InputStreamReader(stream));
//
//                StringBuffer buffer = new StringBuffer();
//                String line = "";
//
//                while ((line = reader.readLine()) != null) {
//                    buffer.append(line+"\n");
//                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
//                }
//
//                Gson jsonData = new Gson();
//
//                return jsonData.toJson(buffer.toString());
//
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                if (connection != null) {
//                    connection.disconnect();
//                }
//                try {
//                    if (reader != null) {
//                        reader.close();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//            if (pdialog.isShowing()){
//                pdialog.dismiss();
//            }
//
//            //txtJson.setText(result);
//            //-- ToDo: Save result in table
//        }
//    }
}
