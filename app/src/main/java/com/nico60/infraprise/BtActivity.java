package com.nico60.infraprise;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nico60.infraprise.Utils.AppFileUtils;
import com.nico60.infraprise.Utils.AppImageUtils;

public class BtActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 1;

    private AppFileUtils mAppFileUtils;
    private AppImageUtils mAppImageUtils;
    private EditText mAdrInput;
    private EditText mBtInput;
    private EditText mNroInput;
    private EditText mPmInput;
    private String mAdrText;
    private String mBtText;
    private String mNroText;
    private String mPmText;
    private String mPoleType = "BT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt);

        mAppFileUtils = new AppFileUtils(this);
        mAppImageUtils = new AppImageUtils(this);

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
                String[] list = new String[] {
                        getString(R.string.nro_number) + " " + mNroText,
                        getString(R.string.pm_number) + " " + mPmText,
                        getString(R.string.bt_number) + " " + mBtText,
                        getString(R.string.adress) + " " + mAdrText};
                if (isAnswered()) {
                    mAppFileUtils.save(mNroText, mPmText, mPoleType, mBtText, list);
                    finish();
                } else {
                    warning();
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
                    warning();
                }
            }
        });

        setSupportActionBar(btToolbar);
    }

    private boolean isAnswered() {
        return (!mNroText.isEmpty() && !mPmText.isEmpty() && !mBtText.isEmpty());
    }

    private void warning() {
        Toast.makeText(this, getString(R.string.bt_warning), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Toast.makeText(this, getString(R.string.picture_saved), Toast.LENGTH_SHORT).show();
        }
    }
}
