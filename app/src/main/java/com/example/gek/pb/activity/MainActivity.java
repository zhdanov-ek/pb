package com.example.gek.pb.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.example.gek.pb.data.Contact;
import com.example.gek.pb.data.ContactsAdapter;
import com.example.gek.pb.helpers.Utils;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private static final String TAG = "11111";

    // флаг показывающий, что авторизирован админ
    public static Boolean isAdmin = false;
    static String userEmail = "";
    static String adminEmail = "";
    private ArrayList<Contact> contacts;
    private ArrayList<String> users;
    private RecyclerView rv;
    private ContactsAdapter contactsAdapter;
    private Context ctx = this;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(myToolbar);

        // Задаем стандартный менеджер макетов для RV
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));

        if (!Utils.hasInternet(this)) {
            Utils.showSnackBar(rv, getResources().getString(R.string.mes_no_internet));
        }

        // Если нет авторизации то выводим окно для нее
        // https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .build(), Const.REQUEST_SIGN_IN);
        } else {
            userEmail = auth.getCurrentUser().getEmail();
            Toast.makeText(this, userEmail, Toast.LENGTH_LONG).show();

            adminEmail = "zhdanov.ek@gmail.com";
            // Инициализация админской учетки в БД и получение текущего значения в программу
            //initAdmin();

            // Ищем введенного пользователя в списке допустимых и только после этого запускаемся
            findUserInWhiteList();
        }
    }


    /** Получаем результат работы с окном авторизации */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Const.REQUEST_SIGN_IN)
        {
            if (resultCode == RESULT_OK)
            {
                initWork();
                Utils.showSnackBar(rv, "Вход выполнен");
                // TODO: 14.01.17 Зафиксировать имя юзера и выводить его где-нибудь в О программе
            } else {
                rv.setVisibility(View.GONE);
                Utils.showSnackBar(rv, "Вход не выполнен");
                finish();
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
                    } else {
                        adminEmail = currentAdmin;
                    }
                    Log.d(TAG, "Read admin account value = " + currentAdmin);

                } else {
                    Log.d(TAG, "Create base value of admin account");
                    db.child(Const.CHILD_ADMIN).setValue(ADMIN);
                    adminEmail = "";
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "initAdmin:onCancelled", databaseError.toException());
            }
        };

        // устанавливаем слушатель на считывание значения админской учетки
        db.child(Const.CHILD_ADMIN).addValueEventListener(adminAccountListener);
    }

    /** Грузим список юзеров и проверяем админ ли наш текущий юзер и вообще есть ли он в белом списке */
    private void findUserInWhiteList(){

        ValueEventListener readUsersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot usersSnapShots) {
                users = new ArrayList<>();
                Boolean isInList = false;
                if (usersSnapShots.getChildrenCount() != 0){
                    for (DataSnapshot user: usersSnapShots.getChildren()) {
                        Log.d(TAG, "Read users: " + user.child("email").getValue(String.class));
                        if (userEmail.contentEquals(user.child("email").getValue((String.class)))) {
                            Utils.showSnackBar(rv, "Wellcome user" + userEmail);
                            initWork();
                            isAdmin = false;
                            isInList = true;
                            break;
                        }
                    }
                }
                if (!isInList) {
                    Intent intentRequestToAdmin = new Intent(ctx, RequestAdminActivity.class);
                    startActivity(intentRequestToAdmin);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "isAdminCheck:onCancelled", databaseError.toException());
            }
        };

        if (userEmail.contentEquals(adminEmail)) {
            isAdmin = true;
            Utils.showSnackBar(rv, "Wellcome ADMIN " + userEmail);
            initWork();
        } else {
            db.child(Const.CHILD_USERS).addValueEventListener(readUsersListener);
        }

    }

    /** Инициализация формирования основного списка контактов */
    private void initWork(){

        // Описываем слушатель, который возвращает в программу весь список данных,
        // которые находятся в child(CHILD_CONTACTS)
        // В итоге при любом изменении вся база перезаливается с БД в программу
        ValueEventListener contactCardListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long num = dataSnapshot.getChildrenCount();
                if (contacts != null) {
                    contacts.clear();
                } else {
                    contacts = new ArrayList<>();
                }
                Log.d(TAG, "Load all list ContactCards: total Children objects:" + num);
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    Contact contact = child.getValue(Contact.class);
                    contacts.add(contact);
                    Log.d(TAG, "onDataChange: " + contact.getName() + " - " + contact.getPhone() + "\n");
                }
                if (contacts.size() == 0) {
                    Toast.makeText(ctx, R.string.mes_no_records, Toast.LENGTH_LONG).show();
                }
                contactsAdapter = new ContactsAdapter(ctx, contacts);
                rv.setAdapter(contactsAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        // устанавливаем слушатель на изменения в нашей базе в разделе контактов
        db.child(Const.CHILD_CONTACTS).addValueEventListener(contactCardListener);
    }


    // Указываем как нам формировать меню в зависимости от того кто авторизирован (юзер/админ)
    // и описываем виджет SearchView
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.ab_search);

        //Скрываем отдельные пункты меню
        String addContact = getResources().getString(R.string.menu_add_contact);
        String removeContact = getResources().getString(R.string.menu_remove_contact);
        String listUsers = getResources().getString(R.string.menu_list_users);
        for (int i = 0; i < menu.size(); i++) {
            if ((menu.getItem(i).getTitle().toString().contentEquals(addContact)) &&
                    (!isAdmin)) {
                menu.getItem(i).setVisible(false);
            }
            if ((menu.getItem(i).getTitle().toString().contentEquals(listUsers)) &&
                    (!isAdmin)) {
                menu.getItem(i).setVisible(false);
            }
            if (menu.getItem(i).getTitle().toString().contentEquals(removeContact)) {
                menu.getItem(i).setVisible(false);
            }
        }


        SearchView searchView =(SearchView) MenuItemCompat.getActionView(searchItem);

//        // Отрабатываем смену текста в окне поиска
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            // Реакция на команду ввода (Enter)
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//            // Непосредственно событие смены содержимого. Делаем запрос к БД по каждому изменению
//            // в окне поиска
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                // Делаем выборку из БД после чего проверяем есть ли результат. Если нет то
//                // делаем выборку всех слов
//                Cursor cursor = db.getAllData(Consts.LIST_TYPE_SEARCH, Consts.ORDER_BY_ABC, newText);
//                if ((cursor == null) || (cursor.getCount() == 0)) {
//                    cursor = db.getAllData(Consts.LIST_TYPE_ALL, Consts.ORDER_BY_ABC, null);
//                }
//                mListWords = db.getFullListWords(cursor);
//                mAdapter = new RecyclerViewAdapter((Activity)mCtx, mListWords);
//                mRrecyclerView.setAdapter(mAdapter);
//                return false;
//            }
//        });

        // По окончанию работы с SearchView отображаем все слова в алфавитном порядке
        // и в меню это отмечаем это в меню
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
//                MenuItem menuItem = menu.findItem(R.id.ab_sort_abc);
//                menuItem.setChecked(true);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.ab_add:
                Intent addContactIntent = new Intent(this, ContactEditActivity.class);
                addContactIntent.putExtra(Const.MODE, Const.MODE_NEW);
                startActivity(addContactIntent);
                break;
            case R.id.ab_users:
                startActivity(new Intent(this, UsersActivity.class));
                break;
            case R.id.ab_sign_out:
                signOut();
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Выход из учетной записи: обнуляем наш инстанс и выводим активити FireBase для авторизации */
    private void signOut(){
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseAuth.getInstance().signOut();
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .build(), Const.REQUEST_SIGN_IN);
        }
    }


}
