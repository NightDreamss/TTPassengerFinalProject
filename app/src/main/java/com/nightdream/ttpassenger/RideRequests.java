package com.nightdream.ttpassenger;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

public class RideRequests extends Fragment {

    private View rideRequested;
    private RecyclerView recyclerView;
    private ViewHolder viewHolder;
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
        return rideRequested;
    }

    private void databaseVariables() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference dataReference = FirebaseDatabase.getInstance().getReference();
        String uID = mAuth.getCurrentUser().getUid();

        dataReference.child("Users").child("Drivers").child(uID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String userName = dataSnapshot.child("name").getValue().toString();
                    name.setText(userName);
                    user_type.setText("Driver");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void configureRecyclerView() {
        FirebaseRecyclerOptions<requestGetterSetter> options = new FirebaseRecyclerOptions.Builder<requestGetterSetter>().setQuery(reference, requestGetterSetter.class).build();

        viewHolder = new ViewHolder(options, getContext());
        recyclerView.setAdapter(viewHolder);
    }

    private void variables() {
        name = rideRequested.findViewById(R.id.account_name);
        user_type = rideRequested.findViewById(R.id.account_type);
        recyclerView = rideRequested.findViewById(R.id.ride_requested);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));

        //firebase
        reference = FirebaseDatabase.getInstance().getReference().child("taxiRequest");
        mAuth = FirebaseAuth.getInstance();
        uID = mAuth.getCurrentUser().getUid();
    }

    //fix the removal of transcation if ride cancel
    private void checkRideTransactions() {
        FirebaseDatabase.getInstance().getReference().child("rideTransaction").orderByChild("driver").equalTo(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String keyId = child.getKey();
                        String statusValue = child.child("status").getValue().toString();

                        if (statusValue.equals("waiting")){
                            Intent intent = new Intent(getContext(), QrCodeHandler.class);
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
        checkRideTransactions();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewHolder.stopListening();
    }
}