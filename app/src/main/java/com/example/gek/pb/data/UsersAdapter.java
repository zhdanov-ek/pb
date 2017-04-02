package com.example.gek.pb.data;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.example.gek.pb.R;
import com.example.gek.pb.dialog.UserDialogFragment;

import java.util.ArrayList;

/**
 * Адаптер показывающий список юзеров, которым предоставляется доступ
 */

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private ArrayList<User> listUsers;
    private Context ctx;
    private FragmentManager fragmentManager;

    public UsersAdapter(Context ctx, ArrayList<User> listUsers, FragmentManager fragmentManager){
        this.listUsers = listUsers;
        this.ctx = ctx;
        this.fragmentManager = fragmentManager;
    }

    // Создаем вью которые заполнят экран и будут обновляться данными при прокрутке
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_user, parent, false);
        return new UsersAdapter.ViewHolder(view);
    }

    // Заносим значения в наши вью
    @Override
    public void onBindViewHolder(UsersAdapter.ViewHolder holder, int position) {
        final User user = listUsers.get(position);
        holder.tvEmail.setText(user.getEmail());
        holder.tvDescription.setText(user.getDescription());
    }

    @Override
    public int getItemCount() {
        return listUsers.size();
    }




    /** Реализация абстрактного класса ViewHolder, хранящего ссылки на виджеты.
     / Он же реализует функцию OnClickListener, что бы не создавать их на каждое поле
     / при прокрутке в onBindViewHolder. Максимум таких холдеров будет на два больше
     / чем вмещается на экране */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView tvEmail;
        private TextView tvDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvEmail = (TextView) itemView.findViewById(R.id.tvEmail);
            tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
        }

        @Override
        public void onClick(View view) {
            int position = this.getAdapterPosition();
            // создаем анимацию - эффект изменения прозрачности по касанию на айтеме
            Animation anim = new AlphaAnimation(0.3f, 1.0f);
            anim.setDuration(1000);
            view.startAnimation(anim);
            UserDialogFragment userDialogFragment =
                    UserDialogFragment.newInstance(
                            listUsers.get(position).getEmail(),
                            listUsers.get(position).getDescription(),
                            listUsers.get(position).getKey());
            userDialogFragment.show(fragmentManager, "user_GEK");
        }
    }
}