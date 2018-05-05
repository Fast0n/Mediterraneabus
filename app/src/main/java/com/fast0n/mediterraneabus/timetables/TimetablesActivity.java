package com.fast0n.mediterraneabus.timetables;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fast0n.mediterraneabus.MainActivity;
import com.fast0n.mediterraneabus.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class TimetablesActivity extends AppCompatActivity {

    AdView mAdView;
    ArrayList<DataTimetables> dataHours;
    ListView listView;
    ProgressBar loading;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    String departure, arrival, period;
    String sort;
    private CustomAdapterTimetables adapter;
    private InterstitialAd mInterstitialAd;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetables);
        // set title activity in the toolbar
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.solutions));

        // set color/text/icon in the task
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground);
        ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(getString(R.string.solutions), bm,
                getResources().getColor(R.color.task));
        TimetablesActivity.this.setTaskDescription(taskDesc);

        // set row icon in the toolbar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // java addresses
        listView = findViewById(R.id.list);
        mAdView = findViewById(R.id.adView1);
        loading = findViewById(R.id.progressBar);

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

        final String url = "https://mediterraneabus-api.herokuapp.com/?periodo=invernale&percorso_linea=" + departure
                + "&percorso_linea1=" + arrival + "&sort_by=time";

        get(url);

    }

    public void get(String url) {

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            dataHours = new ArrayList<>();

                            JSONObject json_raw = new JSONObject(response.toString());
                            String linee = json_raw.getString("linee");
                            JSONArray arrayLinee = new JSONArray(linee);

                            for (int i = 0; i < arrayLinee.length(); i++) {
                                String corse = arrayLinee.getString(i);

                                JSONObject scorroCorse = new JSONObject(corse);

                                String nomeCorsa = scorroCorse.getString("corsa");
                                String orari = scorroCorse.getString("orari");

                                JSONArray arrayOrari = new JSONArray(orari);
                                for (int j = 0; j < arrayOrari.length(); j++) {

                                    String partenza2 = arrayOrari.getString(j);
                                    JSONObject scorrOrari = new JSONObject(partenza2);

                                    String partenza = scorrOrari.getString("partenza");
                                    String arrivo = scorrOrari.getString("arrivo");

                                    @SuppressLint
                                    ("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
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
                                    int min = (int) (difference - (1000 * 60 * 60 * 24 * days)
                                            - (1000 * 60 * 60 * hours)) / (1000 * 60);

                                    int length = String.valueOf(hours).length();
                                    String ora = null;

                                    if (length == 1) {
                                        ora = "0" + hours;
                                    }

                                    dataHours.add(new DataTimetables(nomeCorsa, partenza, arrivo, departure,
                                            arrival, ora + ":" + min));

                                }

                            }

                            adapter = new CustomAdapterTimetables(dataHours, getApplicationContext());

                            listView.setAdapter(adapter);

                            loading.setVisibility(View.INVISIBLE);

                        } catch (JSONException | ParseException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        dataHours = new ArrayList<>();
                        dataHours.add(new DataTimetables(getString(R.string.timetable_not_found), "00:00", "00:00",
                                departure, arrival, "00:00"));
                        adapter = new CustomAdapterTimetables(dataHours, getApplicationContext());

                        listView.setAdapter(adapter);

                        loading.setVisibility(View.INVISIBLE);
                    }
                });

        // add it to the RequestQueue
        queue.add(getRequest);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.sort_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.sort) {


            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                Log.e("TAG", "The interstitial wasn't loaded yet.");
            }


            sort = settings.getString("sort", null);
            new MaterialDialog.Builder(this).title(getString(R.string.sort)).items(R.array.preference_values)
                    .itemsCallbackSingleChoice(Integer.parseInt(sort), new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                            final Bundle extras = getIntent().getExtras();
                            switch (which) {
                            case 0:

                                assert extras != null;
                                departure = extras.getString("departure");
                                arrival = extras.getString("arrival");

                                final String url2 = "https://mediterraneabus-api.herokuapp.com/?periodo=invernale&percorso_linea="
                                        + departure + "&percorso_linea1=" + arrival + "&sort_by=time";

                                get(url2);

                                editor.putString("sort", "0");
                                editor.commit();

                                break;
                            case 1:

                                assert extras != null;
                                departure = extras.getString("departure");
                                arrival = extras.getString("arrival");

                                final String url = "https://mediterraneabus-api.herokuapp.com/?periodo=invernale&percorso_linea="
                                        + departure + "&percorso_linea1=" + arrival + "&sort_by=line";

                                get(url);

                                editor.putString("sort", "1");
                                editor.commit();
                                break;
                            }

                            return true;
                        }
                    }).show();

            return true;
        }

        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            settings.edit().remove("sort").apply();

            Intent mainActivity = new Intent(TimetablesActivity.this, MainActivity.class);
            mainActivity.putExtra("departure", departure);
            mainActivity.putExtra("arrival", arrival);
            mainActivity.putExtra("period", period);
            startActivity(mainActivity);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        settings.edit().remove("sort").apply();

        Intent mainActivity = new Intent(TimetablesActivity.this, MainActivity.class);
        mainActivity.putExtra("departure", departure);
        mainActivity.putExtra("arrival", arrival);
        mainActivity.putExtra("period", period);
        startActivity(mainActivity);
    }
}
