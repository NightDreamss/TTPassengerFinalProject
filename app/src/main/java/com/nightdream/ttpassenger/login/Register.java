package com.nightdream.ttpassenger.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.transition.Fade;
import android.view.View;
import android.widget.Button;

import com.nightdream.ttpassenger.R;

public class Register extends AppCompatActivity {

    private Button driver_account_button, passenger_account_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        variables();
        setupWindowAnimations();
        transitionFragments();

        getSupportFragmentManager().beginTransaction().replace(R.id.registerFrame, new PassengerAccount()).commit();
    }

    private void variables() {
        //variables layout
        driver_account_button = findViewById(R.id.passenger_account_driver_button);
        passenger_account_button = findViewById(R.id.passenger_account_button);

    }

    private void transitionFragments() {

        passenger_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                passenger_account_button.setBackgroundTintList(ContextCompat.getColorStateList(Register.this, R.color.colorPrimary));
                driver_account_button.setBackgroundTintList(ContextCompat.getColorStateList(Register.this, R.color.colorAccent));

                Fragment passenger = new PassengerAccount();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right);
                transaction.replace(R.id.registerFrame, passenger);
                transaction.commit();

                driver_account_button.setEnabled(true);
                passenger_account_button.setEnabled(false);

            }
        });

        driver_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                passenger_account_button.setBackgroundTintList(ContextCompat.getColorStateList(Register.this, R.color.colorAccent));
                driver_account_button.setBackgroundTintList(ContextCompat.getColorStateList(Register.this, R.color.colorPrimary));

                Fragment driver = new DriverAccount();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right);
                transaction.replace(R.id.registerFrame, driver);
                transaction.commit();

                passenger_account_button.setEnabled(true);
                driver_account_button.setEnabled(false);

            }
        });
    }

    protected void setupWindowAnimations() {

        Fade fade = new Fade();
        fade.setDuration(getResources().getInteger(R.integer.anim_duration_short));
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setSharedElementsUseOverlay(true);

        getWindow().setExitTransition(fade);
        getWindow().setEnterTransition(fade);
        getWindow().setReenterTransition(fade);
        getWindow().setReturnTransition(fade);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
