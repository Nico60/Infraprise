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

import com.nico60.infraprise.SQLite.BtDatabase;
import com.nico60.infraprise.Utils.AppFileUtils;
import com.nico60.infraprise.Utils.AppImageUtils;
import com.nico60.infraprise.Utils.BtDbUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BtActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private static final int REQUEST_TAKE_PHOTO = 1;

    private AppFileUtils mAppFileUtils;
    private AppImageUtils mAppImageUtils;
    private boolean isUpdating = false;
    private BtDatabase mBtDb;
    private Dialog mDialog;
    private EditText mAdrInput;
    private EditText mBtInput;
    private EditText mNroInput;
    private EditText mPmInput;
    private String mAdrText;
    private String mBtText;
    private String mNroText;
    private String mPmText;
    private String mPoleType = "BT";
    private String mNameItemPositionForPopupMenu;
    private String mOldBtSheetName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt);

        mAppFileUtils = new AppFileUtils(this);
        mAppImageUtils = new AppImageUtils(this);
        mBtDb = new BtDatabase(this);
        mDialog = new Dialog(this, R.style.MyDialogStyle);

        mAdrInput = findViewById(R.id.btAdrText);
        mBtInput = findViewById(R.id.btBtText);
        mNroInput = findViewById(R.id.btNroText);
        mPmInput = findViewById(R.id.btPmText);

        Toolbar btToolbar = findViewById(R.id.btToolbar);

        FloatingActionButton btFab = findViewById(R.id.btFab);
        btFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdrText = mAdrInput.getText().toString();
                mBtText = mBtInput.getText().toString();
                mNroText = mNroInput.getText().toString();
                mPmText = mPmInput.getText().toString();

                String btSheetName = "NRO" + mNroText + "_PM" +
                        mPmText + "_BT" + mBtText;

                if (isAnswered() && isUpdating) {
                    mBtDb.updateBtDbUtils(new BtDbUtils(btSheetName, mNroText, mPmText,
                            mBtText, mAdrText), mOldBtSheetName);
                    updateSheetToast();
                    finish();
                } else if (isAnswered()) {
                    mBtDb.addBtDbUtils(new BtDbUtils(btSheetName, mNroText, mPmText,
                            mBtText, mAdrText));
                    createSheetToast();
                    finish();
                } else {
                    btWarning();
                }
            }
        });

        Button btPicture = findViewById(R.id.btPicButton);
        btPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBtText = mBtInput.getText().toString();
                mNroText = mNroInput.getText().toString();
                mPmText = mPmInput.getText().toString();
                if (isAnswered()) {
                    mAppImageUtils.dispatchTakePictureIntent(mNroText, mPmText, mPoleType, mBtText);
                } else {
                    btWarning();
                }
            }
        });

        setSupportActionBar(btToolbar);
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
                    showBtList();
                } else {
                    sheetListToast();
                }
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showBtList() {
        mDialog.setContentView(R.layout.dialog_listview);
        mDialog.setTitle(R.string.select_sheet);

        List<BtDbUtils> btDatabaseList = mBtDb.getAllBtDbUtils();
        List<String> r = new ArrayList<>();
        for (BtDbUtils bt : btDatabaseList) {
            String str = bt.getSheetName();
            r.add(str);
        }
        String[] btList = r.toArray(new String[]{});

        ArrayList<String> strList = new ArrayList<>(Arrays.asList(btList));
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
                showBtSheet(mNameItemPositionForPopupMenu);
                mDialog.dismiss();
                return true;
            case R.id.modify_dialog_item:
                updateBtSheet(mNameItemPositionForPopupMenu);
                mDialog.dismiss();
                return true;
            case R.id.create_file_text_item:
                createFileText(mNameItemPositionForPopupMenu);
                mDialog.dismiss();
                return true;
            case R.id.delete_dialog_item:
                deleteBtSheet(mNameItemPositionForPopupMenu);
                mDialog.dismiss();
                return true;
            default:
                return false;
        }
    }

    private void showBtSheet(String btSheet) {
        BtDbUtils bt = mBtDb.getBtDbUtils(btSheet);
        String data = "\n" + getString(R.string.nro_number) + " " + bt.getNroNumber() + "\n\n" +
                getString(R.string.pm_number) + " " + bt.getPmNumber() + "\n\n" +
                getString(R.string.bt_number) + " " + bt.getBtNumber() + "\n\n" +
                getString(R.string.address) + " " + bt.getAdressName() + "\n";
        showMessage(bt.getSheetName(), data);
    }

    private void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogStyle);
        builder.create();
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    private void updateBtSheet(String btSheet) {
        BtDbUtils bt = mBtDb.getBtDbUtils(btSheet);
        mNroInput.setText(bt.getNroNumber());
        mPmInput.setText(bt.getPmNumber());
        mBtInput.setText(bt.getBtNumber());
        mAdrInput.setText(bt.getAdressName());
        mOldBtSheetName = btSheet;
        isUpdating = true;
    }

    private void createFileText(String btSheet) {
        BtDbUtils bt = mBtDb.getBtDbUtils(btSheet);
        String[] list = new String[] {
                getString(R.string.nro_number) + " " + bt.getNroNumber(),
                getString(R.string.pm_number) + " " + bt.getPmNumber(),
                getString(R.string.bt_number) + " " + bt.getBtNumber(),
                getString(R.string.address) + " " + bt.getAdressName()};
        mAppFileUtils.save(bt.getNroNumber(), bt.getPmNumber(), mPoleType, bt.getBtNumber(), list);
    }

    private void deleteBtSheet(String btSheet) {
        mBtDb.deleteBtDbUtils(btSheet);
    }

    private int getSheetCount() {
        return mBtDb.getBtDbUtilsCount();
    }

    private boolean isAnswered() {
        return (!mNroText.isEmpty() && !mPmText.isEmpty() && !mBtText.isEmpty());
    }

    private void createSheetToast() {
        Toast.makeText(this, getString(R.string.create_sheet_toast), Toast.LENGTH_SHORT).show();
    }

    private void updateSheetToast() {
        Toast.makeText(this, getString(R.string.update_sheet_toast), Toast.LENGTH_SHORT).show();
    }

    private void btWarning() {
        Toast.makeText(this, getString(R.string.bt_warning), Toast.LENGTH_SHORT).show();
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
        mBtDb.close();
        super.onDestroy();
    }
}
