package com.fast0n.mediterraneabus.timetables;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.fast0n.mediterraneabus.R;

import java.util.ArrayList;

public class CustomAdapterTimetables extends ArrayAdapter<DataTimetables>  {

    private Context mContext;

    private static class ViewHolder {
        TextView txtRide, txtTime, txtName_time, txtTime1, txtName_time1, txtDuration;
    }

    CustomAdapterTimetables(ArrayList<DataTimetables> data, Context context) {
        super(context, R.layout.row_item, data);
        this.mContext = context;

    }


    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        DataTimetables dataTimetables = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder;

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.txtRide =  convertView.findViewById(R.id.ride);
            viewHolder.txtTime =  convertView.findViewById(R.id.time);
            viewHolder.txtName_time = convertView.findViewById(R.id.name_time);
            viewHolder.txtTime1 = convertView.findViewById(R.id.time1);
            viewHolder.txtName_time1 = convertView.findViewById(R.id.name_time1);
            viewHolder.txtDuration = convertView.findViewById(R.id.duration);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext,
                (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;

        viewHolder.txtRide.setText(dataTimetables.getRide());
        viewHolder.txtTime.setText(dataTimetables.getTime());
        viewHolder.txtName_time.setText(dataTimetables.getName_time());
        viewHolder.txtTime1.setText(dataTimetables.getTime1());
        viewHolder.txtName_time1.setText(dataTimetables.getName_time1());
        viewHolder.txtDuration.setText("⏱ " + dataTimetables.getDuration());
        return convertView;
    }
}