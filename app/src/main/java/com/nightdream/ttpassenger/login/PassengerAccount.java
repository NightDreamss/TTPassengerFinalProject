package com.nightdream.ttpassenger.login;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nightdream.ttpassenger.NavigationView;
import com.nightdream.ttpassenger.R;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.TedBottomSheetDialogFragment;
import id.zelory.compressor.Compressor;

public class PassengerAccount extends Fragment {

    private int STORAGE_PERMISSION_CODE = 100;
    private View passengerAccount;
    private Button create_account;
    private CircleImageView profile_image;
    private String phone, username, deviceToken, UncompressedImage;
    private EditText usernameEditText;
    private TextInputLayout usernameInput, profile_image_layout;
    private Uri selectedUri;
    private Bitmap bitmap;

    //firebase
    private StorageReference userProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference driver, passenger;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        passengerAccount = inflater.inflate(R.layout.passenger_account, container, false);

        variables();
        editTextFocus();
        bottomSheetGallery();
        createAccount();

        return passengerAccount;
    }

    private void variables() {
        //variables
        profile_image = passengerAccount.findViewById(R.id.passenger_account_profile_image);
        usernameInput = passengerAccount.findViewById(R.id.passenger_account_username_input_layout);
        usernameEditText = passengerAccount.findViewById(R.id.passenger_account_username_field);
        create_account = passengerAccount.findViewById(R.id.passenger_create_account_button);
        profile_image_layout = passengerAccount.findViewById(R.id.passenger_account_profile_layout);

        //firebase variables
        mAuth = FirebaseAuth.getInstance();
        userProfileImage = FirebaseStorage.getInstance().getReference().child("Profile Image");
        driver = FirebaseDatabase.getInstance().getReference();
        passenger = FirebaseDatabase.getInstance().getReference();

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                deviceToken = instanceIdResult.getToken();
            }
        });
    }

    private void editTextFocus() {

        usernameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    usernameInput.setHelperText("Minimum of 3 characters");
                } else {
                    usernameInput.setHelperText("");
                }
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
        create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                phone = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
                username = usernameEditText.getText().toString().trim();

                if (!TextUtils.isEmpty(username)) {
                    if (username.length() >= 3) {

                        final String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

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
                            final StorageReference filePath = userProfileImage.child(currentUser + ".jpg");
                            UploadTask function = filePath.putBytes(bytes);

                            function.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String compressedImage = uri.toString();

                                                final HashMap<String, Object> passengerMap = new HashMap<>();
                                                passengerMap.put("name", username);
                                                passengerMap.put("image", compressedImage);
                                                passengerMap.put("userNumber", phone);
                                                passengerMap.put("device_token", deviceToken);

                                                driver.child("Users").child("Drivers").child(currentUser).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            driver.child("Users").child("Drivers").child(currentUser).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {

                                                                        passenger.child("Users").child("Passenger").child(currentUser).updateChildren(passengerMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {

                                                                                    Intent intent = new Intent(getContext(), NavigationView.class);
                                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                    startActivity(intent);

                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        } else {
                                                            passenger.child("Users").child("Passenger").child(currentUser).updateChildren(passengerMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {

                                                                        Intent intent = new Intent(getContext(), NavigationView.class);
                                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                        startActivity(intent);

                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                            });
                        } else {
                            profile_image_layout.setError("Please Insert a Profile Picture");
                            profile_image_layout.requestFocus();
                        }

                    } else {
                        usernameInput.requestFocus();
                        usernameInput.setError("Requires a minimum of 3 characters");
                    }
                } else {
                    usernameInput.requestFocus();
                    usernameInput.setError("Please enter a username");
                }

            }
        });
    }

    private void bottomSheetGallery() {

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    TedBottomPicker.with(getActivity()).
                            setCameraTileBackgroundResId(R.color.colorAccent)
                            .setGalleryTileBackgroundResId(R.color.colorPrimary)
                            .setPeekHeight(getResources().getDisplayMetrics().heightPixels / 2)
                            .setSelectedUri(selectedUri)
                            .show(new TedBottomSheetDialogFragment.OnImageSelectedListener() {
                                @Override
                                public void onImageSelected(Uri uri) {
                                    selectedUri = uri;
                                    UncompressedImage = selectedUri.toString();
                                    Picasso.get().load(selectedUri).into(profile_image);
                                }
                            });

                } else {
                    requestStoragePermission();
                }
            }
        });
    }

    private void requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(getContext())
                    .setTitle("Permission Required")
                    .setMessage("Permission is needed to set a profile picture")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();

        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(getContext(), "Permission Granted", Toast.LENGTH_LONG).show();

                TedBottomPicker.with(getActivity()).
                        setCameraTileBackgroundResId(R.color.colorAccent)
                        .setGalleryTileBackgroundResId(R.color.colorPrimary)
                        .setPeekHeight(getResources().getDisplayMetrics().heightPixels / 2)
                        .setSelectedUri(selectedUri)
                        .show(new TedBottomSheetDialogFragment.OnImageSelectedListener() {
                            @Override
                            public void onImageSelected(Uri uri) {
                                selectedUri = uri;
                                UncompressedImage = selectedUri.toString();
                                Picasso.get().load(selectedUri).into(profile_image);
                            }
                        });

            } else {
                Toast.makeText(getContext(), "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}

