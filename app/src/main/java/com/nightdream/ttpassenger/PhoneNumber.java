package com.nightdream.ttpassenger;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Explode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;

public class PhoneNumber extends AppCompatActivity {

    private ImageView transitionLogo;
    private ImageButton back_button;
    private String phoneNumber;
    private CountryCodePicker ccp;
    private TextInputLayout phoneInput;
    private EditText phoneEditText;
    private Button continue_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_number);

        variables();
        setupWindowAnimations();
        verifyForm();
        editTextErrors();
        back();
    }

    private void editTextErrors() {

        phoneEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    phoneInput.setHelperText(getString(R.string.eg_number));
                } else {
                    phoneInput.setHelperText("");
                }
            }
        });

        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                phoneInput.setHelperText(getString(R.string.eg_number));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void variables() {
        //variables layout
        transitionLogo = findViewById(R.id.phone_number_activity_header_image);
        back_button = findViewById(R.id.passenger_account_back_button);
        continue_button = findViewById(R.id.phone_number_activity_button);
        phoneInput = findViewById(R.id.phone_number_activity_phone_input);

        //variable form
        phoneEditText = findViewById(R.id.phone_number_activity_phone_field);
        ccp = findViewById(R.id.phone_number_activity_ccp);
        ccp.registerCarrierNumberEditText(phoneEditText);

    }

    private void verifyForm() {

        continue_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                phoneNumber = ccp.getFullNumberWithPlus();
                if (ccp.isValidFullNumber()) {

                    Intent intent = new Intent(PhoneNumber.this, VerifyPhoneNumber.class);
                    intent.putExtra("userNumber", phoneNumber);

                    String transitionName = getString(R.string.register_account_logo_transition);

                    ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(PhoneNumber.this, transitionLogo, transitionName);
                    startActivity(intent, transitionActivityOptions.toBundle());

                } else {
                    phoneInput.setError(getString(R.string.phone_error));
                    phoneInput.requestFocus();
                }
            }
        });
    }

    protected void setupWindowAnimations() {

        //enter and exit transition for driver and passenger screen
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

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
        super.onBackPressed();
    }
}
