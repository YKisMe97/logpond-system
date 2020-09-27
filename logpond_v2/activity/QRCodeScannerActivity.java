package com.infocomm.logpond_v2.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.infocomm.logpond_v2.R;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class QRCodeScannerActivity extends AppCompatActivity implements View.OnClickListener, ZBarScannerView.ResultHandler{

    private QRCodeScannerActivity activity;
    private ZBarScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZBarScannerView(this);    // Programmatically initialize the scanner view
        setContentView(mScannerView);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        activity = this;
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                killActivity();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        killActivity();
    }

    private void killActivity(){
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void onClick(View view){
        if(view.getId()== R.id.home){
            killActivity();
        }
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        if(rawResult!=null&&rawResult.getContents()!=null){
            Log.v("~~~~~~~~~~~~~~~~QRCODE", rawResult.getContents()); // Prints scan results
            Log.v("~~~~~~~~~~~~~~~~QRCODE", rawResult.getBarcodeFormat().getName()); // Prints the scan format (qrcode, pdf417 etc.)

            // If you would like to resume scanning, call this method below:
            mScannerView.resumeCameraPreview(this);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("CODE", rawResult.getContents());
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }
}
