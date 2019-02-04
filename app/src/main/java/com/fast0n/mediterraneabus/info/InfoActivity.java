package com.fast0n.mediterraneabus.info;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.fast0n.mediterraneabus.BuildConfig;
import com.fast0n.mediterraneabus.Changelog;
import com.fast0n.mediterraneabus.MainActivity;
import com.fast0n.mediterraneabus.R;
import com.fast0n.mediterraneabus.java.SnackbarMaterial;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

public class InfoActivity extends AppCompatActivity {

    AdView mAdView;
    ArrayList<DataInfo> dataInfos;
    ListView listView;
    CustomAdapterInfo adapter;
    CoordinatorLayout coordinatorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);



        View viewStatusbar = getWindow().getDecorView();
        viewStatusbar.setSystemUiVisibility(viewStatusbar.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(this ,android.R.color.white));



        Toolbar toolbar = findViewById(R.id.toolbar);
        Button button = toolbar.findViewById(R.id.exit);

        // set color/text/icon in the task
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground);
        ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(getString(R.string.info), bm,
                getResources().getColor(R.color.task));
        InfoActivity.this.setTaskDescription(taskDesc);

        // java addresses
        listView = findViewById(R.id.list_info);
        dataInfos = new ArrayList<>();
        coordinatorLayout = findViewById(R.id.cordinatorLayout);

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
        dataInfos.add(new DataInfo(getString(R.string.support), R.drawable.ic_instagram));
        dataInfos.add(new DataInfo(getString(R.string.donate), R.drawable.ic_donate));
        dataInfos.add(new DataInfo(getString(R.string.author) + "<br><small>Massimiliano Montaleone (Fast0n)</small>",
                R.drawable.ic_user));
        dataInfos.add(new DataInfo(getString(R.string.content), R.drawable.ic_warning));

        // set data to Adapter
        adapter = new CustomAdapterInfo(dataInfos, getApplicationContext());
        listView.setAdapter(adapter);

        // setOnItemClickListener listview
        listView.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 1:

                    if (isOnline())
                        new Changelog(InfoActivity.this, true, coordinatorLayout);
                    else {
                        Snackbar snack = Snackbar.make(coordinatorLayout,
                                getString(R.string.errorconnection), Snackbar.LENGTH_SHORT).setAnchorView(R.id.layout);
                        SnackbarMaterial.configSnackbar(getApplicationContext(), snack);
                        snack.show();
                    }

                    break;

                case 3:
                    Intent intent3 = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/fast0n/mediterraneabus"));
                    startActivity(intent3);
                    break;

                case 4:
                    Intent intent4 = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.instagram.com/mediterraneabus"));
                    startActivity(intent4);
                    break;

                case 5:
                    Intent intent5 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/Fast0n/0.5"));
                    startActivity(intent5);
                    break;

                case 6:
                    Intent intent6 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/fast0n/"));
                    startActivity(intent6);
                    break;

            }

        });


        button.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));

        });

    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return Objects.requireNonNull(cm).getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
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
