package com.nico60.infraprise;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.nico60.infraprise.SQLite.FtDatabase;
import com.nico60.infraprise.Utils.AppFileUtils;
import com.nico60.infraprise.Utils.AppImageUtils;
import com.nico60.infraprise.Utils.FtDbUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FtActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private static final int REQUEST_TAKE_PHOTO = 1;

    private AppFileUtils mAppFileUtils;
    private AppImageUtils mAppImageUtils;
    private boolean isUpdating = false;
    private FtDatabase mFtDb;
    private Dialog mDialog;
    private EditText mAdrInput;
    private EditText mFtBeInput;
    private EditText mGftInput;
    private EditText mNroInput;
    private EditText mPmInput;
    private String mAdrText;
    private String mFtBeText;
    private String mGftText;
    private String mNroText;
    private String mPmText;
    private String mPoleType = "FT";
    private String mNameItemPositionForPopupMenu;
    private String mOldFtSheetName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ft);

        mAppFileUtils = new AppFileUtils(this);
        mAppImageUtils = new AppImageUtils(this);
        mFtDb = new FtDatabase(this);
        mDialog = new Dialog(this, R.style.MyDialogStyle);

        mAdrInput = findViewById(R.id.ftAdrText);
        mFtBeInput = findViewById(R.id.ftBeText);
        mGftInput = findViewById(R.id.gFtText);
        mNroInput = findViewById(R.id.ftNroText);
        mPmInput = findViewById(R.id.ftPmText);

        Toolbar ftToolbar = findViewById(R.id.ftToolbar);

        FloatingActionButton ftFab = findViewById(R.id.ftFab);
        ftFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdrText = mAdrInput.getText().toString();
                mFtBeText = mFtBeInput.getText().toString();
                mGftText = mGftInput.getText().toString();
                mNroText = mNroInput.getText().toString();
                mPmText = mPmInput.getText().toString();

                String ftSheetName = "NRO" + mNroText + "_PM" +
                        mPmText + "_FT" + mGftText;

                if (isAnswered() && isUpdating) {
                    mFtDb.updateFtDbUtils(new FtDbUtils(ftSheetName, mNroText, mPmText,
                            mGftText, mFtBeText, mAdrText), mOldFtSheetName);
                    updateSheetToast();
                    finish();
                } else if (isAnswered()) {
                    mFtDb.addFtDbUtils(new FtDbUtils(ftSheetName, mNroText, mPmText,
                            mGftText, mFtBeText, mAdrText));
                    createSheetToast();
                    finish();
                } else {
                    ftWarning();
                }
            }
        });

        Button ftPicture = findViewById(R.id.ftPicButton);
        ftPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGftText = mGftInput.getText().toString();
                mNroText = mNroInput.getText().toString();
                mPmText = mPmInput.getText().toString();
                if (isAnswered()) {
                    mAppImageUtils.dispatchTakePictureIntent(mNroText, mPmText, mPoleType, mGftText);
                } else {
                    ftWarning();
                }
            }
        });

        setSupportActionBar(ftToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sheet_list_db_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.show_list_db_sheet:
                if (getSheetCount() > 0) {
                    showFtList();
                } else {
                    sheetListToast();
                }
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFtList() {
        mDialog.setContentView(R.layout.dialog_listview);
        mDialog.setTitle(R.string.select_sheet);

        List<FtDbUtils> ftDatabaseList = mFtDb.getAllFtDbUtils();
        List<String> r = new ArrayList<>();
        for (FtDbUtils ft : ftDatabaseList) {
            String str = ft.getSheetName();
            r.add(str);
        }
        String[] ftList = r.toArray(new String[]{});

        ArrayList<String> strList = new ArrayList<>(Arrays.asList(ftList));
        Collections.sort(strList);

        ListView dialogListView = (ListView) mDialog.findViewById(R.id.dialog_list_view);
        final ArrayAdapter<String> arrayAdapter
                = new ArrayAdapter<>(this, R.layout.list_view_items, R.id.textListView, strList);
        dialogListView.setAdapter(arrayAdapter);

        Button btnDialog = (Button) mDialog.findViewById(R.id.btn_dialog);
        btnDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.cancel();
            }
        });

        dialogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mNameItemPositionForPopupMenu = arrayAdapter.getItem(position);
                showPopupMenu(view);
            }
        });

        mDialog.show();
    }

    public void showPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.dialog_listview_menu);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.open_dialog_item:
                showFtSheet(mNameItemPositionForPopupMenu);
                mDialog.dismiss();
                return true;
            case R.id.modify_dialog_item:
                updateFtSheet(mNameItemPositionForPopupMenu);
                mDialog.dismiss();
                return true;
            case R.id.create_file_text_item:
                createFileText(mNameItemPositionForPopupMenu);
                mDialog.dismiss();
                return true;
            case R.id.delete_dialog_item:
                deleteFtSheet(mNameItemPositionForPopupMenu);
                mDialog.dismiss();
                return true;
            default:
                return false;
        }
    }

    private void showFtSheet(String ftSheet) {
        FtDbUtils ft = mFtDb.getFtDbUtils(ftSheet);
        String data = "\n" + getString(R.string.nro_number) + " " + ft.getNroNumber() + "\n\n" +
                getString(R.string.pm_number) + " " + ft.getPmNumber() + "\n\n" +
                getString(R.string.ft_be_number) + " " + ft.getFtBeNumber() + "\n\n" +
                getString(R.string.gspot_number) + " " + ft.getGftNumber() + "\n\n" +
                getString(R.string.address) + " " + ft.getAdressName() + "\n";
        showMessage(ft.getSheetName(), data);
    }

    private void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogStyle);
        builder.create();
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    private void updateFtSheet(String ftSheet) {
        FtDbUtils ft = mFtDb.getFtDbUtils(ftSheet);
        mNroInput.setText(ft.getNroNumber());
        mPmInput.setText(ft.getPmNumber());
        mGftInput.setText(ft.getGftNumber());
        mFtBeInput.setText(ft.getFtBeNumber());
        mAdrInput.setText(ft.getAdressName());
        mOldFtSheetName = ftSheet;
        isUpdating = true;
    }

    private void createFileText(String ftSheet) {
        FtDbUtils ft = mFtDb.getFtDbUtils(ftSheet);
        String[] list = new String[] {
                getString(R.string.nro_number) + " " + ft.getNroNumber(),
                getString(R.string.pm_number) + " " + ft.getPmNumber(),
                getString(R.string.ft_be_number) + " " + ft.getFtBeNumber(),
                getString(R.string.gspot_number) + " " + ft.getGftNumber(),
                getString(R.string.address) + " " + ft.getAdressName()};
        mAppFileUtils.save(ft.getNroNumber(), ft.getPmNumber(), mPoleType, ft.getGftNumber(), list);
    }

    private void deleteFtSheet(String ftSheet) {
        mFtDb.deleteFtDbUtils(ftSheet);
    }

    private int getSheetCount() {
        return mFtDb.getFtDbUtilsCount();
    }

    private boolean isAnswered() {
        return (!mNroText.isEmpty() && !mPmText.isEmpty() && !mGftText.isEmpty());
    }

    private void createSheetToast() {
        Toast.makeText(this, getString(R.string.create_sheet_toast), Toast.LENGTH_SHORT).show();
    }

    private void updateSheetToast() {
        Toast.makeText(this, getString(R.string.update_sheet_toast), Toast.LENGTH_SHORT).show();
    }

    private void ftWarning() {
        Toast.makeText(this, getString(R.string.ft_warning), Toast.LENGTH_SHORT).show();
    }

    private void sheetListToast() {
        Toast.makeText(this, getString(R.string.sheet_list_toast), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Toast.makeText(this, getString(R.string.picture_saved), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        mFtDb.close();
        super.onDestroy();
    }
}
