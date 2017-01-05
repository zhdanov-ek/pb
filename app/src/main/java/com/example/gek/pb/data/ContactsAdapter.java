package com.example.gek.pb.data;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gek.pb.R;
import com.example.gek.pb.activity.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;



public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    private ArrayList<Contact> listContacts;
    private Context ctx;
    private String pathToImage;
    private File appFolder;

    private static final String TAG = "ContactsAdapter";

    private StorageReference storageRef;
    private FirebaseStorage storage;
    private StorageReference folderRef;

    public ContactsAdapter(Context ctx, ArrayList<Contact> listContacts){
        this.listContacts = listContacts;
        this.ctx = ctx;
        pathToImage = Const.STORAGE + "/" +Const.IMAGE_FOLDER;
//        appFolder = ctx.getFilesDir();
        appFolder = Environment.getExternalStorageDirectory();

        // Получаем ссылку на наше хранилище
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl(Const.STORAGE);
        folderRef = storageRef.child(Const.IMAGE_FOLDER);
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

        // Если указанно фото то ищем его на телефоне или загружаем с хранилища гугл
        String nameFoto = "";
        if (contact.getPhoto().length() > 0) {
            nameFoto = "\n" + contact.getPhoto();
            if (Const.isFindFile(appFolder, contact.getPhoto())){
                Picasso.with(ctx)
                        .load(new File(appFolder, contact.getPhoto()))
                        .placeholder(R.drawable.person_default)
                        .error(R.drawable.person_default)
                        .into(holder.ivPhoto);
            } else {

                StorageReference loadFile = folderRef.child(contact.getPhoto());

                try {
                    File localFile = File.createTempFile ("images", "jpg");

                    loadFile.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Local temp file has been created
                            Log.d(TAG, "onSuccess: ");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d(TAG, "onFailure: " + exception);
                            // Handle any errors
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "onBindViewHolder: ", e);
                }

            }

        }
        holder.tvName.setText(contact.getName());
        holder.tvPosition.setText(contact.getPosition() + nameFoto);
    }

    @Override
    public int getItemCount() {
        return listContacts.size();
    }




    /** Реализация абстрактного класса ViewHolder, хранящего ссылки на виджеты.
     / Он же реализует функцию OnClickListener, что бы не создавать их на каждое поле
     / при прокрутке в onBindViewHolder. Максимум таких холдеров будет на два больше
     / чем вмещается на экране */
    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView ivPhoto;
        private TextView tvName;
        private TextView tvPosition;

        public ViewHolder(View itemView) {
            super(itemView);
            ivPhoto = (ImageView) itemView.findViewById(R.id.ivPhoto);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvPosition = (TextView) itemView.findViewById(R.id.tvPosition);
        }
    }
}
