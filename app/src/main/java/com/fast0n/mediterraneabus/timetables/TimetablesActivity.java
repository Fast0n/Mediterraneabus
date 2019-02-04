package com.fast0n.mediterraneabus.timetables;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.fast0n.mediterraneabus.MainActivity;
import com.fast0n.mediterraneabus.R;
import com.fast0n.mediterraneabus.java.SnackbarMaterial;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class TimetablesActivity extends AppCompatActivity {

    final int[] select = {1};
    PowerMenu powerMenu;
    ActionBar actionBar;
    AdView mAdView;
    ArrayList<DataTimetables> dataHours;
    ListView listView;
    ProgressBar loading;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    String departure, arrival, period, sort, name;
    Toolbar toolbar;
    Vibrator v;
    CoordinatorLayout coordinatorLayout;
    private CustomAdapterTimetables adapter;
    private InterstitialAd mInterstitialAd;
    private OnMenuItemClickListener<PowerMenuItem> onMenuItemClickListener = new OnMenuItemClickListener<PowerMenuItem>() {
        @Override
        public void onItemClick(int position, PowerMenuItem item) {

            powerMenu.setSelectedPosition(position);
            String ride = name.split("::")[1];
            String time = name.split("::")[2];
            String name_time = name.split("::")[3];
            String time1 = name.split("::")[4];
            String name_time1 = name.split("::")[5];

            if (!ride.equals(getString(R.string.timetable_not_found))) {
                String share = "🚏 " + getString(R.string.departure) + " "
                        + name_time.toUpperCase() + "\n" + ride + "\n🕜 "
                        + getString(R.string.timetables) + " " + time + " --> "
                        + time1 + "\n" + getString(R.string.arrival) + " "
                        + name_time1.toUpperCase();

                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, share);
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_to)));
            }

            powerMenu.dismiss();

        }
    };

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set title activity in the toolbar
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.solutions));

        // set color/text/icon in the task
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground);
        ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(getString(R.string.solutions),
                bm, getResources().getColor(R.color.task));
        TimetablesActivity.this.setTaskDescription(taskDesc);

        // set row icon in the toolbar
        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // java addresses
        listView = findViewById(R.id.list);
        loading = findViewById(R.id.progressBar);
        mAdView = findViewById(R.id.adView1);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        coordinatorLayout = findViewById(R.id.cordinatorLayout);

        listView.setSelector(android.R.color.transparent);

        settings = getSharedPreferences("sharedPreferences", 0);
        editor = settings.edit();

        // banner && interstitialAd
        MobileAds.initialize(this, "ca-app-pub-9646303341923759~6818726547");
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-9646303341923759/3726801956");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

        // hide hours
        loading.setVisibility(View.VISIBLE);

        final Bundle extras = getIntent().getExtras();
        assert extras != null;
        departure = extras.getString("departure");
        arrival = extras.getString("arrival");
        period = extras.getString("period");

        final String url = getString(R.string.server) + "?periodo=invernale&percorso_linea=" + departure
                + "&percorso_linea1=" + arrival + "&sort_by=time";

        get(url);

    }

    public void get(String url) {

        url = url.replaceAll(" ", "%20");
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        dataHours = new ArrayList<>();

                        JSONArray json_raw = new JSONArray(response.toString());

                        if (response.toString().equals("[]")) {
                            dataHours = new ArrayList<>();
                            dataHours.add(new DataTimetables(getString(R.string.timetable_not_found), "00:00", "00:00",
                                    departure, arrival, "00:00"));
                            adapter = new CustomAdapterTimetables(TimetablesActivity.this, dataHours);

                            listView.setAdapter(adapter);

                            loading.setVisibility(View.INVISIBLE);
                        }

                        for (int j = 0; j < json_raw.length(); j++) {

                            String partenza2 = json_raw.getString(j);
                            JSONObject scorrOrari = new JSONObject(partenza2);
                            String nomeCorsa = scorrOrari.getString("a");
                            String partenza = scorrOrari.getString("b");
                            String arrivo = scorrOrari.getString("c");

                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                            Date startDate = simpleDateFormat.parse(partenza);
                            Date endDate = simpleDateFormat.parse(arrivo);

                            long difference = endDate.getTime() - startDate.getTime();
                            if (difference < 0) {
                                Date dateMax = simpleDateFormat.parse("24:00");
                                Date dateMin = simpleDateFormat.parse("00:00");
                                difference = (dateMax.getTime() - startDate.getTime())
                                        + (endDate.getTime() - dateMin.getTime());
                            }
                            int days = (int) (difference / (1000 * 60 * 60 * 24));
                            int hours = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
                            int mins = (int) (difference - (1000 * 60 * 60 * 24 * days)
                                    - (1000 * 60 * 60 * hours)) / (1000 * 60);

                            int length = String.valueOf(hours).length();
                            String ora = null;

                            int length1 = String.valueOf(mins).length();
                            String minuti;

                            if (length == 1) {
                                ora = "0" + hours;
                            }
                            if (length1 == 1)
                                minuti = "0" + mins;
                            else
                                minuti = String.valueOf(mins);

                            dataHours.add(new DataTimetables(nomeCorsa, partenza, arrivo, departure, arrival,
                                    ora + ":" + minuti));

                        }


                        adapter = new CustomAdapterTimetables(TimetablesActivity.this, dataHours);

                        listView.setAdapter(adapter);
                        loading.setVisibility(View.INVISIBLE);

                    } catch (JSONException ignored) {
                        dataHours = new ArrayList<>();
                        dataHours.add(new DataTimetables(getString(R.string.timetable_not_found), "00:00", "00:00",
                                departure, arrival, "00:00"));
                        adapter = new CustomAdapterTimetables(TimetablesActivity.this, dataHours);

                        listView.setAdapter(adapter);

                        loading.setVisibility(View.INVISIBLE);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }, error -> {

            dataHours = new ArrayList<>();
            dataHours.add(new DataTimetables(getString(R.string.timetable_not_found), "00:00", "00:00",
                    departure, arrival, "00:00"));
            adapter = new CustomAdapterTimetables(TimetablesActivity.this, dataHours);

            listView.setAdapter(adapter);

            loading.setVisibility(View.INVISIBLE);


        });

        // add it to the RequestQueue
        queue.add(getRequest);


    }

    public void more(View view) {
        Button button_name = (Button) view;
        name = button_name.getText().toString();
        if (isOnline()) {
            if (name.split("::")[0].equals("options")) {
                powerMenu = new PowerMenu.Builder(this)
                        .addItem(new PowerMenuItem(getString(R.string.share), false)) // add an item.
                        .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT) // Animation start point (TOP | LEFT).
                        .setMenuRadius(10f) // sets the corner radius.
                        .setMenuShadow(10f) // sets the corner radius.
                        .setShowBackground(false) // sets the shadow.
                        .setTextColor(this.getResources().getColor(android.R.color.black))
                        .setSelectedTextColor(Color.WHITE)
                        .setMenuColor(Color.WHITE)
                        .setOnMenuItemClickListener(onMenuItemClickListener)
                        .build();
                powerMenu.showAsDropDown(view, -5, 0);
            }

        } else {
            Snackbar snack = Snackbar.make(coordinatorLayout,
                    getString(R.string.errorconnection), Snackbar.LENGTH_SHORT).setAnchorView(R.id.layout);
            SnackbarMaterial.configSnackbar(this, snack);
            snack.show();
        }

    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return Objects.requireNonNull(cm).getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.sort) {

            if (isOnline()) {

                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }

                sort = settings.getString("sort", null);
                new MaterialDialog.Builder(this).title(getString(R.string.sort)).items(R.array.preference_values)
                        .itemsCallbackSingleChoice(Integer.parseInt(sort), (dialog, view, which, text) -> {

                            final Bundle extras = getIntent().getExtras();
                            switch (which) {
                                case 0:
                                    assert extras != null;
                                    departure = extras.getString("departure");
                                    arrival = extras.getString("arrival");
                                    String url2 = getString(R.string.server) + "?periodo=invernale&percorso_linea="
                                            + departure + "&percorso_linea1=" + arrival + "&sort_by=time";
                                    get(url2);
                                    editor.putString("sort", "0");
                                    editor.commit();
                                    break;
                                case 1:
                                    assert extras != null;
                                    departure = extras.getString("departure");
                                    arrival = extras.getString("arrival");
                                    String url = getString(R.string.server) + "?periodo=invernale&percorso_linea="
                                            + departure + "&percorso_linea1=" + arrival + "&sort_by=line";
                                    get(url);
                                    editor.putString("sort", "1");
                                    editor.commit();
                                    break;
                            }

                            return true;
                        }).show();

            } else {
                Snackbar snack = Snackbar.make(coordinatorLayout,
                        getString(R.string.errorconnection), Snackbar.LENGTH_SHORT).setAnchorView(R.id.layout);
                SnackbarMaterial.configSnackbar(this, snack);
                snack.show();
            }
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:

                closeApplication();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        closeApplication();
    }

    public void closeApplication() {
        final String share = settings.getString("share", null);
        if (share != null) {

            Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.solutions));
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

            final String url = getString(R.string.server) + "?periodo=invernale&percorso_linea="
                    + departure + "&percorso_linea1=" + arrival + "&sort_by=time";

            get(url);

            editor.putString("share", null);
            select[0] = 1;
            editor.apply();

        } else {
            finish();
            settings.edit().remove("sort").apply();

            Intent mainActivity = new Intent(TimetablesActivity.this, MainActivity.class);
            mainActivity.putExtra("departure", departure);
            mainActivity.putExtra("arrival", arrival);
            mainActivity.putExtra("period", period);
            startActivity(mainActivity);
        }
    }

}
