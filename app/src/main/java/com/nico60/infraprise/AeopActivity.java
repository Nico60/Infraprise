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

import com.nico60.infraprise.Utils.FileUtils;
import com.nico60.infraprise.Utils.ImageUtils;

public class AeopActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 1;

    private EditText mAdrInput;
    private EditText mAeopInput;
    private EditText mBtInput;
    private EditText mDrcInput;
    private EditText mHtInput;
    private EditText mNroInput;
    private EditText mPmInput;
    private FileUtils mFileUtils;
    private ImageUtils mImageUtils;
    private String mAdrText;
    private String mAeopText;
    private String mBtText;
    private String mDrcText;
    private String mHtText;
    private String mNroText;
    private String mPmText;
    private String mPoleType = "AEOP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aeop);

        mFileUtils = new FileUtils(this);
        mImageUtils = new ImageUtils(this);

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
                String[] list = new String[] {
                        getString(R.string.nro_number) + " " + mNroText,
                        getString(R.string.pm_number) + " " + mPmText,
                        getString(R.string.aeop_number) + " " + mAeopText,
                        getString(R.string.bt_number) + " " + mBtText,
                        getString(R.string.aeop_height_pole_to_plant, mHtText),
                        getString(R.string.aeop_distance_pole_to_road, mDrcText),
                        getString(R.string.adresse) + " " + mAdrText};
                if (isAnswered()) {
                    mFileUtils.Save(mNroText, mPmText, mPoleType, mAeopText, list);
                    finish();
                } else {
                    warning();
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
                    mImageUtils.dispatchTakePictureIntent(mNroText, mPmText, mPoleType, mAeopText);
                } else {
                    warning();
                }
            }
        });

        setSupportActionBar(aeopToolbar);
    }

    private boolean isAnswered() {
        return (!mNroText.isEmpty() && !mPmText.isEmpty() && !mAeopText.isEmpty());
    }

    private void warning() {
        Toast.makeText(this, getString(R.string.aeop_warning), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Toast.makeText(this, getString(R.string.picture_saved), Toast.LENGTH_SHORT).show();
        }
    }
}
