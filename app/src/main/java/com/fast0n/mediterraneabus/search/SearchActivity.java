package com.fast0n.mediterraneabus.search;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fast0n.mediterraneabus.GPSTracker;
import com.fast0n.mediterraneabus.MainActivity;
import com.fast0n.mediterraneabus.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class SearchActivity extends AppCompatActivity {

    ListView listView, listView2;
    EditText editText;
    InputMethodManager keyboard;
    String activity, getTextArrival, getTextDeparture;
    CardView list, list2;
    Bundle extras;
    Button deleteText;
    TextView txtNearest;

    ArrayAdapter<String> adapter;
    ArrayList<String> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        // set title activity in the toolbar
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.choose_route));

        // set color/text/icon in the task
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground);
        ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(getString(R.string.choose_route),
                bm, getResources().getColor(R.color.task));
        SearchActivity.this.setTaskDescription(taskDesc);

        // set row icon in the toolbar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // java addresses
        deleteText = findViewById(R.id.txtDelete);
        editText = findViewById(R.id.searchdata);
        list = findViewById(R.id.cardView1);
        list2 = findViewById(R.id.cardView2);
        listView = findViewById(R.id.showdata);
        listView2 = findViewById(R.id.shownearest);
        txtNearest = findViewById(R.id.txtNearest);

        // generates the list of routes
        listRoutes();
        extras = getIntent().getExtras();

        // set data to Adapter
        adapter = new ArrayAdapter<>(SearchActivity.this, R.layout.row_search, R.id.results, data);
        listView.setAdapter(adapter);

        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (isOnline()) {
            // if the arrival or departure are NOT empty displays the list
            if (extras.getString("departure_text") != null || extras.getString("arrival_text") != null) {
                deleteText.setVisibility(View.VISIBLE);
                list.setVisibility(View.VISIBLE);

            } else
                show_gps();

            if (extras.getString("departure_text") != null && Objects.equals(extras.getString("type"), "departure")) {
                editText.setText(extras.getString("departure_text"));
                list.setVisibility(View.VISIBLE);

                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.requestFocus();
                keyboard.showSoftInput(editText, 0);

            }

            if (extras.getString("arrival_text") != null && Objects.equals(extras.getString("type"), "arrival")) {
                editText.setText(extras.getString("arrival_text"));
                list.setVisibility(View.VISIBLE);

                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.requestFocus();
                keyboard.showSoftInput(editText, 0);

            }

            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editText.setFocusable(true);
                    editText.setFocusableInTouchMode(true);
                    editText.requestFocus();
                    keyboard.showSoftInput(editText, 0);

                }
            });

            listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                    String text = (listView2.getItemAtPosition(position).toString());
                    list.setVisibility(View.VISIBLE);
                    SearchActivity.this.adapter.getFilter().filter(text);
                    editText.setText(text);

                }
            });

            // clears the text in the field
            deleteText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editText.setText("");
                    list.setVisibility(View.INVISIBLE);
                    deleteText.setVisibility(View.INVISIBLE);
                }
            });

            // search data when text changes in edittext
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    SearchActivity.this.adapter.getFilter().filter(s);
                    list.setVisibility(View.VISIBLE);
                    deleteText.setVisibility(View.VISIBLE);
                    list2.setVisibility(View.INVISIBLE);
                    txtNearest.setVisibility(View.INVISIBLE);

                    if (s.toString().equals("")) {
                        list.setVisibility(View.INVISIBLE);

                        GPSTracker gps = new GPSTracker(SearchActivity.this);
                        gps = new GPSTracker(SearchActivity.this);
                        if (!gps.canGetLocation()) {
                            list2.setVisibility(View.INVISIBLE);
                            txtNearest.setVisibility(View.INVISIBLE);
                        } else {
                            show_gps();
                            list2.setVisibility(View.VISIBLE);
                            txtNearest.setVisibility(View.VISIBLE);
                        }

                    }

                }

                @Override
                public void afterTextChanged(Editable s) {

                }

            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    activity = extras.getString("activity");
                    getTextDeparture = extras.getString("getTextDeparture");
                    getTextArrival = extras.getString("getTextArrival");

                    Intent i = new Intent(SearchActivity.this, MainActivity.class);
                    i.putExtra(activity, adapter.getItem(position));
                    i.putExtra("getTextDeparture", getTextDeparture);
                    i.putExtra("getTextArrival", getTextArrival);
                    startActivity(i);
                }
            });
        } else {
            Toasty.error(SearchActivity.this, getString(R.string.errorconnection), Toast.LENGTH_LONG, true).show();
        }
    }

    public void show_gps() {
        GPSTracker gps = new GPSTracker(this);
        Geocoder geocoder = new Geocoder(SearchActivity.this, Locale.getDefault());
        if (ActivityCompat.checkSelfPermission(SearchActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(SearchActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SearchActivity.this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1);
        } else {

            gps = new GPSTracker(SearchActivity.this);

            if (gps.canGetLocation()) {

                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();

                geocoder = new Geocoder(SearchActivity.this, Locale.getDefault());
                List<Address> addresses = null;

                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);

                    if (addresses != null && !addresses.isEmpty()) {
                        list2.setVisibility(View.VISIBLE);
                        txtNearest.setVisibility(View.VISIBLE);

                        final ArrayList<String> listp = new ArrayList<>();
                        listp.add(addresses.get(0).getLocality());
                        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_list_item_1, listp);
                        listView2.setAdapter(adapter);

                    }

                } catch (IOException ignored) {
                }

            } else {

                gps.showSettingsAlert();
            }

        }

    }

    public void listRoutes() {

        SharedPreferences mSharedPreference1 = getSharedPreferences("listRoutes", 0);
        int size = mSharedPreference1.getAll().size();
        for (int i = 0; i < size; i++) {
            data.add(mSharedPreference1.getString(Integer.toString(i), null));
        }
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

            Intent mainActivity = new Intent(SearchActivity.this, MainActivity.class);
            mainActivity.putExtra("departure", extras.getString("departure_text"));
            mainActivity.putExtra("arrival", extras.getString("arrival_text"));
            startActivity(mainActivity);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();

        Intent mainActivity = new Intent(SearchActivity.this, MainActivity.class);
        mainActivity.putExtra("departure", extras.getString("departure_text"));
        mainActivity.putExtra("arrival", extras.getString("arrival_text"));
        startActivity(mainActivity);
    }

}