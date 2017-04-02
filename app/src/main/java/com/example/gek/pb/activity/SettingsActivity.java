package com.example.gek.pb.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import com.example.gek.pb.R;
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

        switchCallReceiver.setChecked(Utils.isNeedDetectCall(this));

    }
}
