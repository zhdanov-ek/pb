package com.example.gek.pb.helpers;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.example.gek.pb.R;
import com.example.gek.pb.activity.SignInActivity;
import com.example.gek.pb.data.Const;
import com.example.gek.pb.data.Contact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

/**
 * Вспомогательные методы
 */

public class Utils {

    private static final String TAG = "UTILS";

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
                ctx.getResources().getString(R.string.mes_company) + "\n" +
                "\n\n" +
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

    /**  По первым трем цифрам определяет мобильного оператора возвращая его название */
    public static String defineMobile(String num){
        String logo;
        String prefix = String.copyValueOf(num.toCharArray(), 0, 3);
        if (prefix.contentEquals("063") || prefix.contentEquals("093") || prefix.contentEquals("073") ) {
            logo = "life";
        } else if (prefix.contentEquals("099") || prefix.contentEquals("095") ||
                prefix.contentEquals("050") || prefix.contentEquals("066")) {
            logo = "mts";
        } else if (prefix.contentEquals("067") || prefix.contentEquals("068") ||
                prefix.contentEquals("096") || prefix.contentEquals("097") ||
                prefix.contentEquals("098")) {
            logo = "kyivstar";
        } else logo = "other";

        return logo;
    }




    /** Сортировка списка по именам */
    public static void sortContacts(ArrayList<Contact> inputList){
        Collections.sort(inputList, new NameContactComparator());
    }

    /** Вспомогательный класс для сравнения объектов по конкретным полям */
    public static class NameContactComparator implements Comparator<Contact> {
        @Override
        public int compare(Contact c1, Contact c2) {
            return c1.getName().compareTo(c2.getName());
        }
    }

    /** Возвращает отобранный список по указанному тексту */
    public static ArrayList<Contact> searchContacts(ArrayList<Contact> list, String text){
        ArrayList<Contact> result = new ArrayList<>();
        for (Contact contact: list) {
            if ((contact.getName().toLowerCase().contains(text.toLowerCase())) ||
                    (contact.getPosition().toLowerCase().contains(text.toLowerCase())))
            {
                result.add(contact);
            }
        }
        return result;
    }

    public static Boolean isNumberOfContact(Contact contact, String number){
        if ((number != null) && (number.length() > 0)) {
            if ((contact.getPhone2() != null) && (contact.getPhone2().length() > 0)){
                if (((number.contains(contact.getPhone()))) ||
                        (number.contains(contact.getPhone2()))){
                    return true;
                }
            } else {
                if (number.contains(contact.getPhone())){
                    return true;
                }
            }
        }
        return false;
    }

    public static void saveLastContact(Contact contact, Context ctx){
        SharedPreferences pref = ctx.getSharedPreferences(Const.PREF_FILE_NAME, MODE_PRIVATE);
        long date = new Date().getTime();
        pref.edit()
                .putString(Const.PREF_CONTACT_NAME, contact.getName())
                .putString(Const.PREF_CONTACT_POSITION, contact.getPosition())
                .putString(Const.PREF_CONTACT_PHONE, contact.getPhone())
                .putLong(Const.PREF_CONTACT_TIME, date).apply();
        Log.d(TAG, "saveLastContact: date = " + date);
        if ((contact.getPhone2() != null) && (contact.getPhone2().length() > 0)){
            pref.edit().putString(Const.PREF_CONTACT_PHONE2, contact.getPhone2()).apply();
        } else {
            pref.edit().putString(Const.PREF_CONTACT_PHONE2, "").apply();
        }

        if ((contact.getPhotoName() != null) && (contact.getPhotoName().length() > 0)){
            pref.edit().putString(Const.PREF_CONTACT_PHOTO_NAME, contact.getPhotoName()).apply();
        } else {
            pref.edit().putString(Const.PREF_CONTACT_PHOTO_NAME, "").apply();
        }

        if ((contact.getPhotoUrl() != null) && (contact.getPhotoUrl().length() > 0)){
            pref.edit().putString(Const.PREF_CONTACT_PHOTO_URL, contact.getPhotoUrl()).apply();
        } else {
            pref.edit().putString(Const.PREF_CONTACT_PHOTO_URL, "").apply();
        }

        if ((contact.getEmail() != null) && (contact.getEmail().length() > 0)){
            pref.edit().putString(Const.PREF_CONTACT_EMAIL, contact.getEmail()).apply();
        } else {
            pref.edit().putString(Const.PREF_CONTACT_EMAIL, "").apply();
        }
    }

    public static Contact readLastContact(Context ctx){
        SharedPreferences pref = ctx.getSharedPreferences(Const.PREF_FILE_NAME, MODE_PRIVATE);
        if (pref.contains(Const.PREF_CONTACT_NAME)){
            Contact lastContact = new Contact();
            lastContact.setName(pref.getString(Const.PREF_CONTACT_NAME, ""));
            lastContact.setPosition(pref.getString(Const.PREF_CONTACT_POSITION, ""));
            lastContact.setPhotoName(pref.getString(Const.PREF_CONTACT_PHOTO_NAME, ""));
            lastContact.setPhotoUrl(pref.getString(Const.PREF_CONTACT_PHOTO_URL, ""));
            lastContact.setPhone(pref.getString(Const.PREF_CONTACT_PHONE, ""));
            lastContact.setPhone2(pref.getString(Const.PREF_CONTACT_PHONE2, ""));
            lastContact.setEmail(pref.getString(Const.PREF_CONTACT_EMAIL, ""));
            return lastContact;
        }
        return null;
    }

    /** Check time write contact to SharedPreferences. If not more then 60 s this is our contact */
    public static Boolean isLastContact(Context ctx){
        SharedPreferences pref = ctx.getSharedPreferences(Const.PREF_FILE_NAME, MODE_PRIVATE);
        if (pref.contains(Const.PREF_CONTACT_TIME)){
            long lastTime = pref.getLong(Const.PREF_CONTACT_TIME, 0);
            long difference = (new Date().getTime() - lastTime)/1000;
            if (difference < 60){
                Log.d(TAG, "isLastContact: contact writed (seconds) = " + difference);
            }
            return true;
        }
        return false;
    }


}
