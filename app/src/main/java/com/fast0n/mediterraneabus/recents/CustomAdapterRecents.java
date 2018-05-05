package com.fast0n.mediterraneabus.recents;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

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

    static class ViewHolder {
        public TextView textview;
        public Button button;

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
            rowView.setTag(viewHolder);

        }
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.textview.setText(item_list[position]);
        holder.button.setText(item_list[position]);
        return rowView;
    }

}