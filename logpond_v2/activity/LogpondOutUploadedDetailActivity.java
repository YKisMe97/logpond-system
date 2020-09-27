package com.infocomm.logpond_v2.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.infocomm.logpond_v2.R;
import com.infocomm.logpond_v2.storage.SharedPreferencesStorage;
import com.infocomm.logpond_v2.util.CalendarUtil;
import com.infocomm.logpond_v2.util.ArrayListSortingManager;
import com.infocomm.logpond_v2.util.FileManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class LogpondOutUploadedDetailActivity extends AppCompatActivity {
    private final int DETAILS_REQUEST_CODE = 98;
    private LayoutInflater inflater;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CustomAdapter adapter;
    private View loadingView;
    //private JSONArray jsonArray;
    private ArrayList arrayList;
    private AsyncTask queryAsyncTask;
    private TextView totalTextView;
    private String latestEntryNumber;
    private String selected_date;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploaded_detail);
        latestEntryNumber = "";
        query();

        toolbar = (Toolbar) findViewById(R.id.forest_detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setLogo(R.drawable.forest);

        TextView toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(R.string.logpond_out);

        Intent intent = getIntent();
        selected_date = intent.getStringExtra("selected_date");
        arrayList = new ArrayList();
        //jsonArray = new JSONArray();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new CustomAdapter(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(LogpondOutUploadedDetailActivity.this));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(LogpondOutUploadedDetailActivity.this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                        query();
                    }
                }, 500);
            }
        });
        totalTextView = (TextView) findViewById(R.id.total_text_view);
        loadingView = findViewById(R.id.loading_view);
    }
    @Override
    public void onDestroy(){
        if(queryAsyncTask!=null)
            queryAsyncTask.cancel(true);
        super.onDestroy();
    }

    private void query(){
        if(queryAsyncTask!=null) queryAsyncTask.cancel(true);
        queryAsyncTask = new QueryTask();
        queryAsyncTask.execute();
    }

    private class QueryTask extends AsyncTask<Object, Void, ArrayList> {

        private String errorMessage = "";
        String username = "";
        private Object[] prop = null;

        protected void onPreExecute (){
            //progressDialog.show();
            errorMessage = "";
            username = SharedPreferencesStorage.getStringValue(LogpondOutUploadedDetailActivity.this, SharedPreferencesStorage.USERNAME);
        }

        protected ArrayList doInBackground(Object... prop) {
            ArrayList tempArrayList= new ArrayList();
            try {
                File privateBuyerGradingDir = new File(FileManager.getPrivateDirPath(LogpondOutUploadedDetailActivity.this) + File.separator + getString(R.string.transfer_logpond_out));
                //File publicBuyerGradingDir = new File(FileManager.getPublicDirPath(LogpondOutUploadedDetailActivity.this) + "/transfer_forest");

                if(!privateBuyerGradingDir.exists()){
                    privateBuyerGradingDir.mkdirs();
                }
                //if(!publicBuyerGradingDir.exists()){
                //    publicBuyerGradingDir.mkdirs();
                //}

                File[] publicLogpondOutFiles = privateBuyerGradingDir.listFiles();
                for(int i =0;i<publicLogpondOutFiles.length;i++){
                    File logpondOutFile = publicLogpondOutFiles[i];
                    if(logpondOutFile.isFile() && logpondOutFile.getName().startsWith(username.toUpperCase() + "_"+ selected_date)
                            && !logpondOutFile.getName().endsWith("D.txt")){
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("name", logpondOutFile.getName());
                        jsonObject.put("lastModified", logpondOutFile.lastModified());
                        jsonObject.put("size", logpondOutFile.length());
                        jsonObject.put("filePath", logpondOutFile.getAbsolutePath());
                        String date = logpondOutFile.getName();
                        String recordName = logpondOutFile.getName();
                        try{
                            if(date.indexOf("_")>-1) {
                                date = date.substring(date.indexOf("_") + 1);
                                recordName = date;
                            }
                            if(date.indexOf(".")>-1) date = date.substring(0, date.indexOf("."));
                            if(date.length()>8) date = date.substring(0, 8);

                        }catch (Exception e){

                        }
                        jsonObject.put("date", date);
                        jsonObject.put("recordName", recordName);
                        tempArrayList.add(jsonObject);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
            }finally{

            }
            return tempArrayList;
        }
        /*
        protected void onProgressUpdate(Integer... progress) {

        }
        */

        protected void onCancelled (ArrayList result){
            Log.d("onCancelled", "");
        }

        protected void onPostExecute(ArrayList result) {
            try{
                // Error
                if(errorMessage.length()>0){
                    Toast.makeText(LogpondOutUploadedDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    // Success
                }else{
                    arrayList = ArrayListSortingManager.sortJsonObject(result, "recordName", false);
                    //Toast.makeText(LogpondOutUploadedDetailActivity.this, getString(R.string.done), Toast.LENGTH_LONG).show();
                    adapter.notifyDataSetChanged();
                }
                totalTextView.setText(getString(R.string.total) + " : " + arrayList.size());
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                loadingView.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                //progressDialog.dismiss();
            }
        }
    }

    /*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_call_block_blacklist_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    */

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
                return new CustomAdapter.HeaderViewHolder(headerView);
            } else {
                final View layoutView = LayoutInflater.from(LogpondOutUploadedDetailActivity.this).inflate(R.layout.logpond_out_list_row, parent, false);
                CustomAdapter.ItemViewHolders itemViewHolders = new CustomAdapter.ItemViewHolders(layoutView);
                return itemViewHolders;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof CustomAdapter.ItemViewHolders) {
                try {
                    if (headerView != null) {
                        position = position -1;
                    }

                    JSONObject jsonObject = (JSONObject) arrayList.get(position);
                    ((ItemViewHolders) holder).titleTextView.setText(jsonObject.getString("name"));
                    ((ItemViewHolders) holder).messageTextView.setText(jsonObject.getLong("size") + " " + getString(R.string.bytes));

                    String date = jsonObject.getString("date");
                    //((ItemViewHolders) holder).dateTextView.setText(CalendarUtil.getDateTimeFormatByLong("EEE, MMM dd, yyyy HH:mm:ss", jsonObject.getLong("lastModified")));
                    try{
                        ((ItemViewHolders) holder).dateTextView.setText(CalendarUtil.changeDateTimeFormatByString("yyyyMMdd", "EEE, MMM dd, yyyy", date));
                    }catch (Exception e){}

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
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

            public TextView titleTextView, messageTextView, dateTextView;
            private View itemView, binImgeView;

            public ItemViewHolders(View itemView) {
                super(itemView);
                this.itemView = itemView;
                itemView.setClickable(true);
                itemView.setOnClickListener(this);
                binImgeView = itemView.findViewById(R.id.bin);
                binImgeView.setOnClickListener(this);
                binImgeView.setVisibility(View.GONE);
                titleTextView = (TextView) itemView.findViewById(R.id.title);
                messageTextView = (TextView) itemView.findViewById(R.id.message);
                dateTextView = (TextView) itemView.findViewById(R.id.date);
            }

            @Override
            public void onClick(View v) {
                try {
                    if (v == binImgeView) {
                        new AlertDialog.Builder(LogpondOutUploadedDetailActivity.this)
                                .setTitle(getString(R.string.delete))
                                .setMessage(getString(R.string.are_you_sure_to_delete))
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        try {
                                            JSONObject jsonObject = (JSONObject) arrayList.get(getAdapterPosition());
                                            File file = new File(jsonObject.getString("filePath"));
                                            String fileName = file.getName();
                                            fileName = fileName.replace(".txt", "D.txt");
                                            file.renameTo(new File(file.getParent() + "/" + fileName));
                                            arrayList.remove(getAdapterPosition());
                                            adapter.notifyDataSetChanged();
                                            totalTextView.setText(getString(R.string.total) + " : " + arrayList.size());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                })
                                .setNegativeButton(android.R.string.no, null).show();
                    } else {
                        JSONObject jsonObject = (JSONObject) arrayList.get(getAdapterPosition());
                        Intent buyerGradingDetailsIntent = new Intent(LogpondOutUploadedDetailActivity.this, LogpondOutDetailsActivity.class);
                        buyerGradingDetailsIntent.putExtra("data", jsonObject.toString());
                        buyerGradingDetailsIntent.putExtra("position", getAdapterPosition());
                        buyerGradingDetailsIntent.putExtra("isEditable", false);
                        startActivityForResult(buyerGradingDetailsIntent, DETAILS_REQUEST_CODE);
                        LogpondOutUploadedDetailActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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
    public  void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~onActivityResult");
        if (requestCode == DETAILS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if(data.hasExtra("action") && data.getStringExtra("action") != null){
                    int position = data.getIntExtra("position", -1);
                    String fileJsonObjectString = data.getStringExtra("fileJsonObjectString");
                    if(fileJsonObjectString!=null && fileJsonObjectString.length()>0){
                        try {
                            JSONObject fileJsonObject = new JSONObject(fileJsonObjectString);
                            System.out.println("~~~~~~~~~~~fileJsonObject=" + fileJsonObject);
                            if(!fileJsonObject.isNull("size")){
                                String name = fileJsonObject.getString("name");
                                long lastModified = fileJsonObject.getLong("lastModified");
                                long size = fileJsonObject.getLong("size");
                                String date = name;
                                String recordName = name;
                                try{
                                    if(date.indexOf("_")>-1) {
                                        date = date.substring(date.indexOf("_") + 1);
                                        recordName = date;
                                    }
                                    if(date.indexOf(".")>-1) date = date.substring(0, date.indexOf("."));
                                    if(date.length()>8) date = date.substring(0, 8);

                                }catch (Exception e){

                                }
                                fileJsonObject.put("date", date);
                                fileJsonObject.put("recordName", recordName);
                                if(position==-1){
                                    arrayList.add(0, fileJsonObject );
                                }else{
                                    arrayList.set(position, fileJsonObject);
                                }
                                adapter.notifyDataSetChanged();
                                totalTextView.setText(getString(R.string.total) + " : " + arrayList.size());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
    }
}
