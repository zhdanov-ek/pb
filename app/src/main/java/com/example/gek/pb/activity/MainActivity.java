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
import android.widget.Toast;

import com.example.gek.pb.R;
import com.example.gek.pb.data.Const;
import com.example.gek.pb.data.Contact;
import com.example.gek.pb.data.ContactsAdapter;
import com.example.gek.pb.helpers.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "11111";
    private ArrayList<Contact> contacts;
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
        initWork();
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
        Const.db.child(Const.CHILD_CONTACTS).addValueEventListener(contactCardListener);
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
                    (!SignInActivity.isAdmin)) {
                menu.getItem(i).setVisible(false);
            }
            if ((menu.getItem(i).getTitle().toString().contentEquals(listUsers)) &&
                    (!SignInActivity.isAdmin)) {
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
                break;
            case R.id.ab_about:
                Utils.showAbout(this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Переопределяем нажатие кнопки НАЗАД, что бы активити с авторизацией закрыло приложение */
    @Override
    public void onBackPressed() {
        Intent intentTurnOff = new Intent();
        intentTurnOff.putExtra(Const.ACTION_MAIN, Const.ACTION_TURNOFF);
        setResult(RESULT_OK, intentTurnOff);
        finish();
    }

    /** Выход из учетной записи: обнуляем наш инстанс и выводим активити FireBase для авторизации */
    private void signOut(){
        Intent intentSignOut = new Intent();
        intentSignOut.putExtra(Const.ACTION_MAIN, Const.ACTION_SIGNOUT);
        setResult(RESULT_OK, intentSignOut);
        finish();
    }




}
