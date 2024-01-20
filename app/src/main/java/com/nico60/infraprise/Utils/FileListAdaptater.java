package com.nico60.infraprise.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.nico60.infraprise.R;

import java.util.ArrayList;

public class FileListAdaptater extends ArrayAdapter<ListItem> {

    private ArrayList<ListItem> mArrayListItem;
    private final Context mContext;

    public FileListAdaptater(Context context, int resource, ArrayList<ListItem> items) {
        super(context, resource, items);
        mContext = context;
        mArrayListItem = items;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItem item = mArrayListItem.get(position);
        String itemText = item.getFileName();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint({"ViewHolder", "InflateParams"}) View view = inflater.inflate(R.layout.file_list_view_items, null);
        TextView textView = (TextView) view.findViewById(R.id.fileTextListView);
        ImageView imageView = (ImageView) view.findViewById(R.id.fileImageListView);
        textView.setText(itemText);
        if (itemText.endsWith(".txt")) {
            textView.setTypeface(null, Typeface.NORMAL);
            imageView.setImageResource(R.drawable.ic_txt_file_icon);
        } else if (itemText.endsWith(".jpg")) {
            textView.setTypeface(null, Typeface.NORMAL);
            imageView.setImageResource(R.drawable.ic_jpg_file_icon);
        } else if (itemText.endsWith(".zip")) {
            imageView.setImageResource(R.drawable.ic_zip_folder_icon);
        } else {
            imageView.setImageResource(R.drawable.ic_folder_icon);
        }

        return view;
    }
}
