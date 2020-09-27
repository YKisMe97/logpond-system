package com.infocomm.logpond_v2.view;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by DoAsInfinity on 8/30/2017.
 */

public class MyToast {
    public static void show(Context context, String text, int time){
        Toast toast = Toast.makeText(context, text, time);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
