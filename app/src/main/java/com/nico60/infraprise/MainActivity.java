package com.nico60.infraprise;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;

    private Intent intent;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = findViewById(R.id.mainLinear);

        if (checkPermission()) {
            setSingleEvent(linearLayout);
        } else {
            requestPermission();
        }
    }

    private void setSingleEvent(LinearLayout linearLayout) {
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            CardView cardView = (CardView) linearLayout.getChildAt(i);
            final int finalI = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (finalI) {
                        case 0:
                            intent = new Intent(MainActivity.this, AeopActivity.class);
                            break;
                        case 1:
                            intent = new Intent(MainActivity.this, BtActivity.class);
                            break;
                        case 2:
                            intent = new Intent(MainActivity.this, FtActivity.class);
                            break;
                        case 3:
                            intent = new Intent(MainActivity.this, FilesListActivity.class);
                        default:
                            break;
                    }
                    startActivity(intent);
                }
            });
        }
    }

    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, getString(R.string.perm_warning), Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
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
