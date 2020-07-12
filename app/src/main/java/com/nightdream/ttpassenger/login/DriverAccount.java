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
import com.nightdream.ttpassenger.RideManagement.NavigationView;
import com.nightdream.ttpassenger.R;
import com.squareup.picasso.Picasso;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import gun0912.tedbottompicker.TedBottomPicker;
import id.zelory.compressor.Compressor;
import jrizani.jrspinner.JRSpinner;

public class DriverAccount extends Fragment implements com.tsongkha.spinnerdatepicker.DatePickerDialog.OnDateSetListener{

    private int STORAGE_PERMISSION_CODE = 100;
    private View driverAccount;
    private Button create_account;
    private CircleImageView profile_image;
    private String phone, username, dob, licence, vehicle, route, deviceToken, UncompressedImage;
    private EditText usernameEditText, dobEditText, licenceEditText;
    private TextInputLayout usernameInput, dobInput, licenceInput, vehicleInput, routeInput, profile_image_layout;
    private JRSpinner jrSpinnerVehicle, jrSpinnerRoute;
    private Uri selectedUri;
    private Bitmap bitmap;

    //firebase
    private StorageReference userProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference driver, passenger;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        driverAccount = inflater.inflate(R.layout.driver_account, container, false);

        variables();
        editTextFocus();
        bottomSheetGallery();
        createAccount();
        date();

        return driverAccount;
    }

    private void variables() {
        //variables
        profile_image = driverAccount.findViewById(R.id.driver_account_profile_image);
        usernameEditText = driverAccount.findViewById(R.id.driver_account_username_editText);
        dobEditText = driverAccount.findViewById(R.id.driver_account_dob_editText);
        licenceEditText = driverAccount.findViewById(R.id.driver_account_licence_editText);
        jrSpinnerVehicle = driverAccount.findViewById(R.id.driver_account_vehicle_field);
        jrSpinnerVehicle.setItems(getResources().getStringArray(R.array.vehicles));
        jrSpinnerRoute = driverAccount.findViewById(R.id.driver_account_association_field);
        jrSpinnerRoute.setItems(getResources().getStringArray(R.array.route));

        //layout variables
        usernameInput = driverAccount.findViewById(R.id.driver_account_username_input_layout);
        dobInput = driverAccount.findViewById(R.id.driver_account_dob_input_layout);
        licenceInput = driverAccount.findViewById(R.id.driver_account_licence_input_layout);
        vehicleInput = driverAccount.findViewById(R.id.driver_account_vehicle_input_layout);
        routeInput = driverAccount.findViewById(R.id.driver_account_association_input_layout);
        create_account = driverAccount.findViewById(R.id.driver_create_account_button);
        profile_image_layout = driverAccount.findViewById(R.id.driver_account_profile_layout);
        vehicleInput.setFocusable(true);
        vehicleInput.setFocusableInTouchMode(true);
        routeInput.setFocusable(true);
        routeInput.setFocusableInTouchMode(true);

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

        licenceEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    licenceInput.setHelperText("Your Trinidad Licence");
                } else {
                    licenceInput.setHelperText("");
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

        dobEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dobInput.setHelperText("");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        licenceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                licenceInput.setHelperText("Your Trinidad Licence");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void date() {
        dobEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showDate(2000, 0, 1, R.style.DatePickerSpinner);

            }
        });
    }

    private void showDate(int year, int monthOfYear, int dayOfMonth, int spinnerTheme) {
        new SpinnerDatePickerDialogBuilder()
                .context(getContext())
                .callback((com.tsongkha.spinnerdatepicker.DatePickerDialog.OnDateSetListener) this)
                .spinnerTheme(spinnerTheme)
                .defaultDate(year, monthOfYear, dayOfMonth)
                .build()
                .show();
    }


    @Override
    public void onDateSet(com.tsongkha.spinnerdatepicker.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        monthOfYear = monthOfYear + 1;
        String results = monthOfYear + "/" + dayOfMonth + "/" + year;
        dobEditText.setText(results);
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
                            .show(uri -> {
                                selectedUri = uri;
                                UncompressedImage = selectedUri.toString();
                                Picasso.get().load(selectedUri).into(profile_image);
                            });
                } else {
                    requestStoragePermission();
                }
            }
        });
    }

    private void createAccount() {
        create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                phone = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
                username = usernameEditText.getText().toString().trim();
                dob = dobEditText.getText().toString();
                licence = licenceEditText.getText().toString().trim();
                vehicle = Objects.requireNonNull(jrSpinnerVehicle.getText()).toString();
                route = Objects.requireNonNull(jrSpinnerRoute.getText()).toString();

                if (!TextUtils.isEmpty(username)) {
                    if (username.length() >= 3) {
                        if (!TextUtils.isEmpty(dob)) {
                            if (!TextUtils.isEmpty(licence)) {
                                if (!TextUtils.isEmpty(vehicle)) {
                                    if (!TextUtils.isEmpty(route)) {

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

                                                                final HashMap<String, Object> driverMap = new HashMap<>();
                                                                driverMap.put("name", username);
                                                                driverMap.put("image", compressedImage);
                                                                driverMap.put("userNumber", phone);
                                                                driverMap.put("licence", licence);
                                                                driverMap.put("dob", dob);
                                                                driverMap.put("vehicle", vehicle);
                                                                driverMap.put("route", route);
                                                                driverMap.put("device_token", deviceToken);

                                                                passenger.child("Users").child("Passenger").child(currentUser).addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        if (dataSnapshot.exists()) {
                                                                            passenger.child("Users").child("Passenger").child(currentUser).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {

                                                                                        driver.child("Users").child("Drivers").child(currentUser).updateChildren(driverMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    driver.child("Disabled").child(currentUser).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                                            Intent intent = new Intent(getContext(), NavigationView.class);
                                                                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                                            startActivity(intent);

                                                                                                        }
                                                                                                    });
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });
                                                                        } else {
                                                                            driver.child("Users").child("Drivers").child(currentUser).updateChildren(driverMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        driver.child("Disabled").child(currentUser).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                Intent intent = new Intent(getContext(), NavigationView.class);
                                                                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                                startActivity(intent);

                                                                                            }
                                                                                        });
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

                                        jrSpinnerRoute.setOnItemClickListener(new JRSpinner.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(int position) {
                                                if (position == 0) {

                                                    routeInput.requestFocus();
                                                    routeInput.setError("Please select a route");
                                                } else {
                                                    routeInput.setHelperText("");
                                                }
                                            }
                                        });
                                    }
                                } else {

                                    jrSpinnerVehicle.setOnItemClickListener(new JRSpinner.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(int position) {
                                            if (position == 0) {

                                                vehicleInput.requestFocus();
                                                vehicleInput.setError("Please select a vehicle type");
                                            } else {
                                                vehicleInput.setHelperText("");
                                            }
                                        }
                                    });

                                }
                            } else {
                                licenceInput.requestFocus();
                                licenceInput.setError("Please enter your licence");
                            }
                        } else {
                            dobInput.setError("Please enter your date of birth");
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
}
