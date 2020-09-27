package com.infocomm.logpond_v2.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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

import com.infocomm.logpond_v2.R;
import com.infocomm.logpond_v2.storage.SharedPreferencesStorage;
import com.infocomm.logpond_v2.storage.SqlDatabase;
import com.infocomm.logpond_v2.util.FileManager;
import com.infocomm.logpond_v2.view.CustomAlertDialog;
import com.infocomm.logpond_v2.view.CustomWarningAlertDialog;
import com.infocomm.logpond_v2.view.MySnackBar;
import com.infocomm.logpond_v2.view.TTSManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

public class BuyerGradingEntryListActivity extends AppCompatActivity implements View.OnClickListener {

    private final int SCANNER_REQUEST_CODE = 99;
    private final int ENTRY_DETAILS_REQUEST_CODE = 100;
    private BuyerGradingEntryListActivity activity;
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
    private TextView subtitle, entryno, title;

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
        title.setText(getResources().getString(R.string.buyer_grading1));
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
                        addLabelNumber(labelNumberEditTextView.getText().toString(), false);
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
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
        hideKeyboard();
    }

    private void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
            new AlertDialog.Builder(BuyerGradingEntryListActivity.this)
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
            downloadAlertDialogs.MyDialogWithYesNo(BuyerGradingEntryListActivity.this,getString(R.string.app_name), getString(R.string.do_you_want_to_save),save_listener,kill_listener);
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

                        // 0 - Entry No
                        // 1 - Entry Date
                        // 2 - Customer
                        // 3 - Logpond Name
                        // 4 - User Account
                        // 5 - Total label

                        StringBuffer sb = new StringBuffer();
                        sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                        sb.append(detailsJsonObject.getString("date") + "\r\n");
                        sb.append(detailsJsonObject.getString("customer") + "\r\n");
                        sb.append(detailsJsonObject.getString("logpond") + "\r\n");
                        sb.append(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "\r\n");
                        sb.append(labelEntryArrayList.size() + "\r\n");

                        for(int i=0;i<labelEntryArrayList.size();i++){
                            //sb.append(labelEntryArrayList.get(i) + "^" + detailsJsonObject.getString("grade") + "^" + "\r\n");
                            sb.append(labelEntryArrayList.get(i) + "\r\n");
                        }

                        String fileName = SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "_" + detailsJsonObject.getString("entryNo") + ".TXT";
                        fileJsonObject = FileManager.saveFile(activity, "export_buyer_grading", fileName.toUpperCase(), sb.toString());
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
            saveAlertDialogs.MyDialogWithYesNo(BuyerGradingEntryListActivity.this, getString(R.string.app_name), getString(R.string.do_you_want_to_save),save_listener,null);
        }else if(view==nextView){
            if(labelNumberEditTextView.getText().length()>0)
                addLabelNumber(labelNumberEditTextView.getText().toString(), false);
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
                final View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.buyer_grading_entry_list_row, parent, false);
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

                    ((ItemViewHolders) holder).noTextView.setText(String.valueOf(position + 1) + ". ");
                    String[] labelEntryPropArray = labelNumber.split(Pattern.quote("^"), -1);
                    System.out.println("~~~~~~~~~~~~~~~labelNumber = " + labelNumber);
                    JSONObject jsonObject = SqlDatabase.queryInventoryDetailsByLabelNumber(activity, labelEntryPropArray[0]);

                    ((ItemViewHolders) holder).labelNoTextView.setText(labelEntryPropArray[0]);
                    ((ItemViewHolders) holder).pvNoTextView.setText((jsonObject.isNull("pvNo")? "":jsonObject.getString("pvNo")));
                    try{
                        ((ItemViewHolders) holder).hTextView.setText((labelEntryPropArray[1].length()==0? "0.000":labelEntryPropArray[1]));
                    }catch (Exception e){}
                    try{
                        ((ItemViewHolders) holder).rTextView.setText((labelEntryPropArray[2].length()==0? "0.000":labelEntryPropArray[2]));
                    }catch (Exception e){}
                    try{
                        ((ItemViewHolders) holder).dTextView.setText((labelEntryPropArray[3].length()==0? "0.000":labelEntryPropArray[3]));
                    }catch (Exception e){}
                    try{
                        ((ItemViewHolders) holder).lcTextView.setText((labelEntryPropArray[4].length()==0? "0.000":labelEntryPropArray[4]));
                    }catch (Exception e){}
                    try{
                        ((ItemViewHolders) holder).commentTextView.setText(labelEntryPropArray[5].equals("%20")? "":labelEntryPropArray[5]);
                    }catch (Exception e){}
                    try{
                        ((ItemViewHolders) holder).kapRegNoTextView.setText((jsonObject.isNull("kapregisterNo")||jsonObject.getString("kapregisterNo").length()==0? getString(R.string.no).toUpperCase():jsonObject.getString("kapregisterNo")));

                    }catch (Exception e){}


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        System.out.println("~~~~~~~~~~~~~~~buyerReject = " + jsonObject.getString("buyerReject"));
                        if(jsonObject.isNull("buyerReject") || jsonObject.getString("buyerReject").length()==0 || jsonObject.getString("buyerReject").toUpperCase().contains("NO")) {
                            if(!jsonObject.isNull("containerAllocation") && jsonObject.getString("containerAllocation").equalsIgnoreCase("YES")){
                                ((ItemViewHolders) holder).itemView.setBackgroundColor(getResources().getColor(R.color.lightGreen, null));
                            }else{
                                ((ItemViewHolders) holder).itemView.setBackgroundColor(Color.TRANSPARENT);
                            }
                            ((ItemViewHolders) holder).rejectCodeTextView.setText("");
                        }else {
                            ((ItemViewHolders) holder).itemView.setBackgroundColor(getResources().getColor(R.color.lightRed, null));
                            ((ItemViewHolders) holder).rejectCodeTextView.setText(getString(R.string.buyer_reject));
                        }
                    }else{

                        if(jsonObject.isNull("buyerReject") || jsonObject.getString("buyerReject").length()==0 || jsonObject.getString("buyerReject").toUpperCase().contains("NO")) {

                            if(!jsonObject.isNull("containerAllocation") && jsonObject.getString("containerAllocation").equalsIgnoreCase("YES")){
                                ((ItemViewHolders) holder).itemView.setBackgroundColor(getResources().getColor(R.color.lightGreen));
                            }else{
                                ((ItemViewHolders) holder).itemView.setBackgroundColor(Color.TRANSPARENT);
                            }
                            ((ItemViewHolders) holder).rejectCodeTextView.setText("");
                        }else {
                            ((ItemViewHolders) holder).itemView.setBackgroundColor(getResources().getColor(R.color.lightRed));
                            ((ItemViewHolders) holder).rejectCodeTextView.setText(getString(R.string.buyer_reject));
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

            public TextView noTextView,labelNoTextView, pvNoTextView, rejectCodeTextView, hTextView, rTextView, dTextView, lcTextView, commentTextView, kapRegNoTextView;
            private View itemView, binImgeView;

            public ItemViewHolders(View itemView) {
                super(itemView);
                this.itemView = itemView;
                itemView.setClickable(true);
                itemView.setOnClickListener(this);
                itemView.setOnClickListener(this);
                binImgeView = itemView.findViewById(R.id.bin);
                binImgeView.setOnClickListener(this);
                if(!isEditable) binImgeView.setVisibility(View.INVISIBLE);
                noTextView = (TextView) itemView.findViewById(R.id.no);
                labelNoTextView = (TextView) itemView.findViewById(R.id.label_no);
                pvNoTextView = (TextView) itemView.findViewById(R.id.pv_no);
                rejectCodeTextView = (TextView) itemView.findViewById(R.id.reject_code);
                hTextView = (TextView) itemView.findViewById(R.id.h);
                rTextView = (TextView) itemView.findViewById(R.id.r);
                dTextView = (TextView) itemView.findViewById(R.id.d);
                lcTextView = (TextView) itemView.findViewById(R.id.lc);
                commentTextView = (TextView) itemView.findViewById(R.id.comment);
                kapRegNoTextView = (TextView) itemView.findViewById(R.id.kap_reg_no);
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
                    deleteAlertDialogs.MyDialogWithYesNo(BuyerGradingEntryListActivity.this,getString(R.string.delete), getString(R.string.are_you_sure_to_delete),delete_listener,null);

                }else if(v==itemView){
                    String labelNumber = (String) labelEntryArrayList.get(getAdapterPosition());
                    Intent entryDetailsIntent = new Intent(BuyerGradingEntryListActivity.this, BuyerGradingEntryDetailsActivity.class);
                    entryDetailsIntent.putExtra("labelNumber", labelNumber);
                    entryDetailsIntent.putExtra("isEditable", isEditable);
                    entryDetailsIntent.putExtra("position", getAdapterPosition());
                    entryDetailsIntent.putExtra("buyerGradingPosition", position);
                    entryDetailsIntent.putExtra("labelEntryArrayList", labelEntryArrayList);
                    entryDetailsIntent.putExtra("detailsJsonObject", detailsJsonObject.toString());
                    startActivityForResult(entryDetailsIntent, ENTRY_DETAILS_REQUEST_CODE);
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

    private void addLabelNumber(String labelNumber, boolean isSaveButtonPressed){
        isModified = true;
        hideKeyboard(this.getCurrentFocus());
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
            deleteAlertDialogs.MyDialogWithYesNo(BuyerGradingEntryListActivity.this,"Warning!", getString(R.string.less_than_7_digits_error),delete_listener,null);
            return;
        }
        labelNumber = labelNumber.toUpperCase();
        //if(!labelNumber.startsWith("NA-")) labelNumber = "NA-" + labelNumber;
        //boolean isExist = false;

        for(int i=0;i<labelEntryArrayList.size();i++){
            String existingLabelNumber = (String) labelEntryArrayList.get(i);
            // Prevent empty list row
            if(existingLabelNumber.trim().length()>0){
                String[] labelEntryPropArray = existingLabelNumber.split(Pattern.quote("^"), -1);
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~labelNumber = " + labelNumber);
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~existing = " + labelEntryPropArray[0]);
                if(labelEntryPropArray[0].equalsIgnoreCase(labelNumber)){
                    if(!isSaveButtonPressed){
                        Intent entryDetailsIntent = new Intent(BuyerGradingEntryListActivity.this, BuyerGradingEntryDetailsActivity.class);
                        entryDetailsIntent.putExtra("labelNumber", existingLabelNumber);
                        entryDetailsIntent.putExtra("isEditable", isEditable);
                        entryDetailsIntent.putExtra("position", i);
                        entryDetailsIntent.putExtra("buyerGradingPosition", position);
                        entryDetailsIntent.putExtra("labelEntryArrayList", labelEntryArrayList);
                        entryDetailsIntent.putExtra("detailsJsonObject", detailsJsonObject.toString());
                        startActivityForResult(entryDetailsIntent, ENTRY_DETAILS_REQUEST_CODE);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }

                    return;
                }
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
            deleteAlertDialogs.MyDialogWithYesNo(BuyerGradingEntryListActivity.this,getString(R.string.buyer_grading1), getString(R.string.you_have_reach_maximum_entry)+"\n"+getString(R.string.save_and_create_a_new_record),delete_listener,null);
            return;
        }

        labelNumber = labelNumber + "^0.000^0.000^0.000^0.000^%20^";
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
            deleteAlertDialogs.MyDialogWithYesNo(BuyerGradingEntryListActivity.this,getString(R.string.buyer_grading1), getString(R.string.you_have_reach_maximum_entry)+"\n"+getString(R.string.save_and_create_a_new_record),delete_listener,null);
        }else if((leftCount <= 2)){
            String message = getString(R.string.you_have_count_entry_left);
            message = message.replace("|COUNT|", String.valueOf(leftCount));
            TTSManager.sayText(activity, message);
            MySnackBar.showError(getCurrentFocus(), message, Snackbar.LENGTH_LONG);
        }

        if(!isSaveButtonPressed){
            Intent entryDetailsIntent = new Intent(BuyerGradingEntryListActivity.this, BuyerGradingEntryDetailsActivity.class);
            entryDetailsIntent.putExtra("labelNumber", labelNumber);
            entryDetailsIntent.putExtra("isEditable", isEditable);
            entryDetailsIntent.putExtra("position", 0);
            entryDetailsIntent.putExtra("buyerGradingPosition", position);
            entryDetailsIntent.putExtra("labelEntryArrayList", labelEntryArrayList);
            entryDetailsIntent.putExtra("detailsJsonObject", detailsJsonObject.toString());
            startActivityForResult(entryDetailsIntent, ENTRY_DETAILS_REQUEST_CODE);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SCANNER_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                String result = data.getStringExtra("CODE");
                addLabelNumber(result, false);
            }else if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }else if(requestCode == ENTRY_DETAILS_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                if(data.hasExtra("action")){
                    int position = data.getIntExtra("position", -1);
                    if(position!=-1){
                        String labelNumber = data.getStringExtra("labelNumber");
                        labelEntryArrayList.set(position, labelNumber);
                        adapter.notifyDataSetChanged();
                        subtitle.setText(getString(R.string.entries_pcs).replace("|COUNT|", String.valueOf(labelEntryArrayList.size())));
                        //toolbar.setSubtitle(getString(R.string.entries_pcs).replace("|COUNT|", String.valueOf(labelEntryArrayList.size())));
                    }
                }
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
                    // 0 - Entry No
                    // 1 - Entry Date
                    // 2 - Customer
                    // 3 - Logpond Name
                    // 4 - User Account
                    // 5 - Total label

                    StringBuffer sb = new StringBuffer();
                    sb.append(position + "\r\n");
                    sb.append(detailsJsonObject.getString("entryNo") + "\r\n");
                    sb.append(detailsJsonObject.getString("date") + "\r\n");
                    sb.append(detailsJsonObject.getString("customer") + "\r\n");
                    sb.append(detailsJsonObject.getString("logpond") + "\r\n");
                    sb.append(SharedPreferencesStorage.getStringValue(activity, SharedPreferencesStorage.USERNAME) + "\r\n");
                    sb.append(labelEntryArrayList.size() + "\r\n");

                    for(int i=0;i<labelEntryArrayList.size();i++){
                        //sb.append(labelEntryArrayList.get(i) + "^" + detailsJsonObject.getString("grade") + "^" + "\r\n");
                        sb.append(labelEntryArrayList.get(i) + "\r\n");
                    }

                    FileManager.saveTempFile(activity, "BUYER_GRADING.TXT", sb.toString());
                } catch (JSONException e) {

                }finally {

                }
            }
        }).start();

    }
}
