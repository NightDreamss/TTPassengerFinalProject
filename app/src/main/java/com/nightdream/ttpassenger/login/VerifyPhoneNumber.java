package com.nightdream.ttpassenger.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Explode;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.nightdream.ttpassenger.NavigationView;
import com.nightdream.ttpassenger.R;

import java.util.concurrent.TimeUnit;

public class VerifyPhoneNumber extends AppCompatActivity {

    private boolean shouldFinish = false;
    private EditText codeField;
    private String phonenumber, mVerificationId, uID, deviceToken;
    private ImageButton back_button;
    private ImageView transitionLogo;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference checkUserDriver, checkUserPassenger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_phone_number);

        variables();
        setupWindowAnimations();
        checkVerificationCode();
        sendVerificationCode(phonenumber);
        back();
    }

    private void variables() {

        //Firebase variables
        mAuth = FirebaseAuth.getInstance();
        checkUserDriver = FirebaseDatabase.getInstance().getReference();
        checkUserPassenger = FirebaseDatabase.getInstance().getReference();

        //variables
        transitionLogo = findViewById(R.id.verify_phone_activity_header_image);
        codeField = findViewById(R.id.verify_code_number_verification_field);
        TextView displayPhoneNumber = findViewById(R.id.verify_phone_activity_entered_number);
        back_button = findViewById(R.id.verify_phone_activity_back_button);
        phonenumber = getIntent().getStringExtra("userNumber");
        displayPhoneNumber.setText(phonenumber);
    }

    private void checkVerificationCode() {

        codeField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (codeField.length() == 6){
                    String verificationCode = codeField.getText().toString();

                    verifyCode(verificationCode);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);

    }

    private void sendVerificationCode(String number) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                VerifyPhoneNumber.this,
                mCallback
        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                codeField.setText(code);
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(VerifyPhoneNumber.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Toast.makeText(VerifyPhoneNumber.this, "SMS Quote exceeded", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCodeSent(String verificationId,
                               PhoneAuthProvider.ForceResendingToken token) {
            mVerificationId = verificationId;
        }
    };

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                @Override
                                public void onSuccess(InstanceIdResult instanceIdResult) {
                                    deviceToken = instanceIdResult.getToken();
                                }
                            });

                            uID = mAuth.getCurrentUser().getUid();

                            checkUserDriver.child("Users").child("Drivers").child(uID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {

                                        DatabaseReference updateDriverToken = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(uID);
                                        updateDriverToken.child("device_token").setValue(deviceToken);

                                        Intent intent = new Intent(VerifyPhoneNumber.this, NavigationView.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();

                                    } else {

                                        checkUserPassenger.child("Users").child("Passenger").child(uID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {

                                                    DatabaseReference updateDriverToken = FirebaseDatabase.getInstance().getReference().child("Users").child("Passenger").child(uID);
                                                    updateDriverToken.child("device_token").setValue(deviceToken);

                                                    Intent intent = new Intent(VerifyPhoneNumber.this, NavigationView.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    finish();

                                                } else {

                                                    shouldFinish = true;
                                                    Intent intent = new Intent(VerifyPhoneNumber.this, Register.class);
                                                    String transitionName = getString(R.string.register_account_logo_transition);
                                                    ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(VerifyPhoneNumber.this, transitionLogo, transitionName);
                                                    startActivity(intent, transitionActivityOptions.toBundle());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        } else {

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(VerifyPhoneNumber.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
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

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (shouldFinish) {
            finish();
        }
    }
}
