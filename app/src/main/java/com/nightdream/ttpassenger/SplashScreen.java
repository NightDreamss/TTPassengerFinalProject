package com.nightdream.ttpassenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

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

            passengerRef.child("Users").child("Drivers").child(uID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        Intent intent = new Intent(SplashScreen.this, NavigatorScreen.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        driverRef.child("Users").child("Passenger").child(uID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {

                                    Intent intent = new Intent(SplashScreen.this, NavigatorScreen.class);
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
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    splash_title.animate().alpha(0.0f).setDuration(1000).start();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            splash_title.setText(getString(R.string.take_passengerTT));
                            splash_title.animate().alpha(1.0f).start();

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    logo.animate().alpha(1.0f).setDuration(1000).start();

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            startApp = false;
                                            editor.putBoolean("startApp", startApp);
                                            editor.apply();

                                            shouldFinish = true;
                                            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this);
                                            Intent intent = new Intent(SplashScreen.this, WelcomeScreen.class);
                                            startActivity(intent, options.toBundle());

                                        }
                                    }, 2500);
                                }
                            }, 2000);
                        }
                    }, 1000);

                }
            }, 2000);
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
        // inside your activity (if you did not enable transitions in your theme)
        Fade fade = new Fade();
        fade.setDuration(getResources().getInteger(R.integer.anim_duration_short));
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);

        // set an exit transition
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
