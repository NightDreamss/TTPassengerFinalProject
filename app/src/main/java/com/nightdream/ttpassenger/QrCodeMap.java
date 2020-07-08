package com.nightdream.ttpassenger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.module.http.HttpRequestUtil;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.nightdream.ttpassenger.Contacts.ContactsLayout;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class QrCodeMap extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    private static final String ID_ICON_PASSENGER_LOCATION = "passengerLocation";
    private static final String ID_ICON_PASSENGER_DESTINATION = "passengerDestination";
    private ImageView qrCode;
    private String encrypted_qrCode, uID;
    public static String keyId;
    private TextView qrTitle, name, user_type;
    private Button driverSharebtn, driverEmgbtn, driverContactbtn;
    private Symbol passengerLocationSymbol;

    //map
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private LocationEngine locationEngine;
    private static final LatLng BOUND_CORNER_NW = new LatLng(10.854214, -60.880062);
    private static final LatLng BOUND_CORNER_SE = new LatLng(10.033263, -61.957491);
    private static final LatLngBounds RESTRICTED_BOUNDS_AREA = new LatLngBounds.Builder().include(BOUND_CORNER_NW).include(BOUND_CORNER_SE).build();
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private QrCodeMapLocationCallback callback =
            new QrCodeMapLocationCallback(this);

    //firebase
    private DatabaseReference reference;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapboxToken));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Make to run your application only in portrait mode
        setContentView(R.layout.activity_qr_code_map);

        variables();
        databaseVariables();
        contactPage();
        qrCodeBuilder();
    }

    private void contactPage() {
        driverContactbtn.setOnClickListener(v -> {
            Intent intent = new Intent(QrCodeMap.this, ContactsLayout.class);
            startActivity(intent);
        });
    }

    private void databaseVariables() {
        //firebase variables
        mAuth = FirebaseAuth.getInstance();
        uID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        reference = FirebaseDatabase.getInstance().getReference();

        reference.child("Users").child("Drivers").child(uID).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    Object userName = dataSnapshot.child("name").getValue();
                    name.setText(String.valueOf(userName));
                    user_type.setText("Driver");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void qrCodeBuilder() {
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        keyId = bundle.getString("keyId");

        assert keyId != null;
        if (!keyId.isEmpty()) {
            String encrypted ="";
            try {
                encrypted = AESUtils.encrypt(keyId);
                encrypted_qrCode = encrypted;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        qrTitle.setVisibility(View.VISIBLE);
        qrCode.setVisibility(View.VISIBLE);
        mapView.setVisibility(View.INVISIBLE);
        new QrCodeDownloader(qrCode).execute("https://chart.googleapis.com/chart?cht=qr&chs=500x500&chl=" + encrypted_qrCode + "&chld=H|0");

        reference.child("taxiRequest").child(keyId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    Object driverId = snapshot.child("driverId").getValue();
                    Object statusValue = snapshot.child("status").getValue();

                    if (String.valueOf(statusValue).equals("riding")) {
                        String.valueOf(statusValue);
                        if (String.valueOf(driverId).equals(uID)) {
                            String.valueOf(driverId);
                            mapData();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    private void mapData() {

        qrTitle.setVisibility(View.INVISIBLE);
        qrCode.setVisibility(View.INVISIBLE);
        mapView.setVisibility(View.VISIBLE);
    }

    private static class QrCodeMapLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<QrCodeMap> activityWeakReference;
        private String trackLat, trackLng;

        QrCodeMapLocationCallback(QrCodeMap activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            QrCodeMap activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }

                // Displays the new location's coordinates
                trackLat = String.valueOf(result.getLastLocation().getLatitude());
                trackLng = String.valueOf(result.getLastLocation().getLongitude());
                HashMap<String, Object> locationMap = new HashMap<>();
                locationMap.put("driverLocationLat", trackLat);
                locationMap.put("driverLocationLng", trackLng);

                String keyId = QrCodeMap.keyId;

                if (keyId != null) {
                    FirebaseDatabase.getInstance().getReference().child("realTimeTracking").child(keyId).updateChildren(locationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                            }
                        }
                    });
                }

                // Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can't be captured
         *
         * @param exception the exception message
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            QrCodeMap activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void variables() {
        qrTitle = findViewById(R.id.qr_code_header);
        qrCode = findViewById(R.id.qr_Code);
        mapView = findViewById(R.id.mapQrCode);
        driverSharebtn = findViewById(R.id.driver_share_button);
        driverEmgbtn = findViewById(R.id.driver_help_button);
        driverContactbtn = findViewById(R.id.qr_code_contacts_button);
        name = findViewById(R.id.account_name);
        user_type = findViewById(R.id.account_type);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {

            //Checking for connection to mapbox when title update
            HttpRequestUtil.setLogEnabled(false);

            // Find user's location on map
            enableLocationComponent(style);

            // Set up restriction for map to prevent user for viewing outside trinidad
            mapboxMap.setLatLngBoundsForCameraTarget(RESTRICTED_BOUNDS_AREA);

            // create symbol manager object
            addPassengerLocation(style);
            addPassengerDestination(style);
            SymbolManager passengerLocation = new SymbolManager(mapView, mapboxMap, style);
            SymbolManager passengerDestination = new SymbolManager(mapView, mapboxMap, style);
            PassengerLocation(passengerDestination, passengerLocation);

            passengerLocationSymbol = passengerLocation.create(new SymbolOptions()
                    .withLatLng(new LatLng(11.00, -11.00))
                    .withIconImage(ID_ICON_PASSENGER_LOCATION)
                    .withIconSize(0.6f));

            passengerLocation.setIconAllowOverlap(true);
            passengerLocation.setIconIgnorePlacement(true);

        });
    }

    private void addPassengerDestination(Style style) {
        style.addImage(ID_ICON_PASSENGER_DESTINATION,
                Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_marker, null)));
    }

    private void addPassengerLocation(Style style) {
        style.addImage(ID_ICON_PASSENGER_LOCATION,
                Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_man, null)));

    }

    private void PassengerLocation( SymbolManager passengerDestination, SymbolManager passengerLocation) {

        passengerDestination.setIconAllowOverlap(true);
        passengerDestination.setIconIgnorePlacement(true);

        reference.child("taxiRequest").child(keyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Object lat = snapshot.child("dlat").getValue();
                    String sLat = String.valueOf(lat);
                    Object lng = snapshot.child("dlng").getValue();
                    String sLng = String.valueOf(lng);

                    Symbol symbol = passengerDestination.create(new SymbolOptions()
                            .withLatLng(new LatLng(Double.parseDouble(sLat), Double.parseDouble(sLng)))
                            .withIconImage(ID_ICON_PASSENGER_DESTINATION)
                            .withIconSize(0.08f));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference.child("realTimeTracking").child(keyId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("passengerLocationLat").exists()) {
                        Object lat = snapshot.child("passengerLocationLat").getValue();
                        String sLat = String.valueOf(lat);
                        Object lng = snapshot.child("passengerLocationLng").getValue();
                        String sLng = String.valueOf(lng);

                        passengerLocationSymbol.setLatLng(new LatLng(Double.parseDouble(sLat), Double.parseDouble(sLng)));
                        passengerLocation.update(passengerLocationSymbol);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @SuppressLint("UseRequireInsteadOfGet")
    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter

            // Get an instance of the component
            locationComponent = mapboxMap.getLocationComponent();

            // Activate with a built LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING_GPS);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, permissionsToExplain.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(this::enableLocationComponent);
        } else {
            Toast.makeText(this, "Permissions Required!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}