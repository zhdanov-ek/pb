package com.example.gek.pb.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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

import java.util.Calendar;
import java.util.Date;

public class ContactEditActivity extends AppCompatActivity implements View.OnClickListener{

    ProgressBar progressBar;
    Button btnOk;
    Button btnRemovePhoto;
    ImageView ivPhoto;
    EditText etName;
    EditText etPosition;
    EditText etPhone;
    EditText etPhone2;
    EditText etEmail;

    private DatabaseReference db;
    private static final int REQUEST_LOAD_IMG = 1;


    private StorageReference folderRef;

    private Uri uriPhoto;                         // тут хранится выбранная картинка с галереи
    private Boolean isNewContact = true;
    private Boolean isNeedRemovePhoto = false;
    private Contact oldContact;
    private Contact changedContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //todo not showing toolbar - FIX THIS
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(myToolbar);

        setContentView(R.layout.activity_contact_edit);

        // Получаем ссылку на наше хранилище
        FirebaseStorage storage = FirebaseStorage.getInstance();
        folderRef = storage.getReferenceFromUrl(Const.STORAGE).child(Const.IMAGE_FOLDER);

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

        btnRemovePhoto = (Button) findViewById(R.id.btnRemovePhoto);
        btnRemovePhoto.setOnClickListener(this);
        btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);

        // Определяем это новый контакт или редактирование старого
        if (getIntent().hasExtra(Const.MODE) &&
                (getIntent().getIntExtra(Const.MODE, Const.MODE_NEW) == Const.MODE_EDIT)){
            isNewContact = false;
            oldContact = getIntent().getParcelableExtra(Const.EXTRA_CONTACT);
            fillValues(oldContact);
        } else {
            fillValues(null);
        }
    }

    /** Контроль ввода основных полей */
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
                break;
            case R.id.btnRemovePhoto:
                if ((oldContact != null) &&(oldContact.getPhotoName().length() > 0)){
                    isNeedRemovePhoto = true;
                }
                uriPhoto = null;
                ivPhoto.setImageResource(R.drawable.person_default);
                btnRemovePhoto.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void fillValues(Contact contact){
        if (contact == null) {
            etName.setText("");
            etPosition.setText("");
            etPhone.setText("");
            etPhone2.setText("");
            etEmail.setText("");
            uriPhoto = null;
            ivPhoto.setImageResource(R.drawable.person_default);
            btnRemovePhoto.setVisibility(View.INVISIBLE);
        } else {
            etName.setText(contact.getName());
            etPosition.setText(contact.getPosition());
            etPhone.setText(contact.getPhone());
            etPhone2.setText(contact.getPhone2());
            etEmail.setText(contact.getEmail());
            if (contact.getPhotoUrl().length() > 0){
                Glide.with(this)
                        .load(contact.getPhotoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.person_default)
                        .into(ivPhoto);
                btnRemovePhoto.setVisibility(View.VISIBLE);
            } else {
                ivPhoto.setImageResource(R.drawable.person_default);
            }
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


    /** Получаем URI фото с галереи */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null ) {
            if (requestCode == REQUEST_LOAD_IMG ) {
                Uri uri = data.getData();
                ivPhoto.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                ivPhoto.setImageURI(uri);
                uriPhoto = uri;

                if ((!isNewContact) && (oldContact.getPhotoName().length() > 0)) {
                    isNeedRemovePhoto = true;
                }
                btnRemovePhoto.setVisibility(View.VISIBLE);
            }
        } else {
            uriPhoto = null;
        }
    }

    /** Запись на сервер данных */
    private void sendToServer(){
        final String name = etName.getText().toString();
        final String position = etPosition.getText().toString();
        final String phone = etPhone.getText().toString();
        final String phone2 = etPhone2.getText().toString();
        final String email = etEmail.getText().toString();

        progressBar.setVisibility(View.VISIBLE);

        //todo удалить старое фото из БД если его вообще удаляют (добавить кнопку) или меняют

        // Если выбранно фото с галереи то сначало грузим фото, а потом запишем карточку в БД
        // Удаляем старое фото если оно было
        if (uriPhoto != null) {
            final String photoName = makePhotoName();
            StorageReference currentImageRef = folderRef.child(photoName);
            UploadTask uploadTask = currentImageRef.putFile(uriPhoto);

            // Регистрируем слушатель для контроля загрузки файла на сервер.
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
                    Contact newContact = new Contact(name, position, photoName, photoUrl.toString(),
                            email, phone, phone2);
                    if (isNewContact){
                        String key = db.child(Const.CHILD_CONTACTS).push().getKey();
                        newContact.setKey(key);
                        db.child(Const.CHILD_CONTACTS).child(key).setValue(newContact);
                    } else {
                        if (isNeedRemovePhoto) {
                            removeOldPhoto(oldContact.getPhotoName());
                        }
                        String oldKey = oldContact.getKey();
                        newContact.setKey(oldKey);
                        db.child(Const.CHILD_CONTACTS).child(oldKey).setValue(newContact);
                        changedContact = newContact;
                    }
                    fillValues(null);
                    progressBar.setVisibility(View.GONE);
                    if (!isNewContact) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(Const.EXTRA_CONTACT, changedContact);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                }
            });

        // Если фото не выбирали то просто делаем запись в БД с изменениями
        // Удаляем старое фото если его удалил пользователь
        } else {
            Contact newContact = new Contact(name, position, "",
                    "", email, phone, phone2);
            if (isNewContact){
                String newKey = db.child(Const.CHILD_CONTACTS).push().getKey();
                newContact.setKey(newKey);
                db.child(Const.CHILD_CONTACTS).child(newKey).setValue(newContact);
            } else {
                if (isNeedRemovePhoto){
                    removeOldPhoto(oldContact.getPhotoName());
                    newContact.setPhotoName("");
                    newContact.setPhotoUrl("");
                } else {
                    newContact.setPhotoName(oldContact.getPhotoName());
                    newContact.setPhotoUrl(oldContact.getPhotoUrl());
                }
                String oldKey = oldContact.getKey();
                newContact.setKey(oldKey);
                db.child(Const.CHILD_CONTACTS).child(oldKey).setValue(newContact);
                changedContact = newContact;
            }
            progressBar.setVisibility(View.GONE);
            if (!isNewContact) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(Const.EXTRA_CONTACT, changedContact);
                setResult(RESULT_OK, resultIntent);
                //todo Toast for add new Contact is need
            }
            finish();
        }
    }


    /** Формируем имя для фотки из данных пользователя. Убираем нежелательные символы */
    private String makePhotoName(){
        String time = Calendar.getInstance().getTime().toString();
        String name =   etName.getText().toString() +
                        time;
        name = name.replace(".", "");
        name = name.replace("@", "");
        name = name.replace(" ", "");
        name = name.replace("#", "");
        //todo проверить, что будет если грузить формат PNG
        name = name + ".jpg";
        return  name;
    }

    /** Перед сохранением данных проверяем введенные значения */
    //todo check input values
    private Boolean isValidate(){
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    private void removeOldPhoto(String namePhoto){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(Const.STORAGE);
        if (namePhoto.length() > 0){
            storageRef.child(Const.IMAGE_FOLDER).child(namePhoto).delete();
        }
    }

}
