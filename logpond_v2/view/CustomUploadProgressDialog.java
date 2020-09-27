package com.infocomm.logpond_v2.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.infocomm.logpond_v2.R;

public class CustomUploadProgressDialog {

    public static CustomUploadProgressDialog customUploadProgressDialog = null;
    private Dialog mDialog;
    private ProgressBar mProgressBar;
    private int progress_number;
    private TextView current_status, uploaded_file, percentage, progressText, progress_dialog_title;
    private String dl_message;
    private Context mContext;
    public static CustomUploadProgressDialog getInstance() {
        if (customUploadProgressDialog == null) {
            customUploadProgressDialog = new CustomUploadProgressDialog();
        }
        return customUploadProgressDialog;
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
        progressText = mDialog.findViewById(R.id.custom_progress_text);
        current_status = mDialog.findViewById(R.id.progress_status);
        uploaded_file = mDialog.findViewById(R.id.progress_status_filename);
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
        progress_dialog_title.setText(mContext.getString(R.string.send_data));
        if (str_message.equals(mContext.getString(R.string.send_data))){
            progressText.setText(mContext.getString(R.string.connection_establish));
            percentage.setText("");
            current_status.setText("");
            uploaded_file.setText("");
        }
        else if (str_message.equals(mContext.getString(R.string.logpond_in))){
            progressText.setText(mContext.getString(R.string.logpond_in));
            double current_percentage = (new Double(curr_download_file) / new Double(total_files)) * 100;
            percentage.setText((int) Math.round(current_percentage) + "%");
            current_status.setText(curr_download_file + "/" + (total_files));
            uploaded_file.setText(filename);
        }
        else if (str_message.equals(mContext.getString(R.string.logpond_in) + "_" + mContext.getString(R.string.summary))){
            progressText.setText(mContext.getString(R.string.logpond_in));
            double current_percentage = (new Double(curr_download_file) / new Double(total_files)) * 100;
            percentage.setText((int) Math.round(current_percentage) + "%");
            current_status.setText("Summary file:");
            uploaded_file.setText(filename);
        }
        else if (str_message.equals(mContext.getString(R.string.logpond_out))){
            progressText.setText(mContext.getString(R.string.logpond_out));
            double current_percentage = (new Double(curr_download_file) / new Double(total_files)) * 100;
            percentage.setText((int) Math.round(current_percentage) + "%");
            current_status.setText(curr_download_file + "/" + total_files);
            uploaded_file.setText(filename);
        }
        else if (str_message.equals(mContext.getString(R.string.logpond_out) + "_" + mContext.getString(R.string.summary))){
            progressText.setText(mContext.getString(R.string.logpond_out));
            double current_percentage = (new Double(curr_download_file) / new Double(total_files)) * 100;
            percentage.setText((int) Math.round(current_percentage) + "%");
            current_status.setText("Summary file:");
            uploaded_file.setText(filename);
        }
        else if (str_message.equals(mContext.getString(R.string.logpond_pile))){
            progressText.setText(mContext.getString(R.string.logpond_pile));
            double current_percentage = (new Double(curr_download_file) / new Double(total_files)) * 100;
            percentage.setText((int) Math.round(current_percentage) + "%");
            current_status.setText(curr_download_file + "/" + total_files);
            uploaded_file.setText(filename);
        }
        else if (str_message.equals(mContext.getString(R.string.logpond_pile) + "_" + mContext.getString(R.string.summary))){
            progressText.setText(mContext.getString(R.string.logpond_pile));
            double current_percentage = (new Double(curr_download_file) / new Double(total_files)) * 100;
            percentage.setText((int) Math.round(current_percentage) + "%");
            current_status.setText("Summary file:");
            uploaded_file.setText(filename);
        }
        else if (str_message.equals(mContext.getString(R.string.buyer_grading1))){
            progressText.setText(mContext.getString(R.string.buyer_grading1));
            double current_percentage = (new Double(curr_download_file) / new Double(total_files)) * 100;
            percentage.setText((int) Math.round(current_percentage) + "%");
            current_status.setText(curr_download_file + "/" + total_files);
            uploaded_file.setText(filename);
        }
        else if (str_message.equals(mContext.getString(R.string.buyer_grading1) + "_" + mContext.getString(R.string.summary))){
            progressText.setText(mContext.getString(R.string.buyer_grading1));
            double current_percentage = (new Double(curr_download_file) / new Double(total_files)) * 100;
            percentage.setText((int) Math.round(current_percentage) + "%");
            current_status.setText("Summary file:");
            uploaded_file.setText(filename);
        }
        else if (str_message.equals(mContext.getString(R.string.export_summary))){
            progressText.setText("Summary Files");
            double current_percentage = (new Double(curr_download_file) / new Double(total_files)) * 100;
            percentage.setText((int) Math.round(current_percentage) + "%");
            current_status.setText(curr_download_file + "/" + total_files);
            uploaded_file.setText(filename);
        }
        else if (str_message.equals(mContext.getString(R.string.logpond_in_complete))){
            progressText.setText("["+mContext.getString(R.string.logpond_in)+"] "+ mContext.getString(R.string.sent_complete));
            current_status.setText("");
            uploaded_file.setText("");
        }
        else if (str_message.equals(mContext.getString(R.string.logpond_out_complete))){
            progressText.setText("["+mContext.getString(R.string.logpond_out)+"] " + mContext.getString(R.string.sent_complete));
            current_status.setText("");
            uploaded_file.setText("");
        }
        else if (str_message.equals(mContext.getString(R.string.logpond_pile_complete))){
            progressText.setText("["+mContext.getString(R.string.logpond_pile)+"] " + mContext.getString(R.string.sent_complete));
            current_status.setText("");
            uploaded_file.setText("");
        }
        else if (str_message.equals(mContext.getString(R.string.buyer_complete))){
            progressText.setText("["+mContext.getString(R.string.buyer_grading1)+"] " + mContext.getString(R.string.sent_complete));
            current_status.setText("");
            uploaded_file.setText("");
        }
    }

    public void hideUploadProgress() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}