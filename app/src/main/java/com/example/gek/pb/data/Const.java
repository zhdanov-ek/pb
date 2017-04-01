package com.example.gek.pb.data;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by gek on 02.01.17.
 */

public class Const {
    public static final String STORAGE = "gs://corporatephonebook-9cff6.appspot.com";
    public static final String IMAGE_FOLDER = "images";
    public static final String CHILD_CONTACTS = "contacts";
    public static final String CHILD_USERS = "users";
    public static final String CHILD_ADMIN = "admin_account";

    public static final String EXTRA_EMAILS = "emails_array";
    public static final String EXTRA_CONTACT = "contact";
    public static final String EXTRA_NUMBER = "number";

    public static final String MODE = "edit_mode";
    public static final int MODE_NEW = 0;
    public static final int MODE_EDIT = 1;

    public static final String ACTION_MAIN = "action_main";
    public static final int ACTION_TURNOFF = 0;
    public static final int ACTION_SIGNOUT = 1;

    public static final int REQUEST_EDIT_CONTACT = 1;
    public static final int REQUEST_MAIN = 2;
    public static final int REQUEST_SIGN_IN = 101;

    public static final String PREF_FILE_NAME = "preferences.txt";
    public static final String PREF_CONTACT_NAME = "name";
    public static final String PREF_CONTACT_POSITION = "position";
    public static final String PREF_CONTACT_PHOTO_NAME = "photo_name";
    public static final String PREF_CONTACT_PHOTO_URL = "photo_url";
    public static final String PREF_CONTACT_PHONE = "phone";
    public static final String PREF_CONTACT_PHONE2 = "phone2";
    public static final String PREF_CONTACT_EMAIL = "email";
    public static final String PREF_CONTACT_TIME = "date";


    public static final DatabaseReference db = FirebaseDatabase.getInstance().getReference();
}
