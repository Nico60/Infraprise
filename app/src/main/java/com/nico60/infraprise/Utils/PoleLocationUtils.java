package com.nico60.infraprise.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.nico60.infraprise.AeopActivity;
import com.nico60.infraprise.BtActivity;
import com.nico60.infraprise.FtActivity;
import com.nico60.infraprise.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class PoleLocationUtils {

    private Activity mActivity;
    private boolean mIsProviderEnabled = true;
    private double mPoleLatitude = 0.0;
    private double mPoleLongitude = 0.0;
    public ProgressDialog mProgressDialog;
    private String mPoleAddress = "";

    public PoleLocationUtils(Activity activity) {
        mActivity = activity;
    }

    public String getPoleLatitude() {
        if (mPoleLatitude == 0.0) {
            return null;
        } else {
            return convertLatitude(mPoleLatitude);
        }
    }

    public void setPoleLatitude(double latitude) {
        mPoleLatitude = latitude;
    }

    public String getPoleLongitude() {
        if (mPoleLongitude == 0.0) {
            return null;
        } else {
            return convertLongitude(mPoleLongitude);
        }
    }

    public void setPoleLongitude(double longitude) {
        mPoleLongitude = longitude;
    }

    public String getAddress() {
        try {
            Geocoder geocoder = new Geocoder(mActivity, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(mPoleLatitude, mPoleLongitude, 1);
            if (addresses != null && addresses.size() > 0) {
                mPoleAddress = addresses.get(0).getAddressLine(0);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return mPoleAddress;
    }

    private String convertLatitude(double latitude) {
        StringBuilder builder = new StringBuilder();
        String latitudeDegrees = Location.convert(Math.abs(latitude), Location.FORMAT_SECONDS);
        String[] latitudeSplit = latitudeDegrees.split(":");
        builder.append(latitudeSplit[0]);
        builder.append("°");
        builder.append(latitudeSplit[1]);
        builder.append("'");
        builder.append(latitudeSplit[2]);
        builder.append("\"");
        builder.append(latitude > 0 ? " N" : " S");

        return builder.toString();
    }

    private String convertLongitude(double longitude) {
        StringBuilder builder = new StringBuilder();
        String longitudeDegrees = Location.convert(Math.abs(longitude), Location.FORMAT_SECONDS);
        String[] longitudeSplit = longitudeDegrees.split(":");
        builder.append(longitudeSplit[0]);
        builder.append("°");
        builder.append(longitudeSplit[1]);
        builder.append("'");
        builder.append(longitudeSplit[2]);
        builder.append("\"");
        builder.append(longitude > 0 ? " E" : " W");

        return builder.toString();
    }

    public void showProviderSearchDialog() {
        mProgressDialog = new ProgressDialog(mActivity, R.style.MyDialogStyle);
        mProgressDialog.create();
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(mActivity.getString(R.string.loc_provider_info_search));
        mProgressDialog.show();
    }

    public void showProviderDisabledDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.MyDialogStyle);
        builder.create();
        builder.setCancelable(false);
        builder.setTitle(R.string.loc_provider_warning_title);
        builder.setMessage(R.string.loc_provider_warning_message);

        builder.setPositiveButton(R.string.loc_provider_settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mActivity.startActivity(intent);
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

    public void showProviderInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.MyDialogStyle);
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(mActivity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.location_provider_info_dialog, null, false);
        CheckBox checkBox = view.findViewById(R.id.checkBox);
        builder.setTitle(R.string.loc_provider_info_title);
        builder.setMessage(R.string.loc_provider_info_message);
        builder.setView(view);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                setLocationForActivity();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bool) {
                if(compoundButton.isChecked()){
                    storeDialogStatus(true);
                }else{
                    storeDialogStatus(false);
                }
            }
        });

        if(getDialogStatus()){
            dialog.hide();
            setLocationForActivity();
        }else{
            dialog.show();
        }
    }

    private void setLocationForActivity() {
        String name = mActivity.getLocalClassName();
        switch (name){
            case "AeopActivity":
                new AeopActivity.mAeopProviderTask((AeopActivity) mActivity).execute(10);
                break;
            case "BtActivity":
                new BtActivity.mBtProviderTask((BtActivity) mActivity).execute(10);
                break;
            case "FtActivity":
                new FtActivity.mFtProviderTask((FtActivity) mActivity).execute(10);
                break;
            default:
                break;
        }
    }

    private void storeDialogStatus(boolean isChecked) {
        SharedPreferences mSharedPreferences = mActivity.getSharedPreferences("CheckItem", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("item", isChecked);
        mEditor.apply();
    }

    private boolean getDialogStatus() {
        SharedPreferences mSharedPreferences = mActivity.getSharedPreferences("CheckItem", MODE_PRIVATE);
        return mSharedPreferences.getBoolean("item", false);
    }

    public boolean getProviderStatus() {
        return mIsProviderEnabled;
    }

    public void setProviderStatus(boolean status) {
        mIsProviderEnabled = status;
    }
}
