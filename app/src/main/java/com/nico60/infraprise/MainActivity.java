package com.nico60.infraprise;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;

    private Intent intent;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("permSharedPref", MODE_PRIVATE);

        Toolbar mainToolbar = findViewById(R.id.mainToolbar);

        linearLayout = findViewById(R.id.mainLinear);

        if (!prefs.getBoolean("isNotFirstRun", false)) {
            requestPermission(MainActivity.this);
            prefs.edit().putBoolean("isNotFirstRun", true).apply();
        }

        setSupportActionBar(mainToolbar);
        setSingleEvent(linearLayout);
    }

    private void setSingleEvent(LinearLayout linearLayout) {
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            CardView cardView = (CardView) linearLayout.getChildAt(i);
            final int finalI = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    intent = null;
                    switch (finalI) {
                        case 0:
                            if (checkPermissions()) {
                                intent = new Intent(MainActivity.this, AeopActivity.class);
                            } else {
                                requestPermission(MainActivity.this);
                            }
                            break;
                        case 1:
                            if (checkPermissions()) {
                                intent = new Intent(MainActivity.this, BtActivity.class);
                            } else {
                                requestPermission(MainActivity.this);
                            }
                            break;
                        case 2:
                            if (checkPermissions()) {
                                intent = new Intent(MainActivity.this, FtActivity.class);
                            } else {
                                requestPermission(MainActivity.this);
                            }
                            break;
                        case 3:
                            if (checkWritePermission()) {
                                intent = new Intent(MainActivity.this, FilesListActivity.class);
                            } else {
                                requestPermission(MainActivity.this);
                            }
                            break;
                        default:
                            break;
                    }
                    if (intent != null) {
                        startActivity(intent);
                    }
                }
            });
        }
    }

    private void showPermWriteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogStyle);
        builder.create();
        builder.setCancelable(false);
        builder.setTitle(R.string.loc_provider_info_title);
        builder.setMessage(R.string.perm_write_warning);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void showPermLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogStyle);
        builder.create();
        builder.setCancelable(false);
        builder.setTitle(R.string.loc_provider_info_title);
        builder.setMessage(R.string.perm_location_warning);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkWritePermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkLocationPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showPermWriteDialog();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermLocationDialog();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PERMISSION_REQUEST_CODE == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setSingleEvent(linearLayout);
            }
        }
    }
}
