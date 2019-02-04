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
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.fast0n.mediterraneabus.GPSTracker;
import com.fast0n.mediterraneabus.MainActivity;
import com.fast0n.mediterraneabus.R;
import com.tedpark.tedpermission.rx2.TedRx2Permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;


public class SearchActivity extends AppCompatActivity {

    ListView listView, listView2;
    EditText editText;
    InputMethodManager keyboard;
    String activity, getTextArrival, getTextDeparture;
    CardView list, list2, cardLocation;
    Bundle extras;
    Button deleteText, btn_location;
    TextView txtNearest;
    GPSTracker gps;
    Geocoder geocoder;

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
        cardLocation = findViewById(R.id.cardViewLocation);
        listView = findViewById(R.id.showdata);
        listView2 = findViewById(R.id.shownearest);
        txtNearest = findViewById(R.id.txtNearest);
        btn_location = findViewById(R.id.btn_location);
        gps = new GPSTracker(SearchActivity.this);
        geocoder = new Geocoder(SearchActivity.this, Locale.getDefault());


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

            }

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

            editText.setOnClickListener(v -> {
                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.requestFocus();
                keyboard.showSoftInput(editText, 0);

            });

            listView2.setOnItemClickListener((arg0, arg1, position, arg3) -> {

                String text = (listView2.getItemAtPosition(position).toString());
                list.setVisibility(View.VISIBLE);
                SearchActivity.this.adapter.getFilter().filter(text);
                editText.setText(text);

            });

            // clears the text in the field
            deleteText.setOnClickListener(v -> {
                editText.setText("");
                deleteText.setVisibility(View.INVISIBLE);
            });

            // search data when text changes in edittext
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    SearchActivity.this.adapter.getFilter().filter(s);
                    deleteText.setVisibility(View.VISIBLE);
                    show_gps();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }

            });

            listView.setOnItemClickListener((parent, view, position, id) -> {
                activity = extras.getString("activity");
                getTextDeparture = extras.getString("getTextDeparture");
                getTextArrival = extras.getString("getTextArrival");

                Intent i = new Intent(SearchActivity.this, MainActivity.class);
                i.putExtra(activity, adapter.getItem(position));
                i.putExtra("getTextDeparture", getTextDeparture);
                i.putExtra("getTextArrival", getTextArrival);
                startActivity(i);
            });
        }


        btn_location.setOnClickListener(view -> {

            // check location permission
            TedRx2Permission.with(this)
                    .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    .request()
                    .subscribe(tedPermissionResult -> {
                        if (tedPermissionResult.isGranted()) {
                            cardLocation.setVisibility(View.GONE);
                            show_gps();
                            txtNearest.setVisibility(View.VISIBLE);
                            list2.setVisibility(View.VISIBLE);

                            Intent intent = getIntent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            finish();
                            startActivity(intent);
                        } else
                            gps.showSettingsAlert();

                    }, throwable -> {
                    });
        });

        if (ActivityCompat.checkSelfPermission(SearchActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(SearchActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            cardLocation.setVisibility(View.GONE);
            show_gps();
            txtNearest.setVisibility(View.VISIBLE);
            list2.setVisibility(View.VISIBLE);
        }


    }

    public void show_gps() {

        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {

                SharedPreferences mSharedPreference1 = getSharedPreferences("listRoutes", 0);
                int size = mSharedPreference1.getAll().size();

                String location = addresses.get(0).getLocality();
                for (int i = 0; i < size; i++) {

                    String elemento_lista = mSharedPreference1.getString(Integer.toString(i), null);
                    Log.e(String.valueOf(R.string.app_name), elemento_lista);

                    if (elemento_lista.contains(location)) {
                        final ArrayList<String> listp = new ArrayList<>();
                        listp.add(addresses.get(0).getLocality());
                        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_list_item_1, listp);
                        adapter.notifyDataSetChanged();
                        listView2.setAdapter(adapter);
                    }

                }

            }
            gps.stopUsingGPS();
        } catch (IOException ignored) {
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
        return Objects.requireNonNull(cm).getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
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