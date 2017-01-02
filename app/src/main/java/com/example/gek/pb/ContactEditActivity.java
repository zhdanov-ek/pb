package com.example.gek.pb;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.gek.pb.data.Const;
import com.example.gek.pb.data.Contact;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ContactEditActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String MODE = "edit_mode";
    public static final int MODE_NEW = 0;
    public static final int MODE_EDIT = 1;

    Button btnOk;
    ImageView ivPhoto;
    EditText etName;
    EditText etPosition;
    EditText etPhone;
    EditText etPhone2;
    EditText etEmail;

    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);

        db = FirebaseDatabase.getInstance().getReference();

        etName = (EditText) findViewById(R.id.etName);
        etPosition = (EditText) findViewById(R.id.etPosition);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etPhone2 = (EditText) findViewById(R.id.etPhone2);
        etEmail = (EditText) findViewById(R.id.etEmail);
        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);

        btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnOk:
                saveData();
        }

    }



    private void saveData(){
        String name = etName.getText().toString();
        String position = etPosition.getText().toString();
        String phone = etPhone.getText().toString();
        String phone2 = etPhone2.getText().toString();
        String email = etEmail.getText().toString();

        etName.setText("");
        etPosition.setText("");
        etPhone.setText("");
        etPhone2.setText("");
        etEmail.setText("");

        Contact newContact = new Contact(
                name,
                position,
                "",
                email,
                phone,
                phone2
        );
        db.child(Const.CHILD_CONTACTS).push().setValue(newContact);
        showSnackBar();
        finish();
    }


    private void showSnackBar(){
        Snackbar snackbar = Snackbar.make(etEmail, "Add new word?", Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.hint_ok, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), ContactEditActivity.class));
            }
        });
        snackbar.show();
    }
}
