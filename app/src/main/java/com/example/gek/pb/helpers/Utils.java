package com.example.gek.pb.helpers;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.example.gek.pb.R;
import com.example.gek.pb.activity.SignInActivity;
import com.example.gek.pb.data.Const;
import com.example.gek.pb.data.Contact;
import com.example.gek.pb.data.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static android.content.Context.MODE_PRIVATE;

/**
 * Helpers methods
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

    public static void showHelp(final Context ctx){
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        String message = ctx.getResources().getString(R.string.mes_help);
        builder.setTitle(R.string.app_name)
                .setMessage(message)
                .setIcon(R.drawable.ic_help)
                .setCancelable(true)
                .setPositiveButton(R.string.hint_send_email,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Intent emailIntent = new Intent(Intent.ACTION_VIEW);
                                    // Указваем все необходимые данные для написания письма
                                    Uri data = Uri.parse("mailto:?subject=" +
                                            ctx.getResources().getString(R.string.email_add_user_theme)
                                            + "&body=" + ctx.getResources().getString(R.string.mes_hello)
                                            + ",\n  " + ctx.getResources().getString(R.string.email_add_user_body)
                                            + "&to=" + ctx.getResources().getString(R.string.admin_email));
                                    emailIntent.setData(data);
                                    ctx.startActivity(emailIntent);
                                } catch (Exception e) {
                                    Toast.makeText(ctx, "Your mail has failed...",
                                            Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        })
                .setNegativeButton(R.string.hint_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**  По первым трем цифрам определяем мобильного оператора возвращая его название */
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
    private static class NameContactComparator implements Comparator<Contact> {
        @Override
        public int compare(Contact c1, Contact c2) {
            return c1.getName().compareTo(c2.getName());
        }
    }


    /** Сортировка списка по email */
    public static void sortUsers(ArrayList<User> inputList){
        Collections.sort(inputList, new EmailUserComparator());
    }

    /** Вспомогательный класс для сравнения объектов по email */
    private static class EmailUserComparator implements Comparator<User> {
        @Override
        public int compare(User u1, User u2) {
            return u1.getEmail().compareTo(u2.getEmail());
        }
    }

    /** Возвращает отобранный список контактов по указанному тексту */
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

    /** Возвращает отобранный список пользователей по указанному тексту */
    public static ArrayList<User> searchUsers(ArrayList<User> list, String text){
        ArrayList<User> result = new ArrayList<>();
        for (User user: list) {
            if ((user.getEmail().toLowerCase().contains(text.toLowerCase())) ||
                    (user.getDescription().toLowerCase().contains(text.toLowerCase())))
            {
                result.add(user);
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

    /** Get preferences for detect inout call in CallReceiver*/
    public static Boolean isNeedDetectCall(Context ctx){
        SharedPreferences pref = ctx.getSharedPreferences(Const.PREF_FILE_NAME, MODE_PRIVATE);
        if ((pref.contains(Const.PREF_DETECT_CALL)) &&
            (pref.getBoolean(Const.PREF_DETECT_CALL, false))){
            return true;
        } else {
            return false;
        }
    }

    /** Set value to preferences */
    public static void setNeedDetectCall(Boolean choosedValue, Context ctx){
        SharedPreferences pref = ctx.getSharedPreferences(Const.PREF_FILE_NAME, MODE_PRIVATE);
        pref.edit().putBoolean(Const.PREF_DETECT_CALL, choosedValue).apply();
    }


    // Open system settings of program
    public static void openPermissionSettings(Context ctx) {
        final Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + ctx.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        ctx.startActivity(intent);
    }

}
