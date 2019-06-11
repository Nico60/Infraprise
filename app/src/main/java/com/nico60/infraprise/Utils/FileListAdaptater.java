package com.nico60.infraprise.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nico60.infraprise.R;

import java.util.ArrayList;

public class FileListAdaptater extends ArrayAdapter<ListItem> {

    private ArrayList<ListItem> arrayListItem;
    private Context context;

    public FileListAdaptater(Context context, int resource, ArrayList<ListItem> items) {
        super(context, resource, items);
        this.context = context;
        this.arrayListItem = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItem item = arrayListItem.get(position);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_view_items, null);
        TextView textView = (TextView) view.findViewById(R.id.textListView);
        textView.setText(item.getFileName());
        return view;
    }
}
