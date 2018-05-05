package com.fast0n.mediterraneabus.info;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.fast0n.mediterraneabus.BuildConfig;
import com.fast0n.mediterraneabus.Changelog;
import com.fast0n.mediterraneabus.MainActivity;
import com.fast0n.mediterraneabus.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class InfoActivity extends AppCompatActivity {

    AdView mAdView;
    ArrayList<DataInfo> dataInfos;
    ListView listView;
    CustomAdapterInfo adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        // set title activity in the toolbar
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.info));

        // set color/text/icon in the task
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground);
        ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(getString(R.string.info), bm,
                getResources().getColor(R.color.task));
        InfoActivity.this.setTaskDescription(taskDesc);

        // set row icon in the toolbar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // java addresses
        listView = findViewById(R.id.list_info);
        dataInfos = new ArrayList<>();

        // banner google
        MobileAds.initialize(this, "ca-app-pub-9646303341923759~6818726547");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // add data element in listview
        dataInfos
                .add(new DataInfo(
                        getString(R.string.version) + "<br><small>" + BuildConfig.VERSION_NAME + " ("
                                + BuildConfig.VERSION_CODE + ") (" + BuildConfig.APPLICATION_ID + ")</small>",
                        R.drawable.ic_info_outline));
        dataInfos.add(new DataInfo(getString(R.string.changelog), R.drawable.ic_changelog));
        dataInfos.add(
                new DataInfo(getString(R.string.translate) + "<br><small>🤷🏼‍♂️</small>", R.drawable.ic_translate));
        dataInfos.add(new DataInfo(getString(R.string.source_code), R.drawable.ic_github));
        dataInfos.add(new DataInfo(getString(R.string.donate), R.drawable.ic_donate));
        dataInfos.add(new DataInfo(getString(R.string.author) + "<br><small>Massimiliano Montaleone (Fast0n)</small>",
                R.drawable.ic_user));

        // set data to Adapter
        adapter = new CustomAdapterInfo(dataInfos, getApplicationContext());
        listView.setAdapter(adapter);

        // setOnItemClickListener listview
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                case 1:

                    if (isOnline())
                        new Changelog(InfoActivity.this, true);

                    else
                        Toasty.error(InfoActivity.this, getString(R.string.errorconnection), Toast.LENGTH_LONG, true)
                                .show();

                    break;

                case 3:
                    Intent intent3 = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/fast0n/mediterraneabus"));
                    startActivity(intent3);
                    break;

                case 4:
                    Intent intent4 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/Fast0n/0.5"));
                    startActivity(intent4);
                    break;

                case 5:
                    Intent intent5 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/fast0n/"));
                    startActivity(intent5);
                    break;

                }

            }
        });

    }




    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:

            finish();
            Intent mainActivity = new Intent(InfoActivity.this, MainActivity.class);
            startActivity(mainActivity);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent mainActivity = new Intent(InfoActivity.this, MainActivity.class);
        startActivity(mainActivity);
    }
}
