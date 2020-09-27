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

import com.infocomm.logpond_v2.R;
import com.infocomm.logpond_v2.storage.SharedPreferencesStorage;
import com.infocomm.logpond_v2.util.CalendarUtil;
import com.infocomm.logpond_v2.util.FileManager;
import com.infocomm.logpond_v2.view.CustomAlertDialog;
import com.infocomm.logpond_v2.view.MySnackBar;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class LogpondInDetailsActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, View.OnFocusChangeListener, AdapterView.OnItemSelectedListener {

    private final int ENTRY_LIST_REQUEST_CODE = 99;
    private LogpondInDetailsActivity activity;
    private EditText entryNoEditText, dateEditText, vvbNoEditText, driverNameEditText, plateNoEditText;
    private SearchableSpinner logpondSpinner;
    private String[] dataArray, logpondArray, labelEntryArray;
    private View nextLayout;
    private JSONObject jsonObject, detailsJsonObject;
    private boolean isEditable;
    private int position;
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
        setContentView(R.layout.activity_logpond_in_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setLogo(R.drawable.forest);
        isSave = false;
        fileJsonObject = new JSONObject();

        subtitle = findViewById(R.id.logpond_in_detail_subtitle);
        entryno = findViewById(R.id.logpond_in_detail_toolbar_entry_no);

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

        nextLayout = findViewById(R.id.next_layout);
        nextLayout.setOnClickListener(this);

        jsonObject = new JSONObject();
        detailsJsonObject = new JSONObject();

        logpondArray = new String[]{""};
        String[] lodpongDb = FileManager.retrieveTextContentAsStringArray(this, FileManager.getPrivateDirPath(this) + "/android_db/" + SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "/logpond.txt");
        if(lodpongDb!=null) {
            logpondArray = new String[lodpongDb.length + 1];
            logpondArray[0] = "";
            System.arraycopy(lodpongDb, 0, logpondArray, 1, lodpongDb.length);
        }
        ArrayAdapter<String> logpondAdapter = new ArrayAdapter<String>(this, R.layout.spinner, logpondArray);
        logpondAdapter.setDropDownViewResource(R.layout.spinner);
        logpondSpinner = (SearchableSpinner) findViewById(R.id.logpond_spinner);
        logpondSpinner.setAdapter(logpondAdapter);
        logpondSpinner.setOnItemSelectedListener(this);
        logpondSpinner.setEnabled(isEditable);
        logpondSpinner.setOnItemSelectedListener(this);

        if(getIntent().hasExtra("data") && getIntent().getStringExtra("data")!=null){
            position = getIntent().getIntExtra("position", -1);
            try {
                jsonObject = new JSONObject(getIntent().getStringExtra("data"));
                if(!jsonObject.isNull("filePath"))
                    dataArray = FileManager.retrieveTextContentAsStringArray(this, jsonObject.getString("filePath"));

                // 0 - Entry No
                // 1 - Entry Date
                // 2 - Logpond
                // 3 - VVB
                // 4 - Driver
                // 5 - Plate No
                // 6 - User
                // 7 - Total

                if(dataArray!=null && dataArray.length>6){
                    entryNoEditText.setText(dataArray[0]);
                    entryno.setText("Entry No: " + dataArray[0]);
                    dateEditText.setText(CalendarUtil.changeDateTimeFormatByString("yyyy-MM-dd", "dd/MM/yyyy", dataArray[1]));
                    vvbNoEditText.setText(dataArray[3]);
                    driverNameEditText.setText(dataArray[4]);
                    plateNoEditText.setText(dataArray[5]);

                    if(logpondArray!=null){
                        for(int i=0;i<logpondArray.length;i++){
                            if(logpondArray[i].equalsIgnoreCase(dataArray[2])){
                                logpondSpinner.setSelection(i);
                            }
                        }
                    }

                    totalLabelEntry = Integer.parseInt(dataArray[7]);
                    if(totalLabelEntry>0){
                        labelEntryArray = new String[totalLabelEntry];
                        for(int i=0;i<totalLabelEntry;i++){
                            labelEntryArray[i] = dataArray[8 + i];
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

           // Resume
        }else if(FileManager.retrieveTextContentAsStringArrayFromTempFile(activity, "LOGPOND_IN.TXT") != null) {
            Object[] resumeObject = FileManager.retrieveTextContentAsStringArrayFromTempFile(activity, "LOGPOND_IN.TXT");
            position = (int) resumeObject[0];
            dataArray = (String[]) resumeObject[1];

            // 0 - Entry No
            // 1 - Entry Date
            // 2 - Logpond
            // 3 - VVB
            // 4 - Driver
            // 5 - Plate No
            // 6 - User
            // 7 - Total

            if(dataArray!=null && dataArray.length>6){
                entryNoEditText.setText(dataArray[0]);
                entryno.setText("Entry No: " + dataArray[0]);
                dateEditText.setText(CalendarUtil.changeDateTimeFormatByString("yyyy-MM-dd", "dd/MM/yyyy", dataArray[1]));
                vvbNoEditText.setText(dataArray[3]);
                driverNameEditText.setText(dataArray[4]);
                plateNoEditText.setText(dataArray[5]);

                if(logpondArray!=null){
                    for(int i=0;i<logpondArray.length;i++){
                        if(logpondArray[i].equalsIgnoreCase(dataArray[2])){
                            logpondSpinner.setSelection(i);
                        }
                    }
                }

                totalLabelEntry = Integer.parseInt(dataArray[7]);
                if(totalLabelEntry>0){
                    labelEntryArray = new String[totalLabelEntry];
                    for(int i=0;i<totalLabelEntry;i++){
                        labelEntryArray[i] = dataArray[8 + i];
                    }
                }
            }
            // New
        }else{
            position = -1;
            entryNoEditText.setText(FileManager.getLatestLogponInEntryNoInPrivate(this));
            entryno.setText("Entry No: " + FileManager.getLatestLogponInEntryNoInPrivate(this));
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
        FileManager.removeTempFile(getApplicationContext(), "LOGPOND_IN.TXT");
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
            saveAlertDialogs.MyDialogWithYesNo(LogpondInDetailsActivity.this,getString(R.string.app_name), getString(R.string.do_you_want_to_save),save_listener,kill_listener);
//            new AlertDialog.Builder(LogpondInDetailsActivity.this)
//                    .setTitle(getString(R.string.app_name))
//                    .setMessage(getString(R.string.do_you_want_to_save))
//                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
//
//                        public void onClick(DialogInterface dialog, int whichButton) {
//                            saveFile();
//                        }
//                    })
//                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
//
//                        public void onClick(DialogInterface dialog, int whichButton) {
//                            killActivity();
//                        }
//                    }).show();
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
            int year = Integer.parseInt(dateProp[1]);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.MyDatePickerDialogTheme, this, year, month, day);
            //datePickerDialog.getDatePicker().setMaxDate(maxCalendar.getTimeInMillis());
            //datePickerDialog.getDatePicker().setMinDate(minCalendar.getTimeInMillis());
            datePickerDialog.show();
        }else if(view == nextLayout){
            if(logpondSpinner.getSelectedItemPosition()==0 && isEditable){
                MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_select_logpond_message), Snackbar.LENGTH_LONG);
                logpondSpinner.requestFocus();
            }else{
                try {
                    detailsJsonObject.put("entryNo", entryNoEditText.getText().toString());
                    detailsJsonObject.put("date", CalendarUtil.changeDateTimeFormatByString("dd/MM/yyyy", "yyyy-MM-dd", dateEditText.getText().toString()));
                    detailsJsonObject.put("logpond", logpondArray[logpondSpinner.getSelectedItemPosition()]);
                    detailsJsonObject.put("vvbNo", vvbNoEditText.getText().toString());
                    detailsJsonObject.put("driverName", driverNameEditText.getText().toString());
                    detailsJsonObject.put("plateNo", plateNoEditText.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                Intent entryListIntent = new Intent(this, LogpondInEntryListActivity.class);
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

/*
            Intent entryListIntent = new Intent(this, LogpondOutEntryListActivity.class);
            startActivity(entryListIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            */
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
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        saveTempFile();
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
        System.out.println("~~~~~~~~~~~~logpond in details~~~~~~SsaveTempFile ");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    detailsJsonObject.put("entryNo", entryNoEditText.getText().toString());
                    detailsJsonObject.put("date", CalendarUtil.changeDateTimeFormatByString("dd/MM/yyyy", "yyyy-MM-dd", dateEditText.getText().toString()));
                    detailsJsonObject.put("logpond", logpondArray[logpondSpinner.getSelectedItemPosition()]);
                    detailsJsonObject.put("vvbNo", vvbNoEditText.getText().toString());
                    detailsJsonObject.put("driverName", driverNameEditText.getText().toString());
                    detailsJsonObject.put("plateNo", plateNoEditText.getText().toString());

                    ArrayList labelEntryArrayList = new ArrayList();
                    if(labelEntryArray!=null) Collections.addAll(labelEntryArrayList, labelEntryArray);
                    StringBuffer sb = new StringBuffer();
                    sb.append(position + "\r\n");
                    sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                    sb.append(detailsJsonObject.getString("date") + "\r\n");
                    sb.append(detailsJsonObject.getString("logpond") + "\r\n");
                    sb.append(detailsJsonObject.getString("vvbNo") + "\r\n");
                    sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                    sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                    sb.append(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "\r\n");
                    sb.append(labelEntryArrayList.size() + "\r\n");

                    for(int i=0;i<labelEntryArrayList.size();i++){
                        sb.append(labelEntryArrayList.get(i) + "\r\n");
                    }
                    FileManager.saveTempFile(activity, "LOGPOND_IN.TXT", sb.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {

                }
            }
        }).start();

    }

    private void saveFile(){
        hideKeyboard(getCurrentFocus());
        if(logpondSpinner.getSelectedItemPosition()==0 && isEditable){
            MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_select_logpond_message), Snackbar.LENGTH_LONG);
            logpondSpinner.requestFocus();
        }else{
            final ArrayList labelEntryArrayList = new ArrayList();
            if(labelEntryArray!=null) Collections.addAll(labelEntryArrayList, labelEntryArray);

            if(labelEntryArrayList.size()==0){
                MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.please_add_at_least_one_label_before_save), Snackbar.LENGTH_LONG);
            }else{
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            detailsJsonObject.put("entryNo", entryNoEditText.getText().toString());
                            detailsJsonObject.put("date", CalendarUtil.changeDateTimeFormatByString("dd/MM/yyyy", "yyyy-MM-dd", dateEditText.getText().toString()));
                            detailsJsonObject.put("logpond", logpondArray[logpondSpinner.getSelectedItemPosition()]);
                            detailsJsonObject.put("vvbNo", vvbNoEditText.getText().toString());
                            detailsJsonObject.put("driverName", driverNameEditText.getText().toString());
                            detailsJsonObject.put("plateNo", plateNoEditText.getText().toString());

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.show();
                                }
                            });
                            StringBuffer sb = new StringBuffer();
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("logpond") + "\r\n");
                            sb.append(detailsJsonObject.getString("vvbNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                            sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                            sb.append(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "\r\n");
                            sb.append(labelEntryArrayList.size() + "\r\n");

                            for(int i=0;i<labelEntryArrayList.size();i++){
                                sb.append(labelEntryArrayList.get(i) + "\r\n");
                            }

                            String fileName = SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "_" + detailsJsonObject.getString("entryNo") + ".TXT";
                            fileJsonObject = FileManager.saveFile(activity, "export_logpond_in", fileName.toUpperCase(), sb.toString());
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                }
                            });
                            isSave = true;
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
}
