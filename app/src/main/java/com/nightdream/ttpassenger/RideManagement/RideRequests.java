package com.nightdream.ttpassenger.RideManagement;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nightdream.ttpassenger.Contacts.ContactsLayout;
import com.nightdream.ttpassenger.InterfaceModules.ViewHolder;
import com.nightdream.ttpassenger.InterfaceModules.requestGetterSetter;
import com.nightdream.ttpassenger.R;

import java.util.Objects;

public class RideRequests extends Fragment {

    private View rideRequested;
    private RecyclerView recyclerView;
    private ViewHolder viewHolder;
    private Button contactBtn;
    private String uID;
    private TextView name, user_type;

    //Firebase
    private DatabaseReference reference;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rideRequested = inflater.inflate(R.layout.fragment_ride_requests, container, false);

        variables();
        databaseVariables();
        configureRecyclerView();
        contactPage();
        checkRideTransactions();
        return rideRequested;
    }


    private void contactPage() {
        contactBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ContactsLayout.class);
            startActivity(intent);
        });
    }

    private void databaseVariables() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference dataReference = FirebaseDatabase.getInstance().getReference();
        String uID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        dataReference.child("Users").child("Drivers").child(uID).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    Object userName = dataSnapshot.child("name").getValue();

                    name.setText(String.valueOf(userName));
                    user_type.setText("Driver");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void configureRecyclerView() {
        FirebaseRecyclerOptions<requestGetterSetter> options = new FirebaseRecyclerOptions.Builder<requestGetterSetter>().setQuery(reference.child("taxiRequest").orderByChild("status").equalTo("waiting"), requestGetterSetter.class).build();


        viewHolder = new ViewHolder(options, getContext());
        recyclerView.setAdapter(viewHolder);
    }

    private void variables() {
        name = rideRequested.findViewById(R.id.account_name);
        user_type = rideRequested.findViewById(R.id.account_type);
        contactBtn = rideRequested.findViewById(R.id.ride_requested_contacts_button);
        recyclerView = rideRequested.findViewById(R.id.ride_requested);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));

        //firebase
        reference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        uID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    }

    private void checkRideTransactions() {
        reference.child("taxiRequest").orderByChild("driverId").equalTo(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    for (DataSnapshot child : snapshot.getChildren()) {
                        String keyId = child.getKey();
                        Object statusValue = child.child("status").getValue();

                        if (String.valueOf(statusValue).equals("accepted")) {
                            String.valueOf(statusValue);
                            Intent intent = new Intent(getContext(), QrCodeMap.class);
                            intent.putExtra("keyId", keyId);
                            startActivity(intent);
                            Toast.makeText(getContext(), "Ride request already in progress...", Toast.LENGTH_SHORT).show();
                        } else if (String.valueOf(statusValue).equals("riding")) {
                            String.valueOf(statusValue);
                            Intent intent = new Intent(getContext(), QrCodeMap.class);
                            intent.putExtra("keyId", keyId);
                            startActivity(intent);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        viewHolder.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewHolder.stopListening();
    }
}