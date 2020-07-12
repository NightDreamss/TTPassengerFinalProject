package com.nightdream.ttpassenger.RideManagement;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.transition.Fade;
import android.view.Menu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.nightdream.ttpassenger.Notification.NotificationScreen;
import com.nightdream.ttpassenger.Notification.Token;
import com.nightdream.ttpassenger.R;
import com.nightdream.ttpassenger.login.Register;
import com.nightdream.ttpassenger.login.SplashScreen;

import java.util.ArrayList;
import java.util.List;


public class NavigationView extends AppCompatActivity {

    private Menu nav_menu;
    private String uID;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Make to run your application only in portrait mode
        setContentView(R.layout.navigation_view);

        checkPermissions();
        variables();
        configureNavbar();
        getToken();
        setupWindowAnimations();
    }

    private void getToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(NavigationView.this, instanceIdResult -> {
            String mToken = instanceIdResult.getToken();
            updateToken(mToken);
        });
    }

    public void updateToken(String token) {
        DatabaseReference tokeRef = FirebaseDatabase.getInstance().getReference("Tokens");
        Token tokens = new Token(token);
        tokeRef.child(uID).setValue(tokens);

    }

    private void configureNavbar() {
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, SplashScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        } else {
            uID = mAuth.getCurrentUser().getUid();

            SharedPreferences sharedPreferences = getSharedPreferences("Shared_USER", MODE_PRIVATE);
            @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Current_USERID", uID);
            editor.apply();

            if (getIntent().hasExtra("data")) {
                Intent intent = new Intent(NavigationView.this, NotificationScreen.class);
                intent.putExtra("data", getIntent().getStringExtra("data"));
                startActivity(intent);
            }

            reference.child("Users").child("Drivers").child(uID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new RideRequests()).commit();

                    } else {
                        reference.child("Users").child("Passenger").child(uID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {

                                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new RequestRide()).commit();

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
    }

    String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private void checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
        }
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
