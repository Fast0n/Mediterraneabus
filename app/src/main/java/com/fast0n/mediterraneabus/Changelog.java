package com.fast0n.mediterraneabus;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;

public class Changelog {
    public Changelog(final Context context, final boolean cancellable) {

        final String url = "https://gcr-api.herokuapp.com/";

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            JSONObject json_raw = new JSONObject(response.toString());
                            String linee = json_raw.getString("release");
                            JSONArray lineeArr = new JSONArray(linee);

                            JSONObject scorro_orari = new JSONObject(lineeArr.getString(0));

                            String version = scorro_orari.getString("version");
                            String description = scorro_orari.getString("description");

                            if (Build.VERSION.SDK_INT >= 24) {
                                MaterialDialog dialog = new MaterialDialog.Builder(context)
                                        .title(context.getString(R.string.news)).cancelable(cancellable)
                                        .contentColor(Color.BLACK)
                                        .content(Html.fromHtml(
                                                "<strong>" + context.getString(R.string.version) + " " + version
                                                        + "</strong><br />" + description.replace("*  ", "<br />\t•"),
                                                Html.FROM_HTML_MODE_LEGACY))
                                        .positiveText(context.getString(R.string.close)).show();

                            } else {
                                MaterialDialog dialog = new MaterialDialog.Builder(context)
                                        .title(context.getString(R.string.news)).cancelable(cancellable)
                                        .contentColor(Color.BLACK)
                                        .content(Html.fromHtml("<h4>v" + version + "</h4>"
                                                + description.replace("####", "<strong>").replace("*  ", "<br />\t•")))
                                        .positiveText(context.getString(R.string.close)).show();
                            }

                        } catch (JSONException ignored) {
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toasty.error(context, context.getString(R.string.errorchangelog), Toast.LENGTH_SHORT, true)
                                .show();

                    }
                });

        queue.add(getRequest);
    }
}
