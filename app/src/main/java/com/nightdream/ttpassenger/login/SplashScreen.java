package com.nightdream.ttpassenger.login;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nightdream.ttpassenger.R;
import com.nightdream.ttpassenger.RideManagement.NavigationView;

public class SplashScreen extends AppCompatActivity {

    private boolean shouldFinish = false;
    private SharedPreferences sharedPreferences;
    private TextView splash_title;
    private ImageView logo;
    private Boolean startApp;
    private String uID;
    private ConstraintLayout splashscreen_container;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference passengerRef;
    private DatabaseReference driverRef;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        startApp = sharedPreferences.getBoolean("startApp", true);

        variables();
        setupWindowAnimation();
    }

    private void launchOrder() {
        if (mAuth.getCurrentUser() == null) {
            splashscreen_container.setVisibility(View.VISIBLE);
            runSplash();
        } else {

            uID = mAuth.getCurrentUser().getUid();

            passengerRef.child("Users").child("Drivers").child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                            Intent intent = new Intent(SplashScreen.this, NavigationView.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                    } else {
                        driverRef.child("Users").child("Passenger").child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                        Intent intent = new Intent(SplashScreen.this, NavigationView.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();

                                } else {
                                    Intent intent = new Intent(SplashScreen.this, Register.class);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(SplashScreen.this, databaseError.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(SplashScreen.this, databaseError.toString(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void runSplash() {

        if (startApp) {

            handler.postDelayed(() -> splash_title.animate().alpha(0.0f).setDuration(1000).start(),2000);

            handler.postDelayed(() -> {
                splash_title.setText(getString(R.string.take_passengerTT));
                splash_title.animate().alpha(1.0f).start();
            },3000);

            handler.postDelayed(() -> logo.animate().alpha(1.0f).setDuration(1000).start(), 5000);

            handler.postDelayed(() -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                startApp = false;
                editor.putBoolean("startApp", false);
                editor.apply();

                shouldFinish = true;
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this);
                Intent intent = new Intent(SplashScreen.this, WelcomeScreen.class);
                startActivity(intent, options.toBundle());
            }, 7500);
        } else {
            Intent intent = new Intent(SplashScreen.this, WelcomeScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    private void variables() {

        //Firebase variables
        mAuth = FirebaseAuth.getInstance();
        passengerRef = FirebaseDatabase.getInstance().getReference();
        driverRef = FirebaseDatabase.getInstance().getReference();

        //variables
        splashscreen_container = findViewById(R.id.splashscreen_container);
        splash_title = findViewById(R.id.splashscreen_title);
        logo = findViewById(R.id.splashscreen_logo);
        logo.setAlpha(0.0f);
    }

    private void setupWindowAnimation() {

        Fade fade = new Fade();
        fade.setDuration(getResources().getInteger(R.integer.anim_duration_short));
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
        getWindow().setAllowEnterTransitionOverlap(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (shouldFinish) {
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        launchOrder();
    }
}
