package com.example.gek.pb.activity;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pb.R;
import com.example.gek.pb.data.Const;
import com.example.gek.pb.data.Contact;

public class ContactShowActivity extends AppCompatActivity {

    ImageView ivPhoto;
    TextView tvName, tvPosition, tvPhone, tvPhone2, tvEmail;
    Button btnCancel;
    Contact oldContact;

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
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        oldContact = getIntent().getParcelableExtra(Const.EXTRA_CONTACT);

        if ((oldContact.getPhotoUrl() != null) && (oldContact.getPhotoUrl().length() > 0)) {
            Glide.with(this)
                    .load(oldContact.getPhotoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.person_default)
                    .into(ivPhoto);
        } else {
            ivPhoto.setBackground(getResources().getDrawable(R.drawable.person_default));
        }

        tvName.setText(oldContact.getName());
        tvPosition.setText(oldContact.getPosition());
        tvPhone.setText(oldContact.getPhone());
        tvPhone2.setText(oldContact.getPhone2());
        tvEmail.setText(oldContact.getEmail());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        String addContact = getResources().getString(R.string.menu_add_contact);
        String editContact = getResources().getString(R.string.menu_edit_contact);
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
        }
        return true;
    }
}
