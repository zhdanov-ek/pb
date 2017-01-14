package com.example.gek.pb.helpers;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;

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



}
