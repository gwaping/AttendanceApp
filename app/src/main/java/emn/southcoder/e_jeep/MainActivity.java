package emn.southcoder.e_jeep;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.Calendar;
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
    private TextView textViewBlock, textViewGreetings, textViewLogTimeLabel, textViewRiderCount;
    private boolean isNFCSupported = false;
    private boolean loggedIn = false;
    private boolean loginAlertShown = false;
    private AlertDialog loginAlert;
    private Animation animBlink;
    private Spinner spinnerTimeAllowance;
    private Button btnVerifyDevice, btnSyncUserList;
    private TextView status;
    private TextView tvmccnum, tvname, tvbirthdate, tvissuedate, tvexpirydate;
    private TelephonyManager tm;
    private DatabaseHelper dbHelper;
    private String deviceID = null;
    private int logTimeAllowance = 2; //-- Default value
    private String mode;
    private String userMCCNo;
    private String userRole;
    private MCCPlaceHolderApi mccPlaceHolderApi;
    private Menu myMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Handler handler = new Handler();
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
        textViewRiderCount = findViewById(R.id.tvPassengerCount);

        GetPreference();
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
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (isNFCSupported)
            setupForegroundDispatch(this, nfcAdapter);
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
            userRole = dbHelper.GetUserRole(mccno);
            this.loginAlert.dismiss();

            if (userRole.contains("admin")) {
                textViewGreetings.setBackground(getResources().getDrawable(R.drawable.rounded_corner_tv_gray));
                textViewGreetings.setTextColor(getResources().getColor(R.color.colorBlue));
                textViewGreetings.setText(R.string.admin_mode);
            }

            //-- Setup menu depending on the user login role
            setupMenu();

            textViewRiderCount.setText("Total Passengers: " + String.valueOf(dbHelper.GetTransactionCount()));
        }
        else Toast.makeText(this, "Invalid user", Toast.LENGTH_LONG).show();
    }

    public void CloseMainActivity() {
        finish();
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();

        //Toast.makeText(this, action, Toast.LENGTH_SHORT).show();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            //Toast.makeText(this, "ACTION_TECH_DISCOVERED", Toast.LENGTH_SHORT).show();
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            if (tag != null) {
                //clearMessage();
                readMifareClassic(tag);
            }
        }
        else {
            Toast.makeText(this, "onResume() : " + action, Toast.LENGTH_SHORT).show();
        }
    }

    public void readMifareClassic(Tag tag) {
        MifareClassic mifareClassicTag = MifareClassic.get(tag);
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
        //private Context mContext;

        ReadMifareClassicTask(MifareClassic tag, Context context){
            mifareClassic = tag;
            success = false;
            //mContext = context;
        }

        @Override
        protected void onPreExecute() {
            if (loggedIn && !userRole.contains("admin"))
                textViewBlock.setText("Reading Tag, do not remove...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mifareClassic.connect();

                success = true;

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

                acknowledgeBeep();
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
            if (success) {
                try {
                    String cardserial = hexToAscii(byteToString(cardSerial, MifareClassic.BLOCK_SIZE));
                    String mccno = hexToAscii(byteToString(mccNo, MifareClassic.BLOCK_SIZE));
                    String name = hexToAscii(byteToString(fName, MifareClassic.BLOCK_SIZE));
                    String issue = hexToAscii(byteToString(issueDate, MifareClassic.BLOCK_SIZE));
//                    String access = hexToAscii(byteToString(access1, MifareClassic.BLOCK_SIZE)) +
//                            hexToAscii(byteToString(access2, MifareClassic.BLOCK_SIZE)) +
//                            hexToAscii(byteToString(access3, MifareClassic.BLOCK_SIZE));
//                    String scTag = hexToAscii(byteToString(southcoderTag, MifareClassic.BLOCK_SIZE)); //-- To know if the card went through our system
                    Integer cardtype = Integer.parseInt(mccno.substring(0, 1));

                    if (loggedIn) {
                        try {
                            if (!userRole.contains("admin")) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyy");
                                Calendar cal = Calendar.getInstance();
                                Date issueDate = dateFormat.parse(issue.substring(0, 6));
                                cal.setTime(issueDate);
                                cal.add(Calendar.YEAR, 2);
                                Date expiryDate = cal.getTime();
                                Date now = new Date();

                                if (mode == "CARDINFO") {
                                    tvmccnum.setText(mccno.substring(0, 7));
                                    if (name.contains(" "))
                                        tvname.setText(name.substring(0, name.indexOf(' ')));
                                    else
                                        tvname.setText(name);
                                    tvbirthdate.setText(mccno.substring(mccno.length()-6, mccno.length()));
                                    tvissuedate.setText(issue.substring(0, 6));
                                    tvexpirydate.setText(dateFormat.format(expiryDate));
                                } else {
                                    if (mccno != "") {
                                        textViewGreetings.setBackground(getResources().getDrawable(R.drawable.rounded_corner_tv_blue));
                                        textViewGreetings.setTextColor(getResources().getColor(R.color.colorWhite));
                                        textViewGreetings.setText("Welcome " + name + "!\n Enjoy your free ride.");

                                        insertEjeepLog(name, userMCCNo, deviceID, cardserial, mccno.substring(0, 7), cardtype, 0);

                                        //-- Announce expired card
                                        if (now.getTime() > expiryDate.getTime()) {
                                            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.card_expired_audio);
                                            mediaPlayer.start();
                                        }
                                    }
                                    else {
                                        textViewGreetings.setBackground(getResources().getDrawable(R.drawable.rounded_corner_tv_red));
                                        textViewGreetings.setTextColor(getResources().getColor(R.color.colorWhite));
                                        textViewGreetings.setText(R.string.invalid_card);
                                    }
                                }
                                textViewBlock.setText(R.string.reading_nfc_success);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            if (doesDatabaseExist(getApplicationContext(), "MCC.db")) {
                                userMCCNo = mccno.substring(0, 7);
                                VerifyUser(userMCCNo, "ejeep");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Invalid User or Card", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    if (!userRole.contains("admin")) {
                        e.printStackTrace();
                        textViewGreetings.setBackground(getResources().getDrawable(R.drawable.rounded_corner_tv_red));
                        textViewGreetings.setTextColor(getResources().getColor(R.color.colorWhite));
                        textViewGreetings.setText("Your may have an invalid card.");
                    }
                }
            }
            else {
                if (loggedIn && !userRole.contains("admin")) {
                    textViewGreetings.setBackground(getResources().getDrawable(R.drawable.rounded_corner_tv_red));
                    textViewGreetings.setTextColor(getResources().getColor(R.color.colorWhite));
                    textViewGreetings.setText("Unable to read your card. Please try again.");
                    textViewBlock.setText("Failed to read NFC card!");
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        myMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_sync_user_list:
                SyncUserList();
                break;
            case R.id.action_card_info:
                mode = "CARDINFO";
                ShowCardInfo(this);
                break;
            case R.id.action_upload_logs:
                UploadTransactionLogs();
                break;
            case R.id.action_log_allowance:
                showTimeAllowance(this);
                String selItem = logTimeAllowance + " MINUTES";
                spinnerTimeAllowance.setSelection(getIndex(spinnerTimeAllowance, selItem));
                break;
            case R.id.action_logout:
                loggedIn = false;
                loginAlertShown = false;
                clearMessage();
                showLoginAlert(this);
                status.setText("Device ID: " + deviceID);
                break;
        }

        return super.onOptionsItemSelected(item);
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

    private void setupMenu() {
        for (int i=0; i<myMenu.size(); i++) {
            if (!userRole.contains("admin")) {
                if (myMenu.getItem(i).getItemId() == R.id.action_upload_logs)
                    myMenu.getItem(i).setVisible(false);
            }
        }
    }

    private int getIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }

        return 0;
    }

    private void insertEjeepLog(String name, String userid, String deviceid, String cardserial, String mccno, Integer cardtype, Integer isexpired) {
        //-- Check if log is exceeds time threshold before inserting
        EjeepTransaction ejeepTransaction = dbHelper.getEjeepTransaction(mccno);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String currentDateTime = dateFormat.format(new Date()); // Find todays date
        String logid = "ejeep" + currentDateTime;

        if (ejeepTransaction != null) {
            Integer timeThreshold = 1000 * 60 * logTimeAllowance; //--2 minutes

            if (Math.abs(System.currentTimeMillis() - Timestamp.valueOf(ejeepTransaction.getTransactionDate()).getTime()) > timeThreshold) {
                if (dbHelper.insertEjeepTransaction(logid, userid, deviceid, cardserial, mccno, cardtype, isexpired) >= 0) {
                    textViewRiderCount.setText("Total Passengers: " + dbHelper.GetTransactionCount());
                }
            }
            else {
                textViewGreetings.setBackground(getResources().getDrawable(R.drawable.rounded_corner_tv_blue));
                textViewGreetings.setTextColor(getResources().getColor(R.color.colorWhite));
                textViewGreetings.setText("Hi " + name + "! You have already tapped your card.");
            }
        } else {
            dbHelper.insertEjeepTransaction(logid, userid, deviceid, cardserial, mccno, cardtype, isexpired);
            textViewRiderCount.setText("Total Passengers: " + dbHelper.GetTransactionCount());
        }
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

    private void showTimeAllowance(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View vwSettings = inflater.inflate(R.layout.activity_settings, null);

        builder.setView(vwSettings)
                .setCancelable(false)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //CloseMainActivity();
                    }
                });

        AlertDialog settingsAlert = builder.create();
        settingsAlert.show();
        spinnerTimeAllowance = vwSettings.findViewById(R.id.spinner_time_allowance);
        textViewLogTimeLabel = vwSettings.findViewById(R.id.tvLogTimeAllowanceLabel);

        //-- Set values for log time allowance spinner
        ArrayList<String> arrTimeAllowanceList = new ArrayList<>();
        arrTimeAllowanceList.add("2 MINUTES");
        arrTimeAllowanceList.add("3 MINUTES");
        arrTimeAllowanceList.add("4 MINUTES");
        arrTimeAllowanceList.add("5 MINUTES");
        arrTimeAllowanceList.add("6 MINUTES");
        arrTimeAllowanceList.add("7 MINUTES");
        arrTimeAllowanceList.add("8 MINUTES");
        arrTimeAllowanceList.add("9 MINUTES");
        arrTimeAllowanceList.add("10 MINUTES");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(vwSettings.getContext(), R.layout.support_simple_spinner_dropdown_item, arrTimeAllowanceList);
        arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerTimeAllowance.setAdapter(arrayAdapter);

        spinnerTimeAllowance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String timeAllowance = parent.getItemAtPosition(position).toString();
                SetTimeAllowance(timeAllowance);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showLoginAlert(Activity activity) {
        if (!loginAlertShown) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            LayoutInflater inflater = activity.getLayoutInflater();
            View vwLogin = inflater.inflate(R.layout.activity_login, null);

            builder.setView(vwLogin)
                    .setTitle("EJRF Login")
                    .setCancelable(false)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            CloseMainActivity();
                        }
                    });

            loginAlert = builder.create();
            //loginAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            loginAlert.show();
            loginAlertShown = true;

            btnVerifyDevice = vwLogin.findViewById(R.id.btn_verify_device);
            btnSyncUserList = vwLogin.findViewById(R.id.btn_sync_user_list);
            status = vwLogin.findViewById(R.id.tvStatus);

            if (!doesDatabaseExist(this, "MCC.db")) {
                btnVerifyDevice.setVisibility(View.VISIBLE);
                btnSyncUserList.setVisibility(View.INVISIBLE);
            } else {
                btnVerifyDevice.setVisibility(View.INVISIBLE);
                btnSyncUserList.setVisibility(View.VISIBLE);
            }

            btnVerifyDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VerifyDevice();
                }
            });

            btnSyncUserList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SyncUserList();
                }
            });
        }
    }

    private void ShowCardInfo(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View vwCardInfo = inflater.inflate(R.layout.activity_card_info, null);

        builder.setView(vwCardInfo)
                .setCancelable(false)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mode = "";
                    }
                });

        AlertDialog cardinfoAlert = builder.create();
        //cardinfoAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        cardinfoAlert.show();

        tvmccnum = vwCardInfo.findViewById(R.id.tvMCCNum);
        tvname = vwCardInfo.findViewById(R.id.tvName);
        tvbirthdate = vwCardInfo.findViewById(R.id.tvBirthdate);
        tvissuedate = vwCardInfo.findViewById(R.id.tvIssueDate);
        tvexpirydate = vwCardInfo.findViewById(R.id.tvExpiryDate);
    }

    private void SetTimeAllowance(String itemSelected) {
        switch (itemSelected) {
            case "2 MINUTES":
                logTimeAllowance = 2;
                break;
            case "3 MINUTES":
                logTimeAllowance = 3;
                break;
            case "4 MINUTES":
                logTimeAllowance = 4;
                break;
            case "5 MINUTES":
                logTimeAllowance = 5;
                break;
            case "6 MINUTES":
                logTimeAllowance = 6;
                break;
            case "7 MINUTES":
                logTimeAllowance = 7;
                break;
            case "8 MINUTES":
                logTimeAllowance = 8;
                break;
            case "9 MINUTES":
                logTimeAllowance = 9;
                break;
            case "10 MINUTES":
                logTimeAllowance = 10;
                break;
        }

        SavePreference();
    }

    private void VerifyDevice() {
        final ProgressDialog dialogVerify = new ProgressDialog(loginAlert.getContext());

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
                    DeviceApiResponse resp;

                    if (!response.isSuccessful()) {
                        //Toast.makeText(loginAlert.getContext(), "Code: " + response.code(), Toast.LENGTH_LONG).show();
                        status.setTextColor(Color.RED);
                        status.setText("This device is not allowed to use the app.");
                        dialogVerify.cancel();
                        return;
                    }

                    resp = response.body();

                    if ("Success".equals(resp.message)) {
                        if (ShowDeviceID()) {
                            InitDB();
                            btnVerifyDevice.setVisibility(View.INVISIBLE);
                            btnSyncUserList.setVisibility(View.VISIBLE);
                            SyncUserList();
                        }

                    } else {
                        status.setText("This device is not allowed to use the app.");
                    }

                    dialogVerify.cancel();
                }

                @Override
                public void onFailure(Call<DeviceApiResponse> call, Throwable t) {
                    //status.setText(t.getMessage());
                    Toast.makeText(loginAlert.getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                    dialogVerify.cancel();
                }
            });
        } else Toast.makeText(getApplicationContext(), "Please connect to the internet to execute this action.", Toast.LENGTH_LONG).show();
    }
    private void SyncUserList() {
        final ProgressDialog dialogSynching = new ProgressDialog(this);

        if (IsConnectedToTheInternet()) {
            dialogSynching.setTitle("User List");
            dialogSynching.setMessage("Synching. Please wait...");
            dialogSynching.setCancelable(false);
            dialogSynching.show();

            Call<UsersApiResponse> call = mccPlaceHolderApi.getUsers();
            call.enqueue(new Callback<UsersApiResponse>() {
                @Override
                public void onResponse(Call<UsersApiResponse> call, Response<UsersApiResponse> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Code: " + response.code(), Toast.LENGTH_LONG).show();
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

//                        status.setText("User list successfully synced!");
                        Toast.makeText(getApplicationContext(), "User List Synced", Toast.LENGTH_LONG).show();
                    } else {
                        status.setText("User list not synced! Please try again.");
                    }

                    dialogSynching.cancel();
                }

                @Override
                public void onFailure(Call<UsersApiResponse> call, Throwable t) {
                    //status.setText(t.getMessage());
                    Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                    dialogSynching.cancel();
                }
            });
        } else Toast.makeText(getApplicationContext(), "Please connect to the internet to execute this action.", Toast.LENGTH_LONG).show();
    }

    private void UploadTransactionLogs() {
        final ProgressDialog dialogUploading = new ProgressDialog(this);

        if (IsConnectedToTheInternet()) {
            dialogUploading.setTitle("Ejeep Transactions");
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
                        Toast.makeText(getApplicationContext(), "Code: " + response.code(), Toast.LENGTH_LONG).show();
                        dialogUploading.cancel();
                        return;
                    }

                    EjeepTransactions resp = response.body();

                    if (response.code() == 201) { //--successful
                        int totalTransactions = dbHelper.GetTransactionCount();

                        //-- Remove uploaded records
                        dbHelper.deleteEjeepAll();
                        textViewRiderCount.setText("Total Passengers: 0");
                        Toast.makeText(getApplicationContext(), totalTransactions + " Ejeep transactions uploaded", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Ejeep transactions upload failed!", Toast.LENGTH_LONG).show();
                    }

                    dialogUploading.cancel();
                }

                @Override
                public void onFailure(Call<EjeepTransactions> call, Throwable t) {

                    dialogUploading.cancel();
                }
            });
        } else Toast.makeText(getApplicationContext(), "Please connect to the internet to execute this action.", Toast.LENGTH_LONG).show();
    }

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

    private void SavePreference() {
        SharedPreferences settings = getSharedPreferences("ejeepPrefs", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("log_time_allowance", logTimeAllowance);
        editor.commit();
    }

    private void GetPreference() {
        SharedPreferences settings = getSharedPreferences("ejeepPrefs", 0);
        logTimeAllowance = settings.getInt("log_time_allowance", 2);
    }

//    private void displayBetweenDate(DateTime dateToday, DateTime lastDonateDate){
//        Days _days = Days.daysBetween(lastDonateDate,dateToday);
//        int _m = new Period(lastDonateDate,dateToday).getMonths();
//        //Log.i(PROCESS_MAIN,"Months: " + _m);
//    }
}
