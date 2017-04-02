package com.example.gek.pb.activity;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.gek.pb.R;
import com.example.gek.pb.data.Const;
import com.example.gek.pb.helpers.Utils;

import static android.Manifest.permission.CALL_PHONE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Store methods for check permissions, request to get they and abstract methods for work
 */

abstract public class PermissionsActivity extends AppCompatActivity {
    protected View viewForSnackbar;

    abstract void setViewForSnackbar(View v);
    abstract void workIfPermissionsGranded();
    abstract void workIfPermissionsNotGranded();

    // Check version api and granded permission. Open dialog for grand permission if needed
    protected void verifyCallPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && this.checkSelfPermission(CALL_PHONE) != PERMISSION_GRANTED) {
            requestPermissions(new String[]{CALL_PHONE}, Const.REQUEST_CODE_CALL_PHONE);
            workIfPermissionsNotGranded();
        } else {
            workIfPermissionsGranded();
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
        Snackbar.make(viewForSnackbar, R.string.mes_permission_call_not_granded, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.openPermissionSettings(getBaseContext());
                    }
                })
                .show();
    }
}
