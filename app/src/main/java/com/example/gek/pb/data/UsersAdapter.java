package com.example.gek.pb.data;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.example.gek.pb.R;

import java.util.ArrayList;

/**
 * Адаптер показывающий список юзеров, которым предоставляется доступ
 */

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private ArrayList<User> listUsers;
    private Context ctx;


    public UsersAdapter(Context ctx, ArrayList<User> listUsers){
        this.listUsers = listUsers;
        this.ctx = ctx;
    }

    // Создаем вью которые заполнят экран и будут обновляться данными при прокрутке
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_user, parent, false);
        UsersAdapter.ViewHolder viewHolder = new UsersAdapter.ViewHolder(view);
        return viewHolder;
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
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvEmail;
        private TextView tvDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            tvEmail = (TextView) itemView.findViewById(R.id.tvEmail);
            tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
        }
    }
}