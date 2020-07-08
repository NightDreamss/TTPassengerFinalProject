package com.nightdream.ttpassenger.Contacts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.nightdream.ttpassenger.R;

public class ContactsLayout extends AppCompatActivity {

    public static Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Make to run your application only in portrait mode
        setContentView(R.layout.activity_contacts_layout);

        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragment, new ContactListFragment()).commit();
    }
}