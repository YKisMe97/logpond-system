package com.infocomm.logpond_v2.popupwindow;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.infocomm.logpond_v2.R;
import com.infocomm.logpond_v2.util.CalendarUtil;


public class AboutPopUp extends Activity {

    Button closeBtn;
    TextView version, developer_company, website, copyright;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_popup);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        getWindow().setLayout((int)(width*.9), (int)(height*.8));

        closeBtn = findViewById(R.id.about_close_button);
        closeBtnOnClick();

        version = findViewById(R.id.about_versionTV);

        developer_company = findViewById(R.id.about_developer_companyTV);

        String year = CalendarUtil.getYear();
        copyright = findViewById(R.id.about_copyright);
        String copyrighttext = (getResources().getString(R.string.copyright)).replace("YYYY" , year);
        copyright.setText(copyrighttext);


        website = findViewById(R.id.about_website);
        website.setPaintFlags(website.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        websiteOnClick();
    }

//---------------------------------Functions---------------------------------------

    private void closeBtnOnClick() {
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    private void websiteOnClick() {
        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://www.infocomm-solution.com";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }

}
