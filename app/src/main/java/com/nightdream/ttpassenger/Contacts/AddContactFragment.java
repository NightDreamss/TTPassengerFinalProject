package com.nightdream.ttpassenger.Contacts;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nightdream.ttpassenger.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class AddContactFragment extends Fragment {

    private EditText search_contacts;
    private RecyclerView contact_list;
    private RecyclerView.Adapter contact_list_adapter;
    private ArrayList<Contacts> list, contactsList;
    private RelativeLayout add_contact_backbtn;
    private String userPhoneNumber;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View addContactView = inflater.inflate(R.layout.fragment_addcontact, container, false);

        add_contact_backbtn = addContactView.findViewById(R.id.add_contacts_back_layout);
        contact_list = addContactView.findViewById(R.id.add_contacts_recycler);

        addContactBackbtn();
        getContacts();
        startRecyclerView();

        return addContactView;
    }

    private void addContactBackbtn() {

        add_contact_backbtn.setOnClickListener(v -> {
            ContactListFragment contactListFragment = new ContactListFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.mainFragment, contactListFragment);
            fragmentTransaction.commit();
        });
    }

    private void getContacts() {

        list = new ArrayList<>();
        contactsList = new ArrayList<>();

        String ISO = getCounty();

        Map<String, List<String>> contacts = new HashMap<>();

        String[] projection = {ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cur = requireActivity().getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userPhoneNumber = user.getPhoneNumber();
        }

        while (cur != null && cur.moveToNext()) {
            long id = cur.getLong(0); // contact ID
            String name = cur.getString(1); // contact name
            String number = cur.getString(2); // the actual info, e.g. +1-212-555-1234

            // add info to existing list if this contact-id was already found, or create a new list in case it's new
            String key = id + " - " + name;

            List<String> infos;
            if (contacts.containsKey(key)) {
                infos = contacts.get(key);

            } else {
                infos = new ArrayList<>();
                contacts.put(key, infos);

                number = number.replaceAll("[^0-9]", "");

                if (!String.valueOf(number.charAt(0)).equals("+")) {
                    if (number.length() <= 7) {
                        number = ISO + number;
                    } else {
                        number = "+" + number;
                    }
                }
            }
            assert infos != null;
            if (!infos.contains(number)) {
                if (!number.equals(userPhoneNumber)) {
                    infos.add(number);
                    Contacts mContacts = new Contacts("", name, number, "");
                    list.add(mContacts);

                    DatabaseReference driverReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");

                    Query driverQuery = driverReference.orderByChild("userNumber").equalTo(mContacts.getUserNumber());

                    driverQuery.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {

                                for (final DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                                    String key = childSnapshot.getKey();

                                    assert key != null;
                                    DatabaseReference sentRef = FirebaseDatabase.getInstance().getReference().child("Contact Request").child(key);

                                    sentRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Object phone = "", name = "", image = "";

                                            if (!dataSnapshot.exists()) {
                                                if (childSnapshot.child("userNumber").getValue() != null)
                                                    phone = childSnapshot.child("userNumber").getValue();
                                                if (childSnapshot.child("name").getValue() != null)
                                                    name = childSnapshot.child("name").getValue();
                                                if (childSnapshot.child("image").getValue() != null)
                                                    image = childSnapshot.child("image").getValue();

                                                Contacts contacts = new Contacts(childSnapshot.getKey(), String.valueOf(name), String.valueOf(phone), String.valueOf(image));

                                                for (Contacts contactIterator : list) {
                                                    if (contactIterator.getUserNumber().equals(contacts.getUserNumber())) {
                                                        contacts.setName(contactIterator.getName());
                                                    }
                                                }
                                                contactsList.add(contacts);
                                                contact_list_adapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                    DatabaseReference passengerReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Passenger");

                    Query passengerQuery = passengerReference.orderByChild("userNumber").equalTo(mContacts.getUserNumber());

                    passengerQuery.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {

                                for (final DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                                    String key = childSnapshot.getKey();

                                    assert key != null;
                                    DatabaseReference sentRef = FirebaseDatabase.getInstance().getReference().child("Contact Request").child(key);

                                    sentRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Object phone = "", name = "", image = "";

                                            if (!dataSnapshot.exists()) {
                                                if (childSnapshot.child("userNumber").getValue() != null)
                                                    phone = Objects.requireNonNull(childSnapshot.child("userNumber").getValue()).toString();
                                                if (childSnapshot.child("name").getValue() != null)
                                                    name = Objects.requireNonNull(childSnapshot.child("name").getValue()).toString();
                                                if (childSnapshot.child("image").getValue() != null)
                                                    image = Objects.requireNonNull(childSnapshot.child("image").getValue()).toString();

                                                Contacts contacts = new Contacts(childSnapshot.getKey(), String.valueOf(name), String.valueOf(phone), String.valueOf(image));

                                                for (Contacts contactIterator : list) {
                                                    if (contactIterator.getUserNumber().equals(contacts.getUserNumber())) {
                                                        contacts.setName(contactIterator.getName());
                                                    }
                                                }
                                                contactsList.add(contacts);
                                                contact_list_adapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        }
        if (cur != null) {
            cur.close();
        }
    }

    private String getCounty() {
        String iso = null;

        TelephonyManager telephonyManager = (TelephonyManager) requireActivity().getApplicationContext().getSystemService(getActivity().getApplicationContext().TELEPHONY_SERVICE);

        if (telephonyManager.getNetworkCountryIso() != null) {
            if (!telephonyManager.getNetworkCountryIso().equals("")) {
                iso = telephonyManager.getNetworkCountryIso();
            }
        }
        assert iso != null;
        return CountyToPhonePrefix.getPhone(iso);
    }

    private void startRecyclerView() {
        contact_list.setNestedScrollingEnabled(false);
        RecyclerView.LayoutManager contact_list_layout = new LinearLayoutManager(requireActivity().getApplicationContext(), RecyclerView.VERTICAL, false);
        contact_list.setLayoutManager(contact_list_layout);
        contact_list_adapter = new AddContactsAdapter(contactsList, getContext());
        contact_list.setAdapter(contact_list_adapter);
    }
}
