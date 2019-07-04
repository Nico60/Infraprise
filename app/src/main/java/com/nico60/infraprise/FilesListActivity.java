package com.nico60.infraprise;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.nico60.infraprise.Utils.AppFileUtils;
import com.nico60.infraprise.Utils.FileListAdaptater;
import com.nico60.infraprise.Utils.ListItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.support.v4.content.FileProvider.getUriForFile;

public class FilesListActivity extends AppCompatActivity {

    private static final int REQUEST_FILE_VIEW = 1;
    private static final int REQUEST_FILE_SEND = 2;

    private ActionMode mMode;
    private ArrayList<ListItem> mArrayListItem;
    private ArrayList<String> mItem;
    private ArrayList<Uri> mZipUriList;
    private File mCurrentList;
    private File mCurrentPath;
    private File mDirSelected;
    private File mSdRootPath;
    private File mNewList;
    private File mOldDir;
    private File mRootDir;
    private FileListAdaptater mAdapter;
    private AppFileUtils mAppFileUtils;
    private int mId;
    private Intent mSendIntent;
    private Intent mViewIntent;
    private ListView mListView;
    private String mExtType;
    private String mItemName;
    private String mStorageDir = "..";
    private String[] mListDir;
    private Uri mZipUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_list);

        Toolbar fileToolbar = findViewById(R.id.fileToolbar);

        mAppFileUtils = new AppFileUtils(this);

        mSdRootPath = Environment.getExternalStorageDirectory();

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
                openClickedItem(getItemNameAtPosition(position));
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
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mMode = null;
            mZipUriList.clear();
            mItem.clear();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            menu.clear();
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.files_list_mode_menu, menu);
            if (mListView.getCount() == 1 || mListView.getCount() == getItemCheckedCount()) {
                menu.removeItem(R.id.select_all);
            }
            if (getItemCheckedCount() > 1) {
                menu.removeItem(R.id.open);
                menu.removeItem(R.id.rename);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            mMode = mode;
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
                    openClickedItem(mItemName);
                    mode.finish();
                    return true;
                case R.id.select_all:
                    selectAllItem();
                    return true;
                case R.id.deselect_all:
                    deselectAllItem();
                    mode.finish();
                    return true;
                case R.id.copy:
                    mId = 1;
                    loadFileList(mSdRootPath);
                    selectFolder();
                    return true;
                case R.id.move:
                    mId = 2;
                    loadFileList(mSdRootPath);
                    selectFolder();
                    return true;
                case R.id.rename:
                    rename();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            mode.setTitle(getItemCheckedCount() + getString(R.string.item_selected));
            if (getItemCheckedCount() == 1) {
                mItemName = getItemNameAtPosition(position);
            }
            mode.invalidate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.files_list_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.create_folder:
                createFolder();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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

    private String getItemNameAtPosition(int position) {
        return mArrayListItem.get(position).getFileName();
    }

    private int getItemCheckedCount() {
        return mListView.getCheckedItemCount();
    }

    private void openClickedItem(String name) {
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
            mExtType = mAppFileUtils.getFileExt(uri.toString());
        }
        mViewIntent.setDataAndType(uri, mExtType == null ? "text/plain" : mExtType);
        startActivityForResult(mViewIntent, REQUEST_FILE_VIEW);
    }

    private void copyFileItem() {
        for (String item : itemToString()) {
            File fileToCopy = new File(mNewList, item);
            mAppFileUtils.copy(fileToCopy, mCurrentPath);
        }
    }

    private void moveFileItem() {
        for (String item : itemToString()) {
            File fileToMove = new File(mNewList, item);
            mAppFileUtils.move(fileToMove, mCurrentPath);
        }
    }

    private void deleteFileItem() {
        for (String item : itemToString()) {
            File fileToDelete = new File(mNewList, item);
            mAppFileUtils.delete(fileToDelete);
        }
        updateList(mNewList);
    }

    private void sendFileItem() {
        for(String itemPath : itemToString()) {
            File fileUri = new File(mCurrentList, itemPath);
            if (fileUri.isDirectory()) {
                mZipUri = getFileUri(mAppFileUtils.zipFolder(fileUri));
            } else {
                mZipUri = getFileUri(fileUri);
            }
            if (mZipUri != null) {
                mZipUriList.add(mZipUri);
            }
        }
        updateList(mCurrentList);
        if (mZipUriList.size() >= 1 || mZipUri != null) {
            mSendIntent = new Intent();
            mSendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            mExtType = mAppFileUtils.getFileExt(mZipUriList.toString());
            mSendIntent.setType(mExtType == null ? "text/plain" : mExtType);
            mSendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, mZipUriList);
            startActivityForResult(mSendIntent, REQUEST_FILE_SEND);
        }
    }

    private void selectAllItem() {
        new Thread() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < mListView.getCount(); i++) {
                                mListView.setItemChecked(i, true);
                            }
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }

    private void deselectAllItem() {
        mItem.clear();
    }

    private void createFolder() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_create_folder, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogStyle);
        builder.setTitle(R.string.create_folder_title);
        builder.setView(view);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText folderNameInput = (EditText) view.findViewById(R.id.folderName);
                String folderName = folderNameInput.getText().toString().trim();
                if (folderName.isEmpty()) {
                    Toast.makeText(FilesListActivity.this, getString(R.string.folder_warning), Toast.LENGTH_SHORT).show();
                    return;
                }
                File newFolder = new File(mCurrentList, folderName);
                if (!newFolder.exists()) {
                    newFolder.mkdir();
                }
                updateList(newFolder.getParentFile());
                dialog.dismiss();
            }
        });
    }

    private void selectFolder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogStyle);
        builder.setTitle(mCurrentPath.getPath());
        builder.setPositiveButton(R.string.directory_select_title, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                switch (mId) {
                    case 1:
                        copyFileItem();
                        mMode.finish();
                        openDir(mCurrentPath);
                        break;
                    case 2:
                        moveFileItem();
                        mMode.finish();
                        openDir(mCurrentPath);
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                mMode.finish();
            }
        });

        builder.setItems(mListDir, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String fileName = mListDir[id];
                mDirSelected = getDir(fileName);
                if (mDirSelected.isDirectory()) {
                    loadFileList(mDirSelected);
                    selectFolder();
                }
            }
        });
        builder.show();
    }

    private void rename() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_rename, null, false);

        final EditText fileFolderNameInput = (EditText) view.findViewById(R.id.fileFolderName);
        fileFolderNameInput.setText(mItemName);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogStyle);
        builder.setTitle(R.string.rename_title);
        builder.setView(view);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileFolderName = fileFolderNameInput.getText().toString().trim();
                if (fileFolderName.isEmpty()) {
                    Toast.makeText(FilesListActivity.this, getString(R.string.rename_file_folder_warning), Toast.LENGTH_SHORT).show();
                    return;
                }
                File oldName = new File(mCurrentList, mItemName);
                File newName = new File(mNewList, fileFolderName);
                oldName.renameTo(newName);
                updateList(newName.getParentFile());
                dialog.dismiss();
            }
        });
    }

    private void loadFileList(File file) {
        mCurrentPath = file;
        List<String> r = new ArrayList<>();
        if (file.exists()) {
            if (!file.equals(mSdRootPath)) {
                r.add(mStorageDir);
            }
            File[] list = file.listFiles();
            ArrayList<File> fileList = new ArrayList<>(Arrays.asList(list));
            Collections.sort(fileList);
            for (File fileAdded : fileList) {
                if (fileAdded.isDirectory()) {
                    String fileName = fileAdded.getName();
                    r.add(fileName);
                }
            }
        }
        mListDir = r.toArray(new String[]{});
    }

    private File getDir(String dirName) {
        if (dirName.equals(mStorageDir)) {
            return mCurrentPath.getParentFile();
        } else {
            return new File(mCurrentPath, dirName);
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

    private Uri getFileUri(File file) {
        try {
            return getUriForFile(this,
                   getString(R.string.file_provider),
                    file);
        } catch (NullPointerException npe) {
            Toast.makeText(this, getString(R.string.zip_warning), Toast.LENGTH_LONG).show();
            npe.printStackTrace();
            return null;
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

