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
import com.example.gek.pb.data.Contact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

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
        String message = ctx.getResources().getString(R.string.mes_about) + "\n\n" +
                ctx.getResources().getString(R.string.about_user) + ":\n" +
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

    public static void showHelp(Context ctx){
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        String message = ctx.getResources().getString(R.string.mes_help);
        builder.setTitle(R.string.app_name)
                .setMessage(message)
                .setIcon(R.drawable.ic_help)
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


    // Сортировка списка по именам
    public static void sortContacts(ArrayList<Contact> inputList){
        Collections.sort(inputList, new NameContactComparator());
    }

    // Вспомогательный класс для сравнения объектов по конкретным полям
    public static class NameContactComparator implements Comparator<Contact> {
        @Override
        public int compare(Contact c1, Contact c2) {
            return c1.getName().compareTo(c2.getName());
        }
    }
}
