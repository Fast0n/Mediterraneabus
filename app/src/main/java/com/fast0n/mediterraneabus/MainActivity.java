package com.fast0n.mediterraneabus;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fast0n.mediterraneabus.search.SearchActivity;
import com.fast0n.mediterraneabus.timetables.TimetablesActivity;
import com.fast0n.mediterraneabus.info.InfoActivity;
import com.fast0n.mediterraneabus.recents.CustomAdapterRecents;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ListView listView;

    AdView mAdView;
    Bundle extras;
    Button departure, arrival;
    CardView list;
    CustomAdapterRecents adapter;
    DatabaseHelper mDatabaseHelper;
    DrawerLayout drawer;
    FloatingActionButton fab;
    ImageButton change;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    Spinner spinner;
    String change_str;
    TextView recent;
    Toolbar toolbar;
    NavigationView navigationView;

    private final static String ACTION_1 = "action1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // change name of toolbar
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.search));

        // java addresses
        arrival = findViewById(R.id.button2);
        change = findViewById(R.id.imageButton3);
        departure = findViewById(R.id.button);
        drawer = findViewById(R.id.drawer_layout);
        fab = findViewById(R.id.floatingActionButton);
        list = findViewById(R.id.cardView1);
        listView = findViewById(R.id.listrecent);
        recent = findViewById(R.id.textView);
        spinner = findViewById(R.id.spinner);

        // initial info extras and database
        extras = getIntent().getExtras();
        mDatabaseHelper = new DatabaseHelper(this);


        // check first boot and version app
        settings = getSharedPreferences("sharedPreferences", 0);
        String start = settings.getString("start", null);
        String version = settings.getString("version", null);
        editor = settings.edit();

        if (start == null) {
            editor.putString("start", "1");
            editor.apply();

            new MaterialDialog.Builder(this).onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(MaterialDialog dialog, DialogAction which) {
                    new Changelog(MainActivity.this, false);
                }
            }).cancelable(false).title(getString(R.string.title)).content(getString(R.string.content))
                    .positiveText(getString(R.string.positiveTextButton)).icon(getDrawable(R.drawable.ic_warning))
                    .show();

        } else {
            if (version == null) {
                editor.putString("version", BuildConfig.VERSION_NAME);
                editor.commit();
            }

            try {
                if (Integer.parseInt(version) != Integer.parseInt(BuildConfig.VERSION_NAME)) {

                    if (isOnline()) {
                        editor.putString("version", BuildConfig.VERSION_NAME);
                        editor.commit();
                        new Changelog(MainActivity.this, false);
                    } else
                        Toasty.error(MainActivity.this, getString(R.string.errorconnection), Toast.LENGTH_LONG, true)
                                .show();

                }
            } catch (Exception e) {

            }
        }

        // banner
        MobileAds.initialize(this, "ca-app-pub-9646303341923759~6818726547");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // make a list
        List<String> list_spinner = new ArrayList<String>();
        list_spinner.add(getString(R.string.school_hours));
        list_spinner.add(getString(R.string.not_school_hours));
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                list_spinner);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        // set INVISIBLE list and recent
        list.setVisibility(View.INVISIBLE);
        recent.setVisibility(View.INVISIBLE);
        // reload listView
        populateListView();

        try {
            if (extras.getString("departure") != null && extras.getString("arrival") != null
                    && extras.getString("period") != null) {
                departure.setText(extras.getString("departure"));
                arrival.setText(extras.getString("arrival"));
                departure.setTextColor(Color.BLACK);
                arrival.setTextColor(Color.BLACK);

            }
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), e.toString());
        }

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground);
        ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(getString(R.string.app_name), bm,
                getResources().getColor(R.color.task));
        MainActivity.this.setTaskDescription(taskDesc);

        try {
            if (extras.getString("departure") != null && extras.getString("getTextArrival") != null) {
                departure.setText(extras.getString("departure"));
                departure.setTextColor(Color.BLACK);

                if (!Objects.equals(extras.getString("getTextArrival"), "Arrivo")) {
                    arrival.setText(extras.getString("getTextArrival"));
                    arrival.setTextColor(Color.BLACK);
                }

            }

            else if (extras.getString("arrival") != null && extras.getString("getTextDeparture") != null) {
                arrival.setText(extras.getString("arrival"));
                arrival.setTextColor(Color.BLACK);

                if (!Objects.equals(extras.getString("getTextDeparture"), "Partenza")) {
                    departure.setText(extras.getString("getTextDeparture"));
                    departure.setTextColor(Color.BLACK);
                }

            }

        } catch (Exception e) {
            Log.e(getString(R.string.app_name), e.toString());
        }

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!departure.getText().equals(getString(R.string.departure))
                        && !arrival.getText().equals(getString(R.string.arrival))) {
                    change_str = departure.getText().toString();
                    departure.setText(arrival.getText());
                    arrival.setText(change_str);
                }
            }
        });

        departure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isOnline()) {

                    // passing strings between activity
                    Intent start = new Intent(MainActivity.this, SearchActivity.class);

                    if (!departure.getText().equals(getString(R.string.departure)))
                        start.putExtra("departure_text", departure.getText());

                    start.putExtra("type", "departure");

                    start.putExtra("activity", "departure");
                    start.putExtra("getTextArrival", arrival.getText());
                    startActivity(start);

                    // check location permission
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1);
                    }
                } else {
                    Toasty.error(MainActivity.this, getString(R.string.errorconnection), Toast.LENGTH_LONG, true)
                            .show();
                }
            }
        });

        arrival.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isOnline()) {
                    // passaggio di stringhe tra activity
                    Intent start = new Intent(MainActivity.this, SearchActivity.class);

                    if (!arrival.getText().equals(getString(R.string.arrival)))
                        start.putExtra("arrival_text", arrival.getText());

                    start.putExtra("type", "arrival");
                    start.putExtra("activity", "arrival");
                    start.putExtra("getTextDeparture", departure.getText());
                    startActivity(start);

                    // check location permission
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1);
                    }
                } else {
                    Toasty.error(MainActivity.this, getString(R.string.errorconnection), Toast.LENGTH_LONG, true)
                            .show();

                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!departure.getText().equals(getString(R.string.departure))
                        && !arrival.getText().equals(getString(R.string.arrival))) {

                    if (isOnline()) {

                        Intent start = new Intent(MainActivity.this, TimetablesActivity.class);

                        if (spinner.getSelectedItem().toString().equals(getString(R.string.school_hours))) {
                            start.putExtra("period", "invernale");
                        } else {
                            start.putExtra("period", "estiva");
                        }

                        start.putExtra("departure", departure.getText());
                        start.putExtra("arrival", arrival.getText());
                        startActivity(start);

                        editor.putString("sort", "0");
                        editor.commit();
                        try {
                            AddData(departure.getText().toString(), arrival.getText().toString());
                        } catch (Exception e) {
                            Log.e(getString(R.string.app_name), e.toString());

                        }

                    } else {
                        Toasty.error(MainActivity.this, getString(R.string.errorconnection), Toast.LENGTH_LONG, true)
                                .show();
                    }

                } else {
                    Toasty.error(MainActivity.this, getString(R.string.toast_departure_arrival), Toast.LENGTH_LONG,
                            true).show();

                }
            }

        });

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        try {
            switch (Objects.requireNonNull(getIntent().getAction())) {
            case ACTION_1:

                break;
            default:
                break;
            }
        } catch (NullPointerException ignored) {}

    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public void AddData(String departure, String arrival) {
        mDatabaseHelper.addData(departure, arrival);
    }

    private void populateListView() {

        final Cursor data = mDatabaseHelper.getData();
        List<String> dataRecents = new ArrayList<>();

        while (data.moveToNext()) {
            dataRecents.add(data.getString(1) + "\n" + data.getString(2));
            list.setVisibility(View.VISIBLE);
            recent.setVisibility(View.VISIBLE);

        }
        String[] arr = dataRecents.toArray(new String[dataRecents.size()]);

        // set data to Adapter
        adapter = new CustomAdapterRecents(MainActivity.this, R.layout.row_recent, R.id.name, arr);
        listView.setAdapter(adapter);

        // dynamic listview
        if (listView.getAdapter().getCount() > 4) {
            listView.getLayoutParams().height = 700;
            list.getLayoutParams().height = 750;

            listView.requestLayout();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = (String) parent.getItemAtPosition(position);
                departure.setText(name.split("\n")[0]);
                arrival.setText(name.split("\n")[1]);
                departure.setTextColor(Color.BLACK);
                arrival.setTextColor(Color.BLACK);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

                final String name = (String) adapterView.getItemAtPosition(position);

                new MaterialDialog.Builder(MainActivity.this).title(getString(R.string.delete))
                        .positiveText(getString(R.string.yes)).negativeText(getString(R.string.no))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                mDatabaseHelper.deleteName(name.split("\n")[0], name.split("\n")[1]);
                                populateListView();
                            }
                        })

                        .show();

                return false;
            }
        });

    }

    public void directSearch(View view) {
        Button button_name = (Button) view;

        String name = button_name.getText().toString();
        if (isOnline()) {

            Intent start = new Intent(MainActivity.this, TimetablesActivity.class);

            if (spinner.getSelectedItem().toString().equals(getString(R.string.school_hours))) {
                start.putExtra("period", "invernale");
            } else {
                start.putExtra("period", "estiva");
            }

            start.putExtra("departure", name.split("\n")[0]);
            start.putExtra("arrival", name.split("\n")[1]);
            startActivity(start);
            AddData(name.split("\n")[0], name.split("\n")[1]);
            editor.putString("sort", "0");
            editor.commit();

        } else
            Toasty.error(MainActivity.this, getString(R.string.errorconnection), Toast.LENGTH_LONG, true).show();

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            this.finish();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_search) {
            // Handle the camera action
        } else if (id == R.id.nav_setting) {
            Toasty.normal(MainActivity.this, getString(R.string.settings_alert), getDrawable(R.drawable.smile)).show();

        } else if (id == R.id.nav_info) {
            Intent mainActivity = new Intent(MainActivity.this, InfoActivity.class);
            startActivity(mainActivity);

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
