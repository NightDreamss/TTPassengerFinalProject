package com.nightdream.ttpassenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.transition.Fade;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nightdream.ttpassenger.login.Register;
import com.nightdream.ttpassenger.login.SplashScreen;


public class NavigationView extends AppCompatActivity {

    private Menu nav_menu;
    private static final int REQUEST_PERMISSION_CAMERA = 100, REQUEST_PERMISSION_LOCATION = 101, IMAGE_CODE = 200;
    private String uID, geoLocation;
    private BottomNavigationView bottomNavigationView;
    private ResultReceiver receiver;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Make to run your application only in portrait mode
        setContentView(R.layout.navigation_view);

        variables();
        configureNavbar();
        setupWindowAnimations();
    }

    private void configureNavbar() {
        nav_menu = bottomNavigationView.getMenu();

        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, SplashScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        } else {

            uID = mAuth.getCurrentUser().getUid();

            reference.child("Users").child("Drivers").child(uID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                            nav_menu.findItem(R.id.rideRequests).setVisible(true);
                            bottomNavigationView.setSelectedItemId(R.id.rideRequests);

                    } else {
                        reference.child("Users").child("Passenger").child(uID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {

                                    nav_menu.findItem(R.id.requestRide).setVisible(true);
                                    bottomNavigationView.setSelectedItemId(R.id.requestRide);

                                } else {
                                    Intent intent = new Intent(NavigationView.this, Register.class);
                                    startActivity(intent);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(NavigationView.this, databaseError.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(NavigationView.this, databaseError.toString(), Toast.LENGTH_LONG).show();

                    VerificationDialog();
                }
            });
        }
    }

    private void VerificationDialog() {

        AlertDialog.Builder verificationDialog = new AlertDialog.Builder(this);
        verificationDialog.setTitle("Account Notice");
        verificationDialog.setMessage("Account has not been verified, we will verify your information within 48hours.");
        verificationDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                System.exit(0);
            }
        });

        AlertDialog alertDialog = verificationDialog.create();
        alertDialog.show();

        verificationDialog.show();
    }

    private void variables() {

        //firebase variables
        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        //variables
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavController navController = Navigation.findNavController(this,  R.id.fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    protected void setupWindowAnimations() {

        //enter and exit transition for driver and passenger screen
        Fade fade = new Fade();
        fade.setDuration(getResources().getInteger(R.integer.anim_duration_short));
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setAllowEnterTransitionOverlap(false);
        getWindow().setAllowReturnTransitionOverlap(false);

        getWindow().setEnterTransition(fade);
    }

}
