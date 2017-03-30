package com.example.gek.pb.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pb.R;
import com.example.gek.pb.data.Const;
import com.example.gek.pb.data.Contact;
import com.example.gek.pb.helpers.CircleTransform;
import com.example.gek.pb.helpers.Utils;

public class CallActivity extends AppCompatActivity {
    private static final String TAG = "CALL_ACTIVITY";
    private ImageView ivPhoto, ivRing, ivPhone;
    private TextView tvName, tvPosition, tvPhone;
    public static final String filterClose =
            "com.example.gek.pb.activity.CallActivity.closeScreenReceiver";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
        ivRing = (ImageView) findViewById(R.id.ivRing);
        ivPhone = (ImageView) findViewById(R.id.ivPhone);
        tvName = (TextView) findViewById(R.id.tvName);
        tvPosition = (TextView) findViewById(R.id.tvPosition);
        tvPhone = (TextView) findViewById(R.id.tvPhone);

        Contact openContact = getIntent().getParcelableExtra(Const.EXTRA_CONTACT);
        String number = getIntent().getStringExtra(Const.EXTRA_NUMBER);
        fillValues(openContact, number);

        registerReceiver(closeScreenReceiver, new IntentFilter(filterClose));
        Log.d(TAG, "onCreate: register receiver");

    }

    /** Заполняет поля активити значениями */
    private void fillValues(Contact contact, String number){
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
        tvPhone.setText(number);

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
            default:
                ivPhone.setVisibility(View.GONE);
                break;
        }
    }

    private BroadcastReceiver closeScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onCreate: receiver shutdown activity");
            finish();
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(closeScreenReceiver);
        Log.d(TAG, "onCreate: unregister receiver");
        super.onDestroy();
    }
}
