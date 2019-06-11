package com.nico60.infraprise.Utils;

import android.app.Activity;
import android.os.Environment;
import android.widget.Toast;

import com.nico60.infraprise.R;

import java.io.File;
import java.io.FileOutputStream;

import static com.nico60.infraprise.MainActivity.isExternalStorageWritable;
import static java.lang.String.format;

public class FileUtils {

    private Activity activity;
    private String mFileName;
    private String mNroNum;
    private String mPmNum;
    private String mPoleType;

    public FileUtils(Activity activity) {
        this.activity = activity;
    }

    public void Save(String nro, String pm, String type, String fileName, String[] list) {
        mNroNum = nro;
        mPmNum = pm;
        mPoleType = type;
        mFileName = fileName;
        if (isExternalStorageWritable()) {
            File file = new File(createDirectory(), activity.getString(R.string.nro) + mNroNum + "_" +
                    activity.getString(R.string.pm) + mPmNum + "_" + mPoleType + format("%s.txt", mFileName));
            try {
                FileOutputStream out = new FileOutputStream(file);
                for (String listToAdd : list) {
                    out.write(("\n").getBytes());
                    out.write(listToAdd.getBytes());
                    out.write(("\n").getBytes());
                }
                out.close();
                Toast.makeText(activity, type + fileName + activity.getString(R.string.file_saved), Toast.LENGTH_SHORT).show();
            } catch (Throwable t) {
                Toast.makeText(activity, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        }
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
