package com.nightdream.ttpassenger.RideManagement;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.nightdream.ttpassenger.Notification.Token;
import com.nightdream.ttpassenger.R;
import com.nightdream.ttpassenger.login.Register;
import com.nightdream.ttpassenger.login.SplashScreen;


public class NavigationView extends AppCompatActivity {

    private String uID;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_view);

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

            reference.child("Users").child("Drivers").child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
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
                    VerificationDialog();
                }
            });
        }
    }

    private void VerificationDialog() {

        AlertDialog.Builder verificationDialog = new AlertDialog.Builder(this);
        verificationDialog.setTitle("Account Notice");
        verificationDialog.setMessage("Account has not been verified, we will verify your information within 48hours.");
        verificationDialog.setOnCancelListener(dialog -> System.exit(0));

        AlertDialog alertDialog = verificationDialog.create();
        alertDialog.show();

        verificationDialog.show();
    }

    private void variables() {
        //firebase variables
        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
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
