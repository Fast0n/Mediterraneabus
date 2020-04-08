package com.fast0n.mediterraneabus.recents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.fast0n.mediterraneabus.R;

public class CustomAdapterRecents extends ArrayAdapter<String> {
    private int groupid;
    private String[] item_list;
    private Context context;

    public CustomAdapterRecents(Context context, int vg, int id, String[] item_list) {
        super(context, vg, id, item_list);
        this.context = context;
        groupid = vg;
        this.item_list = item_list;
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View rowView = convertView;


        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            rowView = inflater.inflate(groupid, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.textview = rowView.findViewById(R.id.name);
            viewHolder.button = rowView.findViewById(R.id.item_info);
            viewHolder.button2 = rowView.findViewById(R.id.item_info2);
            rowView.setTag(viewHolder);

        }
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.textview.setText(item_list[position]);
        holder.button.setText("search::" + item_list[position]);
        holder.button2.setText("options::" + item_list[position]);


        return rowView;
    }

    static class ViewHolder {
        public TextView textview;
        public Button button, button2;

    }

}