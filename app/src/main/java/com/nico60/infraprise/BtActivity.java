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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import com.nico60.infraprise.SQLite.BtDatabase;
import com.nico60.infraprise.Utils.AppFileUtils;
import com.nico60.infraprise.Utils.AppImageUtils;
import com.nico60.infraprise.Utils.BtDbUtils;
import com.nico60.infraprise.Utils.PoleLocationUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BtActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private static final int REQUEST_TAKE_PHOTO = 1;

    private AlertDialog mDialog;
    private AppFileUtils mAppFileUtils;
    private boolean isUpdating = false;
    private BtDatabase mBtDb;
    private EditText mAdrInput;
    private EditText mBtInput;
    private EditText mLatInput;
    private EditText mLongInput;
    private EditText mNroInput;
    private EditText mPmInput;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private MainActivity mMainActivity;
    private PoleLocationUtils mPoleLocationUtils;
    private String mAdrText;
    private String mBtText;
    private String mLatText;
    private String mLongText;
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
        mBtDb = new BtDatabase(this);
        mMainActivity = new MainActivity();
        mPoleLocationUtils = new PoleLocationUtils(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogStyle);
        mDialog = builder.create();

        mAdrInput = findViewById(R.id.btAdrText);
        mBtInput = findViewById(R.id.btBtText);
        mLatInput = findViewById(R.id.btLatText);
        mLongInput = findViewById(R.id.btLongText);
        mNroInput = findViewById(R.id.btNroText);
        mPmInput = findViewById(R.id.btPmText);

        Toolbar btToolbar = findViewById(R.id.btToolbar);

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

        FloatingActionButton btFab = findViewById(R.id.btFab);
        btFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdrText = mAdrInput.getText().toString();
                mBtText = mBtInput.getText().toString();
                mLatText = mLatInput.getText().toString();
                mLongText = mLongInput.getText().toString();
                mNroText = mNroInput.getText().toString();
                mPmText = mPmInput.getText().toString();

                String btSheetName = "NRO" + mNroText + "_PM" +
                        mPmText + "_BT" + mBtText;

                if (isAnswered() && isUpdating) {
                    mBtDb.updateBtDbUtils(new BtDbUtils(btSheetName, mNroText, mPmText,
                            mBtText, mAdrText, mLatText, mLongText), mOldBtSheetName);
                    updateSheetToast();
                    finish();
                } else if (isAnswered()) {
                    mBtDb.addBtDbUtils(new BtDbUtils(btSheetName, mNroText, mPmText,
                            mBtText, mAdrText, mLatText, mLongText));
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
                    new AppImageUtils(BtActivity.this, mNroText, mPmText, mPoleType,
                            mBtText).poleViewDialog();
                } else {
                    btWarning();
                }
            }
        });

        Button btLocButton = findViewById(R.id.btLocButton);
        btLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPoleLocationUtils.getProviderStatus()) {
                    mPoleLocationUtils.showProviderInfoDialog();
                } else {
                    mPoleLocationUtils.showProviderDisabledDialog();
                }
            }
        });

        setSupportActionBar(btToolbar);
        updatePoleLocation();
    }

    private void updatePoleLocation() {
        if (mMainActivity.checkLocationPermission(this)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500,
                    1, mLocationListener);
        } else {
            mMainActivity.requestPermission(this);
        }
    }

    public static class mBtProviderTask extends AsyncTask<Integer, Integer, String> {

        WeakReference<BtActivity> btActivityWeakReference;

        public mBtProviderTask(BtActivity activity) {
            btActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btActivityWeakReference.get().mPoleLocationUtils.showProviderSearchDialog();
        }

        @Override
        protected String doInBackground(Integer... params) {
            for (int i = 0; i < params[0]; i++) {
                if (btActivityWeakReference.get().mPoleLocationUtils.getPoleLatitude() == null &&
                        btActivityWeakReference.get().mPoleLocationUtils.getPoleLongitude() == null) {
                    try {
                        Thread.sleep(1000);
                        publishProgress(i);
                    } catch (InterruptedException ex) {
                        btActivityWeakReference.get().mPoleLocationUtils.mProgressDialog.cancel();
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
            btActivityWeakReference.get().mPoleLocationUtils.mProgressDialog.dismiss();
            if (btActivityWeakReference.get().mPoleLocationUtils.getPoleLatitude() != null &&
                    btActivityWeakReference.get().mPoleLocationUtils.getPoleLongitude() != null) {
                btActivityWeakReference.get().mLatInput.setText(
                        btActivityWeakReference.get().mPoleLocationUtils.getPoleLatitude());
                btActivityWeakReference.get().mLongInput.setText(
                        btActivityWeakReference.get().mPoleLocationUtils.getPoleLongitude());
                btActivityWeakReference.get().mAdrInput.setText(
                        btActivityWeakReference.get().mPoleLocationUtils.getAddress());
            } else {
                Toast.makeText(btActivityWeakReference.get(), R.string.pole_location_warning,
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
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_listview, null, false);
        mDialog.setView(view);
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

        ListView dialogListView = (ListView) view.findViewById(R.id.dialog_list_view);
        final ArrayAdapter<String> arrayAdapter
                = new ArrayAdapter<>(this, R.layout.list_view_items, R.id.textListView, strList);
        dialogListView.setAdapter(arrayAdapter);

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
                getString(R.string.pole_latitude) + " " + bt.getLatLoc() + "\n\n" +
                getString(R.string.pole_longitude) + " " + bt.getLongLoc() + "\n\n" +
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
        mLatInput.setText(bt.getLatLoc());
        mLongInput.setText(bt.getLongLoc());
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
                getString(R.string.pole_latitude) + " " + bt.getLatLoc(),
                getString(R.string.pole_longitude) + " " + bt.getLongLoc(),
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
