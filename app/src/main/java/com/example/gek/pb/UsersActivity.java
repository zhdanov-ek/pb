package com.example.gek.pb;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.gek.pb.data.Const;
import com.example.gek.pb.data.User;
import com.example.gek.pb.data.UsersAdapter;

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

        // Описываем слушатель, который возвращает в программу весь список данных,
        // которые находятся в child(CHILD_USERS)
        // В итоге при любом изменении вся база перезаливается с БД в программу
        ValueEventListener contactCardListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long num = dataSnapshot.getChildrenCount();
                if (users != null) {
                    users.clear();
                } else {
                    users = new ArrayList<>();
                }
                Log.d(TAG, "Load all list ContactCards: total Children objects:" + num);
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    User user = child.getValue(User.class);
                    users.add(user);
                    Log.d(TAG, "onDataChange: " + user.getEmail() + "\n");
                }
                if (users.size() == 0) {
                    Toast.makeText(ctx, R.string.mes_no_records, Toast.LENGTH_LONG).show();
                }
                usersAdapter = new UsersAdapter(ctx, users);
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
            Toast.makeText(ctx, "Show dialog for add new user", Toast.LENGTH_LONG).show();
        }
    };
}
