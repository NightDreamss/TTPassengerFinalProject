package com.nightdream.ttpassenger.Contacts;

import android.annotation.SuppressLint;
import android.content.Context;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.nightdream.ttpassenger.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class AddContactsBottomSheet extends BottomSheetDialogFragment {

    Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private ImageView userImage;
    private Button sendFriendRequest;
    private TextView userNameText, databaseName;
    private String contactID, userName, userImg, userID;

    private DatabaseReference driverRef, passengerRef, requestRef, notificationRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottomsheet, container, false);

        sendFriendRequest = v.findViewById(R.id.bottomsheet_add_contact_btn);
        userImage = v.findViewById(R.id.bottomsheet_contact_image);
        userNameText = v.findViewById(R.id.bottomsheet_name);
        databaseName = v.findViewById(R.id.bottomsheet_database_name);

        SharedPreferences prefs = getContext().getSharedPreferences("DeviceToken", MODE_PRIVATE);
        contactID = prefs.getString("ID", null);
        userName = prefs.getString("name", null);
        userImg = prefs.getString("image", null);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(contactID);
        passengerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Passenger").child(contactID);
        requestRef = FirebaseDatabase.getInstance().getReference().child("Contact Request");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        RetrieveUserInfo();

        return v;
    }

    private void RetrieveUserInfo() {
        driverRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    String databaseNameText = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();

                    userNameText.setText(userName);
                    databaseName.setText("(" + databaseNameText + ")");

                    if (!userImg.isEmpty()) {
                        Picasso.get().load(userImg).fit().placeholder(R.drawable.profile_image).error(R.drawable.profile_image).into(userImage);
                    } else {
                        userImage.setImageResource(R.drawable.profile_image);
                    }

                    sendFriendRequest.setOnClickListener(v -> {
                        SendFriendRequest();
                        dismiss();
                    });
                }
                else{
                  passengerRef.addValueEventListener(new ValueEventListener() {
                      @SuppressLint("SetTextI18n")
                      @Override
                      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                          if (dataSnapshot.exists()) {
                              Object databaseNameText = dataSnapshot.child("name").getValue();

                              userNameText.setText(userName);
                              databaseName.setText("(" + String.valueOf(databaseNameText) + ")");

                              if (!userImg.isEmpty()) {
                                  Picasso.get().load(userImg).fit().placeholder(R.drawable.profile_image).error(R.drawable.profile_image).into(userImage);
                              } else {
                                  userImage.setImageResource(R.drawable.profile_image);
                              }

                              sendFriendRequest.setOnClickListener(v -> {
                                  SendFriendRequest();
                                  dismiss();
                              });
                          }
                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError databaseError) {

                      }
                  });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void SendFriendRequest() {
        requestRef.child(userID).child(contactID).child("contact_request").setValue("sent").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                requestRef.child(contactID).child(userID).child("contact_request").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            HashMap<String, String> chatnotification = new HashMap<>();
                            chatnotification.put("from", userID);
                            chatnotification.put("type", "request");

                            notificationRef.child(contactID).push().setValue(chatnotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "Contact request sent to " + userName, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}
