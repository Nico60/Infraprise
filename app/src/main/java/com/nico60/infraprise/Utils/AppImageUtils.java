package com.nico60.infraprise.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.nico60.infraprise.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static androidx.core.content.FileProvider.getUriForFile;
import static com.nico60.infraprise.MainActivity.isExternalStorageWritable;
import static java.lang.String.format;

public class AppImageUtils {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Activity mActivity;
    private String mFileName;
    private String mNroNum;
    private String mPmNum;
    private String mPoleType;
    private String mPoleView = "";

    public AppImageUtils(Activity activity, String nro, String pm, String type, String fileName) {
        mActivity = activity;
        mNroNum = nro;
        mPmNum = pm;
        mPoleType = type;
        mFileName = fileName;
    }

    public void poleViewDialog() {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_listview, null, false);
        ListView dialogListView = (ListView) view.findViewById(R.id.dialog_list_view);
        final String[] arrayView = mActivity.getResources().getStringArray(R.array.poleViewArray);
        final ArrayAdapter<String> dialogArrayAdapter = new ArrayAdapter<>(mActivity, R.layout.file_list_view_items,
                R.id.fileTextListView, arrayView);
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setTitle(R.string.pole_view_title);
        dialogListView.setAdapter(dialogArrayAdapter);
        dialogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 3) {
                    mPoleView = "";
                } else {
                    mPoleView = "_" + arrayView[position];
                }
                dispatchTakePictureIntent();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void dispatchTakePictureIntent() {
        if (isExternalStorageWritable()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = null;
            if (takePictureIntent.resolveActivity(mActivity.getPackageManager()) != null) {
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Toast.makeText(mActivity, "Exception: " + ex.toString(), Toast.LENGTH_LONG).show();
                    ex.printStackTrace();
                }
                if (photoFile != null) {
                    Uri photoURI = getUriForFile(mActivity,
                            mActivity.getString(R.string.file_provider),
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    mActivity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmm-ss").format(new Date());
        String imageFileName = mActivity.getString(R.string.nro) + mNroNum + "_" + mActivity.getString(R.string.pm) + mPmNum + "_" + mPoleType +
                mFileName + mPoleView + "_" + format("%s.jpg", timeStamp);
        return new File(createDirectory(), imageFileName);
    }

    private File createDirectory() {
        File rootDir = new File(Environment.getExternalStorageDirectory(), mActivity.getString(R.string.app_name));
        File nroDir = new File(rootDir, mActivity.getString(R.string.nro) + mNroNum);
        File pmDir = new File(nroDir, mActivity.getString(R.string.pm) + mPmNum);
        File fileType = new File(pmDir, mActivity.getString(R.string.sheet, mPoleType));
        File finalDir = new File(fileType, mPoleType + mFileName);
        if (!finalDir.exists()) {
            finalDir.mkdirs();
        }
        return finalDir;
    }
}
