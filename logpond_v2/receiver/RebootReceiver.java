package com.infocomm.logpond_v2.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.infocomm.logpond_v2.storage.SharedPreferencesStorage;

/**
 * Created by DoAsInfinity on 7/12/2016.
 */
public class RebootReceiver extends BroadcastReceiver {

    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    private final int AUTO_SYNC_REQUEST_CODE = 5555;

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~RebootReceiver");
        SharedPreferencesStorage.setBooleanValue(context.getApplicationContext(), SharedPreferencesStorage.IS_AUTO_SYNC_RUNNING, false);
        SharedPreferencesStorage.setBooleanValue(context.getApplicationContext(), SharedPreferencesStorage.IS_AUTO_SYNC_FINISHED, true);
        int autoSyncAction = SharedPreferencesStorage.getIntValue(context.getApplicationContext(), SharedPreferencesStorage.AUTO_SYNC_ACTION);
        if(autoSyncAction>0){
            AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent autoSyncReceiver = new Intent(context.getApplicationContext(), AutoSyncReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), AUTO_SYNC_REQUEST_CODE, autoSyncReceiver, 0);
            // Stop any alarm manager first
            alarmManager.cancel(pendingIntent);
            // Replace the settings with yours here...
            long intervalHourInMilliseconds = autoSyncAction * 60 * 1000 * 60;
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis() + intervalHourInMilliseconds, intervalHourInMilliseconds, pendingIntent);
        }else{
            AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent autoSyncReceiver = new Intent(context.getApplicationContext(), AutoSyncReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), AUTO_SYNC_REQUEST_CODE, autoSyncReceiver, 0);
            alarmManager.cancel(pendingIntent);
        }
    }
}