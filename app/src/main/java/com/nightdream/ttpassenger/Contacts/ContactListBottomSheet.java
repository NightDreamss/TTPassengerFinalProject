package com.nightdream.ttpassenger.Contacts;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nightdream.ttpassenger.R;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class ContactListBottomSheet extends BottomSheetDialogFragment {

    private ImageView userImage;
    private Button sendFriendRequest;
    private TextView userNameText, databaseName;
    private String contactID, userName, userImg, userID;

    private DatabaseReference userRef, contactRef, notificationRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottomsheet, container, false);

        sendFriendRequest = v.findViewById(R.id.bottomsheet_add_contact_btn);
        userImage = v.findViewById(R.id.bottomsheet_contact_image);
        userNameText = v.findViewById(R.id.bottomsheet_name);
        databaseName = v.findViewById(R.id.bottomsheet_database_name);

        SharedPreferences prefs = requireContext().getSharedPreferences("DeviceToken", MODE_PRIVATE);
        contactID = prefs.getString("ID", null);
        userName = prefs.getString("name", null);
        userImg = prefs.getString("image", null);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        RetrieveUserInfo();
        RemoveContact();

        return v;
    }

    private void RetrieveUserInfo() {
        userRef.child("Drivers").child(contactID).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    Object databaseNameText = dataSnapshot.child("name").getValue();

                    userNameText.setText(userName);
                    databaseName.setText("(" + databaseNameText + ")");

                    if (!userImg.isEmpty()) {
                        Picasso.get().load(userImg).fit().placeholder(R.drawable.profile_image).error(R.drawable.profile_image).into(userImage);
                    } else {
                        userImage.setImageResource(R.drawable.profile_image);
                    }

                } else {
                    userRef.child("Passenger").child(contactID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {

                                Object databaseNameText = snapshot.child("name").getValue();

                                userNameText.setText(userName);
                                databaseName.setText("(" + databaseNameText + ")");

                                if (!userImg.isEmpty()) {
                                    Picasso.get().load(userImg).fit().placeholder(R.drawable.profile_image).error(R.drawable.profile_image).into(userImage);
                                } else {
                                    userImage.setImageResource(R.drawable.profile_image);
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void RemoveContact() {
        sendFriendRequest.setText("Remove Contact");
        sendFriendRequest.setOnClickListener(v -> contactRef.child(userID).child(contactID).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                contactRef.child(contactID).child(userID).removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        notificationRef.child(contactID).removeValue().addOnCompleteListener(task11 -> {
                            if (task11.isSuccessful()) {
                                Toast.makeText(getContext(), userName + "successfully removed", Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                        });
                    }
                });
            }
        }));
    }

}

