package com.fast0n.mediterraneabus;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fast0n.mediterraneabus.info.InfoActivity;
import com.fast0n.mediterraneabus.java.SnackbarMaterial;
import com.fast0n.mediterraneabus.recents.CustomAdapterRecents;
import com.fast0n.mediterraneabus.search.SearchActivity;
import com.fast0n.mediterraneabus.timetables.TimetablesActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    final int[] select = {1};
    PowerMenu powerMenu;
    AdView mAdView;
    ActionBarDrawerToggle toggle;
    Bundle extras;
    Button departure, arrival, button_search, button, more;
    CardView list;
    CustomAdapterRecents adapter;
    DatabaseHelper mDatabaseHelper;
    ImageButton change;
    SharedPreferences settings, lista;
    SharedPreferences.Editor editor;
    Spinner spinner;
    String change_str, name;
    Toolbar toolbar;
    Vibrator v;
    ProgressBar progressBar;
    CoordinatorLayout coordinatorLayout;
    private ListView listView;
    private OnMenuItemClickListener<PowerMenuItem> onMenuItemClickListener = new OnMenuItemClickListener<PowerMenuItem>() {
        @Override
        public void onItemClick(int position, PowerMenuItem item) {

            powerMenu.setSelectedPosition(position);

            Log.e("prova", name);
            vibrator();


            new MaterialDialog.Builder(MainActivity.this).title(getString(R.string.sure))
                    .positiveText(getString(R.string.yes)).negativeText(getString(R.string.no))
                    .onPositive((dialog, which) -> {
                        mDatabaseHelper.deleteName(name.split("::")[1].split("\n")[0], name.split("::")[1].split("\n")[1]);

                        // set INVISIBLE list and recent
                        list.setVisibility(View.INVISIBLE);
                        // reload listView
                        populateListView();

                        // update item
                        editor.putString("item", null);
                        editor.commit();

                        select[0] = 1;
                    })

                    .show();


            powerMenu.dismiss();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // java addresses
        mAdView = findViewById(R.id.adView);
        arrival = findViewById(R.id.button2);
        change = findViewById(R.id.imageButton3);
        coordinatorLayout = findViewById(R.id.cordinatorLayout);
        departure = findViewById(R.id.button);
        button_search = findViewById(R.id.button_search);
        list = findViewById(R.id.cardView1);
        listView = findViewById(R.id.listrecent);
        spinner = findViewById(R.id.spinner);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        button = toolbar.findViewById(R.id.info);
        more = toolbar.findViewById(R.id.more);


        button.setOnClickListener(view -> {
            //
            startActivity(new Intent(getApplicationContext(), InfoActivity.class));

        });


        more.setOnClickListener(view -> {
            //Creating the instance of PopupMenu
            PopupMenu popup = new PopupMenu(MainActivity.this, button);
            //Inflating the Popup using xml file
            popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(item -> {

                switch (item.getItemId()) {
                    case R.id.settings:
                        Snackbar snack = Snackbar.make(coordinatorLayout,
                                getString(R.string.settings_alert), Snackbar.LENGTH_SHORT).setAnchorView(R.id.layout);
                        SnackbarMaterial.configSnackbar(getApplicationContext(), snack);
                        snack.show();
                        break;
                }
                return true;
            });

            popup.show();
        });

        // banner
        MobileAds.initialize(this, "ca-app-pub-9646303341923759~6818726547");
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        // initial info extras and database
        extras = getIntent().getExtras();
        mDatabaseHelper = new DatabaseHelper(this);

        // check first boot and version app
        settings = getSharedPreferences("sharedPreferences", 0);
        String start = settings.getString("start", null);
        final String version = settings.getString("version", null);
        final String version_code = settings.getString("version_code", null);
        editor = settings.edit();

        if (start == null) {
            editor.putString("start", "1");
            editor.apply();

            new MaterialDialog.Builder(this).onPositive((dialog, which) -> {
                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage(getString(R.string.changelog_loading));
                listRoutes(getString(R.string.server) + "?lista");
                progressDialog.show();
                if (version == null) {

                    editor.putString("version", BuildConfig.VERSION_NAME);
                    editor.putString("version_code", String.valueOf(BuildConfig.VERSION_CODE));
                    editor.apply();
                    progressDialog.hide();
                    new Changelog(MainActivity.this, false, coordinatorLayout);
                }
            }).cancelable(false).title(getString(R.string.title)).content(getString(R.string.content))
                    .positiveText(getString(R.string.positiveTextButton)).icon(getDrawable(R.drawable.ic_warning))
                    .show();

        } else {

            try {
                if (Integer.parseInt(version_code) != Integer.parseInt(String.valueOf(BuildConfig.VERSION_CODE))) {
                    editor.putString("version_code", String.valueOf(BuildConfig.VERSION_CODE));
                    editor.apply();
                    listRoutes(getString(R.string.server) + "?lista");
                }
            } catch (Exception e) {
                editor.putString("version_code", String.valueOf(BuildConfig.VERSION_CODE));
                editor.apply();
                listRoutes(getString(R.string.server) + "?lista");
            }

            if (Float.parseFloat(version) != Float.parseFloat(BuildConfig.VERSION_NAME)) {
                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage(getString(R.string.changelog_loading));
                progressDialog.show();
                editor.putString("version", BuildConfig.VERSION_NAME);
                editor.apply();
                progressDialog.hide();
                new Changelog(MainActivity.this, false, coordinatorLayout);
            }
        }

        // make a list
        List<String> list_spinner = new ArrayList<>();
        list_spinner.add(getString(R.string.school_hours));
        list_spinner.add(getString(R.string.not_school_hours));
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_text, list_spinner);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        // set INVISIBLE list and recent
        list.setVisibility(View.INVISIBLE);
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

            } else if (extras.getString("arrival") != null && extras.getString("getTextDeparture") != null) {
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

        change.setOnClickListener(v -> {
            if (!departure.getText().equals(getString(R.string.departure))
                    && !arrival.getText().equals(getString(R.string.arrival))) {
                change_str = departure.getText().toString();
                departure.setText(arrival.getText());
                arrival.setText(change_str);
            }
        });

        departure.setOnClickListener(v -> {
            if (isOnline()) {
                listRoutes(getString(R.string.server) + "?lista");
                // passing strings between activity
                Intent start1 = new Intent(MainActivity.this, SearchActivity.class);

                if (!departure.getText().equals(getString(R.string.departure)))
                    start1.putExtra("departure_text", departure.getText());

                start1.putExtra("type", "departure");

                start1.putExtra("activity", "departure");
                start1.putExtra("getTextArrival", arrival.getText());
                startActivity(start1);

            } else {
                Snackbar snack = Snackbar.make(coordinatorLayout,
                        getString(R.string.errorconnection), Snackbar.LENGTH_SHORT).setAnchorView(R.id.layout);
                SnackbarMaterial.configSnackbar(this, snack);
                snack.show();
            }
        });

        arrival.setOnClickListener(v -> {

            if (isOnline()) {
                listRoutes(getString(R.string.server) + "?lista");

                // passaggio di stringhe tra activity
                Intent start12 = new Intent(MainActivity.this, SearchActivity.class);

                if (!arrival.getText().equals(getString(R.string.arrival)))
                    start12.putExtra("arrival_text", arrival.getText());

                start12.putExtra("type", "arrival");
                start12.putExtra("activity", "arrival");
                start12.putExtra("getTextDeparture", departure.getText());
                startActivity(start12);


            } else {
                Snackbar snack = Snackbar.make(coordinatorLayout,
                        getString(R.string.errorconnection), Snackbar.LENGTH_SHORT).setAnchorView(R.id.layout);
                SnackbarMaterial.configSnackbar(this, snack);
                snack.show();

            }
        });

        button_search.setOnClickListener(view -> {
            if (!departure.getText().equals(getString(R.string.departure))
                    && !arrival.getText().equals(getString(R.string.arrival))) {

                if (isOnline()) {

                    Animation fadeInAnimation = AnimationUtils.loadAnimation(view.getContext(), android.R.anim.fade_in);
                    fadeInAnimation.setDuration(10);
                    view.startAnimation(fadeInAnimation);

                    Intent startIntent = new Intent(MainActivity.this, TimetablesActivity.class);

                    if (spinner.getSelectedItem().toString().equals(getString(R.string.school_hours))) {
                        startIntent.putExtra("period", "invernale");
                    } else {
                        startIntent.putExtra("period", "estiva");
                    }

                    startIntent.putExtra("departure", departure.getText());
                    startIntent.putExtra("arrival", arrival.getText());
                    startActivity(startIntent);

                    editor.putString("sort", "0");
                    editor.commit();
                    try {
                        AddData(departure.getText().toString(), arrival.getText().toString());
                    } catch (Exception e) {
                        Log.e(getString(R.string.app_name), e.toString());

                    }

                } else {
                    Snackbar snack = Snackbar.make(coordinatorLayout,
                            getString(R.string.errorconnection), Snackbar.LENGTH_SHORT).setAnchorView(R.id.layout);
                    SnackbarMaterial.configSnackbar(this, snack);
                    snack.show();
                }

            } else {
                Snackbar snack = Snackbar.make(coordinatorLayout,
                        getString(R.string.toast_departure_arrival), Snackbar.LENGTH_SHORT).setAnchorView(R.id.layout);
                SnackbarMaterial.configSnackbar(this, snack);
                snack.show();


            }
        });


    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return Objects.requireNonNull(cm).getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
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

        }
        String[] arr = dataRecents.toArray(new String[0]);

        // set data to Adapter
        adapter = new CustomAdapterRecents(MainActivity.this, R.layout.row_recent, R.id.name, arr);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener((parent, view, position, id) -> {

            if (select[0] == 1) {
                String name = (String) parent.getItemAtPosition(position);
                departure.setText(name.split("\n")[0]);
                arrival.setText(name.split("\n")[1]);
                departure.setTextColor(Color.BLACK);
                arrival.setTextColor(Color.BLACK);
                select[0] = 2;
            }
        });


    }

    public void listRoutes(String url) {


        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {

                        JSONObject json_raw = new JSONObject(response.toString());
                        String linee = json_raw.getString("list");

                        JSONObject lineeArr = new JSONObject(linee);

                        String description = lineeArr.getString("routes");

                        JSONArray lineeArr2 = new JSONArray(description);
                        for (int i = 0; i < lineeArr2.length(); i++) {
                            String corse = lineeArr2.getString(i);

                            lista = getSharedPreferences("listRoutes", 0);
                            editor = lista.edit();
                            editor.putString(Integer.toString(i), corse);
                            editor.apply();
                        }


                    } catch (JSONException ignored) {
                    }
                }, error -> listRoutes(getString(R.string.server) + "?lista"));
        queue.add(getRequest);

    }

    public void directSearch(View view) {
        Button button_name = (Button) view;
        name = button_name.getText().toString();
        if (isOnline()) {
            if (name.split("::")[0].equals("options")) {
                powerMenu = new PowerMenu.Builder(this)
                        .addItem(new PowerMenuItem(getString(R.string.delete), false))
                        .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT)
                        .setMenuRadius(10f) // sets the corner radius.
                        .setMenuShadow(10f) // sets the corner radius.
                        .setShowBackground(false) // sets the shadow.
                        .setTextColor(this.getResources().getColor(android.R.color.black))
                        .setSelectedTextColor(Color.WHITE)
                        .setMenuColor(Color.WHITE)
                        .setOnMenuItemClickListener(onMenuItemClickListener)
                        .build();
                powerMenu.showAsDropDown(view, -5, 0);
            } else {
                Intent start = new Intent(MainActivity.this, TimetablesActivity.class);

                if (spinner.getSelectedItem().toString().equals(getString(R.string.school_hours))) {
                    start.putExtra("period", "invernale");
                } else {
                    start.putExtra("period", "estiva");
                }

                start.putExtra("departure", name.split("::")[1].split("\n")[0]);
                start.putExtra("arrival", name.split("\n")[1]);
                startActivity(start);
                AddData(name.split("::")[1].split("\n")[0], name.split("\n")[1]);
                editor.putString("sort", "0");
                editor.commit();

            }

        } else {
            Snackbar snack = Snackbar.make(coordinatorLayout,
                    getString(R.string.errorconnection), Snackbar.LENGTH_SHORT).setAnchorView(R.id.layout);
            SnackbarMaterial.configSnackbar(this, snack);
            snack.show();

        }
    }

    public void vibrator() {

        // Vibrate for 50 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            // deprecated in API 26
            v.vibrate(50);
        }
    }

    @Override
    public void onBackPressed() {

        final String item = settings.getString("item", null);

        if (select[0] == 2 && item != null) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.search));
            select[0] = 1;
            // set INVISIBLE list and recent
            list.setVisibility(View.INVISIBLE);
            // reload listView
            populateListView();

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


}
