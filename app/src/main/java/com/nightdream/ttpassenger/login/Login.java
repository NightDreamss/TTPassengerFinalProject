package com.nightdream.ttpassenger.login;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Explode;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.nightdream.ttpassenger.R;
import com.nightdream.ttpassenger.RideManagement.NavigationView;

import java.util.Objects;

public class Login extends AppCompatActivity {

    private ImageButton back_button;
    private String email, password;
    private TextInputLayout emailInput, passwordInput;
    private EditText emailEditText, passwordEditText;
    private Button continue_button;

    //firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_form);

        variables();
        setupWindowAnimations();
        verifyForm();
        editTextErrors();
        back();
    }

    private void editTextErrors() {

        emailEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                emailInput.setHelperText("john@hotmail.com");
            } else {
                emailInput.setHelperText("");
            }
        });

        passwordEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                passwordInput.setHelperText("Password must contain a 8 characters");
            } else {
                passwordInput.setHelperText("");
            }
        });

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailInput.setHelperText("john@hotmail.com");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordInput.setHelperText("Password must contain a 8 characters");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void variables() {
        //variables layout
        back_button = findViewById(R.id.login_back_button);
        continue_button = findViewById(R.id.login_button);

        //variable form
        emailEditText = findViewById(R.id.login_email_field);
        passwordEditText = findViewById(R.id.login_password_field);
        emailInput = findViewById(R.id.login_email_input);
        passwordInput = findViewById(R.id.login_password_input);
    }

    private void verifyForm() {

        continue_button.setOnClickListener(v -> {

            email = Objects.requireNonNull(emailInput.getEditText()).getText().toString().trim();
            password = Objects.requireNonNull(passwordInput.getEditText()).getText().toString().trim();

            if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (password.length() >= 8) {
                    mAuth = FirebaseAuth.getInstance();

                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(Login.this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, "Welcome back to TTPassenger!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, NavigationView.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(Login.this, "User does not exist", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    passwordInput.setError("Password must contain a 8 characters");
                }
            } else {
                emailInput.setError("Invalid email address");
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

    private void back() {

        back_button.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
        super.onBackPressed();
    }
}
