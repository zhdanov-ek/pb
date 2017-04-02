package com.example.gek.pb.activity;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import static android.Manifest.permission.CALL_PHONE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import com.example.gek.pb.R;
import com.example.gek.pb.data.Const;
import com.example.gek.pb.helpers.Utils;

public class SettingsActivity extends AppCompatActivity {

    private static final String  TAG = "SETTINGS";
    private SwitchCompat switchCallReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(myToolbar);

        switchCallReceiver = (SwitchCompat) findViewById(R.id.switchCallReceiver);
        switchCallReceiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.setNeedDetectCall(switchCallReceiver.isChecked(), getBaseContext());
                Log.d(TAG, "onClick: " + switchCallReceiver.isChecked());
            }
        });

        verifyCallPermissions();

    }

    // Check version api and granded permission. Open dialog for grand permission
    private void verifyCallPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && this.checkSelfPermission(CALL_PHONE) != PERMISSION_GRANTED) {
            requestPermissions(new String[]{CALL_PHONE}, Const.REQUEST_CODE_CALL_PHONE);
            Utils.setNeedDetectCall(false, this);
            switchCallReceiver.setEnabled(false);
            switchCallReceiver.setChecked(false);
        } else {
            switchCallReceiver.setEnabled(true);
            switchCallReceiver.setChecked(Utils.isNeedDetectCall(this));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Const.REQUEST_CODE_CALL_PHONE: {
                if (grantResults[0] == PERMISSION_GRANTED) {
                    verifyCallPermissions();
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, CALL_PHONE)) {
                        showSnackToSettingsOpen();
                    }
                }
            }
        }
    }

    // If permission can enable from settings OS show SnackBar
    private void showSnackToSettingsOpen(){
        Snackbar.make(switchCallReceiver, R.string.mes_permission_call_not_granded, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.openPermissionSettings(getBaseContext());
                    }
                })
                .show();
    }

}
