package com.infocomm.logpond_v2.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.infocomm.logpond_v2.R;
import com.infocomm.logpond_v2.io.InternetConnection;
import com.infocomm.logpond_v2.popupwindow.AboutPopUp;
import com.infocomm.logpond_v2.popupwindow.SettingDialog;
import com.infocomm.logpond_v2.storage.SharedPreferencesStorage;
import com.infocomm.logpond_v2.storage.SqlDatabase;
import com.infocomm.logpond_v2.util.FTPManager;
import com.infocomm.logpond_v2.util.FileManager;
import com.infocomm.logpond_v2.util.PhoneManager;
import com.infocomm.logpond_v2.view.CustomAlertDialog;
import com.infocomm.logpond_v2.view.CustomProgress;
import com.infocomm.logpond_v2.view.CustomUploadProgressDialog;
import com.infocomm.logpond_v2.view.CustomWarningAlertDialog;
import com.infocomm.logpond_v2.view.MySnackBar;
import com.infocomm.logpond_v2.view.MyToast;
import com.infocomm.logpond_v2.view.TTSManager;


import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button logpondInBtn,logpondOutBtn,logpondPileBtn, buyerGradingBtn, searchBtn, sendBtn;
    private ProgressDialog progressDialog;
    private final int PERMISSION_REQUEST_CODE = 909;
    private static MainActivity activity;
    private ProgressDialog autoSyncProgressDialog;
    private AsyncTask uploadTask;
    private ProgressDialog uploadProgressDialog;
    private BottomNavigationView bottomNavigationView;
    private CustomProgress customProgress;
    private CustomUploadProgressDialog customUploadProgressDialog;
    private int total_server_files, curr_import_data, total_import_length;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        setTitle(" ");
        toolbar.setLogo(R.drawable.forest);

        customProgress = CustomProgress.getInstance();
        customUploadProgressDialog = CustomUploadProgressDialog.getInstance();
        activity = this;
        username = SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME);

        progressDialog = new ProgressDialog(MainActivity.this, R.style.CustomDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setIndeterminate(true);

        autoSyncProgressDialog = new ProgressDialog(MainActivity.this, R.style.CustomDialogStyle);
        autoSyncProgressDialog.setCancelable(false);
        autoSyncProgressDialog.setTitle(getString(R.string.auto_sync_dialog_title));
        autoSyncProgressDialog.setMessage(getString(R.string.please_wait));
        autoSyncProgressDialog.setIndeterminate(true);
        autoSyncProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.stop), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferencesStorage.setBooleanValue(activity, SharedPreferencesStorage.IS_AUTO_SYNC_RUNNING, false);
                SharedPreferencesStorage.setBooleanValue(activity, SharedPreferencesStorage.IS_AUTO_SYNC_FINISHED, true);
                dialog.dismiss();
            }
        });

        /*uploadProgressDialog = new ProgressDialog(this, R.style.CustomDialogStyle);
        uploadProgressDialog.setTitle(getString(R.string.uploading));
        uploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        uploadProgressDialog.setProgress(0);
        uploadProgressDialog.setCancelable(false);
        uploadProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.stop), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(uploadTask!=null) uploadTask.cancel(true);
                dialog.dismiss();
            }
        });*/

        bottomNavigationView = findViewById(R.id.main_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId())
                {
                    case R.id.main_bottom_navigation_setting:
                        startActivity(new Intent(MainActivity.this, SettingDialog.class));
                        break;
                    case R.id.main_bottom_navigation_download:
//                        new DownloadRemoteDBTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                        break;

                        final CustomAlertDialog downloadAlertDialogs = new CustomAlertDialog();
                        View.OnClickListener download_listener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (PhoneManager.isConnectingToInternet(MainActivity.this)) {
                                    new DownloadRemoteDBTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    downloadAlertDialogs.cancel();

                                } else {
                                    //MySnackBar.showError(getCurrentFocus(), getString(R.string.ensure_internet_connection_available), Snackbar.LENGTH_LONG);
                                    MySnackBar.showError(findViewById(android.R.id.content), getString(R.string.ensure_internet_connection_available), Snackbar.LENGTH_LONG);
                                }
                            }
                        };

                        downloadAlertDialogs.MyDialogWithYesNo(MainActivity.this, getString(R.string.download_data), getString(R.string.download_data_now), download_listener,null);
                        break;
                    case R.id.main_bottom_navigation_about:
                        startActivity(new Intent(getApplicationContext(), AboutPopUp.class));
                        break;
                }
                return true;
            }
        });

        logpondInBtn = findViewById(R.id.main_logpond_in);
        logpondInBtnOnClick();
        logpondOutBtn = findViewById(R.id.main_logpond_out);
        logpondOutBtnOnClick();
        logpondPileBtn = findViewById(R.id.main_logpond_pile);
        logpondPileBtnOnClick();
        buyerGradingBtn = findViewById(R.id.main_buyer_grading);
        buyerGradingBtnOnClick();
        searchBtn = findViewById(R.id.searchBtn);
        searchBtnOnClick();
        sendBtn = findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBtn();
            }
        });
    }

//-----------------------------------------Functions-----------------------------------------

    private void searchBtn() {
        final CustomAlertDialog downloadAlertDialogs = new CustomAlertDialog();

        View.OnClickListener send_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PhoneManager.isConnectingToInternet(MainActivity.this)) {
                    upload();
                } else {
                    MySnackBar.showError(getCurrentFocus(), getString(R.string.ensure_internet_connection_available), Snackbar.LENGTH_LONG);
                }
                downloadAlertDialogs.cancel();
            }
        };

        downloadAlertDialogs.MyDialogWithYesNo(MainActivity.this, getString(R.string.upload_data), getString(R.string.are_you_sure_to_upload), send_listener, null);
    }

    private void searchBtnOnClick() {
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });
    }

    private void logpondInBtnOnClick() {
        logpondInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent logpondInIntent = new Intent(MainActivity.this, LogpondInActivity.class);
                startActivity(logpondInIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }


    private void logpondOutBtnOnClick() {
        logpondOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent logpondOutIntent = new Intent(MainActivity.this, LogpondOutActivity.class);
                startActivity(logpondOutIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private void logpondPileBtnOnClick() {
        logpondPileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent logpondPileIntent = new Intent(MainActivity.this, LogpondPileActivity.class);
                startActivity(logpondPileIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private void buyerGradingBtnOnClick() {
        buyerGradingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent buyerGradingIntent = new Intent(MainActivity.this, BuyerGradingActivity.class);
                startActivity(buyerGradingIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private void hideKeyboard(){
        View view = MainActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        hideKeyboard();
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ) {

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }

        // Check if table column count not match then deleta table
        if(!SqlDatabase.isColumnCountMatch(MainActivity.this)){
            SqlDatabase.emptyInventory(MainActivity.this);
            FileManager.removeAndroidDBFolderEmpty(MainActivity.this);
        }

        // Check DB files already downloaded
        if(FileManager.isAndroidDBFolderEmpty(MainActivity.this)){
            final CustomAlertDialog downloadAlertDialogs = new CustomAlertDialog();
            View.OnClickListener download_listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(PhoneManager.isConnectingToInternet(MainActivity.this)){
                        new DownloadRemoteDBTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else{
                        MySnackBar.showError(getCurrentFocus(), getString(R.string.ensure_internet_connection_available), Snackbar.LENGTH_LONG);
                    }
                    downloadAlertDialogs.cancel();
                }
            };
            downloadAlertDialogs.MyDialogWithYesNo(MainActivity.this, getString(R.string.app_name), getString(R.string.system_files_not_found_error)+"\n"+getString(R.string.download_system_files), download_listener,null);
        }

        // Check if Inventory DB Empty
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(SqlDatabase.isInventoryTableEmpty(MainActivity.this)){
                    final String[] inventoryArray = FileManager.retrieveTextContentAsStringArray(activity, FileManager.getPrivateDirPath(MainActivity.this) + "/android_db/" + SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "/inventory.txt");
                    if(inventoryArray!=null && inventoryArray.length>0){
                        SqlDatabase.emptyInventory(MainActivity.this);
                        try {
                            SqlDatabase.insertInventory(activity, inventoryArray);
                        } catch (final Exception e) {
                            e.printStackTrace();
                            try{
                                if(activity!=null)
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            CustomAlertDialog downloadAlertDialogs = new CustomAlertDialog();
                                            View.OnClickListener save_listener = new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    new DownloadRemoteDBTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                                }
                                            };

                                            downloadAlertDialogs.MyDialogWithYesNo(MainActivity.this,getString(R.string.download_failed), e.getMessage()+"\n"+getString(R.string.retry_question),save_listener,null);
                                        }
                                    });
                            }catch (Exception e1){

                            }

                        }
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while(SharedPreferencesStorage.getBooleanValue(activity, SharedPreferencesStorage.IS_AUTO_SYNC_RUNNING) && autoSyncProgressDialog!=null && activity!=null){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!autoSyncProgressDialog.isShowing()) autoSyncProgressDialog.show();
                            }
                        });

                        Thread.sleep(3000);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(activity!=null)
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(autoSyncProgressDialog.isShowing()) autoSyncProgressDialog.cancel();
                            }
                        });

                }

            }
        }).start();

    }

    private class DownloadRemoteDBTask extends AsyncTask<Object, Void, String> {

        private String errorMessage = "";
        private Object[] prop = null;

        protected void onPreExecute (){
            //progressDialog.show();
            customProgress.showProgress(MainActivity.this, getString(R.string.downloading_data), false);
            errorMessage = "";
        }

        protected String doInBackground(Object... prop) {
            FTPClient ftpClient = null;
            InternetConnection connection = null;
            try {
                //connection = new InternetConnection("/lmsServletAndroid.LmsSyncDownload?task=dumpdb&user=" + SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME), InternetConnection.HTTP);
                connection = new InternetConnection("/lmsServletAndroid.LmsSyncAndroidDownload?task=dumpdb&user=" + SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME), InternetConnection.HTTP);
                byte[] bytes = connection.sendResponse(activity, "");
                String data = new String(bytes, "UTF-8");
                System.out.println("~~~~~~~~~~~~~~~~~~data = " + data);
                if(data.trim().equalsIgnoreCase("true")){
                    ftpClient = new FTPClient();
                    ftpClient.setConnectTimeout(10 * 1000);
                    ftpClient.connect(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.FTP_HOST), SharedPreferencesStorage.getIntValue(activity, SharedPreferencesStorage.FTP_PORT));
                    boolean status = ftpClient.login(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.FTP_USERNAME), SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.FTP_PASSWORD));
                    System.out.println("isFTPConnected : " + String.valueOf(status));
                    if(status){
                        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {

                            // use local passive mode to pass firewall
                            ftpClient.enterLocalPassiveMode();

                            // directory on the server to be downloaded
                            String remoteDirPath = "/android_db/" + SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME);
                            FTPFile[] subFiles = ftpClient.listFiles(remoteDirPath);
                            total_server_files = subFiles.length;
                            int total_server_files_plus_one = total_server_files +1;
                            // directory where the files will be saved
                            String saveDirPath = FileManager.getPrivateDirPath(MainActivity.this);
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    customProgress.setMaximumSize(total_server_files);
                                }
                            });
                            int currFileNum = 1;
                            if(FTPManager.isDirectoryExist(ftpClient, remoteDirPath)){
                                for (int i =0;i<total_server_files_plus_one;i++){
                                    if (i<total_server_files){
                                        final String downloaded_file = subFiles[i].getName();
                                        customProgress.setCustomProgress(currFileNum);
                                        //publishProgress(i);
                                        final int currFileNum1 = currFileNum;
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                customProgress.setProgressStatus(currFileNum1,total_server_files, downloaded_file,getString(R.string.downloading_data));
                                            }
                                        });
                                        currFileNum++;
                                        Thread.sleep(getResources().getInteger(R.integer.thread_sleep));
                                    }
                                    else{
                                        final int currFileNum1 = currFileNum;
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                customProgress.setProgressStatus(currFileNum1,total_server_files, "",getString(R.string.download_complete));
                                            }
                                        });
                                        Thread.sleep(getResources().getInteger(R.integer.long_thread_sleep));
                                    }

                                }
                                FTPManager.downloadDirectory(MainActivity.this, ftpClient, remoteDirPath, "", saveDirPath);
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        customProgress.setProgressStatus(0,0,"",getString(R.string.synchronization));
                                    }
                                });

                                // Import inventory table into db
                                final String[] inventoryArray = FileManager.retrieveTextContentAsStringArray(activity, FileManager.getPrivateDirPath(activity) + "/android_db/" + SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "/inventory.txt");

                                if(inventoryArray!=null && inventoryArray.length>0) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            total_import_length = inventoryArray.length;
                                            customProgress.setMaximumSize(total_import_length);
                                        }
                                    });
                                    int inventory_length_plus_one = inventoryArray.length + 1;
                                    int currInventory = 1;
                                    SqlDatabase.emptyInventory(MainActivity.this);
                                    for (int i = 0; i < inventory_length_plus_one; i++) {
                                        if (i<inventoryArray.length){
                                            //final int finalI = i + 1;
                                            customProgress.setCustomProgress(currInventory);
                                            final int currInventory1 = currInventory;
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    curr_import_data = currInventory1;
                                                    customProgress.setProgressStatus(currInventory1, total_import_length, "",getString(R.string.import_database));

                                                }
                                            });
                                            currInventory++;
                                        }
                                        else{
                                            //curr_import_data = finalI;
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    customProgress.setProgressStatus(0, 0, "", getString(R.string.synchronize_complete));
                                                }
                                            });
                                            Thread.sleep(getResources().getInteger(R.integer.long_thread_sleep));
                                        }
                                    }
                                    SqlDatabase.insertInventory(activity, inventoryArray);
                                }

                            }else{
                                errorMessage =  getString(R.string.ftp_directory_not_exist_error);
                                customProgress.hideProgress();

                            }
                        }else{
                            errorMessage =  getString(R.string.ftp_connection_error);
                            customProgress.hideProgress();
                        }
                    }else{
                        errorMessage =  getString(R.string.ftp_connection_error);
                        customProgress.hideProgress();
                    }

                }else{
                    errorMessage =  getString(R.string.username_not_found);
                    customProgress.hideProgress();
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
            }finally{
                if(ftpClient!=null && ftpClient.isConnected()){
                    try {
                        ftpClient.disconnect();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if(connection!=null)
                    try {
                        connection.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
            return "Success";
        }
        /*
        protected void onProgressUpdate(Integer... progress) {

        }
        */

        protected void onCancelled (String result){
            Log.d("onCancelled", "");
        }

        protected void onPostExecute(String result) {
            try{
                // Error
                if(errorMessage.length()>0){
                    final CustomAlertDialog downloadAlertDialogs = new CustomAlertDialog();
                    View.OnClickListener save_listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            customProgress.hideProgress();
                            new DownloadRemoteDBTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            downloadAlertDialogs.cancel();

                        }

                    };

                    View.OnClickListener no_action = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                           customProgress.hideProgress();
                            downloadAlertDialogs.cancel();
                        }
                    };
                    downloadAlertDialogs.MyDialogWithYesNo(MainActivity.this, getString(R.string.download_failed), errorMessage+"\n"+getString(R.string.retry_question), save_listener,no_action);
                    // Success
                }else{
                    //MyToast.show(activity, getString(R.string.done), Toast.LENGTH_LONG);
                    final CustomWarningAlertDialog downloaded_successfully_alert_dialog = new CustomWarningAlertDialog();
                    View.OnClickListener ok_listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //do something
                            downloaded_successfully_alert_dialog.cancel();
                        }
                    };
                    downloaded_successfully_alert_dialog.MyDialogWithYesNo(MainActivity.this,getString(R.string.download_data),getString(R.string.downloaded_sound_text),ok_listener,null);

                    TTSManager.sayText(MainActivity.this, getString(R.string.downloaded_sound_text));
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                //progressDialog.dismiss();
                customProgress.hideProgress();
            }
        }
    }

    public static MainActivity getInstance(){
        return activity;
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

    private void upload(){
        if(uploadTask!=null) uploadTask.cancel(true);
        uploadTask = new UploadTask();
        uploadTask.execute();
    }

    private class UploadTask extends AsyncTask<Object, Integer, String> {

        private String errorMessage = "";
        private Object[] prop = null;
        private File[] files = null;
        private boolean isStopped = false;
        private int cout = 0;

        protected void onPreExecute (){
            errorMessage = "";
            cout = 0;
            progressDialog.show();
            /*uploadProgressDialog.setMessage(getString(R.string.please_wait));
            uploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            uploadProgressDialog.setIndeterminate(true);
            uploadProgressDialog.setIndeterminate(true);
            uploadProgressDialog.show();*/
            customUploadProgressDialog.showProgress(activity, getString(R.string.send_data),false);
        }

        protected String doInBackground(Object... prop) {
            FTPClient ftpClient = null;
            InternetConnection connection = null;
            try {
                ftpClient = new FTPClient();
                ftpClient.setConnectTimeout(10 * 1000);
                ftpClient.connect(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.FTP_HOST), SharedPreferencesStorage.getIntValue(activity, SharedPreferencesStorage.FTP_PORT));
                boolean status = ftpClient.login(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.FTP_USERNAME), SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.FTP_PASSWORD));
                System.out.println("isFTPConnected : " + String.valueOf(status));
                if(status){
                    if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {

                        // use local passive mode to pass firewall
                        ftpClient.enterLocalPassiveMode();
                        ftpClient.enterLocalPassiveMode();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                customUploadProgressDialog.setProgressStatus(0,0,"",getString(R.string.send_data));
                            }
                        });
                        Thread.sleep(getResources().getInteger(R.integer.thread_sleep));
                        // Logpond In
                        files = FileManager.getPublicFileWithoutDeletedFiles(activity, File.separator + getString(R.string.export_logpond_in));
                        int summ_file_length = 1;
                        int total_logpond_in_files_length_plus_one = files.length + summ_file_length + 1;
                        final int logpond_in_summ_size = files.length + summ_file_length;
                        int curr_logpond_in_files_num = 1;
                        if(files!=null && !isStopped){
                            customUploadProgressDialog.setMaximumSize(logpond_in_summ_size);
                            for(int i=0;i<total_logpond_in_files_length_plus_one;i++){
                                if(isStopped) break;
                                if (i<files.length) {
                                    File file = files[i];
                                    final int uploaded_logpond_in_file_num = curr_logpond_in_files_num;
                                    final String fileName = file.getName();
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            customUploadProgressDialog.setCustomProgress(uploaded_logpond_in_file_num);
                                            customUploadProgressDialog.setProgressStatus(uploaded_logpond_in_file_num, logpond_in_summ_size, fileName, getString(R.string.logpond_in));
                                        }
                                    });
                                    curr_logpond_in_files_num++;
                                    FTPManager.uploadFile(ftpClient, file.getAbsolutePath(), File.separator + getString(R.string.export_logpond_in));
                                    if (FTPManager.validateFileSize(ftpClient, file.getAbsolutePath(), File.separator + getString(R.string.export_logpond_in))) {
                                        // Save to private place
                                        File privateExportDir = new File(FileManager.getPrivateDirPath(activity) + File.separator + getString(R.string.export_logpond_in));
                                        File privateTransferDir = new File(FileManager.getPrivateDirPath(activity) + File.separator + getString(R.string.transfer_logpond_in));
                                        FileManager.moveFile(new File(privateExportDir.getAbsolutePath(), file.getName()), privateTransferDir);
                                        // Save to public place
                                        //File publicDir = new File(FileManager.getPublicDirPath(activity) + "/transfer_logpond_in");
                                        //FileManager.moveFile(file.getAbsoluteFile(), publicDir);
                                        file.delete();
                                    }
                                    Thread.sleep(getResources().getInteger(R.integer.thread_sleep));
                                }
                                else if (i==logpond_in_summ_size){
                                    final int uploaded_logpond_in_file_num = curr_logpond_in_files_num;
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            customUploadProgressDialog.setProgressStatus(uploaded_logpond_in_file_num,files.length,"", getString(R.string.logpond_in_complete));
                                        }
                                    });
                                    Thread.sleep(getResources().getInteger(R.integer.long_thread_sleep));
                                }
                               else{
                                    final String logpond_in_summary_file = username+"_"+getString(R.string.logpond_in_summary_name);
                                    FileManager.generateSummaryFile(activity, getString(R.string.transfer_logpond_in));
                                    final File summ_file = new File(FileManager.getPrivateDirPath(activity)+ File.separator+ getString(R.string.export_summary) +File.separator + logpond_in_summary_file);
                                    final int uploaded_file_num = curr_logpond_in_files_num;
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            customUploadProgressDialog.setCustomProgress(uploaded_file_num);
                                            customUploadProgressDialog.setProgressStatus(uploaded_file_num, logpond_in_summ_size, summ_file.getName(),getString(R.string.logpond_in)+"_"+getString(R.string.summary));
                                        }
                                    });
                                    curr_logpond_in_files_num++;
                                    FTPManager.uploadFile(ftpClient, summ_file.getAbsolutePath(), File.separator + getString(R.string.export_summary));
                                    Thread.sleep(getResources().getInteger(R.integer.thread_sleep));
                                }
                            }
                        }

                        // Logpond out
                        files = FileManager.getPublicFileWithoutDeletedFiles(activity, File.separator+ getString(R.string.export_logpond_out));
                        int logpond_out_file_length = 1;
                        final int logpond_out_summ_size = files.length + logpond_out_file_length;
                        int total_logpond_out_files_length_plus_one = files.length + logpond_out_file_length + 1;
                        int curr_logpond_out_files_num = 1;
                        if(files!=null && !isStopped) {
                            customUploadProgressDialog.setMaximumSize(logpond_out_summ_size);
                            for (int i = 0; i < total_logpond_out_files_length_plus_one; i++) {
                                if (isStopped) break;
                                if (i < files.length) {
                                    File file = files[i];
                                    final int uploaded_logpond_out_file_num = curr_logpond_out_files_num;
                                    final String fileName = file.getName();
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            customUploadProgressDialog.setCustomProgress(uploaded_logpond_out_file_num);
                                            customUploadProgressDialog.setProgressStatus(uploaded_logpond_out_file_num, logpond_out_summ_size, fileName, getString(R.string.logpond_out));
                                        }
                                    });
                                    curr_logpond_out_files_num++;
                                    FTPManager.uploadFile(ftpClient, file.getAbsolutePath(), File.separator + getString(R.string.export_logpond_out));
                                    if (FTPManager.validateFileSize(ftpClient, file.getAbsolutePath(), File.separator + getString(R.string.export_logpond_out))) {
                                        // Save to private place
                                        File privateExportDir = new File(FileManager.getPrivateDirPath(activity) + File.separator + getString(R.string.export_logpond_out));
                                        File privateTransferDir = new File(FileManager.getPrivateDirPath(activity) + File.separator + getString(R.string.transfer_logpond_out));
                                        FileManager.moveFile(new File(privateExportDir.getAbsolutePath(), file.getName()), privateTransferDir);
                                        // Save to public place
                                        //File publicDir = new File(FileManager.getPublicDirPath(activity) + "/transfer_logpond_out");
                                        //FileManager.moveFile(file.getAbsoluteFile(), publicDir);
                                        file.delete();
                                    }
                                    Thread.sleep(getResources().getInteger(R.integer.thread_sleep));
                                }
                                else if (i==logpond_out_summ_size){
                                    final int uploaded_logpond_out_file_num = curr_logpond_out_files_num;
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            customUploadProgressDialog.setProgressStatus(uploaded_logpond_out_file_num, files.length, "", getString(R.string.logpond_out_complete));
                                        }
                                    });
                                    Thread.sleep(getResources().getInteger(R.integer.long_thread_sleep));
                                }
                                else{
                                    final String logpond_out_summary_file = username+"_"+getString(R.string.logpond_out_summary_name);
                                    FileManager.generateSummaryFile(activity, getString(R.string.transfer_logpond_out));
                                    final File summ_file = new File(FileManager.getPrivateDirPath(activity)+ File.separator+ getString(R.string.export_summary) +File.separator + logpond_out_summary_file);
                                    final int uploaded_file_num = curr_logpond_out_files_num;
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            customUploadProgressDialog.setCustomProgress(uploaded_file_num);
                                            customUploadProgressDialog.setProgressStatus(uploaded_file_num, logpond_out_summ_size, summ_file.getName(),getString(R.string.logpond_out)+"_"+getString(R.string.summary));
                                        }
                                    });
                                    curr_logpond_out_files_num++;
                                    FTPManager.uploadFile(ftpClient, summ_file.getAbsolutePath(), File.separator + getString(R.string.export_summary));
                                    Thread.sleep(getResources().getInteger(R.integer.thread_sleep));
                                }
                            }
                        }
                        // Logpond Pile
                        files = FileManager.getPublicFileWithoutDeletedFiles(activity, File.separator + getString(R.string.export_logpond_pile));
                        int logpond_pile_file_length = 1;
                        final int logpond_pile_summ_size = files.length + logpond_pile_file_length;
                        int total_logpond_pile_files_length_plus_one = files.length + logpond_pile_file_length + 1;
                        int curr_logpond_pile_files_num = 1;
                            if(files!=null && !isStopped) {
                                for (int i = 0; i < total_logpond_pile_files_length_plus_one; i++) {
                                    customUploadProgressDialog.setMaximumSize(logpond_pile_summ_size);
                                    if (isStopped) break;
                                    if (i < files.length) {
                                        File file = files[i];
                                        //publishProgress(i);
                                        final int uploaded_logpond_pile_file_num = curr_logpond_pile_files_num;
                                        final String fileName = file.getName();
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                customUploadProgressDialog.setCustomProgress(uploaded_logpond_pile_file_num);
                                                customUploadProgressDialog.setProgressStatus(uploaded_logpond_pile_file_num, files.length, fileName, getString(R.string.logpond_pile));
                                            }
                                        });
                                        curr_logpond_pile_files_num++;
                                        FTPManager.uploadFile(ftpClient, file.getAbsolutePath(), File.separator + getString(R.string.export_logpond_pile));
                                        if (FTPManager.validateFileSize(ftpClient, file.getAbsolutePath(), File.separator + getString(R.string.export_logpond_pile))) {
                                            // Save to private place
                                            File privateExportDir = new File(FileManager.getPrivateDirPath(activity) + File.separator + getString(R.string.export_logpond_pile));
                                            File privateTransferDir = new File(FileManager.getPrivateDirPath(activity) + File.separator + getString(R.string.transfer_logpond_pile));
                                            FileManager.moveFile(new File(privateExportDir.getAbsolutePath(), file.getName()), privateTransferDir);
                                            // Save to public place
                                            //File publicDir = new File(FileManager.getPublicDirPath(activity) + "/transfer_logpond_pile");
                                            //FileManager.moveFile(file.getAbsoluteFile(), publicDir);
                                            file.delete();
                                        }
                                        Thread.sleep(getResources().getInteger(R.integer.thread_sleep));
                                    }
                                    else if (i==logpond_pile_summ_size){
                                        final int uploaded_logpond_pile_file_num = curr_logpond_pile_files_num;
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                customUploadProgressDialog.setProgressStatus(uploaded_logpond_pile_file_num, files.length, "", getString(R.string.logpond_pile_complete));
                                            }
                                        });
                                        Thread.sleep(getResources().getInteger(R.integer.long_thread_sleep));
                                    }
                                    else{
                                        final String logpond_pile_summary_file = username+"_"+getString(R.string.logpond_pile_summary_name);
                                        FileManager.generateSummaryFile(activity, getString(R.string.transfer_logpond_pile));
                                        final File summ_file = new File(FileManager.getPrivateDirPath(activity)+ File.separator+ getString(R.string.export_summary) +File.separator + logpond_pile_summary_file);
                                        final int uploaded_file_num = curr_logpond_pile_files_num;
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                customUploadProgressDialog.setCustomProgress(uploaded_file_num);
                                                customUploadProgressDialog.setProgressStatus(uploaded_file_num, logpond_pile_summ_size, summ_file.getName(),getString(R.string.logpond_pile)+"_"+getString(R.string.summary));
                                            }
                                        });
                                        curr_logpond_pile_files_num++;
                                        FTPManager.uploadFile(ftpClient, summ_file.getAbsolutePath(), File.separator + getString(R.string.export_summary));
                                        Thread.sleep(getResources().getInteger(R.integer.thread_sleep));
                                    }
                                }

                            }
                        // Buyer Grading
                        files = FileManager.getPublicFileWithoutDeletedFiles(activity, File.separator + getString(R.string.export_buyer_grading));
                        int buyer_file_length = 1;
                        final int buyer_summ_size = files.length +buyer_file_length;
                        int total_buyer_files_length_plus_one = files.length + buyer_file_length + 1;
                        int curr_buyer_files_num = 1;
                                if(files!=null && !isStopped){
                                    customUploadProgressDialog.setMaximumSize(buyer_summ_size);
                                    for(int i=0;i<total_buyer_files_length_plus_one;i++){
                                        if(isStopped) break;
                                        if (i<files.length){
                                            File file = files[i];
                                            //publishProgress(i);
                                            final int uploaded_buyer_file_num = curr_buyer_files_num;
                                            final String fileName = file.getName();
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    customUploadProgressDialog.setCustomProgress(uploaded_buyer_file_num);
                                                    customUploadProgressDialog.setProgressStatus(uploaded_buyer_file_num,files.length,fileName, getString(R.string.buyer_grading1));
                                                }
                                            });
                                            curr_buyer_files_num++;
                                            FTPManager.uploadFile(ftpClient, file.getAbsolutePath(), File.separator + getString(R.string.export_buyer_grading));
                                            if(FTPManager.validateFileSize(ftpClient, file.getAbsolutePath(), File.separator + getString(R.string.export_buyer_grading))){
                                                // Save to private place
                                                File privateExportDir = new File(FileManager.getPrivateDirPath(activity) + File.separator + getString(R.string.export_buyer_grading));
                                                File privateTransferDir = new File(FileManager.getPrivateDirPath(activity) + File.separator + getString(R.string.transfer_buyer_grading));
                                                FileManager.moveFile(new File(privateExportDir.getAbsolutePath(), file.getName()), privateTransferDir);
                                                // Save to public place
                                                //File publicDir = new File(FileManager.getPublicDirPath(activity) + "/transfer_buyer_grading");
                                                //FileManager.moveFile(file.getAbsoluteFile(), publicDir);
                                                file.delete();
                                             }
                                            Thread.sleep(getResources().getInteger(R.integer.thread_sleep));
                                     }
                                    else if (i==buyer_summ_size){
                                                final int uploaded_buyer_file_num = curr_buyer_files_num;
                                                activity.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        customUploadProgressDialog.setProgressStatus(uploaded_buyer_file_num,files.length,"", getString(R.string.buyer_complete));
                                                    }
                                                });
                                                Thread.sleep(getResources().getInteger(R.integer.long_thread_sleep));
                                    }
                                    else{
                                            final String buyer_summary_file = username+"_"+getString(R.string.buyer_grading_summary_name);
                                            FileManager.generateSummaryFile(activity, getString(R.string.transfer_buyer_grading));
                                            final File summ_file = new File(FileManager.getPrivateDirPath(activity)+ File.separator+ getString(R.string.export_summary) +File.separator + buyer_summary_file);
                                            final int uploaded_file_num = curr_buyer_files_num;
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    customUploadProgressDialog.setCustomProgress(uploaded_file_num);
                                                    customUploadProgressDialog.setProgressStatus(uploaded_file_num, buyer_summ_size, summ_file.getName(),getString(R.string.buyer_grading1)+"_"+getString(R.string.summary));
                                                }
                                            });
                                            curr_buyer_files_num++;
                                            FTPManager.uploadFile(ftpClient, summ_file.getAbsolutePath(), File.separator + getString(R.string.export_summary));
                                            Thread.sleep(getResources().getInteger(R.integer.thread_sleep));
                                        }
                            }
                        }

                        // Generate Summary File
                       /* FileManager.generateSummaryFile(activity, getString(R.string.transfer_logpond_in));
                        FileManager.generateSummaryFile(activity, getString(R.string.transfer_logpond_out));
                        FileManager.generateSummaryFile(activity, getString(R.string.transfer_logpond_pile));
                        FileManager.generateSummaryFile(activity, getString(R.string.transfer_buyer_grading));
                        // Summary
                        files = FileManager.getPrivateFileWithoutDeletedFiles(activity, File.separator + getString(R.string.export_summary));
                        int total_summ_length_plus_one = files.length +1;
                        int summ_files_num = 1;
                        if(files!=null && !isStopped) {
                            for (int i = 0; i < total_summ_length_plus_one; i++) {
                                if (isStopped) break;
                                customUploadProgressDialog.setMaximumSize(files.length);
                                if (i < files.length) {
                                    File file = files[i];
                                    final int uploaded_summ_files_num = summ_files_num;
                                    final String fileName = file.getName();
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            customUploadProgressDialog.setCustomProgress(uploaded_summ_files_num);
                                            customUploadProgressDialog.setProgressStatus(uploaded_summ_files_num, files.length, fileName, getString(R.string.export_summary));
                                        }
                                    });
                                    FTPManager.uploadFile(ftpClient, file.getAbsolutePath(), File.separator + getString(R.string.export_summary));
                                    summ_files_num++;
                                    Thread.sleep(getResources().getInteger(R.integer.thread_sleep));
                                } else {
                                    final int uploaded_summ_files_num = summ_files_num;
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            customUploadProgressDialog.setProgressStatus(uploaded_summ_files_num, files.length, "", "summary_complete");
                                        }
                                    });
                                    Thread.sleep(getResources().getInteger(R.integer.long_thread_sleep));
                                }
                            }
                        }*/
                    }
                    else{
                        errorMessage =  getString(R.string.ftp_connection_error);
                    }
                }
                else{
                    errorMessage =  getString(R.string.ftp_connection_error);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
            }finally{
                if(ftpClient!=null && ftpClient.isConnected()){
                    try {
                        ftpClient.disconnect();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if(connection!=null)
                    try {
                        connection.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
            return "Success";
        }

        protected void onProgressUpdate(Integer... progress) {
            //uploadProgressDialog.setProgress(progress[0]);
            customProgress.setCustomProgress(progress[0]);
            customUploadProgressDialog.setCustomProgress(progress[0]);
        }



        protected void onCancelled (String result){
            isStopped = true;
            progressDialog.dismiss();
            customUploadProgressDialog.hideUploadProgress();
            Log.d("onCancelled", "");
        }

        protected void onPostExecute(String result) {
            try{
                progressDialog.dismiss();
                // Error
                if(errorMessage.length()>0){
                    final CustomAlertDialog downloadAlertDialogs = new CustomAlertDialog();
                    View.OnClickListener save_listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new DownloadRemoteDBTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            downloadAlertDialogs.cancel();
                        }

                    };
                    downloadAlertDialogs.MyDialogWithYesNo(MainActivity.this, getString(R.string.send_data_failed), errorMessage+"\n"+getString(R.string.retry_question), save_listener,null);
                    //Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
                    // Success
                }else{
                    final CustomWarningAlertDialog sent_successfully_alert_dialog = new CustomWarningAlertDialog();
                    View.OnClickListener ok_listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //do something
                            sent_successfully_alert_dialog.cancel();
                        }
                    };
                    sent_successfully_alert_dialog.MyDialogWithYesNo(MainActivity.this,"Send Data",getString(R.string.sent_sound_text),ok_listener,null);

                    TTSManager.sayText(MainActivity.this,getString(R.string.sent_sound_text));
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                //uploadProgressDialog.dismiss();
                customUploadProgressDialog.hideUploadProgress();
            }
        }
    }
}
