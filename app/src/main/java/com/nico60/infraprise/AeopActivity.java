package com.nico60.infraprise;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
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
import com.nico60.infraprise.Utils.PoleLocationUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AeopActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private AeopDatabase mAeopDb;
    private AlertDialog mDialog;
    private AppFileUtils mAppFileUtils;
    private ArrayAdapter<String> mDialogArrayAdapter;
    private ArrayList<String> mDialogArrayList;
    private boolean isUpdating = false;
    private boolean mSheetExist = false;
    private EditText mAdrInput;
    private EditText mAeopInput;
    private EditText mBtInput;
    private EditText mDrcInput;
    private EditText mHtInput;
    private EditText mLatInput;
    private EditText mLongInput;
    private EditText mNroInput;
    private EditText mPmInput;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private MainActivity mMainActivity;
    private PoleLocationUtils mPoleLocationUtils;
    private String mAdrText;
    private String mAeopText;
    private String mBtText;
    private String mDrcText;
    private String mHtText;
    private String mLatText;
    private String mLongText;
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
        mAeopDb = new AeopDatabase(this);
        mMainActivity = new MainActivity();
        mPoleLocationUtils = new PoleLocationUtils(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogStyle);
        mDialog = builder.create();

        mDialogArrayList = new ArrayList<>();
        mDialogArrayAdapter = new ArrayAdapter<>(this, R.layout.db_list_view_items,
                R.id.dbTextListView, mDialogArrayList);

        mAdrInput = findViewById(R.id.aeopAdrText);
        mAeopInput = findViewById(R.id.aeopNumText);
        mBtInput = findViewById(R.id.aeopBtText);
        mDrcInput = findViewById(R.id.aeopDrcText);
        mHtInput = findViewById(R.id.aeopHtText);
        mLatInput = findViewById(R.id.aeopLatText);
        mLongInput = findViewById(R.id.aeopLongText);
        mNroInput = findViewById(R.id.aeopNroText);
        mPmInput = findViewById(R.id.aeopPmText);

        Toolbar aeopToolbar = findViewById(R.id.aeopToolbar);
        aeopToolbar.setOverflowIcon(getDrawable(R.drawable.ic_db_sheet));

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mPoleLocationUtils.setPoleLatitude(location.getLatitude());
                mPoleLocationUtils.setPoleLongitude(location.getLongitude());
            }

            @Override
            public void onStatusChanged(String str, int i, Bundle bundle) {
                //
            }

            @Override
            public void onProviderEnabled(String str) {
                mPoleLocationUtils.setProviderStatus(true);
            }

            @Override
            public void onProviderDisabled(String str) {
                mPoleLocationUtils.setProviderStatus(false);
            }
        };

        FloatingActionButton aeopFab = findViewById(R.id.aeopFab);
        aeopFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdrText = mAdrInput.getText().toString();
                mAeopText = mAeopInput.getText().toString();
                mBtText = mBtInput.getText().toString();
                mDrcText = mDrcInput.getText().toString();
                mHtText = mHtInput.getText().toString();
                mLatText = mLatInput.getText().toString();
                mLongText = mLongInput.getText().toString();
                mNroText = mNroInput.getText().toString();
                mPmText = mPmInput.getText().toString();

                String aeopSheetName = "NRO" + mNroText + "_PM" +
                        mPmText + "_AEOP" + mAeopText;

                if (isAnswered() && isUpdating) {
                    mAeopDb.updateAeopDbUtils(new AeopDbUtils(aeopSheetName, mNroText, mPmText,
                            mAeopText, mBtText, mHtText, mDrcText, mAdrText, mLatText, mLongText),
                            mOldAeopSheetName);
                    updateSheetToast();
                    finish();
                } else if (isAnswered()) {
                    if (!isSheetExist(aeopSheetName)) {
                        mAeopDb.addAeopDbUtils(new AeopDbUtils(aeopSheetName, mNroText, mPmText,
                                mAeopText, mBtText, mHtText, mDrcText, mAdrText, mLatText, mLongText));
                        createSheetToast();
                        finish();
                    } else {
                        shouldOverwriteSheet(aeopSheetName);
                    }
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
                    new AppImageUtils(AeopActivity.this, mNroText, mPmText, mPoleType,
                            mAeopText).dispatchTakePictureIntent();
                } else {
                    aeopWarning();
                }
            }
        });

        Button aeopLocButton = findViewById(R.id.aeopLocButton);
        aeopLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPoleLocationUtils.getProviderStatus()) {
                    mPoleLocationUtils.showProviderInfoDialog();
                } else {
                    mPoleLocationUtils.showProviderDisabledDialog();
                }
            }
        });

        setSupportActionBar(aeopToolbar);
        updatePoleLocation();
    }

    private void updatePoleLocation() {
        if (mMainActivity.checkLocationPermission(this)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
                    0, mLocationListener);
        } else {
            mMainActivity.requestPermission(this);
        }
    }

    public static class mAeopProviderTask extends AsyncTask<Integer, Integer, String> {

        WeakReference<AeopActivity> aeopActivityWeakReference;

        public mAeopProviderTask(AeopActivity activity) {
            aeopActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            aeopActivityWeakReference.get().mPoleLocationUtils.showProviderSearchDialog();
        }

        @Override
        protected String doInBackground(Integer... params) {
            for (int i = 0; i < params[0]; i++) {
                if (aeopActivityWeakReference.get().mPoleLocationUtils.getPoleLatitude() == null &&
                        aeopActivityWeakReference.get().mPoleLocationUtils.getPoleLongitude() == null) {
                    try {
                        Thread.sleep(1000);
                        publishProgress(i);
                    } catch (InterruptedException ex) {
                        aeopActivityWeakReference.get().mPoleLocationUtils.mProgressDialog.cancel();
                        ex.printStackTrace();
                    }
                }
            }
            return "Task finished.";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            aeopActivityWeakReference.get().mPoleLocationUtils.mProgressDialog.dismiss();
            if (aeopActivityWeakReference.get().mPoleLocationUtils.getPoleLatitude() != null &&
                    aeopActivityWeakReference.get().mPoleLocationUtils.getPoleLongitude() != null) {
                aeopActivityWeakReference.get().mLatInput.setText(
                        aeopActivityWeakReference.get().mPoleLocationUtils.getPoleLatitude());
                aeopActivityWeakReference.get().mLongInput.setText(
                        aeopActivityWeakReference.get().mPoleLocationUtils.getPoleLongitude());
                aeopActivityWeakReference.get().mAdrInput.setText(
                        aeopActivityWeakReference.get().mPoleLocationUtils.getAddress());
            } else {
                Toast.makeText(aeopActivityWeakReference.get(), R.string.pole_location_warning,
                        Toast.LENGTH_LONG).show();
            }
        }
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
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_listview, null, false);
        mDialog.setView(view);
        mDialog.setCancelable(false);
        mDialog.setTitle(R.string.select_sheet);

        List<AeopDbUtils> aeopDatabaseList = mAeopDb.getAllAeopDbUtils();
        mDialogArrayList.clear();
        for (AeopDbUtils aeop : aeopDatabaseList) {
            String str = aeop.getSheetName();
            mDialogArrayList.add(str);
        }
        Collections.sort(mDialogArrayList);

        ListView dialogListView = (ListView) view.findViewById(R.id.dialog_list_view);
        dialogListView.setAdapter(mDialogArrayAdapter);

        mDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        dialogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mNameItemPositionForPopupMenu = mDialogArrayAdapter.getItem(position);
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
                showAeopSheet();
                mDialog.dismiss();
                return true;
            case R.id.modify_dialog_item:
                updateAeopSheet();
                mDialog.dismiss();
                return true;
            case R.id.create_file_text_item:
                createFileText();
                mDialog.dismiss();
                return true;
            case R.id.delete_dialog_item:
                deleteAeopSheet();
                mDialog.dismiss();
                return true;
            default:
                return false;
        }
    }

    private void showAeopSheet() {
        AeopDbUtils aeop = mAeopDb.getAeopDbUtils(mNameItemPositionForPopupMenu);
        String data = "\n" + getString(R.string.nro_number) + " " + aeop.getNroNumber() + "\n\n" +
                getString(R.string.pm_number) + " " + aeop.getPmNumber() + "\n\n" +
                getString(R.string.aeop_number) + " " + aeop.getAeopNumber() + "\n\n" +
                getString(R.string.bt_number) + " " + aeop.getBtNumber() + "\n\n" +
                getString(R.string.aeop_height_pole_to_plant, aeop.getHtNumber()) + "\n\n" +
                getString(R.string.aeop_distance_pole_to_road, aeop.getDrcNumber()) + "\n\n" +
                getString(R.string.pole_latitude) + " " + aeop.getLatLoc() + "\n\n" +
                getString(R.string.pole_longitude) + " " + aeop.getLongLoc() + "\n\n" +
                getString(R.string.address) + " " + aeop.getAdressName() + "\n";
        showMessage(aeop.getSheetName(), data);
    }

    private void updateAeopSheet() {
        AeopDbUtils aeop = mAeopDb.getAeopDbUtils(mNameItemPositionForPopupMenu);
        mNroInput.setText(aeop.getNroNumber());
        mPmInput.setText(aeop.getPmNumber());
        mAeopInput.setText(aeop.getAeopNumber());
        mBtInput.setText(aeop.getBtNumber());
        mHtInput.setText(aeop.getHtNumber());
        mDrcInput.setText(aeop.getDrcNumber());
        mLatInput.setText(aeop.getLatLoc());
        mLongInput.setText(aeop.getLongLoc());
        mAdrInput.setText(aeop.getAdressName());
        mOldAeopSheetName = mNameItemPositionForPopupMenu;
        isUpdating = true;
    }

    private void createFileText() {
        AeopDbUtils aeop = mAeopDb.getAeopDbUtils(mNameItemPositionForPopupMenu);
        String[] list = new String[] {
                getString(R.string.nro_number) + " " + aeop.getNroNumber(),
                getString(R.string.pm_number) + " " + aeop.getPmNumber(),
                getString(R.string.aeop_number) + " " + aeop.getAeopNumber(),
                getString(R.string.bt_number) + " " + aeop.getBtNumber(),
                getString(R.string.aeop_height_pole_to_plant, aeop.getHtNumber()),
                getString(R.string.aeop_distance_pole_to_road, aeop.getDrcNumber()),
                getString(R.string.pole_latitude) + " " + aeop.getLatLoc(),
                getString(R.string.pole_longitude) + " " + aeop.getLongLoc(),
                getString(R.string.address) + " " + aeop.getAdressName()};
        mAppFileUtils.createTextFile(aeop.getNroNumber(), aeop.getPmNumber(), mPoleType, aeop.getAeopNumber(), list);
    }

    private void deleteAeopSheet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogStyle);
        builder.create();
        builder.setCancelable(false);
        builder.setMessage(getString(R.string.delete_db_sheet, mNameItemPositionForPopupMenu));

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mAeopDb.deleteAeopDbUtils(mNameItemPositionForPopupMenu);
                mDialogArrayAdapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private int getSheetCount() {
        return mAeopDb.getAeopDbUtilsCount();
    }

    private boolean isAnswered() {
        return (!mNroText.isEmpty() && !mPmText.isEmpty() && !mAeopText.isEmpty());
    }

    private boolean isSheetExist(String sheetName) {
        List<AeopDbUtils> aeopDatabaseList = mAeopDb.getAllAeopDbUtils();
        for (AeopDbUtils aeop : aeopDatabaseList) {
            String str = aeop.getSheetName();
            if (str.equals(sheetName)) {
                mSheetExist = true;
            }
        }

        return mSheetExist;
    }

    private void shouldOverwriteSheet(final String sheetName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogStyle);
        builder.create();
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.exist_warning));
        builder.setMessage(getString(R.string.sheet_exist_message, sheetName));

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mAeopDb.deleteAeopDbUtils(sheetName);
                mAeopDb.addAeopDbUtils(new AeopDbUtils(sheetName, mNroText, mPmText,
                        mAeopText, mBtText, mHtText, mDrcText, mAdrText, mLatText, mLongText));
                createSheetToast();
                finish();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogStyle);
        builder.create();
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.show();
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
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Toast.makeText(this, getString(R.string.picture_saved), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        mAeopDb.close();
        super.onDestroy();
    }
}
