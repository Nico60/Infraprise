package com.nico60.infraprise.Utils;

import android.app.Activity;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.nico60.infraprise.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;

import static com.nico60.infraprise.MainActivity.isExternalStorageWritable;
import static java.lang.String.format;

public class AppFileUtils {

    private Activity mActivity;
    private String mFileName;
    private String mNroNum;
    private String mPmNum;
    private String mPoleType;

    public AppFileUtils(Activity activity) {
        mActivity = activity;
    }

    public void copy(File file, File path) {
        try {
            if (file.isDirectory()) {
                FileUtils.copyDirectoryToDirectory(file, path);
            } else {
                FileUtils.copyFileToDirectory(file, path);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void move(File file, File path) {
        try {
            FileUtils.moveToDirectory(file, path, false);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (FileExistsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delete(File file) {
        try {
            if (file.isDirectory()) {
                String[] child = file.list();
                for (String del : child) {
                    File temp = new File(file, del);
                    if (temp.isDirectory()) {
                        delete(temp);
                    } else {
                        temp.delete();
                    }
                }
            }
            file.delete();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void save(String nro, String pm, String type, String fileName, String[] list) {
        mNroNum = nro;
        mPmNum = pm;
        mPoleType = type;
        mFileName = fileName;
        if (isExternalStorageWritable()) {
            File file = new File(createDirectory(), mActivity.getString(R.string.nro) + mNroNum + "_" +
                    mActivity.getString(R.string.pm) + mPmNum + "_" + mPoleType + format("%s.txt", mFileName));
            try {
                FileOutputStream out = new FileOutputStream(file);
                for (String listToAdd : list) {
                    out.write(("\n").getBytes());
                    out.write(listToAdd.getBytes());
                    out.write(("\n").getBytes());
                }
                out.close();
                Toast.makeText(mActivity, mActivity.getString(R.string.file_text_saved,
                        type + fileName), Toast.LENGTH_SHORT).show();
            } catch (Throwable t) {
                Toast.makeText(mActivity, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        }
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

    public File zipFolder(File zipFolder) {
        File ZipFile = new File(zipFolder.getParent(), format("%s.zip", zipFolder.getName()));
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(ZipFile));
            zipSubFolder(out, zipFolder, zipFolder.getPath().length());
            out.close();
            return ZipFile;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void zipSubFolder(ZipOutputStream out, File folder, int basePathLength) throws IOException {
        final int BUFFER = 2048;
        File[] fileList = folder.listFiles();
        BufferedInputStream origin;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte[] data = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath.substring(basePathLength + 1);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                entry.setTime(file.lastModified());
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
                out.closeEntry();
            }
        }
    }

    public String getFileExt(String ext) {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(ext));
    }
}
