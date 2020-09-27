package com.infocomm.logpond_v2.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.infocomm.logpond_v2.R;
import com.infocomm.logpond_v2.receiver.AutoSyncReceiver;
import com.infocomm.logpond_v2.storage.SharedPreferencesStorage;
import com.infocomm.logpond_v2.util.FileManager;
import com.infocomm.logpond_v2.view.TTSManager;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

//import com.mydreamsoft.logpond_forest.receiver.AutoSyncReceiver;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private final int AUTO_SYNC_REQUEST_CODE = 5555;
    private final int PERMISSION_REQUEST_CODE = 909;
    private SettingsActivity activity;
    private EditText usernameEditText, serverHostEditText, ftpHostEditText, ftpPortEditText, ftpUsernameEditText, ftpPasswordEditText;
    private View saveLayout;
    private Spinner automaticSyncSpinner;
    private String[] automaticSyncAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        activity = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.forest);
        setTitle(getResources().getString(R.string.setting));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usernameEditText = (EditText) findViewById(R.id.username);
        usernameEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        usernameEditText.setText(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME));
        serverHostEditText = (EditText) findViewById(R.id.server_host);
        serverHostEditText.setText(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.SERVER_HOST));
        ftpHostEditText = (EditText) findViewById(R.id.ftp_host);
        ftpHostEditText.setText(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.FTP_HOST));
        ftpPortEditText = (EditText) findViewById(R.id.ftp_port);
        ftpPortEditText.setText(String.valueOf(SharedPreferencesStorage.getIntValue(activity, SharedPreferencesStorage.FTP_PORT)));
        ftpUsernameEditText = (EditText) findViewById(R.id.ftp_username);
        ftpUsernameEditText.setText(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.FTP_USERNAME));
        ftpPasswordEditText = (EditText) findViewById(R.id.ftp_password);
        ftpPasswordEditText.setText(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.FTP_PASSWORD));

        automaticSyncAction = getResources().getStringArray(R.array.automatic_sync_action);
        ArrayAdapter<String> automaticSyncAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, automaticSyncAction);
        automaticSyncAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        automaticSyncSpinner = (Spinner) findViewById(R.id.automatic_sync_spinner);
        automaticSyncSpinner.setAdapter(automaticSyncAdapter);
        automaticSyncSpinner.setOnItemSelectedListener(this);
        automaticSyncSpinner.setSelection(SharedPreferencesStorage.getIntValue(this,SharedPreferencesStorage.AUTO_SYNC_ACTION ));
        saveLayout = findViewById(R.id.save_layout);
        saveLayout.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                killActivity();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        killActivity();
    }

    private void killActivity() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onResume(){
        super.onResume();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ) {

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~onRequestPermissionsResult");
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                boolean isAllGranted = true;
                // If request is cancelled, the result arrays are empty.
                for(int i=0;i<grantResults.length;i++){
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~permissions granted = " + permissions[i]);
                    } else {
                        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~permissions denied = " + permissions[i]);
                        isAllGranted = false;
                    }
                }

                if(!isAllGranted){

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_REQUEST_CODE);

                    }
                }
                return;
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view == saveLayout){
            hideKeyboard();
            if(usernameEditText.getText().toString().trim().length()==0){
                usernameEditText.setError(getString(R.string.field_required_error));
                usernameEditText.requestFocus();
                return;
            }else if(usernameEditText.getText().toString().trim().contains(" ")){
                usernameEditText.setError(getString(R.string.space_not_allow_error));
                usernameEditText.requestFocus();
                return;
            }else if(serverHostEditText.getText().toString().trim().length()==0){
                serverHostEditText.setError(getString(R.string.field_required_error));
                serverHostEditText.requestFocus();
                return;
            }else if(ftpHostEditText.getText().toString().trim().length()==0){
                ftpHostEditText.setError(getString(R.string.field_required_error));
                ftpHostEditText.requestFocus();
                return;
            }else if(ftpPortEditText.getText().toString().trim().length()==0){
                ftpPortEditText.setError(getString(R.string.field_required_error));
                ftpPortEditText.requestFocus();
                return;
            }
            JSONObject jsonObject = new JSONObject();
            FileOutputStream fileOutputStream = null;
            try {
                String serverHost = serverHostEditText.getText().toString().trim();
                if(!serverHost.startsWith("http://")) serverHost = "http://" + serverHost;
                jsonObject.put("username", usernameEditText.getText().toString());
                jsonObject.put("serverHost", serverHost);
                jsonObject.put("ftpHost", ftpHostEditText.getText().toString());
                jsonObject.put("ftpPort", ftpPortEditText.getText().toString());
                jsonObject.put("ftpUsername", usernameEditText.getText().toString().toLowerCase());
                jsonObject.put("ftpPassword", usernameEditText.getText().toString().toLowerCase());
                jsonObject.put("autoSync", automaticSyncSpinner.getSelectedItemPosition());

                SharedPreferencesStorage.setIntValue(this, SharedPreferencesStorage.AUTO_SYNC_ACTION, automaticSyncSpinner.getSelectedItemPosition());
                SharedPreferencesStorage.setBooleanValue(this, SharedPreferencesStorage.IS_AUTO_SYNC_RUNNING, false);
                SharedPreferencesStorage.setBooleanValue(this, SharedPreferencesStorage.IS_AUTO_SYNC_FINISHED, true);
                if(automaticSyncSpinner.getSelectedItemPosition()>0){
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent autoSyncReceiver = new Intent(this, AutoSyncReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), AUTO_SYNC_REQUEST_CODE, autoSyncReceiver, 0);
                    // Stop any alarm manager first
                    alarmManager.cancel(pendingIntent);
                    // Replace the settings with yours here...
                    long intervalHourInMilliseconds = automaticSyncSpinner.getSelectedItemPosition() * 60 * 1000 * 60;
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis() + intervalHourInMilliseconds, intervalHourInMilliseconds, pendingIntent);
                }else{
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent autoSyncReceiver = new Intent(this, AutoSyncReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), AUTO_SYNC_REQUEST_CODE, autoSyncReceiver, 0);
                    alarmManager.cancel(pendingIntent);
                }

                SharedPreferencesStorage.setStringValue(activity, SharedPreferencesStorage.USERNAME, usernameEditText.getText().toString());
                SharedPreferencesStorage.setStringValue(activity, SharedPreferencesStorage.SERVER_HOST, serverHost);
                SharedPreferencesStorage.setStringValue(activity, SharedPreferencesStorage.FTP_HOST, ftpHostEditText.getText().toString());
                SharedPreferencesStorage.setIntValue(activity, SharedPreferencesStorage.FTP_PORT, Integer.parseInt(ftpPortEditText.getText().toString()));
                SharedPreferencesStorage.setStringValue(activity, SharedPreferencesStorage.FTP_USERNAME, usernameEditText.getText().toString().toLowerCase());
                SharedPreferencesStorage.setStringValue(activity, SharedPreferencesStorage.FTP_PASSWORD, usernameEditText.getText().toString().toLowerCase());

                File privateLogpondOutDir = new File(FileManager.getPrivateDirPath(activity) );
                if(!privateLogpondOutDir.exists()) privateLogpondOutDir.mkdirs();
                File configFilePath = new File(FileManager.getPrivateDirPath(activity) + "/config.txt");
                if(!configFilePath.exists()) configFilePath.createNewFile();

                fileOutputStream = new FileOutputStream(configFilePath);
                fileOutputStream.write(jsonObject.toString().getBytes("UTF-8"));

                Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                Toast.makeText(activity, getString(R.string.save_successfully), Toast.LENGTH_SHORT).show();
                TTSManager.sayText(SettingsActivity.this, getString(R.string.save_successfully));
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                if(fileOutputStream!=null){
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
