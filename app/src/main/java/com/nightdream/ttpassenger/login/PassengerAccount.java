package com.nightdream.ttpassenger.login;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.nightdream.ttpassenger.R;
import com.nightdream.ttpassenger.RideManagement.NavigationView;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import gun0912.tedbottompicker.TedBottomPicker;
import id.zelory.compressor.Compressor;

public class PassengerAccount extends Fragment {

    private int STORAGE_PERMISSION_CODE = 100;
    private View passengerAccount;
    private Button create_account;
    private CircleImageView profile_image;
    private String phone, username, UncompressedImage, email, password;
    private EditText usernameEditText, emailEditText, passwordEditText, phoneEditText;
    private TextInputLayout usernameInput, profile_image_layout, emailInput, passwordInput, phoneInput;
    private ImageButton back_button;
    private CountryCodePicker ccp;
    private Uri selectedUri;
    private Bitmap bitmap;

    //firebase
    private StorageReference userProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        passengerAccount = inflater.inflate(R.layout.passenger_account, container, false);

        variables();
        editTextFocus();
        bottomSheetGallery();
        createAccount();
        back();

        return passengerAccount;
    }

    private void variables() {
        //variables
        profile_image = passengerAccount.findViewById(R.id.passenger_account_profile_image);
        usernameInput = passengerAccount.findViewById(R.id.passenger_account_username_input_layout);
        usernameEditText = passengerAccount.findViewById(R.id.passenger_account_username_field);
        create_account = passengerAccount.findViewById(R.id.passenger_create_account_button);
        profile_image_layout = passengerAccount.findViewById(R.id.passenger_account_profile_layout);
        back_button = passengerAccount.findViewById(R.id.passenger_account_back_button);
        emailEditText = passengerAccount.findViewById(R.id.passenger_account_email_field);
        emailInput = passengerAccount.findViewById(R.id.passenger_account_email_input);
        passwordEditText = passengerAccount.findViewById(R.id.passenger_account_password_editText);
        passwordInput = passengerAccount.findViewById(R.id.passenger_account_password_input);
        phoneEditText = passengerAccount.findViewById(R.id.passenger_account_phone_field);
        phoneInput = passengerAccount.findViewById(R.id.passenger_account_phone_input);
        ccp = passengerAccount.findViewById(R.id.passenger_account_ccp);
        ccp.registerCarrierNumberEditText(phoneEditText);

        //firebase variables
        mAuth = FirebaseAuth.getInstance();
        userProfileImage = FirebaseStorage.getInstance().getReference().child("Profile Image");
        reference = FirebaseDatabase.getInstance().getReference();
    }

    private void editTextFocus() {

        phoneEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                phoneInput.setHelperText("Eg. 868-123-4567");
            } else {
                phoneInput.setHelperText("");
            }
        });

        emailEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                emailInput.setHelperText("Eg. john@hotmail.com");
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

        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                phoneInput.setHelperText("Eg. 868-123-4567");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailInput.setHelperText("Eg. john@hotmail.com");
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

        usernameEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                usernameInput.setHelperText("Minimum of 3 characters");
            } else {
                usernameInput.setHelperText("");
            }
        });

        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                usernameInput.setHelperText("Minimum of 3 characters");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void createAccount() {
        create_account.setOnClickListener(v -> {
            phone = ccp.getFullNumberWithPlus();
            username = usernameEditText.getText().toString().trim();
            email = emailEditText.getText().toString().trim();
            password = passwordEditText.getText().toString().trim();

            if (!TextUtils.isEmpty(username) && username.length() >= 3) {
                if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (password.length() >= 8) {
                        if (ccp.isValidFullNumber()) {
                            if (!TextUtils.isEmpty(UncompressedImage)) {

                                Uri resultUri = Uri.parse(UncompressedImage);
                                File file = new File(resultUri.getPath());

                                try {
                                    bitmap = new Compressor(requireContext()).setMaxWidth(150).setMaxHeight(150).compressToBitmap(file);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                                final byte[] bytes = byteArrayOutputStream.toByteArray();
                                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity(), task -> {
                                    if (task.isSuccessful()) {

                                        final String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                                        final StorageReference filePath = userProfileImage.child(currentUser + ".jpg");
                                        UploadTask function = filePath.putBytes(bytes);
                                        function.addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                                                    String compressedImage = uri.toString();

                                                    final HashMap<String, Object> passengerMap = new HashMap<>();
                                                    passengerMap.put("name", username);
                                                    passengerMap.put("email", email);
                                                    passengerMap.put("password", password);
                                                    passengerMap.put("image", compressedImage);
                                                    passengerMap.put("userNumber", phone);

                                                    reference.child("Users").child("Drivers").child(currentUser).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.exists()) {
                                                                reference.child("Users").child("Drivers").child(currentUser).removeValue().addOnCompleteListener(task11 -> {
                                                                    if (task11.isSuccessful()) {

                                                                        reference.child("Users").child("Passenger").child(currentUser).updateChildren(passengerMap).addOnCompleteListener(task111 -> {
                                                                            if (task111.isSuccessful()) {

                                                                                Intent intent = new Intent(getContext(), NavigationView.class);
                                                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                startActivity(intent);

                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                            } else {
                                                                reference.child("Users").child("Passenger").child(currentUser).updateChildren(passengerMap).addOnCompleteListener(task11 -> {
                                                                    if (task11.isSuccessful()) {

                                                                        Intent intent = new Intent(getContext(), NavigationView.class);
                                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                        startActivity(intent);

                                                                    }
                                                                });
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        }
                                                    });
                                                });
                                            }
                                        });
                                    } else {

                                        try {
                                            throw Objects.requireNonNull(task.getException());
                                        } catch (FirebaseAuthWeakPasswordException weakPassword) {
                                            Toast.makeText(getContext(), weakPassword.getReason(), Toast.LENGTH_SHORT).show();
                                        } catch (FirebaseAuthUserCollisionException emailExist) {
                                            Toast.makeText(getContext(), emailExist.getEmail(), Toast.LENGTH_SHORT).show();
                                        } catch (Exception invalidCredentials) {
                                            Toast.makeText(getContext(), invalidCredentials.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                profile_image_layout.setError("Please Insert a Profile Picture");
                                profile_image_layout.requestFocus();
                            }
                        } else {
                            phoneInput.setError("Invalid phone number");
                            phoneInput.requestFocus();
                        }
                    } else {
                        passwordInput.setError("Password must contain a 8 characters");
                        passwordInput.requestFocus();
                    }
                } else {
                    emailInput.requestFocus();
                    emailInput.setError("Invalid email address");
                }
            } else {
                usernameInput.requestFocus();
                usernameInput.setError("Requires a minimum of 3 characters");
            }
        });
    }

    private void bottomSheetGallery() {

        profile_image.setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                TedBottomPicker.with(getActivity()).
                        setCameraTileBackgroundResId(R.color.colorAccent)
                        .setGalleryTileBackgroundResId(R.color.colorPrimary)
                        .setPeekHeight(getResources().getDisplayMetrics().heightPixels / 2)
                        .setSelectedUri(selectedUri)
                        .show(uri -> {
                            selectedUri = uri;
                            UncompressedImage = selectedUri.toString();
                            Picasso.get().load(selectedUri).into(profile_image);
                        });

            } else {
                requestStoragePermission();
            }
        });
    }

    private void requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(getContext())
                    .setTitle("Permission Required")
                    .setMessage("Permission is needed to set a profile picture")
                    .setPositiveButton("ok", (dialog, which) -> requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE))
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss()).create().show();

        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(getContext(), "Permission Granted", Toast.LENGTH_LONG).show();

                TedBottomPicker.with(getActivity()).
                        setCameraTileBackgroundResId(R.color.colorAccent)
                        .setGalleryTileBackgroundResId(R.color.colorPrimary)
                        .setPeekHeight(getResources().getDisplayMetrics().heightPixels / 2)
                        .setSelectedUri(selectedUri)
                        .show(uri -> {
                            selectedUri = uri;
                            UncompressedImage = selectedUri.toString();
                            Picasso.get().load(selectedUri).into(profile_image);
                        });

            } else {
                Toast.makeText(getContext(), "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void back() {
        back_button.setOnClickListener(v -> requireActivity().onBackPressed());
    }
}

