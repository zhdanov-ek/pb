package com.example.gek.pb.activity;

import android.content.Context;
import android.content.Intent;
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

    private static final String TAG = "SignInActivity";
    // флаг показывающий, что авторизирован админ
    public static Boolean isAdmin = false;
    public static String userEmail = "";
    // флаг показывающий, что авторизация сверена с белым списком (или юзер админ)
    private Boolean isCanRun = false;
    static String adminEmail = "";
    private FirebaseAuth auth;

    private TextView tvInfo;
    private Button btnSignOut, btnSignIn;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ctx = this;

        // https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md
        auth = FirebaseAuth.getInstance();

        tvInfo = (TextView) findViewById(R.id.tvInfo);
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

        findViewById(R.id.fabHelp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.showHelp(ctx);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (isCanRun) {
                startMainActivity();
            } else {
                initUser();
            }
        }
    }



    /** Получаем результат работы с окном авторизации или ответы с мейнАктивити */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            // ответ с окна авторизации
            case Const.REQUEST_SIGN_IN:
                if (resultCode == RESULT_OK)
                {
                    tvInfo.setText(R.string.mes_succes_auth);
                } else {
                    tvInfo.setText(R.string.mes_error_auth);
                }
                break;
            // ответ с главного окна (выйти из системы, закончить работу с программой)
            case Const.REQUEST_MAIN:
                if ((resultCode == RESULT_OK) && (data != null)){
                    if (data.hasExtra(Const.ACTION_MAIN)){
                        switch (data.getIntExtra(Const.ACTION_MAIN, 0)){
                            case Const.ACTION_SIGNOUT:
                                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                    FirebaseAuth.getInstance().signOut();
                                    userEmail = "";
                                    isCanRun = false;
                                    isAdmin = false;
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
     * будем сравнивать реальный авторизированный еккаунт
     * Если авторизирован не админ то ищем юзера в белом списке БД */
    private void initUser(){
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
                    } else {
                        adminEmail = currentAdmin;
                    }
                } else {
                    // создаем запись в БД с дефолтным значением
                    Const.db.child(Const.CHILD_ADMIN).setValue(ADMIN);
                    adminEmail = "";
                }

                // Если наш юзер админ то запускаемся как админ, а иначе ищем юзера в белом списке
                userEmail = auth.getCurrentUser().getEmail().toLowerCase();
                if (userEmail.contentEquals(adminEmail)){
                    isCanRun = true;
                    isAdmin = true;
                    startMainActivity();
                } else {
                    findUserInWhiteList();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "initUser:onCancelled", databaseError.toException());
            }
        };

        // устанавливаем слушатель на считывание значения админской учетки
        Const.db.child(Const.CHILD_ADMIN).addValueEventListener(adminAccountListener);
    }


    /** Ищем авторизированного юзера в белом списке  */
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
                            break;
                        }
                    }
                }
                if (!isFound) {
                    String mes = userEmail + "\n" +getResources().getString(R.string.mes_user_not_founded);
                    tvInfo.setText(mes);
                    btnSignOut.setVisibility(View.VISIBLE);
                    btnSignIn.setVisibility(View.GONE);
                } else {
                    isCanRun = true;
                    startMainActivity();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "readUsers: onCancelled", databaseError.toException());
            }
        };
        Const.db.child(Const.CHILD_USERS).addValueEventListener(readUsersListener);
    }

    private void startMainActivity(){
        Intent intentMainActivity = new Intent(this, MainActivity.class);
        startActivityForResult(intentMainActivity, Const.REQUEST_MAIN);
    }
}
