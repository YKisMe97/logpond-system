package com.infocomm.logpond_v2.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.infocomm.logpond_v2.R;
import com.infocomm.logpond_v2.activity.MainActivity;
import com.infocomm.logpond_v2.storage.SharedPreferencesStorage;
import com.infocomm.logpond_v2.util.FTPManager;
import com.infocomm.logpond_v2.util.FileManager;
import com.infocomm.logpond_v2.util.PhoneManager;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.IOException;

/**
 * Created by DoAsInfinity on 7/12/2016.
 */
public class AutoSyncReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~AutoSyncReceiver");
        if(SharedPreferencesStorage.getBooleanValue(context.getApplicationContext(), SharedPreferencesStorage.IS_AUTO_SYNC_FINISHED)){
            SharedPreferencesStorage.setBooleanValue(context.getApplicationContext(), SharedPreferencesStorage.IS_AUTO_SYNC_FINISHED, false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
                    @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "aqs_wake_lock");
                    wakeLock.acquire();


                    //FileOutputStream fileOutputStream = null;
                    boolean shouldSync = false;
                    try{
                        // Logpond In
                        File[] files = FileManager.getPublicFileWithoutDeletedFiles(context.getApplicationContext(), "/export_logpond_in");
                        if(files.length>0) shouldSync = true;
                        files = FileManager.getPublicFileWithoutDeletedFiles(context.getApplicationContext(), "/export_logpond_out");
                        if(files.length>0) shouldSync = true;
                        files = FileManager.getPublicFileWithoutDeletedFiles(context.getApplicationContext(), "/export_logpond_pile");
                        if(files.length>0) shouldSync = true;
                        files = FileManager.getPublicFileWithoutDeletedFiles(context.getApplicationContext(), "/export_buyer_grading");
                        if(files.length>0) shouldSync = true;

                        if(PhoneManager.isConnectingToInternet(context.getApplicationContext()) && shouldSync){
                            while(MainActivity.getInstance()!=null){
                                try {
                                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~Loop and wait in auto sync receiver");
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            syncData(context.getApplicationContext());
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }finally{
                        System.out.println("~~~~~~~~~~~~~~~~~~~~~~Auto Sync Done");
                        SharedPreferencesStorage.setBooleanValue(context.getApplicationContext(), SharedPreferencesStorage.IS_AUTO_SYNC_RUNNING, false);
                        SharedPreferencesStorage.setBooleanValue(context.getApplicationContext(), SharedPreferencesStorage.IS_AUTO_SYNC_FINISHED, true);
                        if (wakeLock != null)
                            wakeLock.release();
                /*
                if(fileOutputStream!=null)
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if (wakeLock != null)
                    wakeLock.release();
                    */


                    }
                }
            }).start();

        }
    }

    public void syncData(Context context){
        SharedPreferencesStorage.setBooleanValue(context.getApplicationContext(), SharedPreferencesStorage.IS_AUTO_SYNC_RUNNING, true);
        String errorMessage = "";
        FTPClient ftpClient = null;
        File[] files = null;

        try {
            //Thread.sleep(10000);
            ftpClient = new FTPClient();
            ftpClient.setConnectTimeout(10 * 1000);
            ftpClient.connect(SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.FTP_HOST), SharedPreferencesStorage.getIntValue(context, SharedPreferencesStorage.FTP_PORT));
            boolean status = ftpClient.login(SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.FTP_USERNAME), SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.FTP_PASSWORD));
            System.out.println("isFTPConnected : " + String.valueOf(status));
            if(status){
                if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {

                    // use local passive mode to pass firewall
                    ftpClient.enterLocalPassiveMode();

                    // Logpond In
                    files = FileManager.getPublicFileWithoutDeletedFiles(context, "/export_logpond_in");

                    for(int i=0;i<files.length;i++){
                        if(!SharedPreferencesStorage.getBooleanValue(context.getApplicationContext(), SharedPreferencesStorage.IS_AUTO_SYNC_RUNNING)) return;
                        File file = files[i];
                        FTPManager.uploadFile(ftpClient, file.getAbsolutePath(), "/export_logpond_in");
                        if(FTPManager.validateFileSize(ftpClient, file.getAbsolutePath(), "/export_logpond_in")){
                            // Save to private place
                            File privateExportDir = new File(FileManager.getPrivateDirPath(context) + "/export_logpond_in");
                            File privateTransferDir = new File(FileManager.getPrivateDirPath(context) + "/transfer_logpond_in");
                            FileManager.moveFile(new File(privateExportDir.getAbsolutePath(), file.getName()), privateTransferDir);
                            // Save to public place
                            File publicDir = new File(FileManager.getPublicDirPath(context) + "/transfer_logpond_in");
                            FileManager.moveFile(file.getAbsoluteFile(), publicDir);
                        }
                    }

                    // Logpond out
                    files = FileManager.getPublicFileWithoutDeletedFiles(context, "/export_logpond_out");

                    for(int i=0;i<files.length;i++){
                        if(!SharedPreferencesStorage.getBooleanValue(context.getApplicationContext(), SharedPreferencesStorage.IS_AUTO_SYNC_RUNNING)) return;
                        File file = files[i];
                        FTPManager.uploadFile(ftpClient, file.getAbsolutePath(), "/export_logpond_out");
                        if(FTPManager.validateFileSize(ftpClient, file.getAbsolutePath(), "/export_logpond_out")){
                            // Save to private place
                            File privateExportDir = new File(FileManager.getPrivateDirPath(context) + "/export_logpond_out");
                            File privateTransferDir = new File(FileManager.getPrivateDirPath(context) + "/transfer_logpond_out");
                            FileManager.moveFile(new File(privateExportDir.getAbsolutePath(), file.getName()), privateTransferDir);
                            // Save to public place
                            //File publicDir = new File(FileManager.getPublicDirPath(activity) + "/transfer_logpond_out");
                            //FileManager.moveFile(file.getAbsoluteFile(), publicDir);
                            file.delete();
                        }
                    }


                    // Logpond Pile
                    files = FileManager.getPublicFileWithoutDeletedFiles(context, "/export_logpond_pile");

                    for(int i=0;i<files.length;i++){
                        if(!SharedPreferencesStorage.getBooleanValue(context.getApplicationContext(), SharedPreferencesStorage.IS_AUTO_SYNC_RUNNING)) return;
                        File file = files[i];
                        FTPManager.uploadFile(ftpClient, file.getAbsolutePath(), "/export_logpond_pile");
                        if(FTPManager.validateFileSize(ftpClient, file.getAbsolutePath(), "/export_logpond_pile")){
                            // Save to private place
                            File privateExportDir = new File(FileManager.getPrivateDirPath(context) + "/export_logpond_pile");
                            File privateTransferDir = new File(FileManager.getPrivateDirPath(context) + "/transfer_logpond_pile");
                            FileManager.moveFile(new File(privateExportDir.getAbsolutePath(), file.getName()), privateTransferDir);
                            // Save to public place
                            //File publicDir = new File(FileManager.getPublicDirPath(activity) + "/transfer_logpond_pile");
                            //FileManager.moveFile(file.getAbsoluteFile(), publicDir);
                            file.delete();
                        }
                    }


                    // Buyer Grading
                    files = FileManager.getPublicFileWithoutDeletedFiles(context, "/export_buyer_grading");

                    for(int i=0;i<files.length;i++){
                        if(!SharedPreferencesStorage.getBooleanValue(context.getApplicationContext(), SharedPreferencesStorage.IS_AUTO_SYNC_RUNNING)) return;
                        File file = files[i];
                        FTPManager.uploadFile(ftpClient, file.getAbsolutePath(), "/export_buyer_grading");
                        if(FTPManager.validateFileSize(ftpClient, file.getAbsolutePath(), "/export_buyer_grading")){
                            // Save to private place
                            File privateExportDir = new File(FileManager.getPrivateDirPath(context) + "/export_buyer_grading");
                            File privateTransferDir = new File(FileManager.getPrivateDirPath(context) + "/transfer_buyer_grading");
                            FileManager.moveFile(new File(privateExportDir.getAbsolutePath(), file.getName()), privateTransferDir);
                            // Save to public place
                            //File publicDir = new File(FileManager.getPublicDirPath(activity) + "/transfer_buyer_grading");
                            //FileManager.moveFile(file.getAbsoluteFile(), publicDir);
                            file.delete();
                        }
                    }

                    // Generate Summary File
                    FileManager.generateSummaryFile(context, "transfer_logpond_in");
                    FileManager.generateSummaryFile(context, "transfer_logpond_out");
                    FileManager.generateSummaryFile(context, "transfer_logpond_pile");
                    FileManager.generateSummaryFile(context, "transfer_buyer_grading");
                    // Summary
                    files = FileManager.getPrivateFileWithoutDeletedFiles(context, "/export_summary");

                    for(int i=0;i<files.length;i++){
                        if(!SharedPreferencesStorage.getBooleanValue(context.getApplicationContext(), SharedPreferencesStorage.IS_AUTO_SYNC_RUNNING)) return;
                        File file = files[i];
                        FTPManager.uploadFile(ftpClient, file.getAbsolutePath(), "/export_summary");
                    }

                }else{
                    errorMessage =  context.getString(R.string.ftp_connection_error);
                }
            }else{
                errorMessage =  context.getString(R.string.ftp_connection_error);
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
        }
    }
}