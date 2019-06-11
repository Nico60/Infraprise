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

public class FtActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 1;

    private EditText mAdrInput;
    private EditText mFtBeInput;
    private EditText mGftInput;
    private EditText mNroInput;
    private EditText mPmInput;
    private FileUtils mFileUtils;
    private ImageUtils mImageUtils;
    private String mAdrText;
    private String mFtBeText;
    private String mGftText;
    private String mNroText;
    private String mPmText;
    private String mPoleType = "FT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ft);

        mFileUtils = new FileUtils(this);
        mImageUtils = new ImageUtils(this);

        mAdrInput = findViewById(R.id.ftAdrText);
        mFtBeInput = findViewById(R.id.ftBeText);
        mGftInput = findViewById(R.id.gFtText);
        mNroInput = findViewById(R.id.ftNroText);
        mPmInput = findViewById(R.id.ftPmText);

        Toolbar ftToolbar = findViewById(R.id.ftToolbar);

        FloatingActionButton ftFab = findViewById(R.id.ftFab);
        ftFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdrText = mAdrInput.getText().toString();
                mFtBeText = mFtBeInput.getText().toString();
                mGftText = mGftInput.getText().toString();
                mNroText = mNroInput.getText().toString();
                mPmText = mPmInput.getText().toString();
                String[] list = new String[] {
                        getString(R.string.nro_number) + " " + mNroText,
                        getString(R.string.pm_number) + " " + mPmText,
                        getString(R.string.pm_number) + " " + mGftText,
                        getString(R.string.ft_be_number) + " " + mFtBeText,
                        getString(R.string.adresse) + " " + mAdrText};
                if (isAnswered()) {
                    mFileUtils.Save(mNroText, mPmText, mPoleType, mGftText, list);
                    finish();
                } else {
                    warning();
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
                    mImageUtils.dispatchTakePictureIntent(mNroText, mPmText, mPoleType, mGftText);
                } else {
                    warning();
                }
            }
        });

        setSupportActionBar(ftToolbar);
    }

    private boolean isAnswered() {
        return (!mNroText.isEmpty() && !mPmText.isEmpty() && !mGftText.isEmpty());
    }

    private void warning() {
        Toast.makeText(this, getString(R.string.ft_warning), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Toast.makeText(this, getString(R.string.picture_saved), Toast.LENGTH_SHORT).show();
        }
    }
}
