package com.example.gek.pb.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;



import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pb.R;
import com.example.gek.pb.data.Const;
import com.example.gek.pb.data.Contact;
import com.example.gek.pb.helpers.CircleTransform;
import com.example.gek.pb.helpers.Utils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ContactShowActivity extends AppCompatActivity {

    private static final String TAG = "CONTACT_SHOW";
    ImageView ivPhoto, ivRing;
    ImageView ivPhone, ivPhone2;
    TextView tvName, tvPosition, tvPhone, tvPhone2, tvEmail;
    Contact openContact;
    LinearLayout llPhone, llPhone2, llEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_show);

        // Анимация для всего поля с телефоном и значком при клике
        final Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(myToolbar);


        llPhone = (LinearLayout) findViewById(R.id.llPhone);
        llPhone2 = (LinearLayout) findViewById(R.id.llPhone2);
        llEmail = (LinearLayout) findViewById(R.id.llEmail);
        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
        ivRing = (ImageView) findViewById(R.id.ivRing);
        tvName = (TextView) findViewById(R.id.tvName);
        tvPosition = (TextView) findViewById(R.id.tvPosition);
        tvPhone = (TextView) findViewById(R.id.tvPhone);
        ivPhone = (ImageView) findViewById(R.id.ivPhone);
        tvPhone2 = (TextView) findViewById(R.id.tvPhone2);
        ivPhone2 = (ImageView) findViewById(R.id.ivPhone2);
        tvEmail = (TextView) findViewById(R.id.tvEmail);

        llPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                llPhone.startAnimation(anim);
                makeCall(tvPhone.getText().toString());
            }
        });

        llPhone2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                llPhone2.startAnimation(anim);
                makeCall(tvPhone2.getText().toString());
            }
        });

        llEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                llEmail.startAnimation(anim);
                sendEmail(tvName.getText().toString(), tvEmail.getText().toString());
            }
        });

        openContact = getIntent().getParcelableExtra(Const.EXTRA_CONTACT);
        fillValues(openContact);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_show, menu);

        // Находим наш пункт меню и с помощью хелпера MenuItemCompat привязываем наш экшн провайдер
        MenuItem menuItem = menu.findItem(R.id.ab_share_item);
        ShareActionProvider shareActionProvider = new ShareActionProvider(this);
        MenuItemCompat.setActionProvider(menuItem, shareActionProvider);
        shareActionProvider.setShareIntent(createShareContactIntent());

        String addContact = getResources().getString(R.string.menu_add_contact);
        String editContact = getResources().getString(R.string.menu_edit_contact);
        String removeContact = getResources().getString(R.string.menu_remove_contact);

        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getTitle().toString().contentEquals(addContact)) {
                menu.getItem(i).setVisible(false);
            }

            if ((menu.getItem(i).getTitle().toString().contentEquals(editContact)) &&
                    (! SignInActivity.isAdmin)){
                menu.getItem(i).setVisible(false);
            }

            if ((menu.getItem(i).getTitle().toString().contentEquals(removeContact)) &&
                    (! SignInActivity.isAdmin)){
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
            case R.id.ab_about:
                Utils.showAbout(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Создание интента с текстом текущего контакта для передачи в ShareActionProvider */
    private Intent createShareContactIntent() {
        String info = tvName.getText().toString() + "\n" + tvPosition.getText().toString() +
                "\n" + tvPhone.getText().toString();
        if (tvPhone2.getText().length() > 0) {
            info = info + "\n" + tvPhone2.getText().toString();
        }

        if (tvEmail.getText().length() > 0) {
            info = info + "\n" + tvEmail.getText().toString();
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, info);
        return shareIntent;
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


    /** Заполняет поля активити значениями или очищает их */
    private void fillValues(Contact contact){
        if (contact.getPhotoUrl().length() > 0){
            Glide.with(this)
                    .load(contact.getPhotoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .error(R.drawable.person_default)
                    .transform(new CircleTransform(this))
                    .into(ivPhoto);
        } else {
            ivPhoto.setImageResource(R.drawable.person_default);
            ivRing.setVisibility(View.GONE);
        }
        tvName.setText(contact.getName());
        tvPosition.setText(contact.getPosition());
        tvPhone.setText(contact.getPhone());
        // Меняем иконку если оператор удалось определить
        switch (Utils.defineMobile(contact.getPhone())) {
            case "life":
                ivPhone.setImageResource(R.drawable.life);
                break;
            case "mts":
                ivPhone.setImageResource(R.drawable.mts);
                break;
            case "kyivstar":
                ivPhone.setImageResource(R.drawable.kyivstar);
                break;
        }

        tvPhone2.setText(contact.getPhone2());
        tvEmail.setText(contact.getEmail());


        if (contact.getPhone2().isEmpty()){
            llPhone2.setVisibility(View.GONE);
        } else {
            // Меняем иконку если оператор удалось определить
            switch (Utils.defineMobile(contact.getPhone2())){
                case "life":
                    ivPhone2.setImageResource(R.drawable.life);
                    break;
                case "mts":
                    ivPhone2.setImageResource(R.drawable.mts);
                    break;
                case "kyivstar":
                    ivPhone2.setImageResource(R.drawable.kyivstar);
                    break;
            }
            llPhone2.setVisibility(View.VISIBLE);
        }
        if (contact.getEmail().isEmpty()){
            llEmail.setVisibility(View.GONE);
        } else {
            llEmail.setVisibility(View.VISIBLE);
        }
    }


    private void makeCall(String number){
        try {
            String uri = "tel:" + number;
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
            //todo: Надо прописать проверку наличия разрешения на доступ к функциям вызова для API 23
             startActivity(callIntent);
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "Your call has failed...",
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "makeCall: " + e);
            e.printStackTrace();
        }
    }

    private void sendEmail(String name, String email){
        try {
            Intent emailIntent = new Intent(Intent.ACTION_VIEW);
            // Указваем все необходимые данные для написания письма
            Uri data = Uri.parse("mailto:?subject=" + "&body=" + getResources().getString(R.string.mes_hello)
                    + ",  " + name + "&to=" + email);
            emailIntent.setData(data);
            startActivity(emailIntent);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Your mail has failed...",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
