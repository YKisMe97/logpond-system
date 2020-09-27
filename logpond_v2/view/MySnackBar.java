package com.infocomm.logpond_v2.view;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

/**
 * Created by DoAsInfinity on 6/5/2017.
 */

public class MySnackBar {

    public static void showError(View view, String message, int time){
        Snackbar snackbar = Snackbar.make(view, message,time).setAction("Action", null);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(Color.BLUE);
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

}
