package com.example.gek.pb.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.gek.pb.R;
import com.example.gek.pb.data.Const;
import com.example.gek.pb.data.Contact;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ContactActivity extends AppCompatActivity implements View.OnClickListener{

    ProgressBar progressBar;
    Button btnOk;
    ImageView ivPhoto;
    EditText etName;
    EditText etPosition;
    EditText etPhone;
    EditText etPhone2;
    EditText etEmail;

    private DatabaseReference db;
    private static final int REQUEST_LOAD_IMG = 1;

    private StorageReference storageRef;
    private FirebaseStorage storage;
    private StorageReference folderRef;

    private Uri uriPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        // Получаем ссылку на наше хранилище
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl(Const.STORAGE);
        folderRef = storageRef.child(Const.IMAGE_FOLDER);

        db = FirebaseDatabase.getInstance().getReference();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        etName = (EditText) findViewById(R.id.etName);
        etPosition = (EditText) findViewById(R.id.etPosition);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etPhone2 = (EditText) findViewById(R.id.etPhone2);
        etEmail = (EditText) findViewById(R.id.etEmail);
        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
        ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePhoto();
            }
        });

        etName.addTextChangedListener(textWatcher);
        etPosition.addTextChangedListener(textWatcher);
        etPhone.addTextChangedListener(textWatcher);

        btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            if ((etName.getText().length() > 0) &&
                (etPosition.getText().length() > 0) &&
                (etPhone.getText().length() > 0)){
                btnOk.setEnabled(true);
            } else {
                btnOk.setEnabled(false);
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnOk:
                sendToServer();
        }
    }



    private void choosePhoto(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        if (photoPickerIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(photoPickerIntent, REQUEST_LOAD_IMG);
        } else {
            Toast.makeText(this, "No program for choose image!", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null ) {
            if (requestCode == REQUEST_LOAD_IMG ) {
                Uri uri = data.getData();
                ivPhoto.setImageURI(uri);
                uriPhoto = uri;
            }
        } else {
            uriPhoto = null;
        }
    }

    private void sendToServer(){
        final String name = etName.getText().toString();
        final String position = etPosition.getText().toString();
        final String phone = etPhone.getText().toString();
        final String phone2 = etPhone2.getText().toString();
        final String email = etEmail.getText().toString();

        progressBar.setVisibility(View.VISIBLE);
        Boolean isHavePhoto = false;
        if (uriPhoto != null) {
            isHavePhoto = true;
        }

        // Сначала грузим фото и после этого записываем контакт с указанием URL на фото
        if (isHavePhoto) {
            String photoName = makePhotoName();
            StorageReference currentImageRef = folderRef.child(photoName);
            UploadTask uploadTask = currentImageRef.putFile(uriPhoto);

            // Регистрируем слушатель для контроля загрузки файла на сервер
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getBaseContext(), "Loading image to server: ERROR", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Получаем ссылку на закачанный файл и сохраняем ее в контакте
                    Uri photoUrl = taskSnapshot.getDownloadUrl();
                    Contact newContact = new Contact(name, position, photoUrl.toString(), email, phone, phone2);
                    db.child(Const.CHILD_CONTACTS).push().setValue(newContact);
                    progressBar.setVisibility(View.GONE);
                }
            });

        // Если фото нет то просто записываем данные в БД
        } else {
            Contact newContact = new Contact(name, position, null, email, phone, phone2);
            db.child(Const.CHILD_CONTACTS).push().setValue(newContact);
            progressBar.setVisibility(View.GONE);
        }



    }


    /** Формируем имя для фотки из данных пользователя. Убираем нежелательные символы */
    private String makePhotoName(){
        String name =   etName.getText().toString() +
                        etPhone.getText().toString();
        name = name.replace(".", "");
        name = name.replace("@", "");
        name = name.replace(" ", "");
        name = name.replace("#", "");
        //todo проверить, что будет если грузить формат PNG
        name = name + ".jpg";
        return  name;
    }

    /** Перед сохранением данных проверяем введенные значения */
    private Boolean isValidate(){
        return true;
    }

    private void clearValues(){
        etName.setText("");
        etPosition.setText("");
        etPhone.setText("");
        etPhone2.setText("");
        etEmail.setText("");
        uriPhoto = null;
        ivPhoto.setImageResource(R.drawable.person_default);
    }

}
