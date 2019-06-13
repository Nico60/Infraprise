package com.nico60.infraprise;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.nico60.infraprise.Utils.FileListAdaptater;
import com.nico60.infraprise.Utils.ListItem;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static android.support.v4.content.FileProvider.getUriForFile;
import static java.lang.String.format;

public class FilesListActivity extends AppCompatActivity {

    private static final int REQUEST_FILE_VIEW = 1;
    private static final int REQUEST_FILE_SEND = 2;

    private ArrayList<ListItem> mArrayListItem;
    private ArrayList<String> mItem;
    private ArrayList<Uri> mZipUriList;
    private File mCurrentList;
    private File mNewList;
    private File mOldDir;
    private File mRootDir;
    private FileListAdaptater mAdapter;
    private int mCheckedPosition;
    private Intent mSendIntent;
    private Intent mViewIntent;
    private ListView mListView;
    private String mExtType;
    private Uri mZipUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_list);

        Toolbar fileToolbar = findViewById(R.id.fileToolbar);

        mArrayListItem = new ArrayList<>();
        mItem = new ArrayList<>();
        mZipUriList = new ArrayList<>();

        mAdapter = new FileListAdaptater(this, R.layout.list_view_items, mArrayListItem);

        mListView = findViewById(R.id.files_listview);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new ModeCallback());
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                openClickedItem(position);
            }
        });
        mListView.setAdapter(mAdapter);

        mRootDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
        if (!mRootDir.exists()) {
            mRootDir.mkdirs();
        }
        openDir(mRootDir);

        setSupportActionBar(fileToolbar);
    }

    private class ModeCallback implements ListView.MultiChoiceModeListener {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.files_list_menu, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mZipUriList.clear();
            mItem.clear();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (mListView.getCount() == 1) {
                menu.removeItem(R.id.select_all);
                return true;
            }
            if (mListView.getCheckedItemCount() > 1) {
                menu.removeItem(R.id.open);
                return true;
            }
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    deleteFileItem();
                    mode.finish();
                    return true;
                case R.id.send:
                    sendFileItem();
                    mode.finish();
                    return true;
                case R.id.open:
                    openClickedItem(mCheckedPosition);
                    mode.finish();
                    return true;
                case R.id.select_all:
                    selectAllItem();
                    return true;
                case R.id.deselect_all:
                    deselectAllItem();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            mCheckedPosition = position;
            int checkedItems = mListView.getCheckedItemCount();
            mode.setTitle(checkedItems + getString(R.string.item_selected));
            if (checked) {
                invalidateOptionsMenu();
            }
        }
    }

    private ArrayList<String> itemToString() {
        SparseBooleanArray checked = mListView.getCheckedItemPositions();
        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                mItem.add(mArrayListItem.get(checked.keyAt(i)).getFileName());
            }
        }
        return mItem;
    }

    private void openClickedItem(int position) {
        String name = mArrayListItem.get(position).getFileName();
        mNewList = new File(mCurrentList, name);
        openDir(mNewList);
    }

    private void openDir(File file) {
        if (file.isDirectory()) {
            mOldDir = file.getParentFile();
            mCurrentList = file;
            mNewList = mCurrentList;
            updateList(file);
        } else {
            mNewList = mCurrentList;
            openFile(file);
        }
    }

    private void openFile(File file) {
        Uri uri = getFileUri(file);
        mViewIntent = new Intent();
        mViewIntent.setAction(Intent.ACTION_VIEW);
        mViewIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);
        mViewIntent.putExtra(Intent.EXTRA_STREAM, uri);
        if (uri != null) {
            mExtType = getFileExt(uri.toString());
        }
        mViewIntent.setDataAndType(uri, mExtType == null ? "text/plain" : mExtType);
        startActivityForResult(mViewIntent, REQUEST_FILE_VIEW);
    }

    private void deleteFileItem() {
        for (String item : itemToString()) {
            File deleteFile = new File(mNewList, item);
            deleteFile(deleteFile);
        }
    }

    private void deleteFile(File file) {
        if (file.isDirectory()) {
            String[] child = file.list();
            for (String del : child) {
                File temp = new File(file, del);
                if (temp.isDirectory()) {
                    deleteFile(temp);
                } else {
                    temp.delete();
                }
            }
        }
        file.delete();
        updateList(mNewList);
    }

    private void sendFileItem() {
        for(String itemPath : itemToString()) {
            File fileUri = new File(mCurrentList, itemPath);
            if (fileUri.isDirectory()) {
                mZipUri = getFileUri(zipFolder(fileUri));
            } else {
                mZipUri = getFileUri(fileUri);
            }
            if (mZipUri != null) {
                mZipUriList.add(mZipUri);
            }
        }
        if (mZipUriList.size() >= 1 || mZipUri != null) {
            mSendIntent = new Intent();
            mSendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            mExtType = getFileExt(mZipUriList.toString());
            mSendIntent.setType(mExtType == null ? "text/plain" : mExtType);
            mSendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, mZipUriList);
            startActivityForResult(mSendIntent, REQUEST_FILE_SEND);
        }
    }

    private void selectAllItem() {
        for (int i = 0; i < mListView.getCount(); i++) {
            mListView.setItemChecked(i, true);
        }
    }

    private void deselectAllItem() {
        mItem.clear();
    }

    private File zipFolder(File zipFolder) {
        File ZipFile = new File(zipFolder.getParent(), format("%s.zip", zipFolder.getName()));
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(ZipFile));
            zipSubFolder(out, zipFolder, zipFolder.getPath().length());
            out.close();
            updateList(mCurrentList);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FILE_VIEW && resultCode == RESULT_OK) {
            startActivity(Intent.createChooser(mViewIntent, "View using"));
        }
        if (requestCode == REQUEST_FILE_SEND && resultCode == RESULT_OK) {
            startActivity(Intent.createChooser(mSendIntent, "Send using"));
        }
    }

    private void updateList(File file) {
        mArrayListItem.clear();
        File[] list = file.listFiles();
        ArrayList<File> fileList = new ArrayList<>(Arrays.asList(list));
        Collections.sort(fileList);
        for(File fileAdded : fileList) {
            mArrayListItem.add(new ListItem(fileAdded.getName()));
        }
        mAdapter.notifyDataSetChanged();
    }

    private String getFileExt(String ext) {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(ext));
    }

    private Uri getFileUri(File file) {
        try {
            return getUriForFile(this,
                    getString(R.string.file_provider),
                    file);
        } catch (NullPointerException npe) {
            Toast.makeText(this, getString(R.string.zip_warning), Toast.LENGTH_LONG).show();
            npe.printStackTrace();
            updateList(mNewList);
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        File newDir = new File(String.valueOf(mOldDir));
        if (!newDir.equals(mRootDir.getParentFile())) {
            openDir(newDir);
        } else {
            finish();
        }
    }
}
