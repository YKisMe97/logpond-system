package com.infocomm.logpond_v2.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.infocomm.logpond_v2.storage.SharedPreferencesStorage;
import com.infocomm.logpond_v2.util.ArrayListSortingManager;
import com.infocomm.logpond_v2.util.CalendarUtil;
import com.infocomm.logpond_v2.util.FileManager;
import com.infocomm.logpond_v2.view.CustomAlertDialog;
import com.infocomm.logpond_v2.view.MySnackBar;
import com.infocomm.logpond_v2.R;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class LogpondOutDetailsActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, View.OnFocusChangeListener, AdapterView.OnItemSelectedListener {

    private final int ENTRY_LIST_REQUEST_CODE = 99;
    private LogpondOutDetailsActivity activity;
   // private TextView entryNoTextView, dateTextView;
    private SearchableSpinner actionSpinner, toLogpondSpinner, fromLogpondSpinner, customerSpinner, vesselNoSpinner, hatchNoSpinner ,sawmillSpinner, campUseSpinner, bargingSpinner, motherHatchSpinner;
    private View truckingLayout, logpondLayout, vesselLayout, containerLayout, customerLayout, vvbLayout, driverLayout, customerLayout1, hatchLayout, sealLayout, pileLayout, spare1, campUseLayout, bargingLayout, operatorLayout, motherHatchLayout, row2;
    private EditText entryNoEditText, dateEditText, vvbNoEditText, driverNameEditText, plateNoEditText, containerNoEditText, serialNoEditText, pileEditText, operatorEditText;
    private View nextLayout;
    private JSONObject jsonObject, detailsJsonObject;
    private String[] dataArray, actionArray, labelEntryArray, logpondArray, customerArray, vesselNoArray, hatchNoArray, sawmillArray, campUseArray, bargingArray;
    private int position;
    private boolean isEditable;
    private int totalLabelEntry;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private boolean isSave;
    private JSONObject fileJsonObject;
    private TextView subtitle, entryno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_logpond_out_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setLogo(R.drawable.forest);
        subtitle = findViewById(R.id.logpond_out_detail_subtitle);
        entryno = findViewById(R.id.logpond_out_detail_toolbar_entry_no);

        isSave = false;
        fileJsonObject = new JSONObject();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setIndeterminate(true);

        totalLabelEntry = 0;
        activity = this;
        isEditable = getIntent().getBooleanExtra("isEditable", true);

        entryNoEditText = (EditText) findViewById(R.id.entry_no);
        entryNoEditText.setEnabled(false);
        dateEditText = (EditText) findViewById(R.id.date);
        dateEditText.setOnClickListener(this);
        dateEditText.setOnFocusChangeListener(this);
        dateEditText.setText(CalendarUtil.getTodayDate());
        dateEditText.setInputType(InputType.TYPE_NULL);
        dateEditText.setEnabled(isEditable);
        vvbNoEditText = (EditText) findViewById(R.id.vvb_no);
        vvbNoEditText.setEnabled(isEditable);
        vvbNoEditText.setOnFocusChangeListener(this);
        driverNameEditText = (EditText) findViewById(R.id.driver_name);
        driverNameEditText.setEnabled(isEditable);
        driverNameEditText.setOnFocusChangeListener(this);
        plateNoEditText = (EditText) findViewById(R.id.plate_no);
        plateNoEditText.setEnabled(isEditable);
        plateNoEditText.setOnFocusChangeListener(this);
        containerNoEditText = (EditText) findViewById(R.id.container_no);
        containerNoEditText.setEnabled(isEditable);
        containerNoEditText.setOnFocusChangeListener(this);
        serialNoEditText = (EditText) findViewById(R.id.serial_no);
        serialNoEditText.setEnabled(isEditable);
        serialNoEditText.setOnFocusChangeListener(this);
        pileEditText = findViewById(R.id.pile);
        pileEditText.setEnabled(isEditable);
        pileEditText.setOnFocusChangeListener(this);
        operatorEditText = findViewById(R.id.operator_editText);
        operatorEditText.setEnabled(isEditable);
        operatorEditText.setOnFocusChangeListener(this);

        //truckingLayout = findViewById(R.id.trucking_layout);
        vvbLayout = findViewById(R.id.vvb_layout);
        driverLayout = findViewById(R.id.driver_layout);
        logpondLayout = findViewById(R.id.logpond_layout);
        vesselLayout = findViewById(R.id.vessel_layout);
        containerLayout = findViewById(R.id.container_layout);
        customerLayout = findViewById(R.id.customer_layout);
        customerLayout1 = findViewById(R.id.customer_layout1);
        hatchLayout = findViewById(R.id.hatch_layout);
        sealLayout = findViewById(R.id.seal_layout);
        pileLayout = findViewById(R.id.pile_Layout);
        campUseLayout = findViewById(R.id.camp_use_layout);
        bargingLayout = findViewById(R.id.barging_layout);
        operatorLayout = findViewById(R.id.operator_Layout);
        spare1 = findViewById(R.id.spare1);
        motherHatchLayout = findViewById(R.id.mother_hatch_layout);
        row2 = findViewById(R.id.row2);

        nextLayout = findViewById(R.id.next_layout);
        nextLayout.setOnClickListener(this);

        actionSpinner = (SearchableSpinner) findViewById(R.id.action_spinner);
        actionSpinner.setEnabled(isEditable);
        actionArray = getResources().getStringArray(R.array.logpond_out_action);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, actionArray);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionSpinner.setAdapter(dataAdapter);
        actionSpinner.setOnItemSelectedListener(this);

        jsonObject = new JSONObject();
        detailsJsonObject = new JSONObject();

        logpondArray = new String[]{""};
        String[] lodpongDb = FileManager.retrieveTextContentAsStringArray(this, FileManager.getPrivateDirPath(this) + "/android_db/" + SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "/logpond.txt");
        if(lodpongDb!=null) {
            logpondArray = new String[lodpongDb.length + 1];
            logpondArray[0] = "";
            System.arraycopy(lodpongDb, 0, logpondArray, 1, lodpongDb.length);
        }

        ArrayAdapter<String> toLogpondAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, logpondArray);
        toLogpondAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toLogpondSpinner = (SearchableSpinner) findViewById(R.id.to_logpond_spinner);
        toLogpondSpinner.setEnabled(isEditable);
        toLogpondSpinner.setAdapter(toLogpondAdapter);
        toLogpondSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<String> fromLogpondAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, logpondArray);
        fromLogpondAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromLogpondSpinner = (SearchableSpinner) findViewById(R.id.from_logpond_spinner);
        fromLogpondSpinner.setEnabled(isEditable);
        fromLogpondSpinner.setAdapter(fromLogpondAdapter);
        fromLogpondSpinner.setOnItemSelectedListener(this);

        vesselNoArray = new String[]{""};
        String[] vesselDb = FileManager.retrieveTextContentAsStringArray(this, FileManager.getPrivateDirPath(this) + "/android_db/" + SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "/category_vessel.txt");
        if(vesselDb!=null) {
            vesselNoArray = new String[vesselDb.length + 1];
            vesselNoArray[0] = "";
            System.arraycopy(vesselDb, 0, vesselNoArray, 1, vesselDb.length);
        }

        ArrayAdapter<String> vesselNoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, vesselNoArray);
        vesselNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vesselNoSpinner = (SearchableSpinner) findViewById(R.id.vessel_no_spinner);
        vesselNoSpinner.setEnabled(isEditable);
        vesselNoSpinner.setAdapter(vesselNoAdapter);
        vesselNoSpinner.setOnItemSelectedListener(this);

        hatchNoArray = new String[]{""};
        String[] hatchDb = FileManager.retrieveTextContentAsStringArray(this, FileManager.getPrivateDirPath(this) + "/android_db/" + SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "/category_hatch.txt");
        if(hatchDb!=null) {
            hatchNoArray = new String[hatchDb.length + 1];
            hatchNoArray[0] = "";
            System.arraycopy(hatchDb, 0, hatchNoArray, 1, hatchDb.length);
        }

        ArrayAdapter<String> hatchNoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, hatchNoArray);
        hatchNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hatchNoSpinner = (SearchableSpinner) findViewById(R.id.hatch_no_spinner);
        hatchNoSpinner.setEnabled(isEditable);
        hatchNoSpinner.setAdapter(hatchNoAdapter);
        hatchNoSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<String> motherHatchNoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, hatchNoArray);
        motherHatchNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        motherHatchSpinner= (SearchableSpinner) findViewById(R.id.mother_hatch_spinner);
        motherHatchSpinner.setEnabled(isEditable);
        motherHatchSpinner.setAdapter(motherHatchNoAdapter);
        motherHatchSpinner.setOnItemSelectedListener(this);

        customerArray = new String[]{""};
        String[] customerDb = FileManager.retrieveTextContentAsStringArray(this, FileManager.getPrivateDirPath(this) + "/android_db/" + SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "/cust.txt");
        if(customerDb!=null) {
            customerArray = new String[customerDb.length + 1];
            customerArray[0] = "";
            System.arraycopy(customerDb, 0, customerArray, 1, customerDb.length);
        }

        ArrayAdapter<String> customerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, customerArray);
        customerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        customerSpinner = (SearchableSpinner) findViewById(R.id.customer_spinner);
        customerSpinner.setEnabled(isEditable);
        customerSpinner.setAdapter(customerAdapter);
        customerSpinner.setOnItemSelectedListener(this);

        sawmillArray = new String[]{""};
        String[] sawmillDb = FileManager.retrieveTextContentAsStringArray(this, FileManager.getPrivateDirPath(this) + "/android_db/" + SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "/category_sawmill.txt");
        for (int i = 0; i<sawmillDb.length;i++){
            sawmillDb[i] = ArrayListSortingManager.encodeSpecialChar(sawmillDb[i]);
        }
        if(sawmillDb!=null) {
            sawmillArray = new String[sawmillDb.length + 1];
            sawmillArray[0] = "";
            System.arraycopy(sawmillDb, 0, sawmillArray, 1, sawmillDb.length);
        }

        ArrayAdapter<String> sawmillAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sawmillArray);
        sawmillAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sawmillSpinner = (SearchableSpinner) findViewById(R.id.sawmill_spinner);
        sawmillSpinner.setEnabled(isEditable);
        sawmillSpinner.setAdapter(sawmillAdapter);
        sawmillSpinner.setOnItemSelectedListener(this);

        campUseArray = new String[]{""};
        String[] campUseDb = FileManager.retrieveTextContentAsStringArray(this, FileManager.getPrivateDirPath(this) + "/android_db/" + SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "/category_camp.txt");

        if(campUseDb!=null) {
            campUseArray = new String[campUseDb.length + 1];
            campUseArray[0] = "";
            System.arraycopy(campUseDb, 0, campUseArray, 1, campUseDb.length);
        }

        ArrayAdapter<String> campUseAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, campUseArray);
        campUseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campUseSpinner = (SearchableSpinner) findViewById(R.id.camp_use_spinner);
        campUseSpinner.setEnabled(isEditable);
        campUseSpinner.setAdapter(campUseAdapter);
        campUseSpinner.setOnItemSelectedListener(this);

        bargingArray = new String[]{""};
        String[] bargingDb = FileManager.retrieveTextContentAsStringArray(this, FileManager.getPrivateDirPath(this) + "/android_db/" + SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "/category_barge.txt");
        for (int i = 0; i<bargingDb.length;i++){
            bargingDb[i] = ArrayListSortingManager.encodeSpecialChar(bargingDb[i]);
        }
        if(bargingDb!=null) {
            bargingArray = new String[bargingDb.length + 1];
            bargingArray[0] = "";
            System.arraycopy(bargingDb, 0, bargingArray, 1, bargingDb.length);
        }

        ArrayAdapter<String> bargingAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, bargingArray);
        bargingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bargingSpinner = (SearchableSpinner) findViewById(R.id.barge_no_spinner);
        bargingSpinner.setEnabled(isEditable);
        bargingSpinner.setAdapter(bargingAdapter);
        bargingSpinner.setOnItemSelectedListener(this);

        if(getIntent().hasExtra("data") && getIntent().getStringExtra("data")!=null){
            position = getIntent().getIntExtra("position", -1);
            try {
                jsonObject = new JSONObject(getIntent().getStringExtra("data"));
                if(!jsonObject.isNull("filePath"))
                    dataArray = FileManager.retrieveTextContentAsStringArray(this, jsonObject.getString("filePath"));

                // 0 - Entry No
                // 1 - Entry Date
                // 2 - Action
                // 3 - VVB
                // 4 - From Logpond
                // 5 - To Logpond
                // 6 - Driver name
                // 7 - Plate No
                // 8 - Vessel No
                // 9 - Hatch No
                // 10 - Container No
                // 11 - Serial No
                // 12 - Customer Name
                // 13 - User Account
                // 14 - Total label

                if(dataArray!=null && dataArray.length>14){
                    entryNoEditText.setText(dataArray[0]);
                    entryno.setText("Entry No: " + dataArray[0]);
                    dateEditText.setText(CalendarUtil.changeDateTimeFormatByString("yyyy-MM-dd", "dd/MM/yyyy",dataArray[1]));
                    if (dataArray[2].equals("Peddling")) {
                        pileEditText.setText(dataArray[3]);
                    }
                    if (dataArray[2].equals("Barging")){
                        if(hatchNoArray!=null){
                            for(int i=0;i<hatchNoArray.length;i++){
                                if(hatchNoArray[i].equalsIgnoreCase(dataArray[7])){
                                    hatchNoSpinner.setSelection(i);
                                }
                            }
                        }
                    }

                    vvbNoEditText.setText(dataArray[3]);
                    driverNameEditText.setText(dataArray[6]);
                    plateNoEditText.setText(dataArray[7]);
                    containerNoEditText.setText(dataArray[10]);
                    serialNoEditText.setText(dataArray[11]);
                    operatorEditText.setText(dataArray[6]);

                    for(int i=0;i<actionArray.length;i++){
                        if(actionArray[i].equalsIgnoreCase(dataArray[2])){
                            actionSpinner.setSelection(i);
                        }
                    }

                    if(logpondArray!=null){
                        for(int i=0;i<logpondArray.length;i++){
                            if(logpondArray[i].equalsIgnoreCase(dataArray[4])){
                                fromLogpondSpinner.setSelection(i);
                            }

                            if(logpondArray[i].equalsIgnoreCase(dataArray[5])){
                                toLogpondSpinner.setSelection(i);
                            }
                        }
                    }

                    if(vesselNoArray!=null){
                        for(int i=0;i<vesselNoArray.length;i++){
                            if(vesselNoArray[i].equalsIgnoreCase(dataArray[8])){
                                vesselNoSpinner.setSelection(i);
                            }
                        }
                    }

                    if(hatchNoArray!=null){
                        if (dataArray[2].equals("Export STS")){
                            if(hatchNoArray!=null){
                                for(int i=0;i<hatchNoArray.length;i++){
                                    if(hatchNoArray[i].equalsIgnoreCase(dataArray[9])){
                                        motherHatchSpinner.setSelection(i);
                                    }
                                    if(hatchNoArray[i].equalsIgnoreCase(dataArray[7])){
                                        hatchNoSpinner.setSelection(i);
                                    }
                                }
                            }
                        }
                        else {
                            for (int i = 0; i < hatchNoArray.length; i++) {
                                if (hatchNoArray[i].equalsIgnoreCase(dataArray[9])) {
                                    hatchNoSpinner.setSelection(i);
                                }
                            }
                        }
                    }

                    if(customerArray!=null){
                        for(int i=0;i<customerArray.length;i++){
                            if(customerArray[i].equalsIgnoreCase(dataArray[12])){
                                customerSpinner.setSelection(i);
                            }
                        }
                    }

                    if(sawmillArray!=null){
                        for(int i=0;i<sawmillArray.length;i++){
                            if(sawmillArray[i].equalsIgnoreCase(dataArray[12])){
                                sawmillSpinner.setSelection(i);
                            }
                        }
                    }

                    if(campUseArray!=null){
                        for(int i=0;i<campUseArray.length;i++){
                            if(campUseArray[i].equalsIgnoreCase(dataArray[12])){
                                campUseSpinner.setSelection(i);
                            }
                        }
                    }
                    if (bargingArray!=null){
                        for(int i=0;i<bargingArray.length;i++){
                            if(bargingArray[i].equalsIgnoreCase(dataArray[3])){
                                bargingSpinner.setSelection(i);
                            }
                        }
                    }

                    totalLabelEntry = Integer.parseInt(dataArray[14]);
                    if(totalLabelEntry>0){
                        labelEntryArray = new String[totalLabelEntry];
                        for(int i=0;i<totalLabelEntry;i++){
                            labelEntryArray[i] = dataArray[15 + i];
                        }
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        // Resume
        }else if(FileManager.retrieveTextContentAsStringArrayFromTempFile(activity, "LOGPOND_OUT.TXT") != null) {
            Object[] resumeObject = FileManager.retrieveTextContentAsStringArrayFromTempFile(activity, "LOGPOND_OUT.TXT");
            position = (int) resumeObject[0];
            dataArray = (String[]) resumeObject[1];

            // 0 - Entry No
            // 1 - Entry Date
            // 2 - Action
            // 3 - VVB
            // 4 - From Logpond
            // 5 - To Logpond
            // 6 - Driver name
            // 7 - Plate No
            // 8 - Vessel No
            // 9 - Hatch No
            // 10 - Container No
            // 11 - Serial No
            // 12 - Customer Name
            // 13 - User Account
            // 14 - Total label

            if(dataArray!=null && dataArray.length>14){
                entryNoEditText.setText(dataArray[0]);
                entryno.setText("Entry No: " + dataArray[0]);
                dateEditText.setText(CalendarUtil.changeDateTimeFormatByString("yyyy-MM-dd", "dd/MM/yyyy",dataArray[1]));
                vvbNoEditText.setText(dataArray[3]);
                driverNameEditText.setText(dataArray[6]);
                operatorEditText.setText(dataArray[6]);
                plateNoEditText.setText(dataArray[7]);
                containerNoEditText.setText(dataArray[10]);
                serialNoEditText.setText(dataArray[11]);

                for(int i=0;i<actionArray.length;i++){
                    if(actionArray[i].equalsIgnoreCase(dataArray[2])){
                        actionSpinner.setSelection(i);
                    }
                }
                if(logpondArray!=null){
                    for(int i=0;i<logpondArray.length;i++){
                        if(logpondArray[i].equalsIgnoreCase(dataArray[4])){
                            fromLogpondSpinner.setSelection(i);
                        }

                        if(logpondArray[i].equalsIgnoreCase(dataArray[5])){
                            toLogpondSpinner.setSelection(i);
                        }
                    }
                }

                if(vesselNoArray!=null){
                    for(int i=0;i<vesselNoArray.length;i++){
                        if(vesselNoArray[i].equalsIgnoreCase(dataArray[8])){
                            vesselNoSpinner.setSelection(i);
                        }
                    }
                }

                if(hatchNoArray!=null){
                    if (dataArray[2].equals("Export STS")){
                        if(hatchNoArray!=null){
                            for(int i=0;i<hatchNoArray.length;i++){
                                if(hatchNoArray[i].equalsIgnoreCase(dataArray[9])){
                                    motherHatchSpinner.setSelection(i);
                                }
                                if(hatchNoArray[i].equalsIgnoreCase(dataArray[7])){
                                    hatchNoSpinner.setSelection(i);
                                }
                            }
                        }
                    }
                    else {
                        for (int i = 0; i < hatchNoArray.length; i++) {
                            if (hatchNoArray[i].equalsIgnoreCase(dataArray[9])) {
                                hatchNoSpinner.setSelection(i);
                            }
                        }
                    }
                }

                if(customerArray!=null){
                    for(int i=0;i<customerArray.length;i++){
                        if(customerArray[i].equalsIgnoreCase(dataArray[12])){
                            customerSpinner.setSelection(i);
                        }
                    }
                }

                if(sawmillArray!=null){
                    for(int i=0;i<sawmillArray.length;i++){
                        if(sawmillArray[i].equalsIgnoreCase(dataArray[12])){
                            sawmillSpinner.setSelection(i);
                        }
                    }
                }

                if(campUseArray!=null){
                    for(int i=0;i<campUseArray.length;i++){
                        if(campUseArray[i].equalsIgnoreCase(dataArray[12])){
                            campUseSpinner.setSelection(i);
                        }
                    }
                }
                if (bargingArray!=null){
                    for(int i=0;i<bargingArray.length;i++){
                        if(bargingArray[i].equalsIgnoreCase(dataArray[3])){
                            bargingSpinner.setSelection(i);
                        }
                    }
                }

                totalLabelEntry = Integer.parseInt(dataArray[14]);
                if(totalLabelEntry>0){
                    labelEntryArray = new String[totalLabelEntry];
                    for(int i=0;i<totalLabelEntry;i++){
                        labelEntryArray[i] = dataArray[15 + i];
                    }
                }

            }

            // New
        }else{
            position = -1;
            entryno.setText("Entry No: " + FileManager.getLatestLogponOutEntryNoInPrivate(this));
            entryNoEditText.setText(FileManager.getLatestLogponOutEntryNoInPrivate(this));
            dateEditText.setText(CalendarUtil.changeDateTimeFormatByString("yyyy-MM-dd", "dd/MM/yyyy", CalendarUtil.getTodayDate()));
        }

        subtitle.setText(getString(R.string.entries_pcs).replace("|COUNT|", String.valueOf(totalLabelEntry)));
        //toolbar.setSubtitle(getString(R.string.entries_pcs).replace("|COUNT|", String.valueOf(totalLabelEntry)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //killActivity();
                showExitSaveDialog();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        //killActivity();
        showExitSaveDialog();
    }

    private void killActivity() {
        FileManager.removeTempFile(getApplicationContext(), "LOGPOND_OUT.TXT");
        Intent resultIntent = new Intent();
        if (isSave) {
            resultIntent.putExtra("action", "saved");
            resultIntent.putExtra("fileJsonObjectString", fileJsonObject.toString());
        }
        resultIntent.putExtra("position", position);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showExitSaveDialog(){
        if(isEditable){
            final CustomAlertDialog saveAlertDialogs = new CustomAlertDialog();
            View.OnClickListener save_listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveFile();
                    saveAlertDialogs.cancel();
                }
            };

            View.OnClickListener kill_listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    killActivity();
                }
            };
            saveAlertDialogs.MyDialogWithYesNo(LogpondOutDetailsActivity.this,getString(R.string.app_name), getString(R.string.do_you_want_to_save),save_listener,kill_listener);
        }else{
            killActivity();
        }

    }

    public void hideKeyboard(View view) {
        if (view!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onFocusChange(View view, boolean focus) {
        saveTempFile();
        if(view==dateEditText){
            if(focus){
                hideKeyboard(dateEditText);
                String dob = dateEditText.getText().toString();
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DATE);
                if(dob!=null&&dob.length()>0){
                    String[] dateProp = dob.split("/", -1);
                    day = Integer.parseInt(dateProp[0]);
                    month = Integer.parseInt(dateProp[1]) - 1;
                    year = Integer.parseInt(dateProp[2]);
                }

                DatePickerDialog datePicker = new DatePickerDialog(this, R.style.MyDatePickerDialogTheme, this, year, month, day);
                datePicker.show();
            }
        }
    }

    @Override
    public void onClick(View view) {
        hideKeyboard(view);
        if(view == dateEditText){
            String[] dateProp = dateEditText.getText().toString().split("/");
            int day = Integer.parseInt(dateProp[0]);
            int month = Integer.parseInt(dateProp[1]) - 1;
            int year = Integer.parseInt(dateProp[2]);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.MyDatePickerDialogTheme, this, year, month, day);
            //datePickerDialog.getDatePicker().setMaxDate(maxCalendar.getTimeInMillis());
            //datePickerDialog.getDatePicker().setMinDate(minCalendar.getTimeInMillis());
            datePickerDialog.show();
        }else if(view == nextLayout){
            if(isEditable){
                // Trucking
                if(actionSpinner.getSelectedItemPosition() == 0){
                    if(fromLogpondSpinner.getSelectedItemPosition() == 0){
                        MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_select_from_logpond_message), Snackbar.LENGTH_LONG);
                        fromLogpondSpinner.requestFocus();
                        return;
                    }
                    else if(toLogpondSpinner.getSelectedItemPosition() == 0){
                        MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_select_to_logpond_message), Snackbar.LENGTH_LONG);
                        toLogpondSpinner.requestFocus();
                        return;
                    }
                    else if(toLogpondSpinner.getSelectedItemPosition() == fromLogpondSpinner.getSelectedItemPosition()){
                        MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.from_logpond_same_to_logpond_error), Snackbar.LENGTH_LONG);
                        toLogpondSpinner.requestFocus();
                        return;
                    }

                    //Peddling
                }
                else if (actionSpinner.getSelectedItemPosition() == 1){
                    if (driverNameEditText.getText().toString().isEmpty()){
                        MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_fill_in_driver_name_message), Snackbar.LENGTH_LONG);
                        driverNameEditText.requestFocus();
                        return;
                    }else if (plateNoEditText.getText().toString().isEmpty()){
                        MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_fill_in_plate_no_message), Snackbar.LENGTH_LONG);
                        plateNoEditText.requestFocus();
                        return;
                    }else if(fromLogpondSpinner.getSelectedItemPosition() == 0){
                        MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_select_from_logpond_message), Snackbar.LENGTH_LONG);
                        fromLogpondSpinner.requestFocus();
                        return;
                    }else if(toLogpondSpinner.getSelectedItemPosition() == 0){
                        MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_select_to_logpond_message), Snackbar.LENGTH_LONG);
                        toLogpondSpinner.requestFocus();
                        return;
                    }else if(toLogpondSpinner.getSelectedItemPosition() == fromLogpondSpinner.getSelectedItemPosition()){
                        MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.from_logpond_same_to_logpond_error), Snackbar.LENGTH_LONG);
                        toLogpondSpinner.requestFocus();
                        return;
                    }
                }

                // Local sales
                else if(actionSpinner.getSelectedItemPosition() == 2){

                    if (vvbNoEditText.getText().toString().isEmpty()){
                        MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_fill_vvb_no), Snackbar.LENGTH_LONG);
                        vvbNoEditText.requestFocus();
                        return;
                    }
                    else if (driverNameEditText.getText().toString().isEmpty()){
                        MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_fill_in_driver_name_message), Snackbar.LENGTH_LONG);
                        driverNameEditText.requestFocus();
                        return;
                    }
                    else if (plateNoEditText.getText().toString().isEmpty()){
                        MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_fill_in_plate_no_message), Snackbar.LENGTH_LONG);
                        plateNoEditText.requestFocus();
                        return;
                    }
                }
                else if(actionSpinner.getSelectedItemPosition() == 3){
                    if(vesselNoSpinner.getSelectedItemPosition() == 0){
                        MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_select_vessel_no_message), Snackbar.LENGTH_LONG);
                        vesselNoSpinner.requestFocus();
                        return;
                    }else if(hatchNoSpinner.getSelectedItemPosition() == 0){
                        MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_select_hatch_no_message), Snackbar.LENGTH_LONG);
                        hatchNoSpinner.requestFocus();
                        return;
                    }

                    // Export Container
                }
                else if (actionSpinner.getSelectedItemPosition() == 4){
                    if(containerNoEditText.getText().length() == 0){
                        MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_fill_in_container_no_message), Snackbar.LENGTH_LONG);
                        containerNoEditText.requestFocus();
                        return;
                    }
                }
                //Sawmill
                else if (actionSpinner.getSelectedItemPosition() == 5){
                    if(sawmillSpinner.getSelectedItemPosition() == 0){
                        MySnackBar.showError(customerLayout1, "Please Select Sawmill", Snackbar.LENGTH_LONG);
                        sawmillSpinner.requestFocus();
                        return;
                    }
                }
                //Camp Use
                else if (actionSpinner.getSelectedItemPosition() == 6){
                    if(campUseSpinner.getSelectedItemPosition() == 0){
                        MySnackBar.showError(findViewById(android.R.id.content), "Please Select Camp Use", Snackbar.LENGTH_LONG);
                        campUseSpinner.requestFocus();
                        return;
                    }
                }
                //Barging
                else if (actionSpinner.getSelectedItemPosition()== 7){
                    if (bargingSpinner.getSelectedItemPosition()==0){
                        MySnackBar.showError(findViewById(android.R.id.content), "Please Select Barge No", Snackbar.LENGTH_LONG);
                        bargingSpinner.requestFocus();
                        return;
                    }
                    else if(fromLogpondSpinner.getSelectedItemPosition() == 0){
                        MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_select_from_logpond_message), Snackbar.LENGTH_LONG);
                        fromLogpondSpinner.requestFocus();
                        return;
                    }
                    else if(toLogpondSpinner.getSelectedItemPosition() == 0){
                        MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_select_to_logpond_message), Snackbar.LENGTH_LONG);
                        toLogpondSpinner.requestFocus();
                        return;
                    }
                    else if(toLogpondSpinner.getSelectedItemPosition() == fromLogpondSpinner.getSelectedItemPosition()){
                        MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.from_logpond_same_to_logpond_error), Snackbar.LENGTH_LONG);
                        toLogpondSpinner.requestFocus();
                        return;
                    }
                }
                //Export STS
                else if (actionSpinner.getSelectedItemPosition()==8){
                    if (bargingSpinner.getSelectedItemPosition()==0){
                        MySnackBar.showError(findViewById(android.R.id.content), "Please Select Barge No", Snackbar.LENGTH_LONG);
                        bargingSpinner.requestFocus();
                        return;
                    }
                    else if (motherHatchSpinner.getSelectedItemPosition()==0){
                        MySnackBar.showError(findViewById(android.R.id.content), "Please Select Mother Hatch", Snackbar.LENGTH_LONG);
                        motherHatchSpinner.requestFocus();
                        return;
                    }
                    else if (vesselNoSpinner.getSelectedItemPosition()==0){
                        MySnackBar.showError(findViewById(android.R.id.content), "Please Select Mother Vessel", Snackbar.LENGTH_LONG);
                        vesselNoSpinner.requestFocus();
                        return;
                    }
                }
            }
            try {
                detailsJsonObject.put("entryNo", entryNoEditText.getText().toString());
                detailsJsonObject.put("date", CalendarUtil.changeDateTimeFormatByString("dd/MM/yyyy", "yyyy-MM-dd", dateEditText.getText().toString()));
                detailsJsonObject.put("action", actionArray[actionSpinner.getSelectedItemPosition()]);
                detailsJsonObject.put("vvbNo", vvbNoEditText.getText().toString());
                detailsJsonObject.put("fromLogpond", logpondArray[fromLogpondSpinner.getSelectedItemPosition()]);
                detailsJsonObject.put("toLogpond", logpondArray[toLogpondSpinner.getSelectedItemPosition()]);
                detailsJsonObject.put("driverName", driverNameEditText.getText().toString());
                detailsJsonObject.put("plateNo", plateNoEditText.getText().toString());
                detailsJsonObject.put("vesselNo", vesselNoArray[vesselNoSpinner.getSelectedItemPosition()]);
                detailsJsonObject.put("hatchNo", hatchNoArray[hatchNoSpinner.getSelectedItemPosition()]);
                detailsJsonObject.put("containerNo", containerNoEditText.getText().toString());
                detailsJsonObject.put("serialNo", serialNoEditText.getText().toString());
                detailsJsonObject.put("customer", customerArray[customerSpinner.getSelectedItemPosition()]);
                detailsJsonObject.put("sawmill", sawmillArray[sawmillSpinner.getSelectedItemPosition()]);
                detailsJsonObject.put("campUse", campUseArray[campUseSpinner.getSelectedItemPosition()]);
                detailsJsonObject.put("pile", pileEditText.getText().toString());
                detailsJsonObject.put("bargeNo", bargingArray[bargingSpinner.getSelectedItemPosition()]);
                detailsJsonObject.put("operator", operatorEditText.getText().toString());
                detailsJsonObject.put("motherHatchNo", hatchNoArray[motherHatchSpinner.getSelectedItemPosition()]);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

            Intent entryListIntent = new Intent(LogpondOutDetailsActivity.this, LogpondOutEntryListActivity.class);
            //entryListIntent.putExtra("dataArray", dataArray);
            entryListIntent.putExtra("entryno", entryNoEditText.getText().toString());
            entryListIntent.putExtra("position", position);
            entryListIntent.putExtra("details", detailsJsonObject.toString());
            entryListIntent.putExtra("labelEntryArray", labelEntryArray);
            entryListIntent.putExtra("isEditable", isEditable);
            startActivityForResult(entryListIntent, ENTRY_LIST_REQUEST_CODE);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            if(isEditable)
                saveTempFile();
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        if (year != -1) calendar.set(Calendar.YEAR, year);
        if (month != -1) calendar.set(Calendar.MONTH, month);
        if (day != -1) calendar.set(Calendar.DATE, day);
        dateEditText.setText(sdf.format(calendar.getTime()));
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        saveTempFile();
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~view,=" + view);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~adapterView,=" + adapterView);
        hideKeyboard(view);
        if(adapterView.getId() == R.id.action_spinner){
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~actionSpinner,=" + view);
            if (position==1){
                row2.setVisibility(View.GONE);
            }else{
                row2.setVisibility(View.VISIBLE);
            }

            switch(position){
                // Trucking
                case 0:
                    //truckingLayout.setVisibility(View.VISIBLE);
                    pileEditText.setText("");
                    pileLayout.setVisibility(View.GONE);
                    vvbLayout.setVisibility(View.VISIBLE);
                    driverLayout.setVisibility(View.VISIBLE);
                    logpondLayout.setVisibility(View.VISIBLE);
                    vesselLayout.setVisibility(View.GONE);
                    containerLayout.setVisibility(View.GONE);
                    customerLayout1.setVisibility(View.GONE);
                    campUseLayout.setVisibility(View.GONE);
                    customerLayout.setVisibility(View.GONE);
                    hatchLayout.setVisibility(View.GONE);
                    sealLayout.setVisibility(View.GONE);
                    spare1.setVisibility(View.GONE);
                    bargingLayout.setVisibility(View.GONE);
                    operatorLayout.setVisibility(View.GONE);
                    motherHatchLayout.setVisibility(View.GONE);
                    break;

                // Peddling
                case 1:
                    vvbNoEditText.setText("");
                    pileLayout.setVisibility(View.VISIBLE);
                    vvbLayout.setVisibility(View.GONE);
                    driverLayout.setVisibility(View.VISIBLE);
                    logpondLayout.setVisibility(View.VISIBLE);
                    vesselLayout.setVisibility(View.GONE);
                    containerLayout.setVisibility(View.GONE);
                    customerLayout.setVisibility(View.GONE);
                    customerLayout1.setVisibility(View.GONE);
                    campUseLayout.setVisibility(View.GONE);
                    hatchLayout.setVisibility(View.GONE);
                    sealLayout.setVisibility(View.GONE);
                    spare1.setVisibility(View.INVISIBLE);
                    bargingLayout.setVisibility(View.GONE);
                    operatorLayout.setVisibility(View.GONE);
                    motherHatchLayout.setVisibility(View.GONE);
                    break;

                // Local sales
                case 2:
                    //truckingLayout.setVisibility(View.VISIBLE);
                    pileLayout.setVisibility(View.GONE);
                    vvbLayout.setVisibility(View.VISIBLE);
                    driverLayout.setVisibility(View.VISIBLE);
                    logpondLayout.setVisibility(View.GONE);
                    vesselLayout.setVisibility(View.GONE);
                    containerLayout.setVisibility(View.GONE);
                    customerLayout.setVisibility(View.VISIBLE);
                    customerLayout1.setVisibility(View.GONE);
                    campUseLayout.setVisibility(View.GONE);
                    hatchLayout.setVisibility(View.GONE);
                    sealLayout.setVisibility(View.GONE);
                    spare1.setVisibility(View.GONE);
                    bargingLayout.setVisibility(View.GONE);
                    operatorLayout.setVisibility(View.GONE);
                    motherHatchLayout.setVisibility(View.GONE);
                    break;

                // Export Vessel
                case 3:
                    //truckingLayout.setVisibility(View.GONE);
                    toLogpondSpinner.setSelection(0);
                    fromLogpondSpinner.setSelection(0);
                    pileLayout.setVisibility(View.GONE);
                    vvbLayout.setVisibility(View.GONE);
                    driverLayout.setVisibility(View.GONE);
                    logpondLayout.setVisibility(View.GONE);
                    vesselLayout.setVisibility(View.VISIBLE);
                    containerLayout.setVisibility(View.GONE);
                    customerLayout.setVisibility(View.VISIBLE);
                    customerLayout1.setVisibility(View.GONE);
                    campUseLayout.setVisibility(View.GONE);
                    hatchLayout.setVisibility(View.VISIBLE);
                    sealLayout.setVisibility(View.GONE);
                    spare1.setVisibility(View.GONE);
                    bargingLayout.setVisibility(View.GONE);
                    operatorLayout.setVisibility(View.GONE);
                    motherHatchLayout.setVisibility(View.GONE);
                    break;

                // Export Container
                case 4:
                    //truckingLayout.setVisibility(View.GONE);
                    pileLayout.setVisibility(View.GONE);
                    vvbLayout.setVisibility(View.GONE);
                    driverLayout.setVisibility(View.GONE);
                    logpondLayout.setVisibility(View.GONE);
                    vesselLayout.setVisibility(View.GONE);
                    containerLayout.setVisibility(View.VISIBLE);
                    customerLayout.setVisibility(View.VISIBLE);
                    customerLayout1.setVisibility(View.GONE);
                    campUseLayout.setVisibility(View.GONE);
                    hatchLayout.setVisibility(View.GONE);
                    sealLayout.setVisibility(View.VISIBLE);
                    spare1.setVisibility(View.GONE);
                    bargingLayout.setVisibility(View.GONE);
                    operatorLayout.setVisibility(View.GONE);
                    motherHatchLayout.setVisibility(View.GONE);
                    break;

                // Sawmill
                case 5:
                    pileLayout.setVisibility(View.GONE);
                    vvbLayout.setVisibility(View.VISIBLE);
                    driverLayout.setVisibility(View.VISIBLE);
                    logpondLayout.setVisibility(View.GONE);
                    vesselLayout.setVisibility(View.GONE);
                    containerLayout.setVisibility(View.GONE);
                    customerLayout.setVisibility(View.GONE);
                    customerLayout1.setVisibility(View.VISIBLE);
                    campUseLayout.setVisibility(View.GONE);
                    hatchLayout.setVisibility(View.GONE);
                    sealLayout.setVisibility(View.GONE);
                    spare1.setVisibility(View.GONE);
                    bargingLayout.setVisibility(View.GONE);
                    operatorLayout.setVisibility(View.GONE);
                    motherHatchLayout.setVisibility(View.GONE);
                    break;
                // Campuse
                case 6:
                    pileLayout.setVisibility(View.GONE);
                    vvbLayout.setVisibility(View.VISIBLE);
                    driverLayout.setVisibility(View.VISIBLE);
                    logpondLayout.setVisibility(View.GONE);
                    vesselLayout.setVisibility(View.GONE);
                    containerLayout.setVisibility(View.GONE);
                    customerLayout.setVisibility(View.GONE);
                    customerLayout1.setVisibility(View.GONE);
                    campUseLayout.setVisibility(View.VISIBLE);
                    hatchLayout.setVisibility(View.GONE);
                    sealLayout.setVisibility(View.GONE);
                    spare1.setVisibility(View.GONE);
                    bargingLayout.setVisibility(View.GONE);
                    operatorLayout.setVisibility(View.GONE);
                    motherHatchLayout.setVisibility(View.GONE);
                    break;

                //Barging
                case 7:
                    pileLayout.setVisibility(View.GONE);
                    vvbLayout.setVisibility(View.GONE);
                    driverLayout.setVisibility(View.GONE);
                    logpondLayout.setVisibility(View.VISIBLE);
                    vesselLayout.setVisibility(View.GONE);
                    containerLayout.setVisibility(View.GONE);
                    customerLayout.setVisibility(View.GONE);
                    customerLayout1.setVisibility(View.GONE);
                    campUseLayout.setVisibility(View.GONE);
                    hatchLayout.setVisibility(View.VISIBLE);
                    sealLayout.setVisibility(View.GONE);
                    spare1.setVisibility(View.GONE);
                    bargingLayout.setVisibility(View.VISIBLE);
                    operatorLayout.setVisibility(View.VISIBLE);
                    motherHatchLayout.setVisibility(View.GONE);
                    break;

                //Export STS
                case 8:
                    pileLayout.setVisibility(View.GONE);
                    vvbLayout.setVisibility(View.GONE);
                    driverLayout.setVisibility(View.GONE);
                    logpondLayout.setVisibility(View.GONE);
                    vesselLayout.setVisibility(View.VISIBLE);
                    containerLayout.setVisibility(View.GONE);
                    customerLayout.setVisibility(View.VISIBLE);
                    customerLayout1.setVisibility(View.GONE);
                    campUseLayout.setVisibility(View.GONE);
                    hatchLayout.setVisibility(View.VISIBLE);
                    sealLayout.setVisibility(View.GONE);
                    spare1.setVisibility(View.GONE);
                    bargingLayout.setVisibility(View.VISIBLE);
                    operatorLayout.setVisibility(View.VISIBLE);
                    motherHatchLayout.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENTRY_LIST_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if(data.hasExtra("totalEntries")){
                    totalLabelEntry = data.getIntExtra("totalEntries", 0);
                    subtitle.setText(getString(R.string.entries_pcs).replace("|COUNT|", String.valueOf(totalLabelEntry)));
                    //toolbar.setSubtitle(getString(R.string.entries_pcs).replace("|COUNT|", String.valueOf(totalLabelEntry)));
                }
                if(data.hasExtra("labelEntryArray") && data.getStringArrayExtra("labelEntryArray")!=null){
                    labelEntryArray = data.getStringArrayExtra("labelEntryArray");
                }
                if(data.hasExtra("action") && data.getStringExtra("action") != null){
                    String action = data.getStringExtra("action");
                    if(action.equalsIgnoreCase("saved")){
                        String fileJsonObjectString = data.getStringExtra("fileJsonObjectString");
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("action", "saved");
                        resultIntent.putExtra("fileJsonObjectString", fileJsonObjectString);
                        resultIntent.putExtra("position", position);
                        setResult(Activity.RESULT_OK, resultIntent);
                        isSave = true;
                        try {
                            this.fileJsonObject = new JSONObject(fileJsonObjectString);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        killActivity();
                    }
                }
                //ArrayList arrayList = data.getParcelableArrayListExtra("PLACE");


            }
        }
    }

    private void saveTempFile(){
        System.out.println("~~~~~~~~~~~~logpond out details~~~~~~SsaveTempFile ");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    detailsJsonObject.put("entryNo", entryNoEditText.getText().toString());
                    detailsJsonObject.put("date", CalendarUtil.changeDateTimeFormatByString("dd/MM/yyyy", "yyyy-MM-dd", dateEditText.getText().toString()));
                    detailsJsonObject.put("action", actionArray[actionSpinner.getSelectedItemPosition()]);
                    detailsJsonObject.put("vvbNo", vvbNoEditText.getText().toString());
                    detailsJsonObject.put("fromLogpond", logpondArray[fromLogpondSpinner.getSelectedItemPosition()]);
                    detailsJsonObject.put("toLogpond", logpondArray[toLogpondSpinner.getSelectedItemPosition()]);
                    detailsJsonObject.put("driverName", driverNameEditText.getText().toString());
                    detailsJsonObject.put("plateNo", plateNoEditText.getText().toString());
                    detailsJsonObject.put("vesselNo", vesselNoArray[vesselNoSpinner.getSelectedItemPosition()]);
                    detailsJsonObject.put("hatchNo", hatchNoArray[hatchNoSpinner.getSelectedItemPosition()]);
                    detailsJsonObject.put("containerNo", containerNoEditText.getText().toString());
                    detailsJsonObject.put("serialNo", serialNoEditText.getText().toString());
                    detailsJsonObject.put("customer", customerArray[customerSpinner.getSelectedItemPosition()]);
                    detailsJsonObject.put("sawmill", sawmillArray[sawmillSpinner.getSelectedItemPosition()]);
                    detailsJsonObject.put("campUse", campUseArray[campUseSpinner.getSelectedItemPosition()]);
                    detailsJsonObject.put("pile", pileEditText.getText().toString());
                    detailsJsonObject.put("bargeNo", bargingArray[bargingSpinner.getSelectedItemPosition()]);
                    detailsJsonObject.put("operator", operatorEditText.getText().toString());
                    detailsJsonObject.put("motherHatchNo", hatchNoArray[motherHatchSpinner.getSelectedItemPosition()]);

                    ArrayList labelEntryArrayList = new ArrayList();
                    if(labelEntryArray!=null) Collections.addAll(labelEntryArrayList, labelEntryArray);
                    StringBuffer sb = new StringBuffer();
                    sb.append(position + "\r\n");
                    if (detailsJsonObject.getString("action").equals("Trucking")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append(detailsJsonObject.getString("vvbNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("fromLogpond") + "\r\n");
                        sb.append(detailsJsonObject.getString("toLogpond") + "\r\n");
                        sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                        sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                    }
                    else if (detailsJsonObject.getString("action").equals("Local Sales")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append(detailsJsonObject.getString("vvbNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                        sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("customer") + "\r\n");
                    }
                    else if (detailsJsonObject.getString("action").equals("Export Vessel")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("vesselNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("hatchNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("customer") + "\r\n");
                    }
                    else if (detailsJsonObject.getString("action").equals("Export Container")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("containerNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("serialNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("customer") + "\r\n");
                    }
                    else if (detailsJsonObject.getString("action").equals("Peddling")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append(detailsJsonObject.getString("pile") + "\r\n");
                        sb.append(detailsJsonObject.getString("fromLogpond") + "\r\n");
                        sb.append(detailsJsonObject.getString("toLogpond") + "\r\n");
                        sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                        sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                    }
                    else if (detailsJsonObject.getString("action").equals("Sawmill")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append(detailsJsonObject.getString("vvbNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                        sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("sawmill") + "\r\n");
                    }
                    else if (detailsJsonObject.getString("action").equals("Camp Use")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append(detailsJsonObject.getString("vvbNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                        sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("campUse") + "\r\n");
                    }
                    else if (detailsJsonObject.getString("action").equals("Barging")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append(detailsJsonObject.getString("bargeNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("fromLogpond") + "\r\n");
                        sb.append(detailsJsonObject.getString("toLogpond") + "\r\n");
                        sb.append(detailsJsonObject.getString("operator") + "\r\n");
                        sb.append(detailsJsonObject.getString("hatchNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                    }
                    else if (detailsJsonObject.getString("action").equals("Export STS")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append(detailsJsonObject.getString("bargeNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("operator") + "\r\n");
                        sb.append(detailsJsonObject.getString("hatchNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("vesselNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("motherHatchNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("customer") + "\r\n");
                    }
                    sb.append(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "\r\n");
                    sb.append(labelEntryArrayList.size() + "\r\n");

                    for(int i=0;i<labelEntryArrayList.size();i++){
                        sb.append(labelEntryArrayList.get(i) + "\r\n");
                    }
                    FileManager.saveTempFile(activity, "LOGPOND_OUT.TXT", sb.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {

                }
            }
        }).start();

    }

    private void saveFile(){
        hideKeyboard(getCurrentFocus());
        if(isEditable){
            // Trucking
            if(actionSpinner.getSelectedItemPosition() == 0){
                if(fromLogpondSpinner.getSelectedItemPosition() == 0){
                    MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_select_from_logpond_message), Snackbar.LENGTH_LONG);
                    fromLogpondSpinner.requestFocus();
                    return;
                }else if(toLogpondSpinner.getSelectedItemPosition() == 0){
                    MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_select_to_logpond_message), Snackbar.LENGTH_LONG);
                    toLogpondSpinner.requestFocus();
                    return;
                }else if(toLogpondSpinner.getSelectedItemPosition() == fromLogpondSpinner.getSelectedItemPosition()){
                    MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.from_logpond_same_to_logpond_error), Snackbar.LENGTH_LONG);
                    toLogpondSpinner.requestFocus();
                    return;
                }

                // Peddling
            }else if (actionSpinner.getSelectedItemPosition() == 1){
                if (driverNameEditText.getText().toString().isEmpty()){
                    MySnackBar.showError(getCurrentFocus(), getString(R.string.please_fill_in_driver_name_message), Snackbar.LENGTH_LONG);
                    driverNameEditText.requestFocus();
                    return;
                }else if (plateNoEditText.getText().toString().isEmpty()){
                    MySnackBar.showError(getCurrentFocus(), getString(R.string.please_fill_in_plate_no_message), Snackbar.LENGTH_LONG);
                    plateNoEditText.requestFocus();
                    return;
                }else if(fromLogpondSpinner.getSelectedItemPosition() == 0){
                    MySnackBar.showError(getCurrentFocus(), getString(R.string.please_select_from_logpond_message), Snackbar.LENGTH_LONG);
                    fromLogpondSpinner.requestFocus();
                    return;
                }else if(toLogpondSpinner.getSelectedItemPosition() == 0){
                    MySnackBar.showError(getCurrentFocus(), getString(R.string.please_select_to_logpond_message), Snackbar.LENGTH_LONG);
                    toLogpondSpinner.requestFocus();
                    return;
                }else if(toLogpondSpinner.getSelectedItemPosition() == fromLogpondSpinner.getSelectedItemPosition()){
                    MySnackBar.showError(getCurrentFocus(), getString(R.string.from_logpond_same_to_logpond_error), Snackbar.LENGTH_LONG);
                    toLogpondSpinner.requestFocus();
                    return;
                }

                // Local Sales
            }else if(actionSpinner.getSelectedItemPosition() == 2){

                // Export Vessel
            }else if(actionSpinner.getSelectedItemPosition() == 3){
                if(vesselNoSpinner.getSelectedItemPosition() == 0){
                    MySnackBar.showError(getCurrentFocus(), getString(R.string.please_select_vessel_no_message), Snackbar.LENGTH_LONG);
                    vesselNoSpinner.requestFocus();
                    return;
                }else if(hatchNoSpinner.getSelectedItemPosition() == 0){
                    MySnackBar.showError(getCurrentFocus(), getString(R.string.please_select_hatch_no_message), Snackbar.LENGTH_LONG);
                    hatchNoSpinner.requestFocus();
                    return;
                }

                // Export Container
            }else if (actionSpinner.getSelectedItemPosition() == 4){
                if(containerNoEditText.getText().length() == 0){
                    MySnackBar.showError(getCurrentFocus(), getString(R.string.please_fill_in_container_no_message), Snackbar.LENGTH_LONG);
                    containerNoEditText.requestFocus();
                    return;
                }

                //Sawmill
                else if (actionSpinner.getSelectedItemPosition() == 5){
                    if(sawmillSpinner.getSelectedItemPosition() == 0){
                        MySnackBar.showError(customerLayout1, "Please Select Sawmill", Snackbar.LENGTH_LONG);
                        sawmillSpinner.requestFocus();
                        return;
                    }
                }
                //Camp Use
                else if (actionSpinner.getSelectedItemPosition() == 6){
                    if(campUseSpinner.getSelectedItemPosition() == 0){
                        MySnackBar.showError(findViewById(android.R.id.content), "Please Select Camp Use", Snackbar.LENGTH_LONG);
                        campUseSpinner.requestFocus();
                        return;
                    }
                }

                //Peddling
            }
        }
        final ArrayList labelEntryArrayList = new ArrayList();
        if(labelEntryArray!=null) Collections.addAll(labelEntryArrayList, labelEntryArray);

        if(labelEntryArrayList.size()==0){
            MySnackBar.showError(getCurrentFocus(), getString(R.string.please_add_at_least_one_label_before_save), Snackbar.LENGTH_LONG);
        }else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.show();
                            }
                        });
                        detailsJsonObject.put("entryNo", entryNoEditText.getText().toString());
                        detailsJsonObject.put("date", CalendarUtil.changeDateTimeFormatByString("dd/MM/yyyy", "yyyy-MM-dd", dateEditText.getText().toString()));
                        detailsJsonObject.put("action", actionArray[actionSpinner.getSelectedItemPosition()]);
                        detailsJsonObject.put("vvbNo", vvbNoEditText.getText().toString());
                        detailsJsonObject.put("fromLogpond", logpondArray[fromLogpondSpinner.getSelectedItemPosition()]);
                        detailsJsonObject.put("toLogpond", logpondArray[toLogpondSpinner.getSelectedItemPosition()]);
                        detailsJsonObject.put("driverName", driverNameEditText.getText().toString());
                        detailsJsonObject.put("plateNo", plateNoEditText.getText().toString());
                        detailsJsonObject.put("vesselNo", vesselNoArray[vesselNoSpinner.getSelectedItemPosition()]);
                        detailsJsonObject.put("hatchNo", hatchNoArray[hatchNoSpinner.getSelectedItemPosition()]);
                        detailsJsonObject.put("containerNo", containerNoEditText.getText().toString());
                        detailsJsonObject.put("serialNo", serialNoEditText.getText().toString());
                        detailsJsonObject.put("customer", customerArray[customerSpinner.getSelectedItemPosition()]);
                        detailsJsonObject.put("pile", pileEditText.getText().toString());
                        detailsJsonObject.put("bargeNo", bargingArray[bargingSpinner.getSelectedItemPosition()]);
                        detailsJsonObject.put("operator", operatorEditText.getText().toString());
                        detailsJsonObject.put("motherHatchNo", hatchNoArray[motherHatchSpinner.getSelectedItemPosition()]);

                        StringBuffer sb = new StringBuffer();
                        if (detailsJsonObject.getString("action").equals("Trucking")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append(detailsJsonObject.getString("vvbNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("fromLogpond") + "\r\n");
                            sb.append(detailsJsonObject.getString("toLogpond") + "\r\n");
                            sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                            sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                        }
                        else if (detailsJsonObject.getString("action").equals("Local Sales")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append(detailsJsonObject.getString("vvbNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                            sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("customer") + "\r\n");
                        }
                        else if (detailsJsonObject.getString("action").equals("Export Vessel")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("vesselNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("hatchNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("customer") + "\r\n");
                        }
                        else if (detailsJsonObject.getString("action").equals("Export Container")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("containerNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("serialNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("customer") + "\r\n");
                        }
                        else if (detailsJsonObject.getString("action").equals("Peddling")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append(detailsJsonObject.getString("pile") + "\r\n");
                            sb.append(detailsJsonObject.getString("fromLogpond") + "\r\n");
                            sb.append(detailsJsonObject.getString("toLogpond") + "\r\n");
                            sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                            sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                        }
                        else if (detailsJsonObject.getString("action").equals("Sawmill")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                            sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("sawmill") + "\r\n");
                        }
                        else if (detailsJsonObject.getString("action").equals("Camp Use")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                            sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("campUse") + "\r\n");
                        }
                        else if (detailsJsonObject.getString("action").equals("Barging")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append(detailsJsonObject.getString("bargeNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("fromLogpond") + "\r\n");
                            sb.append(detailsJsonObject.getString("toLogpond") + "\r\n");
                            sb.append(detailsJsonObject.getString("operator") + "\r\n");
                            sb.append(detailsJsonObject.getString("hatchNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                        }
                        else if (detailsJsonObject.getString("action").equals("Export STS")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append(detailsJsonObject.getString("bargeNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("operator") + "\r\n");
                            sb.append(detailsJsonObject.getString("hatchNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("vesselNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("motherHatchNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("customer") + "\r\n");
                        }
                        sb.append(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "\r\n");
                        sb.append(labelEntryArrayList.size() + "\r\n");

                        for(int i=0;i<labelEntryArrayList.size();i++){
                            sb.append(labelEntryArrayList.get(i) + "\r\n");
                        }

                        String fileName = SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "_" + detailsJsonObject.getString("entryNo") + ".TXT";
                        fileJsonObject = FileManager.saveFile(activity, "export_logpond_out", fileName.toUpperCase(), sb.toString());
                        isSave = true;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                        killActivity();
                    } catch (JSONException e) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                        e.printStackTrace();
                    }finally {

                    }
                }
            }).start();
        }
    }
}
