package com.infocomm.logpond_v2.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.infocomm.logpond_v2.R;
import com.infocomm.logpond_v2.storage.SharedPreferencesStorage;
import com.infocomm.logpond_v2.storage.SqlDatabase;
import com.infocomm.logpond_v2.util.FileManager;
import com.infocomm.logpond_v2.view.CustomAlertDialog;
import com.infocomm.logpond_v2.view.CustomWarningAlertDialog;
import com.infocomm.logpond_v2.view.MySnackBar;
import com.infocomm.logpond_v2.view.MyToast;
import com.infocomm.logpond_v2.view.TTSManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class LogpondOutEntryListActivity extends AppCompatActivity implements View.OnClickListener {

    private final int SCANNER_REQUEST_CODE = 99;
    private LogpondOutEntryListActivity activity;
    private RecyclerView recyclerView;
    private CustomAdapter adapter;
    private ImageView scanImageView;
    private EditText labelNumberEditTextView;
    private View saveView, searchView, nextView;
    private JSONObject detailsJsonObject;
    private ArrayList labelEntryArrayList;
    private String[] labelEntryArray;
    private final int MAX_ENTRY = 100;
    private boolean isModified, isSave;
    private int leftCount;
    private JSONObject fileJsonObject;
    public String type;
    private ProgressDialog progressDialog;
    private boolean isEditable;
    private int position;
    private Toolbar toolbar;
    private TextView title, subtitle, entryno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_entry_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setLogo(R.drawable.forest);
        title = findViewById(R.id.toolbar_title);
        title.setText(getResources().getString(R.string.logpond_out));
        subtitle = findViewById(R.id.entry_list_subtitle);
        entryno = findViewById(R.id.entry_list_toolbar_entry_no);
        entryno.setText("Entry No: " + getIntent().getExtras().getString("entryno"));

        position = getIntent().getIntExtra("position", -1);
        activity = this;
        fileJsonObject = new JSONObject();
        //jsonArray = new JSONArray();
        isModified = false;
        isSave = false;
        type = "";
        isEditable = getIntent().getBooleanExtra("isEditable", true);
        if(getIntent().hasExtra("type") && getIntent().getStringExtra("type") != null) type = getIntent().getStringExtra("type");
        labelEntryArrayList = new ArrayList();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new CustomAdapter(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        searchView = findViewById(R.id.search_layout);
        saveView = findViewById(R.id.save_layout);
        saveView.setOnClickListener(this);
        nextView = findViewById(R.id.next_layout);
        nextView.setOnClickListener(this);
        if(!isEditable){
            saveView.setVisibility(View.GONE);
            nextView.setVisibility(View.GONE);
            searchView.setVisibility(View.GONE);
        }

        labelNumberEditTextView = (EditText) findViewById(R.id.label_no);
        labelNumberEditTextView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    if(labelNumberEditTextView.getText().length()>0)
                        addLabelNumber(labelNumberEditTextView.getText().toString());
                    return true;
                }
                return false;
            }
        });
        scanImageView = (ImageView) findViewById(R.id.barcode_icon);
        scanImageView.setOnClickListener(this);

        leftCount = 0;
        if(getIntent().hasExtra("labelEntryArray") && getIntent().getStringArrayExtra("labelEntryArray")!=null){
            labelEntryArray = getIntent().getStringArrayExtra("labelEntryArray");
            if(labelEntryArray!=null && labelEntryArray.length>0){
                Collections.addAll(labelEntryArrayList, labelEntryArray);
            }
        }

        // Prevent empty list row
        for(int i=0;i<labelEntryArrayList.size();i++){
            String existingLabelNumber = (String) labelEntryArrayList.get(i);
            if(existingLabelNumber.trim().length()==0){
                labelEntryArrayList.remove(i);
                i = i - 1;
            }
        }

        leftCount = MAX_ENTRY - labelEntryArrayList.size();

        if(getIntent().hasExtra("details") && getIntent().getStringExtra("details")!=null){
            try {
                detailsJsonObject = new JSONObject( getIntent().getStringExtra("details"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setIndeterminate(true);

        subtitle.setText(getString(R.string.entries_pcs).replace("|COUNT|", String.valueOf(labelEntryArrayList.size())));
        //toolbar.setSubtitle(getString(R.string.entries_pcs).replace("|COUNT|", String.valueOf(labelEntryArrayList.size())));
    }

    protected void onDestroy(){
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
                //killActivity();
                showExitSaveDialog();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        showExitSaveDialog();
        /*
        if(isModified){
            new AlertDialog.Builder(LogpondOutEntryListActivity.this)
                    .setTitle(getString(R.string.app_name))
                    .setMessage(getString(R.string.do_you_want_to_save))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            saveFile();
                        }
                    })
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            killActivity();
                        }
                    }).show();
        }else{
            killActivity();
        }
        */

    }

    private void killActivity() {
        Intent resultIntent = new Intent();
        if (isSave) {
            resultIntent.putExtra("action", "saved");
            resultIntent.putExtra("fileJsonObjectString", fileJsonObject.toString());
        }
        resultIntent.putExtra("labelEntryArray", labelEntryArrayList.toArray(new String[labelEntryArrayList.size()]));
        resultIntent.putExtra("totalEntries", labelEntryArrayList.size());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showExitSaveDialog(){
        if(isEditable){
            CustomAlertDialog downloadAlertDialogs = new CustomAlertDialog();
            View.OnClickListener save_listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveFile();
//                    killActivity();
                }
            };

            View.OnClickListener kill_listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    killActivity();
                }
            };
            downloadAlertDialogs.MyDialogWithYesNo(LogpondOutEntryListActivity.this,getString(R.string.app_name), getString(R.string.do_you_want_to_save),save_listener,kill_listener);
        }else{
            killActivity();
        }

    }

    private void saveFile(){
        // Prevent empty list row
        for(int i=0;i<labelEntryArrayList.size();i++){
            String existingLabelNumber = (String) labelEntryArrayList.get(i);
            if(existingLabelNumber.trim().length()==0){
                labelEntryArrayList.remove(i);
                i = i - 1;
            }
        }

        hideKeyboard(this.getCurrentFocus());
        if(labelNumberEditTextView.getText().length()>0)
            addLabelNumber(labelNumberEditTextView.getText().toString());

        if(labelEntryArrayList.size()==0){
            MySnackBar.showError(getCurrentFocus(), getString(R.string.please_add_at_least_one_label_before_save), Snackbar.LENGTH_LONG);
        }else{
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
                        StringBuffer sb = new StringBuffer();
                        if (detailsJsonObject.getString("action").equals("Trucking")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append(detailsJsonObject.getString("vvbNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("fromLogpond") + "\r\n");
                            sb.append(detailsJsonObject.getString("toLogpond") + "\r\n");
                            sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                            sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                        }
                        else if (detailsJsonObject.getString("action").equals("Local Sales")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append(detailsJsonObject.getString("vvbNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                            sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("customer") + "\r\n");
                        }
                        else if (detailsJsonObject.getString("action").equals("Export Vessel")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("vesselNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("hatchNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("customer") + "\r\n");
                        }
                        else if (detailsJsonObject.getString("action").equals("Export Container")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("containerNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("serialNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("customer") + "\r\n");
                        }
                        else if (detailsJsonObject.getString("action").equals("Peddling")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append(detailsJsonObject.getString("pile") + "\r\n");
                            sb.append(detailsJsonObject.getString("fromLogpond") + "\r\n");
                            sb.append(detailsJsonObject.getString("toLogpond") + "\r\n");
                            sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                            sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                        }
                        else if (detailsJsonObject.getString("action").equals("Sawmill")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append(detailsJsonObject.getString("vvbNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                            sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("sawmill") + "\r\n");
                        }
                        else if (detailsJsonObject.getString("action").equals("Camp Use")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append(detailsJsonObject.getString("vvbNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                            sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("campUse") + "\r\n");
                        }
                        else if (detailsJsonObject.getString("action").equals("Barging")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append(detailsJsonObject.getString("bargeNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("fromLogpond") + "\r\n");
                            sb.append(detailsJsonObject.getString("toLogpond") + "\r\n");
                            sb.append(detailsJsonObject.getString("operator") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("hatchNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                        }
                        else if (detailsJsonObject.getString("action").equals("Export STS")) {
                            sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("date") + "\r\n");
                            sb.append(detailsJsonObject.getString("action") + "\r\n");
                            sb.append(detailsJsonObject.getString("bargeNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("operator") + "\r\n");
                            sb.append(detailsJsonObject.getString("hatchNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("vesselNo") + "\r\n");
                            sb.append(detailsJsonObject.getString("motherHatchNo") + "\r\n");
                            sb.append("\r\n");
                            sb.append("\r\n");
                            sb.append(detailsJsonObject.getString("customer") + "\r\n");
                        }
                        sb.append(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "\r\n");
                        sb.append(labelEntryArrayList.size() + "\r\n");

                        for(int i=0;i<labelEntryArrayList.size();i++){
                            sb.append(labelEntryArrayList.get(i) + "\r\n");
                        }

                        String fileName = SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "_" + detailsJsonObject.getString("entryNo") + ".TXT";
                        fileJsonObject = FileManager.saveFile(activity, "export_logpond_out", fileName.toUpperCase(), sb.toString());
                        isSave = true;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                        killActivity();
                    } catch (JSONException e) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                        e.printStackTrace();
                    }finally {

                    }
                }
            }).start();
        }
    }

    @Override
    public void onClick(View view) {
        if(view==scanImageView){
            Intent scannerIntent = new Intent(this, QRCodeScannerActivity.class);
            startActivityForResult(scannerIntent, SCANNER_REQUEST_CODE);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }else if(view == saveView){
            CustomAlertDialog saveAlertDialogs = new CustomAlertDialog();
            View.OnClickListener save_listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveFile();
                }
            };
            saveAlertDialogs.MyDialogWithYesNo(LogpondOutEntryListActivity.this, getString(R.string.app_name), getString(R.string.do_you_want_to_save),save_listener,null);

        }else if(view==nextView){
            if(labelNumberEditTextView.getText().length()>0)
                addLabelNumber(labelNumberEditTextView.getText().toString());
        }
    }

    public class CustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private static final int VIEW_TYPE_HEADER = 0;
        private static final int VIEW_TYPE_ITEM = 1;
        private View headerView;

        public CustomAdapter(View headerView) {
            this.headerView = headerView;
            if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {

            }
        }

        public void setLoaded() {

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_HEADER) {
                return new HeaderViewHolder(headerView);
            } else {
                final View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_list_row, parent, false);
                ItemViewHolders itemViewHolders = new ItemViewHolders(layoutView);
                return itemViewHolders;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ItemViewHolders) {
                try {
                    if (headerView != null) {
                        position = position -1;
                    }
                    String labelNumber = (String) labelEntryArrayList.get(position);
                    JSONObject jsonObject = SqlDatabase.queryInventoryDetailsByLabelNumber(activity, labelNumber);
                    System.out.println("~~~~~~~~~~~~~~~~~~~jsonObject= " + jsonObject);
                    ((ItemViewHolders) holder).noTextView.setText(String.valueOf(position + 1) + ". ");
                    ((CustomAdapter.ItemViewHolders) holder).labelNoTextView.setText(labelNumber);
                    ((ItemViewHolders) holder).speciesTextView.setText((jsonObject.isNull("speciesCode")||jsonObject.getString("speciesCode").length()==0? "":jsonObject.getString("speciesCode")));
                    ((CustomAdapter.ItemViewHolders) holder).allocationTextView.setText((jsonObject.isNull("buyerAllocation")? "":jsonObject.getString("buyerAllocation")));
                    ((CustomAdapter.ItemViewHolders) holder).containerTextView.setText((jsonObject.isNull("container")||jsonObject.getString("container").length()==0? getString(R.string.no).toUpperCase():jsonObject.getString("container")));
                    ((CustomAdapter.ItemViewHolders) holder).rejectCodeTextView.setText((jsonObject.isNull("buyerReject")||jsonObject.getString("buyerReject").length()==0? getString(R.string.no).toUpperCase():jsonObject.getString("buyerReject")));
                    ((CustomAdapter.ItemViewHolders) holder).pvNoTextView.setText((jsonObject.isNull("pvNo")||jsonObject.getString("pvNo").length()==0? getString(R.string.no).toUpperCase():jsonObject.getString("pvNo")));
                    ((CustomAdapter.ItemViewHolders) holder).licenseNoTextView.setText((jsonObject.isNull("licenseNo")||jsonObject.getString("licenseNo").length()==0? getString(R.string.no).toUpperCase():jsonObject.getString("licenseNo")));
                    ((CustomAdapter.ItemViewHolders) holder).kapRegNoTextView.setText((jsonObject.isNull("kapregisterNo")||jsonObject.getString("kapregisterNo").length()==0? getString(R.string.no).toUpperCase():jsonObject.getString("kapregisterNo")));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(jsonObject.isNull("buyerReject") || jsonObject.getString("buyerReject").length()==0 || jsonObject.getString("buyerReject").equalsIgnoreCase("NO")) {
                            if(!jsonObject.isNull("container") && jsonObject.getString("container").equalsIgnoreCase("YES")){
                                ((ItemViewHolders) holder).itemView.setBackgroundColor(getResources().getColor(R.color.lightGreen, null));
                            }else{
                                ((ItemViewHolders) holder).itemView.setBackgroundColor(Color.TRANSPARENT);
                            }
                            //((ItemViewHolders) holder).rejectCodeTextView.setText("");
                        }else {
                            ((ItemViewHolders) holder).itemView.setBackgroundColor(getResources().getColor(R.color.lightRed, null));
                            //((ItemViewHolders) holder).rejectCodeTextView.setText(getString(R.string.buyer_reject));
                        }
                    }else{
                        if(jsonObject.isNull("buyerReject") || jsonObject.getString("buyerReject").length()==0 || jsonObject.getString("buyerReject").equalsIgnoreCase("NO")) {
                            if(!jsonObject.isNull("container") && jsonObject.getString("container").equalsIgnoreCase("YES")){
                                ((ItemViewHolders) holder).itemView.setBackgroundColor(getResources().getColor(R.color.lightGreen));
                            }else{
                                ((ItemViewHolders) holder).itemView.setBackgroundColor(Color.TRANSPARENT);
                            }
                            //((ItemViewHolders) holder).rejectCodeTextView.setText("");
                        }else {
                            ((ItemViewHolders) holder).itemView.setBackgroundColor(getResources().getColor(R.color.lightRed));
                            //((ItemViewHolders) holder).rejectCodeTextView.setText(getString(R.string.buyer_reject));
                        }
                    }
                    //((ItemViewHolders) holder).rejectCodeTextView.setText(getString(R.string.reject_code) + " : " + "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public int getItemCount() {
            return labelEntryArrayList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (headerView == null) {
                return VIEW_TYPE_ITEM;
            }
            return (position == 0) ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
        }

        public class HeaderViewHolder extends RecyclerView.ViewHolder {
            public HeaderViewHolder(View view) {
                super(view);
            }
        }

        public class ItemViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

            public TextView speciesTextView, noTextView,labelNoTextView, allocationTextView, containerTextView, rejectCodeTextView, pvNoTextView, licenseNoTextView, kapRegNoTextView;
            private View itemView, binImgeView, more_detail_itemView;

            public ItemViewHolders(View itemView) {
                super(itemView);
                this.itemView = itemView;
                itemView.setClickable(true);
                itemView.setOnClickListener(this);
                binImgeView = itemView.findViewById(R.id.bin);
                binImgeView.setOnClickListener(this);
                if(!isEditable) binImgeView.setVisibility(View.INVISIBLE);
                speciesTextView = itemView.findViewById(R.id.species_code);
                noTextView = (TextView) itemView.findViewById(R.id.no);
                labelNoTextView = (TextView) itemView.findViewById(R.id.label_no);
                allocationTextView = (TextView) itemView.findViewById(R.id.allocation);
                containerTextView = (TextView) itemView.findViewById(R.id.container);
                pvNoTextView = (TextView) itemView.findViewById(R.id.pv_no);
                licenseNoTextView = (TextView) itemView.findViewById(R.id.license_no);
                rejectCodeTextView = (TextView) itemView.findViewById(R.id.reject_code);
                kapRegNoTextView = (TextView) itemView.findViewById(R.id.kap_reg_no);
                more_detail_itemView = itemView.findViewById(R.id.more_label_detail);
                more_detail_itemView.setClickable(true);
                more_detail_itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if(v == binImgeView){
                    final CustomAlertDialog deleteAlertDialogs = new CustomAlertDialog();
                    View.OnClickListener delete_listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            isModified = true;
                            labelEntryArrayList.remove(getAdapterPosition());
                            adapter.notifyDataSetChanged();
                            subtitle.setText(getString(R.string.entries_pcs).replace("|COUNT|", String.valueOf(labelEntryArrayList.size())));
                            //toolbar.setSubtitle(getString(R.string.entries_pcs).replace("|COUNT|", String.valueOf(labelEntryArrayList.size())));
                            saveTempFile();
                            deleteAlertDialogs.cancel();
                        }
                    };
                    deleteAlertDialogs.MyDialogWithYesNo(LogpondOutEntryListActivity.this,getString(R.string.delete), getString(R.string.are_you_sure_to_delete),delete_listener,null);
                }
                else if (v == more_detail_itemView){
                    String labelNumber = (String) labelEntryArrayList.get(getAdapterPosition());
                    Intent entryDetailsIntent = new Intent(LogpondOutEntryListActivity.this, ViewLabelDetailActivity.class);
                    entryDetailsIntent.putExtra("viewLabelNumber", labelNumber);
                    startActivity(entryDetailsIntent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        }
    }

    public void hideKeyboard(View view) {
        if (view!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addLabelNumber(String labelNumber){
        isModified = true;
        //hideKeyboard(this.getCurrentFocus());
        labelNumberEditTextView.setText("");
        if(labelNumber.trim().length()<7){
            //MyToast.show(activity, getString(R.string.less_than_7_digits_error), Toast.LENGTH_LONG);
            final CustomWarningAlertDialog deleteAlertDialogs = new CustomWarningAlertDialog();
            View.OnClickListener delete_listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteAlertDialogs.cancel();
                }
            };
            deleteAlertDialogs.MyDialogWithYesNo(LogpondOutEntryListActivity.this,"Warning!", getString(R.string.less_than_7_digits_error),delete_listener,null);
            return;
        }
        labelNumber = labelNumber.toUpperCase();
        //if(!labelNumber.startsWith("NA-")) labelNumber = "NA-" + labelNumber;
        //boolean isExist = false;

        for(int i=0;i<labelEntryArrayList.size();i++){
            if(((String)labelEntryArrayList.get(i)).equalsIgnoreCase(labelNumber)){
                MyToast.show(activity, getString(R.string.duplicate_label), Toast.LENGTH_LONG);
                return;
            }
        }

        int leftCount = MAX_ENTRY - labelEntryArrayList.size();
        if(leftCount<=0){
            //MySnackBar.showError(getCurrentFocus(), getString(R.string.entry_full_error), Snackbar.LENGTH_LONG);
            String message = getString(R.string.you_have_count_entry_left);
            message = message.replace("|COUNT|", String.valueOf(leftCount));
            TTSManager.sayText(activity, message);
            final CustomAlertDialog deleteAlertDialogs = new CustomAlertDialog();
            View.OnClickListener delete_listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveFile();
                }
            };
            deleteAlertDialogs.MyDialogWithYesNo(LogpondOutEntryListActivity.this,getString(R.string.logpond_out), getString(R.string.you_have_reach_maximum_entry)+"\n"+getString(R.string.save_and_create_a_new_record),delete_listener,null);
            return;
        }

        labelEntryArrayList.add(0, labelNumber);
        //jsonArray.put(jsonObject);
        adapter.notifyDataSetChanged();
        subtitle.setText(getString(R.string.entries_pcs).replace("|COUNT|", String.valueOf(labelEntryArrayList.size())));
        //toolbar.setSubtitle(getString(R.string.entries_pcs).replace("|COUNT|", String.valueOf(labelEntryArrayList.size())));
        if(labelEntryArrayList.size()>0)
            recyclerView.scrollToPosition(0);

        saveTempFile();
        leftCount = MAX_ENTRY - labelEntryArrayList.size();
        if(leftCount<=0){
            //MySnackBar.showError(getCurrentFocus(), getString(R.string.entry_full_error), Snackbar.LENGTH_LONG);
            String message = getString(R.string.you_have_count_entry_left);
            message = message.replace("|COUNT|", String.valueOf(leftCount));
            TTSManager.sayText(activity, message);
            final CustomAlertDialog deleteAlertDialogs = new CustomAlertDialog();
            View.OnClickListener delete_listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveFile();
                }
            };
            deleteAlertDialogs.MyDialogWithYesNo(LogpondOutEntryListActivity.this,getString(R.string.logpond_out), getString(R.string.you_have_reach_maximum_entry)+"\n"+getString(R.string.save_and_create_a_new_record),delete_listener,null);
        }else if((leftCount <= 2)){
            String message = getString(R.string.you_have_count_entry_left);
            message = message.replace("|COUNT|", String.valueOf(leftCount));
            TTSManager.sayText(activity, message);
            MyToast.show(activity, message, Toast.LENGTH_LONG);
            //MySnackBar.showError(getCurrentFocus(), message, Snackbar.LENGTH_LONG);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SCANNER_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                String result = data.getStringExtra("CODE");
                addLabelNumber(result);
            }else if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private void saveTempFile(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuffer sb = new StringBuffer();
                    String logpond_out_action = detailsJsonObject.getString("action");
                    sb.append(position + "\r\n");
                    if (detailsJsonObject.getString("action").equals("Trucking")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append(detailsJsonObject.getString("vvbNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("fromLogpond") + "\r\n");
                        sb.append(detailsJsonObject.getString("toLogpond") + "\r\n");
                        sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                        sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                    }
                    else if (detailsJsonObject.getString("action").equals("Local Sales")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append(detailsJsonObject.getString("vvbNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                        sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("customer") + "\r\n");
                    }
                    else if (detailsJsonObject.getString("action").equals("Export Vessel")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("vesselNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("hatchNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("customer") + "\r\n");
                    }
                    else if (detailsJsonObject.getString("action").equals("Export Container")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("containerNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("serialNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("customer") + "\r\n");
                    }
                    else if (detailsJsonObject.getString("action").equals("Peddling")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append(detailsJsonObject.getString("pile") + "\r\n");
                        sb.append(detailsJsonObject.getString("fromLogpond") + "\r\n");
                        sb.append(detailsJsonObject.getString("toLogpond") + "\r\n");
                        sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                        sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                    }
                    else if (detailsJsonObject.getString("action").equals("Sawmill")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append(detailsJsonObject.getString("vvbNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                        sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("sawmill") + "\r\n");
                    }
                    else if (detailsJsonObject.getString("action").equals("Camp Use")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append(detailsJsonObject.getString("vvbNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("driverName") + "\r\n");
                        sb.append(detailsJsonObject.getString("plateNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("campUse") + "\r\n");
                    }
                    else if (detailsJsonObject.getString("action").equals("Barging")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append(detailsJsonObject.getString("bargeNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("fromLogpond") + "\r\n");
                        sb.append(detailsJsonObject.getString("toLogpond") + "\r\n");
                        sb.append(detailsJsonObject.getString("operator") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("hatchNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                    }
                    else if (detailsJsonObject.getString("action").equals("Export STS")) {
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("action") + "\r\n");
                        sb.append(detailsJsonObject.getString("bargeNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("operator") + "\r\n");
                        sb.append(detailsJsonObject.getString("hatchNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("vesselNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("motherHatchNo") + "\r\n");
                        sb.append("\r\n");
                        sb.append("\r\n");
                        sb.append(detailsJsonObject.getString("customer") + "\r\n");
                    }
                    sb.append(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "\r\n");
                    sb.append(labelEntryArrayList.size() + "\r\n");

                    for(int i=0;i<labelEntryArrayList.size();i++){
                        sb.append(labelEntryArrayList.get(i) + "\r\n");
                    }
                    FileManager.saveTempFile(activity, "LOGPOND_OUT.TXT", sb.toString());
                } catch (JSONException e) {

                }finally {

                }
            }
        }).start();
    }
}
