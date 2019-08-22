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

import com.nico60.infraprise.SQLite.FtDatabase;
import com.nico60.infraprise.Utils.AppFileUtils;
import com.nico60.infraprise.Utils.AppImageUtils;
import com.nico60.infraprise.Utils.FtDbUtils;
import com.nico60.infraprise.Utils.PoleLocationUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FtActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private static final int REQUEST_TAKE_PHOTO = 1;

    private AlertDialog mDialog;
    private AppFileUtils mAppFileUtils;
    private ArrayAdapter<String> mDialogArrayAdapter;
    private ArrayList<String> mDialogArrayList;
    private boolean isUpdating = false;
    private boolean mSheetExist = false;
    private EditText mAdrInput;
    private EditText mFtBeInput;
    private EditText mGftInput;
    private EditText mLatInput;
    private EditText mLongInput;
    private EditText mNroInput;
    private EditText mPmInput;
    private FtDatabase mFtDb;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private MainActivity mMainActivity;
    private PoleLocationUtils mPoleLocationUtils;
    private String mAdrText;
    private String mFtBeText;
    private String mGftText;
    private String mLatText;
    private String mLongText;
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
        mFtDb = new FtDatabase(this);
        mMainActivity = new MainActivity();
        mPoleLocationUtils = new PoleLocationUtils(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogStyle);
        mDialog = builder.create();

        mDialogArrayList = new ArrayList<>();
        mDialogArrayAdapter = new ArrayAdapter<>(this, R.layout.list_view_items,
                R.id.textListView, mDialogArrayList);

        mAdrInput = findViewById(R.id.ftAdrText);
        mFtBeInput = findViewById(R.id.ftBeText);
        mLatInput = findViewById(R.id.ftLatText);
        mLongInput = findViewById(R.id.ftLongText);
        mGftInput = findViewById(R.id.gFtText);
        mNroInput = findViewById(R.id.ftNroText);
        mPmInput = findViewById(R.id.ftPmText);

        Toolbar ftToolbar = findViewById(R.id.ftToolbar);

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

        FloatingActionButton ftFab = findViewById(R.id.ftFab);
        ftFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdrText = mAdrInput.getText().toString();
                mFtBeText = mFtBeInput.getText().toString();
                mGftText = mGftInput.getText().toString();
                mLatText = mLatInput.getText().toString();
                mLongText = mLongInput.getText().toString();
                mNroText = mNroInput.getText().toString();
                mPmText = mPmInput.getText().toString();

                String ftSheetName = "NRO" + mNroText + "_PM" +
                        mPmText + "_FT" + mGftText;

                if (isAnswered() && isUpdating) {
                    mFtDb.updateFtDbUtils(new FtDbUtils(ftSheetName, mNroText, mPmText,
                            mGftText, mFtBeText, mAdrText, mLatText, mLongText), mOldFtSheetName);
                    updateSheetToast();
                    finish();
                } else if (isAnswered()) {
                    if (!isSheetExist(ftSheetName)) {
                        mFtDb.addFtDbUtils(new FtDbUtils(ftSheetName, mNroText, mPmText,
                                mGftText, mFtBeText, mAdrText, mLatText, mLongText));
                        createSheetToast();
                        finish();
                    } else {
                        shouldOverwriteSheet(ftSheetName);
                    }
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
                    new AppImageUtils(FtActivity.this, mNroText, mPmText, mPoleType,
                            mGftText).poleViewDialog();
                } else {
                    ftWarning();
                }
            }
        });

        Button ftLocButton = findViewById(R.id.ftLocButton);
        ftLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPoleLocationUtils.getProviderStatus()) {
                    mPoleLocationUtils.showProviderInfoDialog();
                } else {
                    mPoleLocationUtils.showProviderDisabledDialog();
                }
            }
        });

        setSupportActionBar(ftToolbar);
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

    public static class mFtProviderTask extends AsyncTask<Integer, Integer, String> {

        WeakReference<FtActivity> ftActivityWeakReference;

        public mFtProviderTask(FtActivity activity) {
            ftActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ftActivityWeakReference.get().mPoleLocationUtils.showProviderSearchDialog();
        }

        @Override
        protected String doInBackground(Integer... params) {
            for (int i = 0; i < params[0]; i++) {
                if (ftActivityWeakReference.get().mPoleLocationUtils.getPoleLatitude() == null &&
                        ftActivityWeakReference.get().mPoleLocationUtils.getPoleLongitude() == null) {
                    try {
                        Thread.sleep(1000);
                        publishProgress(i);
                    } catch (InterruptedException ex) {
                        ftActivityWeakReference.get().mPoleLocationUtils.mProgressDialog.cancel();
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
            ftActivityWeakReference.get().mPoleLocationUtils.mProgressDialog.dismiss();
            if (ftActivityWeakReference.get().mPoleLocationUtils.getPoleLatitude() != null &&
                    ftActivityWeakReference.get().mPoleLocationUtils.getPoleLongitude() != null) {
                ftActivityWeakReference.get().mLatInput.setText(
                        ftActivityWeakReference.get().mPoleLocationUtils.getPoleLatitude());
                ftActivityWeakReference.get().mLongInput.setText(
                        ftActivityWeakReference.get().mPoleLocationUtils.getPoleLongitude());
                ftActivityWeakReference.get().mAdrInput.setText(
                        ftActivityWeakReference.get().mPoleLocationUtils.getAddress());
            } else {
                Toast.makeText(ftActivityWeakReference.get(), R.string.pole_location_warning,
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
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_listview, null, false);
        mDialog.setView(view);
        mDialog.setCancelable(false);
        mDialog.setTitle(R.string.select_sheet);

        List<FtDbUtils> ftDatabaseList = mFtDb.getAllFtDbUtils();
        mDialogArrayList.clear();
        for (FtDbUtils ft : ftDatabaseList) {
            String str = ft.getSheetName();
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
                showFtSheet();
                mDialog.dismiss();
                return true;
            case R.id.modify_dialog_item:
                updateFtSheet();
                mDialog.dismiss();
                return true;
            case R.id.create_file_text_item:
                createFileText();
                mDialog.dismiss();
                return true;
            case R.id.delete_dialog_item:
                deleteFtSheet();
                mDialog.dismiss();
                return true;
            default:
                return false;
        }
    }

    private void showFtSheet() {
        FtDbUtils ft = mFtDb.getFtDbUtils(mNameItemPositionForPopupMenu);
        String data = "\n" + getString(R.string.nro_number) + " " + ft.getNroNumber() + "\n\n" +
                getString(R.string.pm_number) + " " + ft.getPmNumber() + "\n\n" +
                getString(R.string.ft_be_number) + " " + ft.getFtBeNumber() + "\n\n" +
                getString(R.string.gspot_number) + " " + ft.getGftNumber() + "\n\n" +
                getString(R.string.pole_latitude) + " " + ft.getLatLoc() + "\n\n" +
                getString(R.string.pole_longitude) + " " + ft.getLongLoc() + "\n\n" +
                getString(R.string.address) + " " + ft.getAdressName() + "\n";
        showMessage(ft.getSheetName(), data);
    }

    private void updateFtSheet() {
        FtDbUtils ft = mFtDb.getFtDbUtils(mNameItemPositionForPopupMenu);
        mNroInput.setText(ft.getNroNumber());
        mPmInput.setText(ft.getPmNumber());
        mGftInput.setText(ft.getGftNumber());
        mFtBeInput.setText(ft.getFtBeNumber());
        mLatInput.setText(ft.getLatLoc());
        mLongInput.setText(ft.getLongLoc());
        mAdrInput.setText(ft.getAdressName());
        mOldFtSheetName = mNameItemPositionForPopupMenu;
        isUpdating = true;
    }

    private void createFileText() {
        FtDbUtils ft = mFtDb.getFtDbUtils(mNameItemPositionForPopupMenu);
        String[] list = new String[] {
                getString(R.string.nro_number) + " " + ft.getNroNumber(),
                getString(R.string.pm_number) + " " + ft.getPmNumber(),
                getString(R.string.ft_be_number) + " " + ft.getFtBeNumber(),
                getString(R.string.gspot_number) + " " + ft.getGftNumber(),
                getString(R.string.pole_latitude) + " " + ft.getLatLoc(),
                getString(R.string.pole_longitude) + " " + ft.getLongLoc(),
                getString(R.string.address) + " " + ft.getAdressName()};
        mAppFileUtils.createTextFile(ft.getNroNumber(), ft.getPmNumber(), mPoleType, ft.getGftNumber(), list);
    }

    private void deleteFtSheet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogStyle);
        builder.create();
        builder.setCancelable(false);
        builder.setMessage(getString(R.string.delete_db_sheet, mNameItemPositionForPopupMenu));

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mFtDb.deleteFtDbUtils(mNameItemPositionForPopupMenu);
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
        return mFtDb.getFtDbUtilsCount();
    }

    private boolean isAnswered() {
        return (!mNroText.isEmpty() && !mPmText.isEmpty() && !mGftText.isEmpty());
    }

    private boolean isSheetExist(String sheetName) {
        List<FtDbUtils> ftDatabaseList = mFtDb.getAllFtDbUtils();
        for (FtDbUtils ft : ftDatabaseList) {
            String str = ft.getSheetName();
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
                mFtDb.deleteFtDbUtils(sheetName);
                mFtDb.addFtDbUtils(new FtDbUtils(sheetName, mNroText, mPmText,
                        mGftText, mFtBeText, mAdrText, mLatText, mLongText));
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
