package com.example.gek.pb.data;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pb.R;
import com.example.gek.pb.activity.ContactShowActivity;
import com.example.gek.pb.helpers.CircleTransform;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;



public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    private ArrayList<Contact> listContacts;
    private Context ctx;
    private static final String TAG = "ContactsAdapter";
    private CircleTransform circleTransform;

    public ContactsAdapter(Context ctx, ArrayList<Contact> listContacts){
        this.listContacts = listContacts;
        this.ctx = ctx;
        this.circleTransform = new CircleTransform(ctx);
    }

    // Создаем вью которые заполнят экран и будут обновляться данными при прокрутке
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_contact, parent, false);
        ContactsAdapter.ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // Заносим значения в наши вью
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Contact contact = listContacts.get(position);
        holder.tvName.setText(contact.getName());
        holder.tvPosition.setText(contact.getPosition());
        if ((contact.getPhotoUrl() != null) && (contact.getPhotoUrl().length() > 0)) {
            Glide.with(ctx)
                    .load(contact.getPhotoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .transform(circleTransform)
                    .error(R.drawable.person_default)
                    .into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.person_default);
        }

    }

    @Override
    public int getItemCount() {
        return listContacts.size();
    }


    /** Реализация абстрактного класса ViewHolder, хранящего ссылки на виджеты.
     / Он же реализует функцию OnClickListener, что бы не создавать их на каждое поле
     / при прокрутке в onBindViewHolder. Максимум таких холдеров будет на два больше
     / чем вмещается на экране */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView ivPhoto;
        private TextView tvName;
        private TextView tvPosition;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            ivPhoto = (ImageView) itemView.findViewById(R.id.ivPhoto);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvPosition = (TextView) itemView.findViewById(R.id.tvPosition);
        }

        @Override
        public void onClick(View view) {
            // изымаем данные по позиции где был клик
            Contact contact = listContacts.get(getAdapterPosition());
            Intent detailIntent = new Intent(ctx, ContactShowActivity.class);
            detailIntent.putExtra(Const.EXTRA_CONTACT, contact);
            ctx.startActivity(detailIntent);
        }
    }
}
