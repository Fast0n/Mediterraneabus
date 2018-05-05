package com.fast0n.mediterraneabus.info;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fast0n.mediterraneabus.R;

import java.util.ArrayList;

public class CustomAdapterInfo extends ArrayAdapter<DataInfo> implements View.OnClickListener {

    private ArrayList<DataInfo> dataSet;
    private Context mContext;
    private int lastPosition = -1;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        ImageView icon;
    }

    CustomAdapterInfo(ArrayList<DataInfo> data, Context context) {
        super(context, R.layout.row_info, data);
        this.dataSet = data;
        this.mContext = context;

    }

    @Override
    public void onClick(View v) {


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DataInfo dataInfo = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_info, parent, false);
            viewHolder.txtName =convertView.findViewById(R.id.name);
            viewHolder.icon = convertView.findViewById(R.id.icon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        lastPosition = position;


        viewHolder.txtName.setText(dataInfo.getName());
        viewHolder.txtName.setText(Html.fromHtml(dataInfo.getName()));
        viewHolder.icon.setImageResource(dataInfo.getIcon());
        viewHolder.icon.setOnClickListener(this);
        viewHolder.icon.setTag(position);

        return convertView;
    }
}