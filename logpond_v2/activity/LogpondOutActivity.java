package com.infocomm.logpond_v2.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import com.infocomm.logpond_v2.R;
import com.infocomm.logpond_v2.fragment.LogpondOutLocalFragment;
import com.infocomm.logpond_v2.fragment.LogpondOutUploadedFragment;

public class LogpondOutActivity extends AppCompatActivity {

    private LogpondOutLocalFragment logpondOutLocalFragment;
    private LogpondOutUploadedFragment logpondOutUploadedFragment;
    private CollectionPagerAdapter collectionPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_logpond_out);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.forest);
        title = findViewById(R.id.toolbar_title);
        title.append(" OUT");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        logpondOutLocalFragment = new LogpondOutLocalFragment();
        logpondOutUploadedFragment = new LogpondOutUploadedFragment();
        collectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(collectionPagerAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorColor(Color.GRAY);
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

    public class CollectionPagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter {
        public CollectionPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int i) {
            Bundle args = new Bundle();
            switch(i){
                case 0:
                    return logpondOutLocalFragment;

                default:
                case 1:
                    return logpondOutUploadedFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position){
                case 0:
                    return getString(R.string.local);

                case 1:
                    return getString(R.string.uploaded);
            }
            return"";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
