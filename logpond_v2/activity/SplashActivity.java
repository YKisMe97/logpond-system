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
import android.util.Log;

import com.infocomm.logpond_v2.R;
import com.infocomm.logpond_v2.receiver.AutoSyncReceiver;
import com.infocomm.logpond_v2.storage.SharedPreferencesStorage;
import com.infocomm.logpond_v2.util.FileManager;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SplashActivity extends AppCompatActivity {

    private final int AUTO_SYNC_REQUEST_CODE = 5555;
    private final int PERMISSION_REQUEST_CODE = 909;
    private boolean isNewInstalled;
    private SplashActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        activity = this;
        isNewInstalled = SharedPreferencesStorage.getBooleanValue(this, SharedPreferencesStorage.IS_NEW_INSTALLED);
    }

    private void startActivity(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                File privateDir = new File(FileManager.getPrivateDirPath(activity) );
                if(!privateDir.exists()) privateDir.mkdirs();
                File configFilePath = new File(FileManager.getPrivateDirPath(activity) + "/config.txt");
                if(configFilePath.exists()){
                    Log.e("configFilePath", configFilePath.getAbsolutePath());

                    FileInputStream fileInputStream = null;
                    ByteArrayOutputStream baos = null;
                    int readInt = -1;
                    byte[] bytes = new byte[2048];
                    JSONObject jsonObject = new JSONObject();
                    try{
                        baos = new ByteArrayOutputStream();
                        fileInputStream = new FileInputStream(configFilePath);
                        while((readInt = fileInputStream.read(bytes)) > -1){
                            baos.write(bytes, 0, readInt);
                        }
                        jsonObject = new JSONObject(new String(baos.toByteArray(), "UTF-8"));

                        SharedPreferencesStorage.setStringValue(activity, SharedPreferencesStorage.USERNAME, jsonObject.getString("username"));
                        SharedPreferencesStorage.setStringValue(activity, SharedPreferencesStorage.SERVER_HOST, jsonObject.getString("serverHost"));
                        SharedPreferencesStorage.setStringValue(activity, SharedPreferencesStorage.FTP_HOST, jsonObject.getString("ftpHost"));
                        SharedPreferencesStorage.setIntValue(activity, SharedPreferencesStorage.FTP_PORT, jsonObject.getInt("ftpPort"));
                        SharedPreferencesStorage.setStringValue(activity, SharedPreferencesStorage.FTP_USERNAME, jsonObject.getString("ftpUsername"));
                        SharedPreferencesStorage.setStringValue(activity, SharedPreferencesStorage.FTP_PASSWORD, jsonObject.getString("ftpPassword"));
                        if(!jsonObject.isNull("autoSync")){
                            try{
                                SharedPreferencesStorage.setIntValue(activity, SharedPreferencesStorage.AUTO_SYNC_ACTION, jsonObject.getInt("autoSync"));
                            }catch (Exception e){

                            }
                        }else{
                            SharedPreferencesStorage.setIntValue(activity, SharedPreferencesStorage.AUTO_SYNC_ACTION, 0);
                        }

                        if(isNewInstalled && SharedPreferencesStorage.getIntValue(activity, SharedPreferencesStorage.AUTO_SYNC_ACTION)>0){
                            SharedPreferencesStorage.setBooleanValue(activity, SharedPreferencesStorage.IS_AUTO_SYNC_RUNNING, false);
                            SharedPreferencesStorage.setBooleanValue(activity, SharedPreferencesStorage.IS_AUTO_SYNC_FINISHED, true);

                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            Intent autoSyncReceiver = new Intent(activity, AutoSyncReceiver.class);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), AUTO_SYNC_REQUEST_CODE, autoSyncReceiver, 0);
                            // Stop any alarm manager first
                            alarmManager.cancel(pendingIntent);
                            // Replace the settings with yours here...
                            long intervalHourInMilliseconds = SharedPreferencesStorage.getIntValue(activity, SharedPreferencesStorage.AUTO_SYNC_ACTION) * 60 * 1000 * 60;
                            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis() + intervalHourInMilliseconds, intervalHourInMilliseconds, pendingIntent);

                        }
                    }catch (final Exception e){
                        e.printStackTrace();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //MySnackBar.showError(activity.getCurrentFocus(), e.getMessage(), Snackbar.LENGTH_LONG);
                            }
                        });

                    }finally{
                        File temp_file = new File(FileManager.getPrivateDirPath(getApplicationContext())+ File.separator + "temp_folder"+ File.separator + getApplicationContext().getString(R.string.logpond_out_temp_file_name));
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        //Date lastModifiedDate = new Date(temp_file.lastModified());
                        String lastModifiedDate = dateFormat.format(temp_file.lastModified());
                        try {
                            Date d = dateFormat.parse(lastModifiedDate);
                            Date jan11 = dateFormat.parse("11/01/2020");
                            if (temp_file.exists() && d.before(jan11)){
                                temp_file.delete();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if(baos!=null) try {
                            baos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(fileInputStream!=null){
                            try {
                                fileInputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }


                }else{

                    JSONObject jsonObject = new JSONObject();
                    FileOutputStream fileOutputStream = null;
                    try {
                        String username = "DEMO";
                        String serverHost = "http://palmeratimber.homelinux.com:8830/Lms";  //default port: 8830, infocomm
                        String ftpHost = "palmeratimber.homelinux.com";
                        String ftpPort = "9009";
                        String ftpUsername = "demo";
                        String ftpPassword = "demo";
                        jsonObject.put("username", username);
                        jsonObject.put("serverHost", serverHost);
                        jsonObject.put("ftpHost" ,ftpHost);
                        jsonObject.put("ftpPort", ftpPort);
                        jsonObject.put("ftpUsername", ftpUsername);
                        jsonObject.put("ftpPassword", ftpPassword);
                        jsonObject.put("autoSync", 0);

                        SharedPreferencesStorage.setStringValue(activity, SharedPreferencesStorage.USERNAME, username);
                        SharedPreferencesStorage.setStringValue(activity, SharedPreferencesStorage.SERVER_HOST, serverHost);
                        SharedPreferencesStorage.setStringValue(activity, SharedPreferencesStorage.FTP_HOST, ftpHost);
                        SharedPreferencesStorage.setIntValue(activity, SharedPreferencesStorage.FTP_PORT, Integer.parseInt(ftpPort));
                        SharedPreferencesStorage.setStringValue(activity, SharedPreferencesStorage.FTP_USERNAME,ftpUsername);
                        SharedPreferencesStorage.setStringValue(activity, SharedPreferencesStorage.FTP_PASSWORD, ftpPassword);
                        SharedPreferencesStorage.setIntValue(activity, SharedPreferencesStorage.AUTO_SYNC_ACTION, 0);

                        if(!configFilePath.exists()) configFilePath.createNewFile();

                        fileOutputStream = new FileOutputStream(configFilePath);
                        fileOutputStream.write(jsonObject.toString().getBytes("UTF-8"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }

                SharedPreferencesStorage.setBooleanValue(activity, SharedPreferencesStorage.IS_NEW_INSTALLED, false);

                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();

            }
        }).start();
    }

    @Override
    protected void onResume(){
        super.onResume();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ) {
            startActivity();
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
                }else{
                    startActivity();
                }
                return;
            }
        }
    }
}
