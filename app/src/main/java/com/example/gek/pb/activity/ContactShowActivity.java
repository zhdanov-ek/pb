package com.example.gek.pb.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pb.R;
import com.example.gek.pb.data.Const;
import com.example.gek.pb.data.Contact;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ContactShowActivity extends AppCompatActivity {

    ImageView ivPhoto;
    TextView tvName, tvPosition, tvPhone, tvPhone2, tvEmail;
    Contact openContact;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_show);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(myToolbar);

        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
        tvName = (TextView) findViewById(R.id.tvName);
        tvPosition = (TextView) findViewById(R.id.tvPosition);
        tvPhone = (TextView) findViewById(R.id.tvPhone);
        tvPhone2 = (TextView) findViewById(R.id.tvPhone2);
        tvEmail = (TextView) findViewById(R.id.tvEmail);

        openContact = getIntent().getParcelableExtra(Const.EXTRA_CONTACT);
        fillValues(openContact);
    }

    private void fillValues(Contact contact){
        if (contact.getPhotoUrl().length() > 0){
            Glide.with(this)
                    .load(contact.getPhotoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.person_default)
                    .into(ivPhoto);
        } else {
            ivPhoto.setImageResource(R.drawable.person_default);
        }
        tvName.setText(contact.getName());
        tvPosition.setText(contact.getPosition());
        tvPhone.setText(contact.getPhone());
        tvPhone2.setText(contact.getPhone2());
        tvEmail.setText(contact.getEmail());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        String addContact = getResources().getString(R.string.menu_add_contact);
        String editContact = getResources().getString(R.string.menu_edit_contact);
        String removeContact = getResources().getString(R.string.menu_remove_contact);
        String listUsers = getResources().getString(R.string.menu_list_users);
        String search = getResources().getString(R.string.menu_search);

        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getTitle().toString().contentEquals(addContact)) {
                menu.getItem(i).setVisible(false);
            }

            if (menu.getItem(i).getTitle().toString().contentEquals(search)) {
                menu.getItem(i).setVisible(false);
            }

            if (menu.getItem(i).getTitle().toString().contentEquals(listUsers)) {
                menu.getItem(i).setVisible(false);
            }

            if ((menu.getItem(i).getTitle().toString().contentEquals(editContact)) &&
                    (! MainActivity.isAdmin)){
                menu.getItem(i).setVisible(false);
            }
            if ((menu.getItem(i).getTitle().toString().contentEquals(removeContact)) &&
                    (! MainActivity.isAdmin)){
                menu.getItem(i).setVisible(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.ab_edit:
                Intent editContactIntent = new Intent(this, ContactEditActivity.class);
                editContactIntent.putExtra(Const.MODE, Const.MODE_EDIT);
                editContactIntent.putExtra(Const.EXTRA_CONTACT, openContact);
                startActivityForResult(editContactIntent, Const.REQUEST_EDIT_CONTACT);
                break;
            case R.id.ab_remove:
                removeContact();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Удаление контакта из базы и фото с хранилища */
    private void removeContact(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.menu_remove_contact);
        builder.setIcon(R.drawable.ic_warning);
        String message = getResources().getString(R.string.confirm_remove_contact);
        builder.setMessage(message + "\n" + openContact.getName());
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.hint_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setPositiveButton(R.string.hint_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //todo повесить лисенеры на отслеживание успешного и не успешного удаления записи

                // Получаем ссылку на наше хранилище и удаляем фото по названию
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl(Const.STORAGE);
                if (openContact.getPhotoUrl().length() > 0){
                    storageRef.child(Const.IMAGE_FOLDER).child(openContact.getPhotoName()).delete();
                }

                // Получаем ссылку на базу данных и удаляем контакт по ключу загруженного контакта
                DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                db.child(Const.CHILD_CONTACTS).child(openContact.getKey()).removeValue();
                finish();
            }
        });
        builder.show();
    }


    // обновляем карточку если ее успешно отредактировали
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((data != null) && (requestCode == Const.REQUEST_EDIT_CONTACT) && (resultCode == RESULT_OK)){
            openContact = data.getParcelableExtra(Const.EXTRA_CONTACT);
            fillValues(openContact);
        }
    }
}
