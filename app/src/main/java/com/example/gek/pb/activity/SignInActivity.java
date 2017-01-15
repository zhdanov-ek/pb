package com.example.gek.pb.activity;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.pb.R;
import com.example.gek.pb.data.Const;
import com.example.gek.pb.helpers.Utils;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "11111111111";
    // флаг показывающий, что авторизирован админ
    public static Boolean isAdmin = false;
    public static String userEmail = "";
    static String adminEmail = "";


    TextView tvInfo, tvAdminMessage;
    Button btnSignOut, btnSignIn;
    private Context ctx;
    FloatingActionButton fabHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ctx = this;
        initAdmin();


        tvInfo = (TextView) findViewById(R.id.tvInfo);
        tvAdminMessage = (TextView) findViewById(R.id.tvAdminMessage);
        btnSignIn = (Button)findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .build(), Const.REQUEST_SIGN_IN);
            }
        });

        btnSignOut = (Button) findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    FirebaseAuth.getInstance().signOut();
                    userEmail = "";
                    btnSignIn.setVisibility(View.VISIBLE);
                    btnSignOut.setVisibility(View.GONE);
                    tvInfo.setText("");
                }
            }
        });
        fabHelp = (FloatingActionButton) findViewById(R.id.fabHelp);
        fabHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.showHelp(ctx);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        // https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Если авторизация есть то проверяем есть ли даннный юзер в нашем белом списке
        // и после успшного поиска запускаем программу
        if (auth.getCurrentUser() != null) {
            userEmail = auth.getCurrentUser().getEmail();
            findUserInWhiteList();
        }
    }


    /** Получаем результат работы с окном авторизации или ответы с мейнАктивити*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case Const.REQUEST_SIGN_IN:
                if (resultCode == RESULT_OK)
                {
                    tvInfo.setText("Успешная авторизация");
                } else {
                    tvInfo.setText(R.string.mes_error_auth);
                }
                break;
            case Const.REQUEST_MAIN:
                if ((resultCode == RESULT_OK) && (data != null)){
                    if (data.hasExtra(Const.ACTION_MAIN)){
                        switch (data.getIntExtra(Const.ACTION_MAIN, 0)){
                            case Const.ACTION_SIGNOUT:
                                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                    FirebaseAuth.getInstance().signOut();
                                    userEmail = "";
                                    tvInfo.setText("");
                                    btnSignOut.setVisibility(View.GONE);
                                    btnSignIn.setVisibility(View.VISIBLE);
                                }
                                break;
                            case Const.ACTION_TURNOFF:
                                finish();
                                break;
                        }
                    }
                }
        }
    }

    /** Пытаемся получить с БД учетку админа: если поля еще нет то создаем его с базовым значением
     * Если есть то полученное значение фиксируем в переменной  adminEmail с которой в дальнейшем
     * будем сравнивать реальный авторизированный еккаунт */
    private void initAdmin(){
        ValueEventListener adminAccountListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String ADMIN = "myadmin@gmail.com";
                Object value = dataSnapshot.getValue();
                if (value != null){
                    String currentAdmin = dataSnapshot.getValue().toString();
                    if (currentAdmin.contentEquals(ADMIN)) {
                        adminEmail = "";
                        Toast.makeText(ctx, R.string.mes_register_admin_account, Toast.LENGTH_LONG).show();
                        //tvAdminMessage.setVisibility(View.VISIBLE);
                    } else {
                        adminEmail = currentAdmin;
                        //Log.d(TAG, "onDataChange: admin = " + adminEmail);
                    }
                } else {
                    Log.d(TAG, "Create base value of admin account");
                    Const.db.child(Const.CHILD_ADMIN).setValue(ADMIN);
                    adminEmail = "";
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "initAdmin:onCancelled", databaseError.toException());
            }
        };

        // устанавливаем слушатель на считывание значения админской учетки
        Const.db.child(Const.CHILD_ADMIN).addValueEventListener(adminAccountListener);
    }


    /** Грузим список юзеров и проверяем админ ли наш текущий юзер и вообще есть ли он в белом списке */
    private void findUserInWhiteList(){
        ValueEventListener readUsersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot usersSnapShots) {
                Boolean isFound = false;
                if (usersSnapShots.getChildrenCount() != 0){
                    for (DataSnapshot user: usersSnapShots.getChildren()) {
                        Log.d(TAG, "readUsers: " + user.child("email").getValue(String.class));
                        if (userEmail.contentEquals(user.child("email").getValue((String.class)))) {
                            isFound = true;         // юзер найден в белом списке
                            // проверяем не админская ли это учетка (нужно для показа админ меню)
                            if (userEmail.contentEquals(adminEmail)) {
                                isAdmin = true;
                            } else {
                                isAdmin = false;
                            }
                            startMainActivity();
                            break;
                        }
                    }
                }
                if (!isFound) {
                    String mes = userEmail + "\n" +getResources().getString(R.string.mes_user_not_founded);
                    tvInfo.setText(mes);
                    btnSignOut.setVisibility(View.VISIBLE);
                    btnSignIn.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "readUsers: onCancelled", databaseError.toException());
            }
        };

        // Если авторизированный админ то запускаемся. Иначе - проверям юзера по белому списку с БД
        if (userEmail.contentEquals(adminEmail)) {
            isAdmin = true;
            startMainActivity();
        } else {
            Const.db.child(Const.CHILD_USERS).addValueEventListener(readUsersListener);
        }

    }

    private void startMainActivity(){
        Intent intentMainActivity = new Intent(this, MainActivity.class);
        startActivityForResult(intentMainActivity, Const.REQUEST_MAIN);
    }
}
