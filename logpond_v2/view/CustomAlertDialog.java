package com.infocomm.logpond_v2.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.infocomm.logpond_v2.R;


public class CustomAlertDialog {

    private AlertDialog.Builder downloadAlertDialogBuilder;
    private AlertDialog downloadAlertDialog;
    private Context mContext;
    public void MyDialogWithYesNo(Context context, String dialog_title, String dialog_content, View.OnClickListener yes_action, View.OnClickListener no_action) {
        this.mContext = context;
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View custom_layout = inflater.inflate(R.layout.save_exit_dialog, null);

        downloadAlertDialogBuilder = new AlertDialog.Builder(context);
        downloadAlertDialogBuilder.setView(custom_layout);
        TextView download_title = custom_layout.findViewById(R.id.dialog_title);
        TextView download_content = custom_layout.findViewById(R.id.alert_dialog_content);
        ImageView true_btn = custom_layout.findViewById(R.id.true_btn);
        ImageView false_btn = custom_layout.findViewById(R.id.false_btn);
        download_title.setText(dialog_title);
        download_content.setText(dialog_content);

        downloadAlertDialog = downloadAlertDialogBuilder.create();
        downloadAlertDialog.getWindow().getAttributes().windowAnimations = R.style.custom_dialog_animation;
        downloadAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        downloadAlertDialog.setCanceledOnTouchOutside(false);
        true_btn.setOnClickListener(yes_action);
        if(no_action==null) {
            false_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadAlertDialog.dismiss();
                }
            });
        }
        else{
            false_btn.setOnClickListener(no_action);
        }
        downloadAlertDialog.show();
}

    public void cancel(){
        downloadAlertDialog.dismiss();
    }
}
