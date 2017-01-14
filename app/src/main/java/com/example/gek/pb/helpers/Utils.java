package com.example.gek.pb.helpers;


import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.example.gek.pb.R;
import com.example.gek.pb.activity.SignInActivity;

/**
 * Created by gek on 12.01.2017.
 */

public class Utils {

    private static final String TAG = "GEK";

    public static boolean hasInternet(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static void showSnackBar(View view, String s){
        Snackbar snackbar = Snackbar.make(view, s, Snackbar.LENGTH_LONG);
        snackbar.show();
    }



    public static void showAbout(Context ctx){
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        String message = ctx.getResources().getString(R.string.mes_about) + "\n" +
                ctx.getResources().getString(R.string.about_user) + ": " +
                SignInActivity.userEmail;
        builder.setTitle(R.string.menu_about)
                .setMessage(message)
                .setIcon(R.drawable.ic_about)
                .setCancelable(true)
                .setNegativeButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}