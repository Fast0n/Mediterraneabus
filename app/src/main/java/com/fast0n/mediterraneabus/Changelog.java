package com.fast0n.mediterraneabus;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.fast0n.mediterraneabus.java.SnackbarMaterial;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class Changelog {
    public Changelog(final Context context, final boolean cancellable, CoordinatorLayout coordinatorLayout) {

        final String url = "http://51.15.222.184:1339/";

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray json_raw = new JSONArray(response.toString());


                        String partenza2 = json_raw.getString(0);


                        JSONObject scorrOrari = new JSONObject(partenza2);
                        String version = scorrOrari.getString("version");
                        String description = scorrOrari.getString("description");


                        if (Build.VERSION.SDK_INT >= 24) {
                            new MaterialDialog.Builder(context)
                                    .title(context.getString(R.string.news)).cancelable(cancellable)
                                    .contentColor(Color.BLACK)
                                    .content(Html.fromHtml(
                                            "<strong>" + context.getString(R.string.version) + " " + version
                                                    + "</strong><br />" + description.replace("- ", "<br />\t•"),
                                            Html.FROM_HTML_MODE_LEGACY))
                                    .positiveText(context.getString(R.string.close)).show();

                        } else {
                            new MaterialDialog.Builder(context)
                                    .title(context.getString(R.string.news)).cancelable(cancellable)
                                    .contentColor(Color.BLACK)
                                    .content(Html.fromHtml("<h4>v" + version + "</h4>"
                                            + description.replace("####", "<strong>").replace("-  ", "<br />\t•")))
                                    .positiveText(context.getString(R.string.close)).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }, error -> {

            Log.e(String.valueOf(R.string.app_name), error.toString());
            Snackbar snack = Snackbar.make(coordinatorLayout,
                    context.getString(R.string.errorchangelog), Snackbar.LENGTH_SHORT).setAnchorView(R.id.layout);
            SnackbarMaterial.configSnackbar(context.getApplicationContext(), snack);
            snack.show();


        });

        queue.add(getRequest);


    }
}
