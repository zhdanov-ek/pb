package com.example.gek.pb.activity;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.gek.pb.R;
import com.example.gek.pb.data.Const;
import com.example.gek.pb.data.User;
import com.example.gek.pb.data.UsersAdapter;

import com.example.gek.pb.dialog.UserDialogFragment;
import com.example.gek.pb.helpers.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UsersActivity extends AppCompatActivity {

    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private static final String TAG = "11111";

    private ArrayList<User> users;
    private ArrayList<String> emails;
    private RecyclerView rv;
    private UsersAdapter usersAdapter;
    private Context ctx = this;
    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);


        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(myToolbar);

        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL ));

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(fabListener);

        users = new ArrayList<>();
        emails = new ArrayList<>();
        // Описываем слушатель, который возвращает в программу весь список данных,
        // которые находятся в child(CHILD_USERS)
        // В итоге при любом изменении вся база перезаливается с БД в программу
        ValueEventListener contactCardListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long num = dataSnapshot.getChildrenCount();
                users.clear();
                emails.clear();

                Log.d(TAG, "Load all list ContactCards: total Children objects:" + num);
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    User user = child.getValue(User.class);
                    users.add(user);
                    emails.add(user.getEmail());
                }
                if (users.size() == 0) {
                    Toast.makeText(ctx, R.string.mes_no_records, Toast.LENGTH_LONG).show();
                }
                usersAdapter = new UsersAdapter(ctx, users, getSupportFragmentManager());
                rv.setAdapter(usersAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        // устанавливаем слушатель на изменения в нашей базе в разделе контактов
        db.child(Const.CHILD_USERS).addValueEventListener(contactCardListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        //Скрываем отдельные пункты меню
        String addContact = getResources().getString(R.string.menu_add_contact);
        String listUsers = getResources().getString(R.string.menu_list_users);
        String signOut = getResources().getString(R.string.menu_sign_out);
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getTitle().toString().contentEquals(addContact)) {
                menu.getItem(i).setVisible(false);
            }
            if (menu.getItem(i).getTitle().toString().contentEquals(listUsers)) {
                menu.getItem(i).setVisible(false);
            }
            if (menu.getItem(i).getTitle().toString().contentEquals(signOut)) {
                menu.getItem(i).setVisible(false);
            }
        }

        MenuItem searchItem = menu.findItem(R.id.ab_search);
        SearchView searchView =(SearchView) MenuItemCompat.getActionView(searchItem);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Query query = db.child(Const.CHILD_USERS).orderByChild("email").startAt(newText);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            for (DataSnapshot item: dataSnapshot.getChildren()) {
                                Log.d(TAG, "onDataChange: " + item.child("email").getValue().toString());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                Log.d(TAG, "onQueryTextChange: " + newText);
                return false;
            }
        });


        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.ab_about:
                Utils.showAbout(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /** Запуск диалога на добавление пользователя */
    View.OnClickListener fabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            UserDialogFragment userDialogFragment =
                    UserDialogFragment.newInstance(emails);
            userDialogFragment.show(fragmentManager, "ADD_NEW_USER");
        }
    };
}
