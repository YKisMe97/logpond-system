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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infocomm.logpond_v2.R;
import com.infocomm.logpond_v2.activity.LogpondInUploadedDetailActivity;
import com.infocomm.logpond_v2.storage.SharedPreferencesStorage;
import com.infocomm.logpond_v2.util.CalendarUtil;
import com.infocomm.logpond_v2.util.FileManager;
//import com.mydreamsoft.palmera_forest.activity.ForestUploadedActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DoAsInfinity on 6/4/2017.
 */

public class LogpondInUploadedFragment extends android.support.v4.app.Fragment implements  View.OnClickListener, View.OnLongClickListener {

    private final int DETAILS_REQUEST_CODE = 98;
    private View rootView;
    private LayoutInflater inflater;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerViewAdapter adapter;
    private List<String> listDataHeader;
    private List<String> listFilesCount;
    private View loadingView;
    //private JSONArray jsonArray;
    private ArrayList listArrayHeader;
    private ArrayList listArrayFiles;
    private AsyncTask queryAsyncTask;
    private TextView totalTextView;
    private String latestEntryNumber;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        latestEntryNumber = "";
        //query();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        // properly.
        // arrayList = new ArrayList();
        //jsonArray = new JSONArray();
        rootView = inflater.inflate(R.layout.fragment_logpond_out_uploaded, container, false);
        this.inflater = inflater;

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        adapter = new RecyclerViewAdapter();

        totalTextView = (TextView) rootView.findViewById(R.id.total_text_view);
        loadingView = rootView.findViewById(R.id.loading_view);
        //loadingView.setVisibility(View.GONE);
        //totalTextView.setText(getString(R.string.total)+" : 0");
        getSummaryFileItem();
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
                        if (swipeRefreshLayout.isRefreshing()){
                            swipeRefreshLayout.setRefreshing(false);
                            getSummaryFileItem();
                        }
                    }
                }, 500);
            }
        });
        return rootView;
    }

    public void getSummaryFileItem() {
        String[] stringArrayHeader = null;
        listArrayHeader = new ArrayList<>();
        listArrayFiles = new ArrayList<>();
        File summaryDir = new File(FileManager.getPrivateDirPath(getContext()) + File.separator + getString(R.string.export_summary));
        File[] summaryFiles = summaryDir.listFiles();
        if (!summaryDir.exists()) {
            closeLoading();
        }
        else {
            for (int i = 0; i < summaryFiles.length; i++) {
                File export_summary_file = summaryFiles[i];
                String username = SharedPreferencesStorage.getStringValue(getContext(), SharedPreferencesStorage.USERNAME);
                if (export_summary_file.getName().startsWith(username.toUpperCase() + "_LOGPONDIN")) {
                    if (export_summary_file.exists()) {
                        StringBuffer sb = new StringBuffer();
                        BufferedReader br = null;
                        try {
                            br = new BufferedReader(new FileReader(export_summary_file));
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line);
                                sb.append("\r\n");
                            }
                        } catch (Exception e1) {

                        } finally {
                            if (br != null)
                                try {
                                    br.close();
                                    loadingView.setVisibility(View.GONE);
                                    //swipeRefreshLayout.setRefreshing(false);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                        }
                        if (sb.length() > 0) {
                            stringArrayHeader = sb.toString().split("\r\n");
                            int arrayLength = Integer.parseInt(stringArrayHeader[0]);
                            for (int j = 1; j <= arrayLength; j++) {
                                String[] date_fileCount = stringArrayHeader[j].split("\\^",2);
                                listArrayHeader.add(date_fileCount[0]);
                                listArrayFiles.add(date_fileCount[1]);
                            }
                            totalTextView.setText(getString(R.string.total)+": " + arrayLength);
                            Log.d("TAG", "Array length" + arrayLength);
                        }
                        listDataHeader = listArrayHeader;
                        listFilesCount = listArrayFiles;
                    }
                    else{
                        closeLoading();
                    }
                }
            }
        }
    }

    private void closeLoading(){
        listArrayHeader.add("");
        listArrayFiles.add("");
        listDataHeader = listArrayHeader;
        listFilesCount = listArrayFiles;
        totalTextView.setText(getString(R.string.total)+": 0");
        loadingView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }
    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

        private static final String TAG = "RecyclerViewAdapter";

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.forest_uploaded_summary_row, parent, false);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            Log.d(TAG, "onBindViewHolder: called.");

            holder.textView_summary_fileName.setText(CalendarUtil.changeDateTimeFormatByString("yyyyMMdd", "dd/MM/yyyy",listDataHeader.get(position)));
            holder.textView_total_files.setText(" ("+listFilesCount.get(position)+")");

            holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), LogpondInUploadedDetailActivity.class);
                    intent.putExtra("selected_date",listDataHeader.get(position));
                    startActivity(intent);
                    getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                }
            });
        }

        @Override
        public int getItemCount() {
            return listArrayHeader.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder{

            private TextView textView_total_files, textView_summary_fileName;
            RelativeLayout parentLayout;

            public ViewHolder(View itemView) {
                super(itemView);
                textView_total_files = itemView.findViewById(R.id.total_files);
                textView_summary_fileName = itemView.findViewById(R.id.summary_row_name);
                parentLayout = itemView.findViewById(R.id.relative_layout_summray_file);
            }
        }
    }

}
