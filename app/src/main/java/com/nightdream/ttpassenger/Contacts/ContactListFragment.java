package com.nightdream.ttpassenger.Contacts;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nightdream.ttpassenger.R;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class ContactListFragment extends Fragment {

    private View ContactView;
    private FloatingActionButton floatingActionButton;
    private RecyclerView ContactRecycler, ContactRequestRecycler;
    private String currentUserID, contactID;
    private Button backToMap;

    //Firebase
    private DatabaseReference requestRef, reference, acceptRef, notificationRef;
    private FirebaseAuth mAuth;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ContactView = inflater.inflate(R.layout.fragment_contacts, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        variables();
        backToMapButton();
        myContactRecycler();
        myRequestRecylcer();
        addContact();

        return ContactView;
    }

    private void backToMapButton() {
        backToMap.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
    }

    private void variables() {
        backToMap = ContactView.findViewById(R.id.back_to_map);
        requestRef = FirebaseDatabase.getInstance().getReference().child("Contact Request");
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        ContactRequestRecycler = ContactView.findViewById(R.id.contact_list_request_recycler);
        ContactRequestRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        ContactRequestRecycler.setNestedScrollingEnabled(false);
    }

    private void myRequestRecylcer() {

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(requestRef.child(currentUserID), Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, RequestContactViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestContactViewHolder requestContactViewHolder, int i, @NonNull Contacts contacts) {
                final String contactsIDs = getRef(i).getKey();
                DatabaseReference getType = getRef(i).child("contact_request").getRef();

                getType.addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            Object type = dataSnapshot.getValue();

                            if (String.valueOf(type).equals("received")) {

                                assert contactsIDs != null;
                                reference.child("Drivers").child(contactsIDs).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            if (dataSnapshot.hasChild("image")) {

                                                requestContactViewHolder.acceptBtn.setVisibility(View.VISIBLE);
                                                requestContactViewHolder.declineBtn.setText("Decline");

                                                contactID = dataSnapshot.getKey();
                                                Object image = dataSnapshot.child("image").getValue();
                                                Object name = dataSnapshot.child("name").getValue();
                                                Object number = dataSnapshot.child("userNumber").getValue();

                                                requestContactViewHolder.request_contact_name.setText(String.valueOf(name));
                                                requestContactViewHolder.request_contact_num.setText(String.valueOf(number));
                                                Picasso.get().load(String.valueOf(image)).into(requestContactViewHolder.request_profile_icon);
                                            } else {

                                                Object name = dataSnapshot.child("name").getValue();
                                                Object number = dataSnapshot.child("userNumber").getValue();

                                                requestContactViewHolder.request_contact_name.setText(String.valueOf(name));
                                                requestContactViewHolder.request_contact_num.setText(String.valueOf(number));
                                                requestContactViewHolder.request_profile_icon.setImageResource(R.drawable.profile_image);
                                            }
                                        } else {

                                            reference.child("Passenger").child(contactsIDs).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        if (dataSnapshot.hasChild("image")) {

                                                            requestContactViewHolder.acceptBtn.setVisibility(View.VISIBLE);
                                                            requestContactViewHolder.declineBtn.setText("Decline");

                                                            contactID = dataSnapshot.getKey();
                                                            Object image = dataSnapshot.child("image").getValue();
                                                            Object name = dataSnapshot.child("name").getValue();
                                                            Object number = dataSnapshot.child("userNumber").getValue();

                                                            requestContactViewHolder.request_contact_name.setText(String.valueOf(name));
                                                            requestContactViewHolder.request_contact_num.setText(String.valueOf(number));
                                                            Picasso.get().load(String.valueOf(image)).into(requestContactViewHolder.request_profile_icon);
                                                        } else {

                                                            Object name = dataSnapshot.child("name").getValue();
                                                            Object number = dataSnapshot.child("userNumber").getValue();

                                                            requestContactViewHolder.request_contact_name.setText(String.valueOf(name));
                                                            requestContactViewHolder.request_contact_num.setText(String.valueOf(number));
                                                            requestContactViewHolder.request_profile_icon.setImageResource(R.drawable.profile_image);
                                                        }
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

                            } else if (String.valueOf(type).equals("sent")) {
                                requestContactViewHolder.acceptBtn.setVisibility(View.INVISIBLE);
                                requestContactViewHolder.declineBtn.setText("Cancel");

                                assert contactsIDs != null;
                                reference.child("Drivers").child(contactsIDs).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            if (dataSnapshot.hasChild("image")) {

                                                contactID = dataSnapshot.getKey();
                                                Object image = dataSnapshot.child("image").getValue();
                                                Object name = dataSnapshot.child("name").getValue();
                                                Object number = dataSnapshot.child("userNumber").getValue();

                                                requestContactViewHolder.request_contact_name.setText(String.valueOf(name));
                                                requestContactViewHolder.request_contact_num.setText(String.valueOf(number));
                                                Picasso.get().load(String.valueOf(image)).into(requestContactViewHolder.request_profile_icon);
                                            } else {

                                                Object name = dataSnapshot.child("name").getValue();
                                                Object number = dataSnapshot.child("userNumber").getValue();

                                                requestContactViewHolder.request_contact_name.setText(String.valueOf(name));
                                                requestContactViewHolder.request_contact_num.setText(String.valueOf(number));
                                                requestContactViewHolder.request_profile_icon.setImageResource(R.drawable.profile_image);
                                            }
                                        } else {
                                            reference.child("Passenger").child(contactsIDs).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        if (dataSnapshot.hasChild("image")) {

                                                            contactID = dataSnapshot.getKey();
                                                            Object image = dataSnapshot.child("image").getValue();
                                                            Object name = dataSnapshot.child("name").getValue();
                                                            Object number = dataSnapshot.child("userNumber").getValue();

                                                            requestContactViewHolder.request_contact_name.setText(String.valueOf(name));
                                                            requestContactViewHolder.request_contact_num.setText(String.valueOf(number));
                                                            Picasso.get().load(String.valueOf(image)).into(requestContactViewHolder.request_profile_icon);
                                                        } else {

                                                            Object name = dataSnapshot.child("name").getValue();
                                                            Object number = dataSnapshot.child("userNumber").getValue();

                                                            requestContactViewHolder.request_contact_name.setText(String.valueOf(name));
                                                            requestContactViewHolder.request_contact_num.setText(String.valueOf(number));
                                                            requestContactViewHolder.request_profile_icon.setImageResource(R.drawable.profile_image);
                                                        }
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
                        }
                        requestContactViewHolder.acceptBtn.setOnClickListener(v -> {
                            acceptRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

                            assert contactsIDs != null;
                            acceptRef.child(currentUserID).child(contactsIDs).child("Contacts").setValue("Accepted").addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    acceptRef.child(contactsIDs).child(currentUserID).child("Contacts").setValue("Accepted").addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            requestRef.child(currentUserID).child(contactsIDs).removeValue().addOnCompleteListener(task11 -> {
                                                if (task11.isSuccessful()) {
                                                    requestRef.child(contactsIDs).child(currentUserID).removeValue().addOnCompleteListener(task111 -> {
                                                        if (task111.isSuccessful()) {
                                                            notificationRef.child(currentUserID).removeValue().addOnCompleteListener(task1111 -> {
                                                                if (task1111.isSuccessful()) {
                                                                    Toast.makeText(getActivity(), "Contact Successfully Added", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        });

                        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

                        requestContactViewHolder.declineBtn.setOnClickListener(v -> {
                            assert contactsIDs != null;
                            requestRef.child(currentUserID).child(contactsIDs).removeValue().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    requestRef.child(contactsIDs).child(currentUserID).removeValue().addOnCompleteListener(task12 -> {
                                        if (task12.isSuccessful()) {
                                            notificationRef.child(currentUserID).removeValue().addOnCompleteListener(task121 -> {
                                                if (task121.isSuccessful()) {
                                                    notificationRef.child(contactsIDs).removeValue().addOnCompleteListener(task1211 -> {
                                                        if (task1211.isSuccessful()) {
                                                            if (requestContactViewHolder.declineBtn.getText().equals("Decline")) {
                                                                Toast.makeText(getContext(), "Contact Request Declined", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(getContext(), "Contact Request Cancel", Toast.LENGTH_SHORT).show();
                                                            }

                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public RequestContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_request_item, parent, false);
                return new RequestContactViewHolder(view);
            }
        };

        ContactRequestRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestContactViewHolder extends RecyclerView.ViewHolder {

        TextView request_contact_name, request_contact_num;
        CircleImageView request_profile_icon;
        Button acceptBtn, declineBtn;

        public RequestContactViewHolder(@NonNull View itemView) {
            super(itemView);

            request_contact_name = itemView.findViewById(R.id.contact_request_item_Contact_Name);
            request_contact_num = itemView.findViewById(R.id.contact_request_item_Contact_Number);
            request_profile_icon = itemView.findViewById(R.id.contact_request_item_ProfileImage);
            acceptBtn = itemView.findViewById(R.id.contact_request_item_acceptbtn);
            declineBtn = itemView.findViewById(R.id.contact_request_item_declinebtn);

        }
    }

    private void myContactRecycler() {
        DatabaseReference contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        ContactRecycler = ContactView.findViewById(R.id.contact_list_recycler);
        ContactRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        ContactRecycler.setNestedScrollingEnabled(false);

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(contactRef, Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder contactsViewHolder, int i, @NonNull Contacts contacts) {
                String contactsIds = getRef(i).getKey();

                assert contactsIds != null;
                reference.child("Drivers").child(contactsIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (dataSnapshot.hasChild("image")) {
                                String id = dataSnapshot.getKey();
                                Object image = dataSnapshot.child("image").getValue();
                                Object name = dataSnapshot.child("name").getValue();
                                Object number = dataSnapshot.child("userNumber").getValue();

                                contactsViewHolder.contact_name.setText(String.valueOf(name));
                                contactsViewHolder.contact_num.setText(String.valueOf(number));
                                Picasso.get().load(String.valueOf(image)).into(contactsViewHolder.profile_icon);

                                contactsViewHolder.itemView.setOnClickListener(v -> {

                                    ContactListBottomSheet ContactListBottomSheet = new ContactListBottomSheet();
                                    FragmentTransaction ft = ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction();
                                    SharedPreferences.Editor editor = getContext().getSharedPreferences("DeviceToken", MODE_PRIVATE).edit();
                                    editor.putString("ID", id);
                                    editor.putString("name", String.valueOf(name));
                                    editor.putString("image", String.valueOf(image));
                                    editor.apply();

                                    ContactListBottomSheet.show(ft, "ContactListBottomSheet");
                                });
                            } else {

                                Object name = dataSnapshot.child("name").getValue();
                                Object number = dataSnapshot.child("userNumber").getValue();

                                contactsViewHolder.contact_name.setText(String.valueOf(name));
                                contactsViewHolder.contact_num.setText(String.valueOf(number));
                                contactsViewHolder.profile_icon.setImageResource(R.drawable.profile_image);
                            }
                        } else {
                            reference.child("Passenger").child(contactsIds).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        if (snapshot.hasChild("image")) {
                                            String id = snapshot.getKey();
                                            Object image = snapshot.child("image").getValue();
                                            Object name = snapshot.child("name").getValue();
                                            Object number = snapshot.child("userNumber").getValue();

                                            contactsViewHolder.contact_name.setText(String.valueOf(name));
                                            contactsViewHolder.contact_num.setText(String.valueOf(number));
                                            Picasso.get().load(String.valueOf(image)).into(contactsViewHolder.profile_icon);

                                            contactsViewHolder.itemView.setOnClickListener(v -> {

                                                ContactListBottomSheet ContactListBottomSheet = new ContactListBottomSheet();
                                                FragmentTransaction ft = ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction();
                                                SharedPreferences.Editor editor = getContext().getSharedPreferences("DeviceToken", MODE_PRIVATE).edit();
                                                editor.putString("ID", id);
                                                editor.putString("name", String.valueOf(name));
                                                editor.putString("image", String.valueOf(image));
                                                editor.apply();

                                                ContactListBottomSheet.show(ft, "ContactListBottomSheet");
                                            });
                                        } else {

                                            Object name = snapshot.child("name").getValue();
                                            Object number = snapshot.child("userNumber").getValue();

                                            contactsViewHolder.contact_name.setText(String.valueOf(name));
                                            contactsViewHolder.contact_num.setText(String.valueOf(number));
                                            contactsViewHolder.profile_icon.setImageResource(R.drawable.profile_image);
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

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_items, parent, false);
                return new ContactsViewHolder(view);

            }
        };
        ContactRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        TextView contact_name, contact_num;
        CircleImageView profile_icon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            contact_name = itemView.findViewById(R.id.contact_item_Contact_Name);
            contact_num = itemView.findViewById(R.id.contact_item_Contact_Number);
            profile_icon = itemView.findViewById(R.id.contact_item_ProfileImage);
        }
    }

    private void addContact() {

        floatingActionButton = ContactView.findViewById(R.id.contacts_list_add_contact);
        floatingActionButton.setOnClickListener(v -> {
            TelephonyManager telephonyManager = (TelephonyManager) getActivity().getApplicationContext().getSystemService(getActivity().getApplicationContext().TELEPHONY_SERVICE);

            assert telephonyManager != null;
            if (telephonyManager.getNetworkCountryIso() != null) {
                if (!telephonyManager.getNetworkCountryIso().equals("")) {
                    AddContactFragment addContactFragment = new AddContactFragment();

                    FragmentManager fragmentManager = getParentFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.mainFragment, addContactFragment);
                    fragmentTransaction.commit();
                }
            } else {
                Toast.makeText(getContext(), "No Mobile Network Found", Toast.LENGTH_SHORT).show();
            }

        });
    }
}
