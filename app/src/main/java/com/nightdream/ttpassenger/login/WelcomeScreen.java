package com.nightdream.ttpassenger.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Explode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.nightdream.ttpassenger.R;

public class WelcomeScreen extends AppCompatActivity {

    private Button phone_number_verification;
    private ImageView accept_transition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);

        variables();
        buttonClicks();
        setupWindowAnimations();
    }

    private void variables() {
        phone_number_verification = findViewById(R.id.welcome_screen_phone_number_verification_button);
        accept_transition = findViewById(R.id.welcome_screen_transition_image);
    }

    private void buttonClicks() {

        phone_number_verification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String transitionName = getString(R.string.register_account_logo_transition);

                Intent intent = new Intent(WelcomeScreen.this, PhoneNumber.class);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(WelcomeScreen.this, accept_transition, transitionName);
                startActivity(intent, options.toBundle());
            }
        });
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

}
