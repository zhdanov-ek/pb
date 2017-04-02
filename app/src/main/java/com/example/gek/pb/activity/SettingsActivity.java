package com.example.gek.pb.activity;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;


import com.example.gek.pb.R;
import com.example.gek.pb.helpers.Utils;

public class SettingsActivity extends PermissionsActivity {

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

        setViewForSnackbar(switchCallReceiver);
        verifyCallPermissions();

    }


    @Override
    void setViewForSnackbar(View v) {
        viewForSnackbar = v;
    }

    @Override
    void workIfPermissionsGranded() {
        switchCallReceiver.setEnabled(true);
        switchCallReceiver.setChecked(Utils.isNeedDetectCall(this));
    }

    @Override
    void workIfPermissionsNotGranded() {
        Utils.setNeedDetectCall(false, this);
        switchCallReceiver.setEnabled(false);
        switchCallReceiver.setChecked(false);
    }
}
