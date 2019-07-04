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

import com.nico60.infraprise.SQLite.AeopDatabase;
import com.nico60.infraprise.Utils.AeopDbUtils;
import com.nico60.infraprise.Utils.AppFileUtils;
import com.nico60.infraprise.Utils.AppImageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AeopActivity extends AppCompatActivity  implements PopupMenu.OnMenuItemClickListener{

    private static final int REQUEST_TAKE_PHOTO = 1;

    private AppFileUtils mAppFileUtils;
    private AppImageUtils mAppImageUtils;
    private boolean isUpdating = false;
    private AeopDatabase mAeopDb;
    private Dialog mDialog;
    private EditText mAdrInput;
    private EditText mAeopInput;
    private EditText mBtInput;
    private EditText mDrcInput;
    private EditText mHtInput;
    private EditText mNroInput;
    private EditText mPmInput;
    private String mAdrText;
    private String mAeopText;
    private String mBtText;
    private String mDrcText;
    private String mHtText;
    private String mNroText;
    private String mPmText;
    private String mPoleType = "AEOP";
    private String mNameItemPositionForPopupMenu;
    private String mOldAeopSheetName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aeop);

        mAppFileUtils = new AppFileUtils(this);
        mAppImageUtils = new AppImageUtils(this);
        mAeopDb = new AeopDatabase(this);
        mDialog = new Dialog(this, R.style.MyDialogStyle);

        mAdrInput = findViewById(R.id.aeopAdrText);
        mAeopInput = findViewById(R.id.aeopNumText);
        mBtInput = findViewById(R.id.aeopBtText);
        mDrcInput = findViewById(R.id.aeopDrcText);
        mHtInput = findViewById(R.id.aeopHtText);
        mNroInput = findViewById(R.id.aeopNroText);
        mPmInput = findViewById(R.id.aeopPmText);

        Toolbar aeopToolbar = findViewById(R.id.aeopToolbar);

        FloatingActionButton aeopFab = findViewById(R.id.aeopFab);
        aeopFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdrText = mAdrInput.getText().toString();
                mAeopText = mAeopInput.getText().toString();
                mBtText = mBtInput.getText().toString();
                mDrcText = mDrcInput.getText().toString();
                mHtText = mHtInput.getText().toString();
                mNroText = mNroInput.getText().toString();
                mPmText = mPmInput.getText().toString();

                String aeopSheetName = "NRO" + mNroText + "_PM" +
                        mPmText + "_AEOP" + mAeopText;

                if (isAnswered() && isUpdating) {
                    mAeopDb.updateAeopDbUtils(new AeopDbUtils(aeopSheetName, mNroText, mPmText,
                            mAeopText, mBtText, mHtText, mDrcText, mAdrText), mOldAeopSheetName);
                    updateSheetToast();
                    finish();
                } else if (isAnswered()) {
                    mAeopDb.addAeopDbUtils(new AeopDbUtils(aeopSheetName, mNroText, mPmText,
                            mAeopText, mBtText, mHtText, mDrcText, mAdrText));
                    createSheetToast();
                    finish();
                } else {
                    aeopWarning();
                }
            }
        });

        Button aeopPicture = findViewById(R.id.aeopPicButton);
        aeopPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAeopText = mAeopInput.getText().toString();
                mNroText = mNroInput.getText().toString();
                mPmText = mPmInput.getText().toString();
                if (isAnswered()) {
                    mAppImageUtils.dispatchTakePictureIntent(mNroText, mPmText, mPoleType, mAeopText);
                } else {
                    aeopWarning();
                }
            }
        });

        setSupportActionBar(aeopToolbar);
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
                    showAeopList();
                } else {
                    sheetListToast();
                }
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAeopList() {
        mDialog.setContentView(R.layout.dialog_listview);
        mDialog.setTitle(R.string.select_sheet);

        List<AeopDbUtils> aeopDatabaseList = mAeopDb.getAllAeopDbUtils();
        List<String> r = new ArrayList<>();
        for (AeopDbUtils aeop : aeopDatabaseList) {
            String str = aeop.getSheetName();
            r.add(str);
        }
        String[] aeopList = r.toArray(new String[]{});

        ArrayList<String> strList = new ArrayList<>(Arrays.asList(aeopList));
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
                showAeopSheet(mNameItemPositionForPopupMenu);
                mDialog.dismiss();
                return true;
            case R.id.modify_dialog_item:
                updateAeopSheet(mNameItemPositionForPopupMenu);
                mDialog.dismiss();
                return true;
            case R.id.create_file_text_item:
                createFileText(mNameItemPositionForPopupMenu);
                mDialog.dismiss();
                return true;
            case R.id.delete_dialog_item:
                deleteAeopSheet(mNameItemPositionForPopupMenu);
                mDialog.dismiss();
                return true;
            default:
                return false;
        }
    }

    private void showAeopSheet(String aeopSheet) {
        AeopDbUtils aeop = mAeopDb.getAeopDbUtils(aeopSheet);
        String data = "\n" + getString(R.string.nro_number) + " " + aeop.getNroNumber() + "\n\n" +
                getString(R.string.pm_number) + " " + aeop.getPmNumber() + "\n\n" +
                getString(R.string.aeop_number) + " " + aeop.getAeopNumber() + "\n\n" +
                getString(R.string.bt_number) + " " + aeop.getBtNumber() + "\n\n" +
                getString(R.string.aeop_height_pole_to_plant, aeop.getHtNumber()) + "\n\n" +
                getString(R.string.aeop_distance_pole_to_road, aeop.getDrcNumber()) + "\n\n" +
                getString(R.string.address) + " " + aeop.getAdressName() + "\n";
        showMessage(aeop.getSheetName(), data);
    }

    private void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogStyle);
        builder.create();
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    private void updateAeopSheet(String aeopSheet) {
        AeopDbUtils aeop = mAeopDb.getAeopDbUtils(aeopSheet);
        mNroInput.setText(aeop.getNroNumber());
        mPmInput.setText(aeop.getPmNumber());
        mAeopInput.setText(aeop.getAeopNumber());
        mBtInput.setText(aeop.getBtNumber());
        mHtInput.setText(aeop.getHtNumber());
        mDrcInput.setText(aeop.getDrcNumber());
        mAdrInput.setText(aeop.getAdressName());
        mOldAeopSheetName = aeopSheet;
        isUpdating = true;
    }

    private void createFileText(String aeopSheet) {
        AeopDbUtils aeop = mAeopDb.getAeopDbUtils(aeopSheet);
        String[] list = new String[] {
                getString(R.string.nro_number) + " " + aeop.getNroNumber(),
                getString(R.string.pm_number) + " " + aeop.getPmNumber(),
                getString(R.string.aeop_number) + " " + aeop.getAeopNumber(),
                getString(R.string.bt_number) + " " + aeop.getBtNumber(),
                getString(R.string.aeop_height_pole_to_plant, aeop.getHtNumber()),
                getString(R.string.aeop_distance_pole_to_road, aeop.getDrcNumber()),
                getString(R.string.address) + " " + aeop.getAdressName()};
        mAppFileUtils.save(aeop.getNroNumber(), aeop.getPmNumber(), mPoleType, aeop.getAeopNumber(), list);
    }

    private void deleteAeopSheet(String aeopSheet) {
        mAeopDb.deleteAeopDbUtils(aeopSheet);
    }

    private int getSheetCount() {
        return mAeopDb.getAeopDbUtilsCount();
    }

    private boolean isAnswered() {
        return (!mNroText.isEmpty() && !mPmText.isEmpty() && !mAeopText.isEmpty());
    }

    private void createSheetToast() {
        Toast.makeText(this, getString(R.string.create_sheet_toast), Toast.LENGTH_SHORT).show();
    }

    private void updateSheetToast() {
        Toast.makeText(this, getString(R.string.update_sheet_toast), Toast.LENGTH_SHORT).show();
    }

    private void aeopWarning() {
        Toast.makeText(this, getString(R.string.aeop_warning), Toast.LENGTH_SHORT).show();
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
        mAeopDb.close();
        super.onDestroy();
    }
}
