package com.nico60.infraprise.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nico60.infraprise.R;

import java.util.ArrayList;

public class FileListAdaptater extends ArrayAdapter<ListItem> {

    private ArrayList<ListItem> mArrayListItem;
    private Context mContext;

    public FileListAdaptater(Context context, int resource, ArrayList<ListItem> items) {
        super(context, resource, items);
        mContext = context;
        mArrayListItem = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItem item = mArrayListItem.get(position);
        String itemText = item.getFileName();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_view_items, null);
        TextView textView = (TextView) view.findViewById(R.id.textListView);
        textView.setText(itemText);
        if (itemText.endsWith(".txt") || itemText.endsWith(".jpg")) {
            textView.setTypeface(null, Typeface.NORMAL);
        }
        return view;
    }
}
