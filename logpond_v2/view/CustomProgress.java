package com.infocomm.logpond_v2.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.infocomm.logpond_v2.R;

public class CustomProgress {

    public static CustomProgress customProgress = null;
    private Dialog mDialog;
    private ProgressBar mProgressBar;
    private Context mContext;
    private int progress_number;
    private TextView current_status, downloaded_file, percentage, progressText;
    private TextView progress_dialog_title;
    private String dl_message="Connection establish";

    public static CustomProgress getInstance() {
        if (customProgress == null) {
            customProgress = new CustomProgress();
        }
        return customProgress;
    }

    public void showProgress(Context context, String message, boolean cancelable) {
        mContext = context;
        dl_message=message;
        mDialog = new Dialog(context);
        // no tile for the dialog
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.custom_progress_dialog);

        mProgressBar = mDialog.findViewById(R.id.custom_progressBar);
        progress_dialog_title = mDialog.findViewById(R.id.progress_dialog_title);
        progress_dialog_title.setText(mContext.getString(R.string.download_data));
        //  mProgressBar.getIndeterminateDrawable().setColorFilter(context.getResources()
        // .getColor(R.color.material_blue_gray_500), PorterDuff.Mode.SRC_IN);
        progressText = mDialog.findViewById(R.id.custom_progress_text);
        current_status = mDialog.findViewById(R.id.progress_status);
        downloaded_file = mDialog.findViewById(R.id.progress_status_filename);
        percentage = mDialog.findViewById(R.id.percentage_text);
        progressText.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(false);
        // you can change or add this line according to your need
        //mProgressBar.setIndeterminate(true);
        mProgressBar.setProgress(0);
        mDialog.setCancelable(cancelable);
        mDialog.setCanceledOnTouchOutside(cancelable);
        mDialog.show();
    }

    public void setMaximumSize(int max){
        mProgressBar.setMax(max);
    }

    public void setCustomProgress(int progress){
        mProgressBar.setProgress(progress);
    }

    public void setProgressStatus(int curr_download_file, int total_files, String filename, String str_message){
        if (str_message.equals(mContext.getString(R.string.downloading_data))){
            double current_percentage = (new Double(curr_download_file)/ new Double(total_files))*100;
            percentage.setText((int)Math.round(current_percentage)+"%");
            current_status.setText(curr_download_file +"/"+ total_files);
            progressText.setText(String.valueOf(dl_message));
            downloaded_file.setText(filename);
        }
        else if (str_message.equals(mContext.getString(R.string.download_complete))){
            progressText.setText(mContext.getString(R.string.download_complete));
            current_status.setText("");
            downloaded_file.setText("");
        }
        else if (str_message.equals(mContext.getString(R.string.synchronization))){
            //progress_dialog_title.setText(mContext.getString(R.string.synchronization));
            progressText.setText(mContext.getString(R.string.synchronization));
            downloaded_file.setText("");
            current_status.setText("");
            percentage.setText("");
            mProgressBar.setVisibility(View.GONE);
        }
        //downloaded_file.setText(filename);
        // if (!filename.equals(mContext.getString(R.string.import_database)) && curr_download_file != total_files){ }
        else if (str_message.equals(mContext.getString(R.string.import_database))){
            current_status.setText(mContext.getString(R.string.import_database)+": "+mContext.getString(R.string.inventory_file).replace(".txt",""));
            // progressText.setText("");
        }
        else if (str_message.equals(mContext.getString(R.string.synchronize_complete))){
            progressText.setText(mContext.getString(R.string.synchronize_complete));
            downloaded_file.setText("");
            current_status.setText("");
        }
    }

    public void hideProgress() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}