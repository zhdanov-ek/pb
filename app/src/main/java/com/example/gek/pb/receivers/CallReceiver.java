package com.example.gek.pb.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;
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
import com.example.gek.pb.activity.ContactShowActivity;
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


import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Receiver catch input call, check number in DB company and show Toast if found contact
 */

public class CallReceiver extends PhonecallReceiver {
    private static final String TAG = "CALL_RECEIVER: ";

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
                        Utils.saveLastContact(contact, ctx);
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
        // пропущенный звонок
        Log.d(TAG, "onMissedCall: ");
        Contact lastContact = Utils.readLastContact(ctx);
        if (lastContact != null) {
            showNotificationMissed(lastContact, ctx);
        }
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


    /** Show notification if missed corporate contact */
    private void showNotificationMissed(Contact contact, Context ctx){
        if (Utils.isLastContact(ctx)){
            String name = contact.getName();
            String position = contact.getPosition();

            NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder ntfBuilder = new NotificationCompat.Builder(ctx.getApplicationContext());
            ntfBuilder.setSmallIcon(R.drawable.ic_call);
            ntfBuilder.setContentTitle(name);
            ntfBuilder.setContentText(position);
            ntfBuilder.setAutoCancel(true);
            //ntfBuilder.setLargeIcon(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.ic_notification));
            ntfBuilder.setTicker(name);

            Intent intent = new Intent(ctx.getApplicationContext(), ContactShowActivity.class);
            intent.putExtra(Const.EXTRA_CONTACT, contact);
            PendingIntent pendingIntent = PendingIntent.getActivity(ctx.getApplicationContext(), 0, intent, 0);
            ntfBuilder.setContentIntent(pendingIntent);
            Notification notification = ntfBuilder.build();

            // make random id
            int idNotif = (int)(new Date().getTime());
            notificationManager.notify(idNotif, notification);
        }
    }
}
