package com.example.gek.pb.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.example.gek.pb.activity.CallActivity;
import com.example.gek.pb.data.Const;
import com.example.gek.pb.data.Contact;
import com.example.gek.pb.helpers.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by gek on 30.03.17.
 */

public class CallReceiver extends PhonecallReceiver {
    private static final String TAG = "CALL: ";
    private List<Contact> contacts;

    private void showScreen(final Contact contact, final Context ctx, final String number){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ctx, CallActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Const.EXTRA_CONTACT, contact);
                intent.putExtra(Const.EXTRA_NUMBER, number);
                ctx.startActivity(intent);
            }
        }, 2000);

    }

    @Override
    protected void onIncomingCallReceived(final Context ctx, final String number, Date start) {

        ValueEventListener contactCardListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (contacts != null) {
                    contacts.clear();
                } else {
                    contacts = new ArrayList<>();
                }
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    Contact contact = child.getValue(Contact.class);
                    contacts.add(contact);
                }

                for (Contact contact: contacts) {
                    if (Utils.isNumberOfContact(contact, number)){
                        showScreen(contact, ctx, number);
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


        //Toast.makeText(ctx, "Incoming call " + number, Toast.LENGTH_LONG).show();


        Log.d(TAG, "onIncomingCallReceived: " + number);
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {

    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        ctx.sendBroadcast(new Intent(CallActivity.filterClose));
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {

    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {

    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
    }
}
