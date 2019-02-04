package com.fast0n.mediterraneabus.timetables;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.fast0n.mediterraneabus.R;

import java.util.ArrayList;

public class CustomAdapterTimetables extends ArrayAdapter<DataTimetables> {

    private Context context;

    CustomAdapterTimetables(Context context, ArrayList<DataTimetables> data) {
        super(context, R.layout.row_item, data);
        this.context = context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        DataTimetables dataTimetables = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.txtRide = convertView.findViewById(R.id.ride);
            viewHolder.txtTime = convertView.findViewById(R.id.time);
            viewHolder.txtName_time = convertView.findViewById(R.id.name_time);
            viewHolder.txtTime1 = convertView.findViewById(R.id.time1);
            viewHolder.txtName_time1 = convertView.findViewById(R.id.name_time1);
            viewHolder.txtDuration = convertView.findViewById(R.id.duration);
            viewHolder.button = convertView.findViewById(R.id.share);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();


        viewHolder.txtRide.setText(dataTimetables.getRide());
        viewHolder.txtTime.setText(dataTimetables.getTime());
        viewHolder.txtName_time.setText(dataTimetables.getName_time());
        viewHolder.txtTime1.setText(dataTimetables.getTime1());
        viewHolder.txtName_time1.setText(dataTimetables.getName_time1());
        viewHolder.txtDuration.setText(dataTimetables.getDuration());
        viewHolder.button.setText("options" + "::" +
                dataTimetables.getRide() + "::" +
                dataTimetables.getTime() + "::" +
                dataTimetables.getName_time() + "::" +
                dataTimetables.getTime1() + "::" +
                dataTimetables.getName_time1() + "::" +
                dataTimetables.getDuration());

        return convertView;
    }

    private static class ViewHolder {
        TextView txtRide, txtTime, txtName_time, txtTime1, txtName_time1, txtDuration;
        Button button;
    }
}