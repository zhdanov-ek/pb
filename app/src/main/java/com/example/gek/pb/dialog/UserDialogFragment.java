package com.example.gek.pb.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.pb.R;
import com.example.gek.pb.data.Const;
import com.example.gek.pb.data.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


/**
 * Кастом дилог, который добавляет (редактирует) юзеров
 */

public class UserDialogFragment extends DialogFragment {

    private String email;
    private String description;
    private String key;
    private int mode;
    private ArrayList<String> emails;

    EditText etEmail, etDescription;

    private static final String TAG = "GEK";

    /** Создаем экземпляр диалога с передачей значений - редактирование сюществующей записи */
    public static UserDialogFragment newInstance(String email, String description, String key) {
        UserDialogFragment rdf = new UserDialogFragment();
        Bundle args = new Bundle();
        args.putInt(Const.MODE, Const.MODE_EDIT);
        args.putString("email", email);
        args.putString("description", description);
        args.putString("key", key);
        rdf.setArguments(args);
        return rdf;
    }


    /** Создаем экземпляр диалога для создания новой записи */
    public static UserDialogFragment newInstance(ArrayList<String> emails) {
        UserDialogFragment rdf = new UserDialogFragment();
        Bundle args = new Bundle();
        args.putInt(Const.MODE, Const.MODE_NEW);
        args.putStringArrayList(Const.EXTRA_EMAILS, emails);
        rdf.setArguments(args);
        return rdf;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mode = getArguments().getInt(Const.MODE);
        String title;
        if (mode == Const.MODE_EDIT){
            this.email = getArguments().getString("email");
            this.description = getArguments().getString("description");
            this.key = getArguments().getString("key");
            title = "Edit user: " + email;
        } else {
            this.email = "";
            this.description = "";
            this.key = null;
            this.emails = getArguments().getStringArrayList(Const.EXTRA_EMAILS);
            title = "New user";
        }


        // Создаем вью по нашему лаяуту
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.dialog_user, null);
        etEmail = (EditText)root.findViewById(R.id.etEmail);
        etDescription = (EditText)root.findViewById(R.id.etDescription);

        etEmail.setText(email);
        etDescription.setText(description);
        
        // Строим диалог, добавляя кнопки и пишем обработчик
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(root)
                .setTitle(title)
                .setPositiveButton(R.string.hint_ok, listenerOk)
                .setNegativeButton(R.string.hint_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                });
        return builder.create();
    }


    DialogInterface.OnClickListener listenerOk = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            String email = etEmail.getText().toString();
            email = email.replace(" ", "");
            if ((email.isEmpty()) || (email.length() < 6)) {
                //todo При не успешной валидации данных не закрывать диалог
                // http://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
                Toast.makeText(getContext(), "Value EMAIL is incorrect", Toast.LENGTH_SHORT).show();
            } else {
                switch (mode) {
                    case Const.MODE_NEW:
                        if (isNewEmail(email, emails)) {
                            saveUser(email, etDescription.getText().toString(), key);
                            Toast.makeText(getContext(), "Write " + email, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Entered " + email + " finded in list.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case Const.MODE_EDIT:
                        saveUser(email, etDescription.getText().toString(), key);
                        break;
                }
            }
        }
    };



    /** По клику на ОК записываем данные в БД */
    private void saveUser(String email, String description, String key){
        User user = new User(email, description);
        DatabaseReference db;
        db = FirebaseDatabase.getInstance().getReference();
        if (key == null) {
            String newKey = db.child(Const.CHILD_USERS).push().getKey();
            user.setKey(newKey);
            db.child(Const.CHILD_USERS).child(newKey).setValue(user);
            Toast.makeText(getContext(), "New  " + email + " writed", Toast.LENGTH_SHORT).show();
        } else {
            user.setKey(key);
            db.child(Const.CHILD_USERS).child(key).setValue(user);
        }
    }


    /** Проверяем не добавлен ли такой емейл ранее */
    private Boolean isNewEmail(String email, ArrayList<String> emails){
        for (String current: emails) {
            if (email.contentEquals(current)) {
                return false;
            }
        }
        return true;
    }
    
}
