package com.example.gek.pb.activity;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.gek.pb.R;
import com.example.gek.pb.data.Const;
import com.example.gek.pb.data.User;
import com.example.gek.pb.data.UsersAdapter;

import com.example.gek.pb.dialog.UserDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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



    View.OnClickListener fabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            UserDialogFragment userDialogFragment =
                    UserDialogFragment.newInstance(emails);
            userDialogFragment.show(fragmentManager, "user_GEK");
        }
    };
}
