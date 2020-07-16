package com.nightdream.ttpassenger.Contacts;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.nightdream.ttpassenger.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class AddContactsAdapter extends RecyclerView.Adapter<AddContactsAdapter.ContactListViewHolder> {

    ArrayList<Contacts> contactUsers;
    Context context;

    public AddContactsAdapter(ArrayList<Contacts> contactUsers, Context context) {
        this.contactUsers = contactUsers;
        this.context = context;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_items, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        return new ContactListViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactListViewHolder holder, final int position) {
        holder.contact_name.setText(contactUsers.get(position).getName());
        holder.contact_num.setText(contactUsers.get(position).getUserNumber());
        if (!contactUsers.get(position).getImage().isEmpty()) {
            Picasso.get().load(contactUsers.get(position).getImage()).placeholder(R.drawable.profile_image).error(R.drawable.profile_image).into(holder.profile_icon);
        } else {
            holder.profile_icon.setImageResource(R.drawable.profile_image);
        }

        holder.itemView.setOnClickListener(v -> {

            String id = contactUsers.get(position).getuId();
            String name = contactUsers.get(position).getName();
            String image = contactUsers.get(position).getImage();

            AddContactsBottomSheet addContactsBottomSheet = new AddContactsBottomSheet();
            FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
            SharedPreferences.Editor editor = context.getSharedPreferences("DeviceToken", MODE_PRIVATE).edit();
            editor.putString("ID", id);
            editor.putString("name", name);
            editor.putString("image", image);
            editor.apply();

            addContactsBottomSheet.show(ft, "addContactsBottomSheet");
        });
    }

    @Override
    public int getItemCount() {
        return contactUsers.size();
    }

    public static class ContactListViewHolder extends RecyclerView.ViewHolder {

        TextView contact_name, contact_num;
        CircleImageView profile_icon;

        public ContactListViewHolder(View view) {
            super(view);

            contact_name = itemView.findViewById(R.id.contact_item_Contact_Name);
            contact_num = itemView.findViewById(R.id.contact_item_Contact_Number);
            profile_icon = itemView.findViewById(R.id.contact_item_ProfileImage);
        }
    }
}
