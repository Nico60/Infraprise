package com.nico60.infraprise.Utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.nico60.infraprise.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.support.v4.content.FileProvider.getUriForFile;
import static com.nico60.infraprise.MainActivity.isExternalStorageWritable;
import static java.lang.String.format;

public class ImageUtils {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Activity activity;
    private String mFileName;
    private String mNroNum;
    private String mPmNum;
    private String mPoleType;

    public ImageUtils(Activity activity) {
        this.activity = activity;
    }

    public void dispatchTakePictureIntent(String nro, String pm, String type, String fileName) {
        mNroNum = nro;
        mPmNum = pm;
        mPoleType = type;
        mFileName = fileName;
        if (isExternalStorageWritable()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Toast.makeText(activity, "Exception: " + ex.toString(), Toast.LENGTH_LONG).show();
                    ex.printStackTrace();
                }
                if (photoFile != null) {
                    Uri photoURI = getUriForFile(activity,
                            activity.getString(R.string.file_provider),
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String imageFileName = activity.getString(R.string.nro) + mNroNum + "_" + activity.getString(R.string.pm) + mPmNum + "_" + mPoleType +
                mFileName + "_" + format("%s.jpg", timeStamp);
        return new File(createDirectory(), imageFileName);
    }

    private File createDirectory() {
        File rootDir = new File(Environment.getExternalStorageDirectory(), activity.getString(R.string.app_name));
        File nroDir = new File(rootDir, activity.getString(R.string.nro) + mNroNum);
        File pmDir = new File(nroDir, activity.getString(R.string.pm) + mPmNum);
        File fileType = new File(pmDir, activity.getString(R.string.sheet, mPoleType));
        File finalDir = new File(fileType, mPoleType + mFileName);
        if (!finalDir.exists()) {
            finalDir.mkdirs();
        }
        return finalDir;
    }
}
