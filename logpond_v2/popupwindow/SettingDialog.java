package com.infocomm.logpond_v2.popupwindow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.infocomm.logpond_v2.R;
import com.infocomm.logpond_v2.activity.SettingsActivity;
import com.infocomm.logpond_v2.view.TTSManager;

public class SettingDialog extends Activity {

    EditText password;
    ImageView yesBtn, noBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingdialog);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        getWindow().setLayout((int)(width*.8), (int)(height*.4));

        password = findViewById(R.id.settingPasswordET);
        yesBtn = findViewById(R.id.settingDialogYesBtn);
        noBtn = findViewById(R.id.settingDialogNoBtn);

        //Toast.makeText(getBaseContext(), getString(R.string.enter_password_sound_text), Toast.LENGTH_SHORT).show();
        TTSManager.sayText(SettingDialog.this, getString(R.string.enter_password_sound_text));
        noBtnOnClick();
        yesBtnOnClick();
    }

//----------------------------------Functions-----------------------------

    private void noBtnOnClick() {
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void yesBtnOnClick() {
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String admin_pwd = "INFOCOMM123456";
                if (password.getText().toString().toUpperCase().equals(admin_pwd)) {
                    Toast.makeText(getBaseContext(), getString(R.string.access_approved_sound_text), Toast.LENGTH_SHORT).show();
                    TTSManager.sayText(SettingDialog.this, getString(R.string.access_approved_sound_text));
                    Intent settingsIntent = new Intent(SettingDialog.this, SettingsActivity.class);
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(settingsIntent);
                }

                else {
                    Toast.makeText(getBaseContext(), getString(R.string.access_denied_sound_text), Toast.LENGTH_SHORT).show();
                    TTSManager.sayText(SettingDialog.this, getString(R.string.access_denied_sound_text));
                }
            }
        });
    }


}
