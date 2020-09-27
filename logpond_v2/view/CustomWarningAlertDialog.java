package com.infocomm.logpond_v2.view;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.infocomm.logpond_v2.R;


public class CustomWarningAlertDialog {

    private AlertDialog.Builder downloadAlertDialogBuilder;
    private AlertDialog downloadAlertDialog;
    private ImageView warning, info;
    public void MyDialogWithYesNo(Context context, String dialog_title, String dialog_content, View.OnClickListener yes_action, View.OnClickListener no_action) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View custom_layout = inflater.inflate(R.layout.warning_alert_dialog, null);

        downloadAlertDialogBuilder = new AlertDialog.Builder(context);
        downloadAlertDialogBuilder.setView(custom_layout);
        warning =custom_layout.findViewById(R.id.imageViewWarning);
        info = custom_layout.findViewById(R.id.imageViewInfo);
        TextView download_title = custom_layout.findViewById(R.id.dialog_title);
        TextView download_content = custom_layout.findViewById(R.id.alert_dialog_content);
        TextView positive_button = custom_layout.findViewById(R.id.positive_btn);
        download_title.setText(dialog_title);
        download_content.setText(dialog_content);
        positive_button.setOnClickListener(yes_action);

        if (dialog_title.equals(context.getString(R.string.download_data)) || dialog_title.equals(context.getString(R.string.send_data))){
            warning.setVisibility(View.GONE);
            info.setVisibility(View.VISIBLE);
        }

        downloadAlertDialog = downloadAlertDialogBuilder.create();
        downloadAlertDialog.getWindow().getAttributes().windowAnimations = R.style.custom_dialog_animation;
        downloadAlertDialog.setCanceledOnTouchOutside(false);
        downloadAlertDialog.show();
    }

    public void cancel(){
        downloadAlertDialog.dismiss();
    }
}
