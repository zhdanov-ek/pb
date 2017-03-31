package com.example.gek.pb.receivers;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pb.R;
import com.example.gek.pb.data.Const;
import com.example.gek.pb.data.Contact;
import com.example.gek.pb.helpers.CircleTransform;
import com.example.gek.pb.helpers.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Receiver catch input call, check number in DB company and show Toast if found contact
 */

public class CallReceiver extends PhonecallReceiver {
    private static final String TAG = "CALLS: ";


   

    @Override
    protected void onIncomingCallReceived(final Context ctx, final String number, Date start) {
        ValueEventListener contactCardListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Contact> contacts = new ArrayList<>();
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    Contact contact = child.getValue(Contact.class);
                    contacts.add(contact);
                }

                for (Contact contact: contacts) {
                    if (Utils.isNumberOfContact(contact, number)){
                        showToast(contact, ctx);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        // устанавливаем слушатель на изменения в нашей базе в разделе контактов
        Const.db.child(Const.CHILD_CONTACTS).addValueEventListener(contactCardListener);
        Log.d(TAG, "onIncomingCallReceived: " + number);
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        Log.d(TAG, "onIncomingCallAnswered: ");
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        // срабатывает когда приняли входящий и потом положили трубку
        Log.d(TAG, "onIncomingCallEnded: ");
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Log.d(TAG, "onOutgoingCallStarted: ");
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.d(TAG, "onOutgoingCallEnded: ");
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        // не приняв звонок нажали отмену
        Log.d(TAG, "onMissedCall: ");
    }

    /** Show custom Toast with contact info */
    private void showToast(final Contact contact, final Context ctx){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = LayoutInflater.from(ctx);
                View layout = inflater.inflate(R.layout.layout_toast, null);

                ImageView ivPhoto = (ImageView) layout.findViewById(R.id.ivPhoto);
                TextView tvName = (TextView) layout.findViewById(R.id.tvName);
                TextView tvPosition = (TextView) layout.findViewById(R.id.tvPosition);

                tvName.setText(contact.getName());
                tvPosition.setText(contact.getPosition());

                if (contact.getPhotoUrl().length() > 0) {
                    Glide.with(ctx)
                            .load(contact.getPhotoUrl())
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .error(R.drawable.person_default)
                            .transform(new CircleTransform(ctx))
                            .into(ivPhoto);
                }

                Toast toast = new Toast(ctx);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();
            }
        }, 2000);
    }
}
