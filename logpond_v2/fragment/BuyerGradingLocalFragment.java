package com.infocomm.logpond_v2.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.infocomm.logpond_v2.R;
import com.infocomm.logpond_v2.activity.BuyerGradingDetailsActivity;
import com.infocomm.logpond_v2.storage.SharedPreferencesStorage;
import com.infocomm.logpond_v2.util.ArrayListSortingManager;
import com.infocomm.logpond_v2.util.CalendarUtil;
import com.infocomm.logpond_v2.util.FileManager;
import com.infocomm.logpond_v2.view.CustomAlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Created by DoAsInfinity on 6/4/2017.
 */

public class BuyerGradingLocalFragment extends android.support.v4.app.Fragment implements  View.OnClickListener, View.OnLongClickListener{

    private final int DETAILS_REQUEST_CODE = 98;
    private View rootView;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        latestEntryNumber = "";
        query();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        arrayList = new ArrayList();
        //jsonArray = new JSONArray();
        rootView = inflater.inflate( R.layout.fragment_logpond_out_local, container, false);
        this.inflater = inflater;
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        adapter = new CustomAdapter(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
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
        totalTextView = (TextView) rootView.findViewById(R.id.total_text_view);
        loadingView = rootView.findViewById(R.id.loading_view);
        Button fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent buyerGradingDetailsIntent = new Intent(getContext(), BuyerGradingDetailsActivity.class);
                startActivityForResult(buyerGradingDetailsIntent, DETAILS_REQUEST_CODE);
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        return rootView;
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
            username = SharedPreferencesStorage.getStringValue(getContext(), SharedPreferencesStorage.USERNAME);
        }

        protected ArrayList doInBackground(Object... prop) {
            ArrayList tempArrayList= new ArrayList();
            try {
                File privateBuyerGradingDir = new File(FileManager.getPrivateDirPath(getContext()) + "/export_buyer_grading");
                File publicBuyerGradingDir = new File(FileManager.getPublicDirPath(getContext()) + "/export_buyer_grading");

                if(!privateBuyerGradingDir.exists()){
                    privateBuyerGradingDir.mkdirs();
                }

                if(!publicBuyerGradingDir.exists()){
                    publicBuyerGradingDir.mkdirs();
                }

                File[] publicLogpondFiles = publicBuyerGradingDir.listFiles();
                for(int i =0;i<publicLogpondFiles.length;i++){
                    File logpondFile = publicLogpondFiles[i];
                    if(logpondFile.isFile() && logpondFile.getName().startsWith(username.toUpperCase() + "_")
                            && !logpondFile.getName().endsWith("D.TXT")&& !logpondFile.getName().endsWith("D.txt")){
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("name", logpondFile.getName());
                        jsonObject.put("lastModified", logpondFile.lastModified());
                        jsonObject.put("size", logpondFile.length());
                        jsonObject.put("filePath", logpondFile.getAbsolutePath());
                        String date = logpondFile.getName();
                        String recordName = logpondFile.getName();
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
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    FileManager.removeTempFile(getContext(), "BUYER_GRADING.TXT");
                    // Success
                }else{
                    arrayList = ArrayListSortingManager.sortJsonObject(result, "recordName", false);
                    //Toast.makeText(getContext(), getString(R.string.done), Toast.LENGTH_LONG).show();
                    adapter.notifyDataSetChanged();

                    if(FileManager.retrieveTextContentAsStringArrayFromTempFile(getContext(), "BUYER_GRADING.TXT") != null){
                        Object[] object = FileManager.retrieveTextContentAsStringArrayFromTempFile(getContext(), "BUYER_GRADING.TXT");
                        int position = (int) object[0];
                        if(arrayList.size()>position){
                            Intent buyerGradingDetailsIntent = new Intent(getContext(), BuyerGradingDetailsActivity.class);
                            startActivityForResult(buyerGradingDetailsIntent, DETAILS_REQUEST_CODE);
                            getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }else{
                            FileManager.removeTempFile(getContext(), "BUYER_GRADING.TXT");
                        }
                    }
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

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onLongClick(View v) {

        return false;
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
                return new CustomAdapter.HeaderViewHolder(headerView);
            } else {
                final View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.logpond_out_list_row, parent, false);
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
                titleTextView = (TextView) itemView.findViewById(R.id.title);
                messageTextView = (TextView) itemView.findViewById(R.id.message);
                dateTextView = (TextView) itemView.findViewById(R.id.date);
            }

            @Override
            public void onClick(View v) {
                try {
                    if (v == binImgeView) {
                        final CustomAlertDialog deleteAlertDialogs = new CustomAlertDialog();
                        View.OnClickListener delete_listener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    JSONObject jsonObject = (JSONObject) arrayList.get(getAdapterPosition());
                                    File file = new File(jsonObject.getString("filePath"));
                                    String fileName = file.getName();
                                    fileName = fileName.replace(".TXT", "D.TXT");
                                    file.renameTo(new File(file.getParent() + "/" + fileName.toUpperCase()));

                                    // Rename private file too
                                    File privateFile = new File(FileManager.getPrivateDirPath(getContext()) + "/export_logpond_in", file.getName());
                                    privateFile.renameTo(new File(privateFile.getParent() + "/" + fileName.toUpperCase()));

                                    arrayList.remove(getAdapterPosition());
                                    adapter.notifyDataSetChanged();
                                    totalTextView.setText(getString(R.string.total) + " : " + arrayList.size());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                deleteAlertDialogs.cancel();
                            }
                        };
                        deleteAlertDialogs.MyDialogWithYesNo(getContext(),getString(R.string.delete), getString(R.string.are_you_sure_to_delete),delete_listener,null);
                    } else {
                        JSONObject jsonObject = (JSONObject) arrayList.get(getAdapterPosition());
                        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~logpondPile = " + jsonObject);
                        Intent buyerGradingDetailsIntent = new Intent(getContext(), BuyerGradingDetailsActivity.class);
                        buyerGradingDetailsIntent.putExtra("data", jsonObject.toString());
                        buyerGradingDetailsIntent.putExtra("position", getAdapterPosition());
                        startActivityForResult(buyerGradingDetailsIntent, DETAILS_REQUEST_CODE);
                        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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
