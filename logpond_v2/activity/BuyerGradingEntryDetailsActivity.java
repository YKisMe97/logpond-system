package com.infocomm.logpond_v2.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.infocomm.logpond_v2.R;
import com.infocomm.logpond_v2.storage.SharedPreferencesStorage;
import com.infocomm.logpond_v2.storage.SqlDatabase;
import com.infocomm.logpond_v2.util.FileManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class BuyerGradingEntryDetailsActivity extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener, AdapterView.OnItemSelectedListener {

    private final int ENTRY_LIST_REQUEST_CODE = 99;
    private BuyerGradingEntryDetailsActivity activity;
    private EditText hEditText, rEditText, minudDEditText, lcEditText, commentEditText;
    private Spinner gradeSpinner;
    private TextView speciesTextView, voet1TextView, voet2TextView, top1TextView, top2TextView, lengthTextView, averageDiametersTextView, volumeTextView;
    private View nextLayout;
    private String[] dataArray, labelEntryArray, gradeArray;
    private JSONObject inventoryJsonObject;
    private int position;
    private boolean isEditable;
    private DecimalFormat decimalFormat;
    private String[] labelEntryPropArray;
    private String labelNumber;
    private boolean isSave;
    private JSONObject detailsJsonObject;
    private int buyerGradingPosition;
    private ArrayList labelEntryArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_buyer_grading_entry_details);

        activity = this;
        decimalFormat = new DecimalFormat("#0.000");
        isSave = false;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitle(getString(R.string.buyer_grading1));

        if(getIntent().hasExtra("labelNumber") && getIntent().getStringExtra("labelNumber")!=null){
            labelNumber = getIntent().getStringExtra("labelNumber");
        }else{
            labelNumber = "";
        }
        isEditable = getIntent().getBooleanExtra("isEditable", true);
        position = getIntent().getIntExtra("position", -1);

        if(getIntent().hasExtra("detailsJsonObject") && getIntent().getStringExtra("detailsJsonObject")!=null){
            try {
                detailsJsonObject = new JSONObject(getIntent().getStringExtra("detailsJsonObject"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        buyerGradingPosition = getIntent().getIntExtra("buyerGradingPosition", -1);
        if(getIntent().hasExtra("labelEntryArrayList") && getIntent().getSerializableExtra("labelEntryArrayList")!=null){
            labelEntryArrayList = (ArrayList) getIntent().getSerializableExtra("labelEntryArrayList");
        }

        hEditText = (EditText) findViewById(R.id.h);
        hEditText.setEnabled(isEditable);
        hEditText.setOnFocusChangeListener(this);
        rEditText = (EditText) findViewById(R.id.r);
        rEditText.setEnabled(isEditable);
        rEditText.setOnFocusChangeListener(this);
        minudDEditText = (EditText) findViewById(R.id.negative_d);
        minudDEditText.setEnabled(isEditable);
        minudDEditText.setOnFocusChangeListener(this);
        lcEditText = (EditText) findViewById(R.id.lc);
        lcEditText.setEnabled(isEditable);
        lcEditText.setOnFocusChangeListener(this);
        commentEditText = (EditText) findViewById(R.id.comments);
        commentEditText.setEnabled(isEditable);
        commentEditText.setOnFocusChangeListener(this);

        gradeArray = getResources().getStringArray(R.array.buyer_grade_array);
        ArrayAdapter<String> logpondAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, gradeArray);
        logpondAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradeSpinner = (Spinner) findViewById(R.id.grade_spinner);
        gradeSpinner.setAdapter(logpondAdapter);
        gradeSpinner.setOnItemSelectedListener(this);
        gradeSpinner.setEnabled(isEditable);

        speciesTextView = (TextView) findViewById(R.id.species);
        voet1TextView = (TextView) findViewById(R.id.voet_1);
        voet2TextView = (TextView) findViewById(R.id.voet_2);
        top1TextView = (TextView) findViewById(R.id.top_1);
        top2TextView = (TextView) findViewById(R.id.top_2);
        lengthTextView = (TextView) findViewById(R.id.length);
        averageDiametersTextView = (TextView) findViewById(R.id.average_diameters);
        volumeTextView = (TextView) findViewById(R.id.volume);

        nextLayout = findViewById(R.id.next_layout);
        nextLayout.setOnClickListener(this);
        nextLayout.setVisibility(isEditable? View.VISIBLE:View.GONE);

        labelEntryPropArray = labelNumber.split(Pattern.quote("^"), -1);
        if(labelEntryPropArray!=null && labelEntryPropArray.length>5){
            toolbar.setTitle(labelEntryPropArray[0]);
            inventoryJsonObject = new JSONObject();
            inventoryJsonObject = SqlDatabase.queryInventoryDetailsByLabelNumber(activity, labelEntryPropArray[0]);
            toolbar.setTitle(labelEntryPropArray[0]);

            try {
                if(!inventoryJsonObject.isNull("speciesCode"))
                    speciesTextView.setText(inventoryJsonObject.getString("speciesCode"));

                if(!inventoryJsonObject.isNull("boomVoet1"))
                    voet1TextView.setText(inventoryJsonObject.getString("boomVoet1"));

                if(!inventoryJsonObject.isNull("boomVoet2"))
                    voet2TextView.setText(inventoryJsonObject.getString("boomVoet2"));

                if(!inventoryJsonObject.isNull("boomTop1"))
                    top1TextView.setText(inventoryJsonObject.getString("boomTop1"));

                if(!inventoryJsonObject.isNull("boomTop2"))
                    top2TextView.setText(inventoryJsonObject.getString("boomTop2"));

                if(!inventoryJsonObject.isNull("boomLength"))
                    lengthTextView.setText(inventoryJsonObject.getString("boomLength"));

                if(!inventoryJsonObject.isNull("boomDiameters"))
                    averageDiametersTextView.setText(inventoryJsonObject.getString("boomDiameters"));

                if(!inventoryJsonObject.isNull("boomVolume"))
                    volumeTextView.setText(inventoryJsonObject.getString("boomVolume"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try{
                if(!labelEntryPropArray[1].equalsIgnoreCase("0.000"))
                    hEditText.setText(decimalFormat.format(Double.parseDouble(labelEntryPropArray[1])));
            }catch (Exception e){
                hEditText.setText("");
            }
            try{
                if(!labelEntryPropArray[2].equalsIgnoreCase("0.000"))
                    rEditText.setText(decimalFormat.format(Double.parseDouble(labelEntryPropArray[2])));
            }catch (Exception e){
                rEditText.setText("");
            }
            try{
                if(!labelEntryPropArray[3].equalsIgnoreCase("0.000"))
                    minudDEditText.setText(decimalFormat.format(Double.parseDouble(labelEntryPropArray[3])));
            }catch (Exception e){
                minudDEditText.setText("");
            }
            try{
                if(!labelEntryPropArray[4].equalsIgnoreCase("0.000"))
                    lcEditText.setText(decimalFormat.format(Double.parseDouble(labelEntryPropArray[4])));
            }catch (Exception e){
                lcEditText.setText("");
            }
            try{
                commentEditText.setText(labelEntryPropArray[5].equals("%20")? "":labelEntryPropArray[5]);
            }catch (Exception e){
                commentEditText.setText("");
            }

            for(int i=0;i<gradeArray.length;i++){
                if(gradeArray[i].equalsIgnoreCase(labelEntryPropArray[6])){
                    gradeSpinner.setSelection(i);
                    break;
                }
            }
        }
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

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onFocusChange(View view, boolean focus) {
        saveTempFile();
        if(view == hEditText){
            if(!focus){
                try {
                    if(hEditText.length()>0)
                        hEditText.setText(decimalFormat.format(Double.parseDouble(hEditText.getText().toString())));
                }catch (Exception e){
                    hEditText.setText("");
                }

            }
        }else if(view == rEditText){
            if(!focus){
                try {
                    if(rEditText.length()>0)
                        rEditText.setText(decimalFormat.format(Double.parseDouble(rEditText.getText().toString())));
                }catch (Exception e){
                    rEditText.setText("");
                }

            }
        }else if(view == minudDEditText){
            if(!focus){
                try {
                    if(minudDEditText.length()>0)
                        minudDEditText.setText(decimalFormat.format(Double.parseDouble(minudDEditText.getText().toString())));
                }catch (Exception e){
                    minudDEditText.setText("");
                }

            }
        }else if(view == lcEditText){
            if(!focus){
                try {
                    if(lcEditText.length()>0)
                        lcEditText.setText(decimalFormat.format(Double.parseDouble(lcEditText.getText().toString())));
                }catch (Exception e){
                    lcEditText.setText("");
                }

            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view==nextLayout){
            isSave = true;
            hideKeyboard();
            killActivity();
        }
    }

    private void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void killActivity() {
        if (isSave) {
            StringBuffer sb = new StringBuffer();
            if(labelEntryPropArray!=null && labelEntryPropArray.length>0){
                String h = hEditText.getText().toString();
                if(h.length()==0) h = "0.000";

                String r = rEditText.getText().toString();
                if(r.length()==0) r = "0.000";

                String d = minudDEditText.getText().toString();
                if(d.length()==0) d = "0.000";

                String l = lcEditText.getText().toString();
                if(l.length()==0) l = "0.000";

                String comment = commentEditText.getText().toString();
                if(comment.length()==0) comment = "%20";

                sb.append(labelEntryPropArray[0] + "^");
                sb.append(h + "^");
                sb.append(r + "^");
                sb.append(d + "^");
                sb.append(l + "^");
                sb.append(comment + "^");
                sb.append(gradeArray[gradeSpinner.getSelectedItemPosition()] + "^");
                Intent resultIntent = new Intent();
                resultIntent.putExtra("action", "saved");
                resultIntent.putExtra("position", position);
                resultIntent.putExtra("labelNumber", sb.toString());
                setResult(Activity.RESULT_OK, resultIntent);
            }

        }
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void saveTempFile(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 0 - Entry No
                    // 1 - Entry Date
                    // 2 - Customer
                    // 3 - Logpond Name
                    // 4 - User Account
                    // 5 - Total label

                    if(labelEntryPropArray!=null){
                        StringBuffer sb = new StringBuffer();
                        sb.append(buyerGradingPosition + "\r\n");
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("customer") + "\r\n");
                        sb.append(detailsJsonObject.getString("logpond") + "\r\n");
                        sb.append(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "\r\n");
                        sb.append(labelEntryArrayList.size() + "\r\n");

                        for(int i=0;i<labelEntryArrayList.size();i++){
                            //sb.append(labelEntryArrayList.get(i) + "^" + detailsJsonObject.getString("grade") + "^" + "\r\n");
                            if(i==position && labelEntryPropArray!=null){
                                StringBuffer labelEntrySB = new StringBuffer();
                                labelEntrySB.append(labelEntryPropArray[0] + "^");
                                labelEntrySB.append(hEditText.getText().toString() + "^");
                                labelEntrySB.append(rEditText.getText().toString() + "^");
                                labelEntrySB.append(minudDEditText.getText().toString() + "^");
                                labelEntrySB.append(lcEditText.getText().toString() + "^");
                                labelEntrySB.append(commentEditText.getText().toString() + "^");
                                labelEntrySB.append(gradeArray[gradeSpinner.getSelectedItemPosition()] + "^");

                                sb.append(labelEntrySB.toString() + "\r\n");

                            }else{
                                sb.append(labelEntryArrayList.get(i) + "\r\n");
                            }

                        }

                        FileManager.saveTempFile(activity, "BUYER_GRADING.TXT", sb.toString());
                    }

                } catch (JSONException e) {

                }finally {

                }
            }
        }).start();

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        saveTempFile();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
