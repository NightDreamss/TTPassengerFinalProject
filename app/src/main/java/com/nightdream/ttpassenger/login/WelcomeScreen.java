package com.nightdream.ttpassenger.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.transition.Explode;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import com.nightdream.ttpassenger.R;

import java.util.ArrayList;
import java.util.List;

public class WelcomeScreen extends AppCompatActivity {

    private Button phone_number_login, phone_number_register;
    private ImageView accept_transition;
    private String transitionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);

        variables();
        buttonClicks();
        setupWindowAnimations();
    }

    private void variables() {
        phone_number_login = findViewById(R.id.welcome_screen_login);
        phone_number_register = findViewById(R.id.welcome_screen_register);
        accept_transition = findViewById(R.id.welcome_screen_transition_image);
        transitionName = getString(R.string.register_account_logo_transition);
    }

    private void buttonClicks() {

        phone_number_login.setOnClickListener(v -> {

            Intent intent = new Intent(WelcomeScreen.this, Login.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(WelcomeScreen.this, accept_transition, transitionName);
            startActivity(intent, options.toBundle());
        });

        phone_number_register.setOnClickListener(v -> {

            Intent intent = new Intent(WelcomeScreen.this, Register.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(WelcomeScreen.this, accept_transition, transitionName);
            startActivity(intent, options.toBundle());
        });
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
                Toast.makeText(this, "These Permissions are required for the application to work properly", Toast.LENGTH_SHORT).show();
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), 100);
        }
    }

    protected void setupWindowAnimations() {

        Explode explode = new Explode();
        explode.setDuration(getResources().getInteger(R.integer.anim_duration_short));
        explode.excludeTarget(android.R.id.statusBarBackground, true);
        explode.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setAllowEnterTransitionOverlap(true);
        getWindow().setAllowReturnTransitionOverlap(true);
        getWindow().setSharedElementsUseOverlay(true);

        getWindow().setExitTransition(explode);
        getWindow().setEnterTransition(explode);
        getWindow().setReenterTransition(explode);
        getWindow().setReturnTransition(explode);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }
}
