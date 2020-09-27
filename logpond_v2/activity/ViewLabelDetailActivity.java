package com.infocomm.logpond_v2.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.infocomm.logpond_v2.R;
import com.infocomm.logpond_v2.storage.SqlDatabase;
import com.infocomm.logpond_v2.view.CustomWarningAlertDialog;
import com.infocomm.logpond_v2.view.TTSManager;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewLabelDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private final int SCANNER_REQUEST_CODE = 99;
    private ViewLabelDetailActivity activity;
    private TextView labelNumberTextView, oldLabelTextView, motherLabelTextView, cnbEntryNoTextView, cnbTextView,
            companyIdTextView, logResourceTextView, logpondNameTextView, logpondPileTextView, gradeTextView,
            statusTextView, concessionTextView, kapvalTextView, parcelTextView, terrainTextView, pvNoTextView,
            speciesTextView, boomNumberTextView, voet1TextView, voet2TextView, top1TextView, top2TextView, lengthTextView,
            hTextView, rTextView,commentsTextView, averageDiametersTextView, volumeTextView, exportNoTextView, inspectionDateTextView, expiryTextView,
            exportVergunningTextView, licenseNoTextView, buyerRejectTextView, containerTextView, buyerAllocationTextView, kapRegNoTextView, searchLabelNo, toolbar_title;
    private ImageView searchImageView;
    private EditText searchLabelNumberEditTextView;
    private ProgressDialog progressDialog;
    private String searchLabelNumber, viewLabelNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_view_label_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        activity = this;
        searchLabelNumber = "";
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setIndeterminate(true);

        toolbar_title= findViewById(R.id.toolbar_title);
        viewLabelNumber= getIntent().getStringExtra("viewLabelNumber");
        toolbar_title.setText(viewLabelNumber);


        searchLabelNo = findViewById(R.id.search_labelnumber);
        if (viewLabelNumber.length()>0){
            searchLabel();
        }
        searchLabelNumberEditTextView = (EditText) findViewById(R.id.search_label_no);
        searchLabelNumberEditTextView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    if(searchLabelNumberEditTextView.getText().length()>0)
                        searchLabel();
                    return true;
                }
                return false;
            }
        });
        searchImageView = (ImageView) findViewById(R.id.search_image_view);

        searchImageView.setOnClickListener(this);
        searchImageView.setVisibility(View.GONE);
        labelNumberTextView = (TextView) findViewById(R.id.label_no);
        oldLabelTextView = (TextView) findViewById(R.id.old_label);
        motherLabelTextView = (TextView) findViewById(R.id.mother_label);
        cnbEntryNoTextView = (TextView) findViewById(R.id.cnb_entry_no);
        cnbTextView = (TextView) findViewById(R.id.cnb_no);
        companyIdTextView = (TextView) findViewById(R.id.company_id);
        logResourceTextView = (TextView) findViewById(R.id.log_resource);
        logpondNameTextView = (TextView) findViewById(R.id.logpond_name);
        logpondPileTextView = (TextView) findViewById(R.id.logpond_pile);
        gradeTextView = (TextView) findViewById(R.id.grade);
        statusTextView = (TextView) findViewById(R.id.status);
        concessionTextView = (TextView) findViewById(R.id.concession);
        kapvalTextView = (TextView) findViewById(R.id.kapvak);
        parcelTextView = (TextView) findViewById(R.id.parcel);
        terrainTextView = (TextView) findViewById(R.id.terrain);
        kapRegNoTextView = (TextView) findViewById(R.id.kap_reg_no);

        speciesTextView = (TextView) findViewById(R.id.species);
        boomNumberTextView = (TextView) findViewById(R.id.boom_number);
        voet1TextView = (TextView) findViewById(R.id.voet_1);
        voet2TextView = (TextView) findViewById(R.id.voet_2);
        top1TextView = (TextView) findViewById(R.id.top_1);
        top2TextView = (TextView) findViewById(R.id.top_2);
        lengthTextView = (TextView) findViewById(R.id.length);
        hTextView = (TextView) findViewById(R.id.h);
        rTextView = (TextView) findViewById(R.id.r);
        commentsTextView = (TextView) findViewById(R.id.comments);
        averageDiametersTextView = (TextView) findViewById(R.id.average_diameters);
        volumeTextView = (TextView) findViewById(R.id.volume);
        exportNoTextView = (TextView) findViewById(R.id.export_no);

        pvNoTextView = (TextView) findViewById(R.id.pv_no);
        inspectionDateTextView = (TextView) findViewById(R.id.inspection_date);
        expiryTextView = (TextView) findViewById(R.id.expiry);
        exportVergunningTextView = (TextView) findViewById(R.id.export_vergunning);
        exportNoTextView = (TextView) findViewById(R.id.export_no);
        licenseNoTextView = (TextView) findViewById(R.id.license_no);
        buyerRejectTextView = (TextView) findViewById(R.id.buyer_reject);
        containerTextView = (TextView) findViewById(R.id.container);
        buyerAllocationTextView = (TextView) findViewById(R.id.buyer_allocation);
    }

    protected void onDestroy(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

    private void killActivity() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


    @Override
    public void onClick(View view) {
        if(view == searchImageView){
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~search");
            searchLabel();
        }
    }

    private void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void searchLabel(){
        hideKeyboard();
        //searchLabelNumber = searchLabelNumberEditTextView.getText().toString().toUpperCase();
        searchLabelNo.setText(viewLabelNumber);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.show();
                        }
                    });

                    final JSONObject jsonObject = SqlDatabase.queryInventoryDetailsByLabelNumber(activity, viewLabelNumber);
                    if(jsonObject.length()>0){
                        /*
                        jsonObject.put("labelNumber", labelNumber);
                        jsonObject.put("speciesCode", speciesCode);
                        jsonObject.put("boomVoet1", boomVoet1);
                        jsonObject.put("boomVoet2", boomVoet2);
                        jsonObject.put("boomTop1", boomTop1);
                        jsonObject.put("boomTop2", boomTop2);
                        jsonObject.put("boomLength", boomLength);
                        jsonObject.put("boomDiameters", boomDiameters);
                        jsonObject.put("boomVolume", boomVolume);
                        jsonObject.put("boomH", boomH);
                        jsonObject.put("boomR", boomR);
                        jsonObject.put("oldLabelNo", oldLabelNo);
                        jsonObject.put("motherLabelNo", motherLabelNo);
                        jsonObject.put("logpondName", logpondName);
                        jsonObject.put("logpondPile", logpondPile);
                        jsonObject.put("logpondGrade", logpondGrade);
                        jsonObject.put("kapregisterNo", kapregisterNo);
                        jsonObject.put("stampTime", stampTime);
                        jsonObject.put("retributionNo", retributionNo);
                        jsonObject.put("concessionName", concessionName);
                        jsonObject.put("kapvakNo", kapvakNo);
                        jsonObject.put("parcelNo", parcelNo);
                        jsonObject.put("coIdNo", coIdNo);
                        jsonObject.put("pvNo", pvNo);
                        jsonObject.put("logResource", logResource);
                        jsonObject.put("buyerReject", buyerReject);
                        jsonObject.put("status", status);
                        */

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    labelNumberTextView.setText(jsonObject.getString("labelNumber"));
                                    oldLabelTextView.setText(jsonObject.getString("oldLabelNo"));
                                    motherLabelTextView.setText(jsonObject.getString("motherLabelNo"));
                                    cnbEntryNoTextView.setText("");
                                    cnbTextView.setText("");
                                    kapRegNoTextView.setText(jsonObject.getString("kapregisterNo"));
                                    companyIdTextView.setText(jsonObject.getString("coIdNo"));
                                    logResourceTextView.setText(jsonObject.getString("logResource"));
                                    logpondNameTextView.setText(jsonObject.getString("logpondName"));
                                    logpondPileTextView.setText(jsonObject.getString("logpondPile"));
                                    gradeTextView.setText(jsonObject.getString("logpondGrade"));
                                    statusTextView.setText(jsonObject.getString("status"));
                                    concessionTextView.setText(jsonObject.getString("concessionName"));
                                    kapvalTextView.setText(jsonObject.getString("kapvakNo"));
                                    parcelTextView.setText(jsonObject.getString("parcelNo"));
                                    terrainTextView.setText("");
                                    pvNoTextView.setText(jsonObject.getString("pvNo"));
                                    speciesTextView.setText(jsonObject.getString("speciesCode"));
                                    boomNumberTextView.setText("");
                                    voet1TextView.setText(jsonObject.getString("boomVoet1"));
                                    voet2TextView.setText(jsonObject.getString("boomVoet2"));
                                    top1TextView.setText(jsonObject.getString("boomTop1"));
                                    top2TextView.setText(jsonObject.getString("boomTop2"));
                                    lengthTextView.setText(jsonObject.getString("boomLength"));
                                    hTextView.setText(jsonObject.getString("boomH"));
                                    rTextView.setText(jsonObject.getString("boomR"));
                                    commentsTextView.setText("");
                                    averageDiametersTextView.setText(jsonObject.getString("boomDiameters"));
                                    volumeTextView.setText(jsonObject.getString("boomVolume"));


                                    pvNoTextView.setText(jsonObject.getString("pvNo"));
                                    inspectionDateTextView.setText(jsonObject.getString("pvInspectionDate"));
                                    expiryTextView.setText(jsonObject.getString("pvExpireDate"));
                                    exportVergunningTextView.setText(jsonObject.getString("exportVergunning"));
                                    exportNoTextView.setText(jsonObject.getString("exportNo"));
                                    licenseNoTextView.setText(jsonObject.getString("licenseNo"));
                                    buyerRejectTextView.setText(jsonObject.getString("buyerReject"));
                                    containerTextView.setText(jsonObject.getString("container"));
                                    buyerAllocationTextView.setText(jsonObject.getString("buyerAllocation"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });

                    }else{
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TTSManager.sayText(ViewLabelDetailActivity.this, getString(R.string.label_no_not_found));

                                final CustomWarningAlertDialog warningAlertDialog = new CustomWarningAlertDialog();

                                View.OnClickListener delete_listener = new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        labelNumberTextView.setText("");
                                        oldLabelTextView.setText("");
                                        motherLabelTextView.setText("");
                                        cnbEntryNoTextView.setText("");
                                        cnbTextView.setText("");
                                        companyIdTextView.setText("");
                                        logResourceTextView.setText("");
                                        logpondNameTextView.setText("");
                                        logpondPileTextView.setText("");
                                        gradeTextView.setText("");
                                        statusTextView.setText("");
                                        concessionTextView.setText("");
                                        kapvalTextView.setText("");
                                        parcelTextView.setText("");
                                        terrainTextView.setText("");
                                        pvNoTextView.setText("");
                                        speciesTextView.setText("");
                                        boomNumberTextView.setText("");
                                        voet1TextView.setText("");
                                        voet2TextView.setText("");
                                        top1TextView.setText("");
                                        top2TextView.setText("");
                                        lengthTextView.setText("");
                                        hTextView.setText("");
                                        rTextView.setText("");
                                        commentsTextView.setText("");
                                        averageDiametersTextView.setText("");
                                        volumeTextView.setText("");
                                        warningAlertDialog.cancel();
                                    }
                                };
                                warningAlertDialog.MyDialogWithYesNo(ViewLabelDetailActivity.this,getString(R.string.search),getString(R.string.label_no_not_found),delete_listener,null);

//                                new AlertDialog.Builder(activity).setCancelable(false)
//                                        .setTitle(getString(R.string.search))
//                                        .setMessage(getString(R.string.label_no_not_found))
//                                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//
//                                            public void onClick(DialogInterface dialog, int whichButton) {
//                                                labelNumberTextView.setText("");
//                                                oldLabelTextView.setText("");
//                                                motherLabelTextView.setText("");
//                                                cnbEntryNoTextView.setText("");
//                                                cnbTextView.setText("");
//                                                companyIdTextView.setText("");
//                                                logResourceTextView.setText("");
//                                                logpondNameTextView.setText("");
//                                                logpondPileTextView.setText("");
//                                                gradeTextView.setText("");
//                                                statusTextView.setText("");
//                                                concessionTextView.setText("");
//                                                kapvalTextView.setText("");
//                                                parcelTextView.setText("");
//                                                terrainTextView.setText("");
//                                                pvNoTextView.setText("");
//                                                speciesTextView.setText("");
//                                                boomNumberTextView.setText("");
//                                                voet1TextView.setText("");
//                                                voet2TextView.setText("");
//                                                top1TextView.setText("");
//                                                top2TextView.setText("");
//                                                lengthTextView.setText("");
//                                                hTextView.setText("");
//                                                rTextView.setText("");
//                                                commentsTextView.setText("");
//                                                averageDiametersTextView.setText("");
//                                                volumeTextView.setText("");
//                                            }
//                                        }).show();
                            }
                        });

                        //.setNegativeButton(getString(R.string.no), null).show();
                    }

                } catch (Exception e) {

                    e.printStackTrace();
                }finally {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        }).start();

    }
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SCANNER_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                String result = data.getStringExtra("CODE");

            }else if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }
}
