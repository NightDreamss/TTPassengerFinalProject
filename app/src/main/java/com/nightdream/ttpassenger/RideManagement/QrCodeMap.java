package com.nightdream.ttpassenger.RideManagement;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.nightdream.ttpassenger.InterfaceModules.AESUtils;
import com.nightdream.ttpassenger.InterfaceModules.QrCodeDownloader;
import com.nightdream.ttpassenger.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class QrCodeMap extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    private static final String ID_ICON_PASSENGER_LOCATION = "passengerLocation";
    private static final String ID_ICON_PASSENGER_DESTINATION = "passengerDestination";
    private ImageView qrCode;
    private String encrypted_qrCode, uID;
    private RequestQueue requestQueue;
    public static String keyId;
    private TextView qrTitle, name, user_type;
    private Button driverSharebtn, driverEmgbtn, driverContactbtn;

    //map
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private static final LatLng BOUND_CORNER_NW = new LatLng(10.854214, -60.880062);
    private static final LatLng BOUND_CORNER_SE = new LatLng(10.033263, -61.957491);
    private static final LatLngBounds RESTRICTED_BOUNDS_AREA = new LatLngBounds.Builder().include(BOUND_CORNER_NW).include(BOUND_CORNER_SE).build();
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 2000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private QrCodeMapLocationCallback callback =
            new QrCodeMapLocationCallback(this);

    //firebase
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapboxToken));
        setContentView(R.layout.activity_qr_code_map);

        variables();
        databaseVariables();
        contactPage();
        buttons();
        checkStatus();

    }

    private void checkStatus() {
        qrTitle.setVisibility(View.VISIBLE);
        qrCode.setVisibility(View.VISIBLE);
        mapView.setVisibility(View.INVISIBLE);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        keyId = bundle.getString("keyId");

        reference.child("taxiRequest").orderByChild("driverId").equalTo(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    for (DataSnapshot child : snapshot.getChildren()) {
                        Object statusValue = child.child("status").getValue();

                        if (String.valueOf(statusValue).equals("accepted")) {
                            qrCodeBuilder();
                        } else if (String.valueOf(statusValue).equals("riding")) {
                            Toast.makeText(QrCodeMap.this, "Resuming Ride", Toast.LENGTH_SHORT).show();
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

    private void buttons() {
        driverEmgbtn.setOnClickListener(v -> {
            int messageType = 1;
            MessageDriverThread messageDriverThread = new MessageDriverThread(messageType);
            new Thread(messageDriverThread).start();
        });

        driverSharebtn.setOnClickListener(v -> {
            int messageType = 0;
            MessageDriverThread messageDriverThread = new MessageDriverThread(messageType);
            new Thread(messageDriverThread).start();
        });
    }


    class MessageDriverThread extends Thread {

        int messageType;

        public MessageDriverThread(int messageType) {
            this.messageType = messageType;
        }

        @Override
        public void run() {
            reference.child("Contacts").child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("MissingPermission")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Object contactId = child.getKey();
                            String s = String.valueOf(contactId);

                            reference.child("Tokens").child(s).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Object tokenId = snapshot.child("token").getValue();
                                        String tokenValueId = String.valueOf(tokenId);
                                        reference.child("taxiRequest").child(keyId).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {

                                                    Object driverId = snapshot.child("driverId").getValue();
                                                    reference.child("Users").child("Drivers").child(String.valueOf(driverId)).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.exists()) {
                                                                Object name = snapshot.child("name").getValue();
                                                                Object number = snapshot.child("userNumber").getValue();
                                                                Object vehicle = snapshot.child("vehicle").getValue();
                                                                Object licence = snapshot.child("licence").getValue();

                                                                reference.child("realTimeTracking").child(keyId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        if (snapshot.exists()) {
                                                                            Object driverLat = snapshot.child("driverLocationLat").getValue();
                                                                            Object driverLng = snapshot.child("driverLocationLng").getValue();
                                                                            Object currentLat = snapshot.child("passengerLocationLat").getValue();
                                                                            Object currentLng = snapshot.child("passengerLocationLng").getValue();

                                                                            String notificationMessage = "Emergency Request \n Driver: " + name + "\n Driver Phonenumber: " + number + "\n Driver Vehicle: " + vehicle + "\n Driver Licence: " + licence + "\n My Location Latitude: " + currentLat + "\n My Location Longitude: " + currentLng + "\n Driver Location Latitude: " + driverLat + "\n Driver Location Longitude: " + driverLng;

                                                                            JSONObject mainObject = new JSONObject();
                                                                            try {
                                                                                if (messageType == 1) {
                                                                                    mainObject.put("to", tokenValueId);

                                                                                    JSONObject jsonObject = new JSONObject();
                                                                                    jsonObject.put("title", "Emergency Message");
                                                                                    jsonObject.put("body", notificationMessage);

                                                                                    mainObject.put("notification", jsonObject);

                                                                                    String URL = "https://fcm.googleapis.com/fcm/send";
                                                                                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, mainObject, response -> Toast.makeText(QrCodeMap.this, "Message Sent", Toast.LENGTH_SHORT).show(), error -> {

                                                                                    }) {
                                                                                        @Override
                                                                                        public Map<String, String> getHeaders() {
                                                                                            Map<String, String> header = new HashMap<>();
                                                                                            header.put("Content-Type", "application/json");
                                                                                            header.put("authorization", "key=AAAAYIUz3UU:APA91bE5EXx3Ixl0F4Jm28A5qv339nra-XfAxEx9xdfdz1eyW0zCJ6EcWrI7zcE_55976xTao4xeecjsZfOAY7HUGGO0wn0mw_B4J7pEfrp177Vpp9UWrRhihKaRNDKnsyFYQjDsDhxO");
                                                                                            return header;
                                                                                        }
                                                                                    };

                                                                                    requestQueue.add(request);
                                                                                } else if (messageType == 0) {
                                                                                    mainObject.put("to", tokenValueId);

                                                                                    JSONObject jsonObject = new JSONObject();
                                                                                    jsonObject.put("title", "Share Ride");
                                                                                    jsonObject.put("body", "Hi, I want to share my ride!");

                                                                                    JSONObject extraObject = new JSONObject();
                                                                                    extraObject.put("key", keyId);

                                                                                    mainObject.put("notification", jsonObject);
                                                                                    mainObject.put("data", extraObject);

                                                                                    String URL = "https://fcm.googleapis.com/fcm/send";
                                                                                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, mainObject, response -> Toast.makeText(QrCodeMap.this, "Message Sent", Toast.LENGTH_SHORT).show(), error -> {

                                                                                    }) {
                                                                                        @Override
                                                                                        public Map<String, String> getHeaders() {
                                                                                            Map<String, String> header = new HashMap<>();
                                                                                            header.put("Content-Type", "application/json");
                                                                                            header.put("authorization", "key=AAAAYIUz3UU:APA91bE5EXx3Ixl0F4Jm28A5qv339nra-XfAxEx9xdfdz1eyW0zCJ6EcWrI7zcE_55976xTao4xeecjsZfOAY7HUGGO0wn0mw_B4J7pEfrp177Vpp9UWrRhihKaRNDKnsyFYQjDsDhxO");
                                                                                            return header;
                                                                                        }
                                                                                    };

                                                                                    requestQueue.add(request);
                                                                                }
                                                                            } catch (JSONException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    } else {
                        String emg = "tel:991";
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse(emg));
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    private void contactPage() {
        driverContactbtn.setOnClickListener(v -> {
            Intent intent = new Intent(QrCodeMap.this, ContactsLayout.class);
            startActivity(intent);
        });
    }

    private void databaseVariables() {
        //firebase variables
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
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

        assert keyId != null;
        if (!keyId.isEmpty()) {
            String encrypted;
            try {
                encrypted = AESUtils.encrypt(keyId);
                encrypted_qrCode = encrypted;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        new QrCodeDownloader(qrCode).execute("https://chart.googleapis.com/chart?cht=qr&chs=500x500&chl=" + encrypted_qrCode + "&chld=H|0");

        reference.child("taxiRequest").child(keyId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    Object driverId = snapshot.child("driverId").getValue();
                    Object statusValue = snapshot.child("status").getValue();

                    if (String.valueOf(statusValue).equals("riding")) {
                        if (String.valueOf(driverId).equals(uID)) {
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
        initLocationEngine();
        checkComplete();
    }

    private void checkComplete() {
        reference.child("taxiRequest").child(keyId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object status = snapshot.child("status").getValue();
                    String statusValue = String.valueOf(status);

                    if (statusValue.equals("completed")) {
                        Intent intent = new Intent(QrCodeMap.this, NavigationView.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private static class QrCodeMapLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<QrCodeMap> activityWeakReference;

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
                String trackLat = String.valueOf(result.getLastLocation().getLatitude());
                String trackLng = String.valueOf(result.getLastLocation().getLongitude());
                HashMap<String, Object> locationMap = new HashMap<>();
                locationMap.put("driverLocationLat", trackLat);
                locationMap.put("driverLocationLng", trackLng);

                String keyId = QrCodeMap.keyId;

                if (keyId != null) {
                    FirebaseDatabase.getInstance().getReference().child("realTimeTracking").child(keyId).updateChildren(locationMap).addOnCompleteListener(Task::isSuccessful);
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
        requestQueue = Volley.newRequestQueue(this);
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


    static class RealTimeTracking extends Thread {

        SymbolManager passengerLocation;
        String uID;
        DatabaseReference reference;
        Symbol passengerLocationSymbol;

        public RealTimeTracking(SymbolManager passengerLocation, Symbol passengerLocationSymbol, String uID, DatabaseReference reference) {
            this.passengerLocation = passengerLocation;
            this.uID = uID;
            this.reference = reference;
            this.passengerLocationSymbol = passengerLocationSymbol;
        }

        @Override
        public void run() {
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
    }

    private void PassengerLocation(SymbolManager passengerDestination, SymbolManager passengerLocation) {

        passengerDestination.setIconAllowOverlap(true);
        passengerDestination.setIconIgnorePlacement(true);

        Float[] f = {0.f, 1.5f};

        Symbol passengerLocationSymbol = passengerLocation.create(new SymbolOptions()
                .withLatLng(new LatLng(11.00, -11.00))
                .withIconImage(ID_ICON_PASSENGER_LOCATION)
                .withTextField("Passenger Location")
                .withTextOffset(f)
                .withTextSize(14.f)
                .withIconSize(0.6f));

        RealTimeTracking realTimeTracking = new RealTimeTracking(passengerLocation, passengerLocationSymbol, uID, reference);
        new Thread(realTimeTracking).start();

        reference.child("taxiRequest").child(keyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object lat = snapshot.child("dlat").getValue();
                    String sLat = String.valueOf(lat);
                    Object lng = snapshot.child("dlng").getValue();
                    String sLng = String.valueOf(lng);

                    passengerDestination.create(new SymbolOptions()
                            .withLatLng(new LatLng(Double.parseDouble(sLat), Double.parseDouble(sLng)))
                            .withIconImage(ID_ICON_PASSENGER_DESTINATION)
                            .withTextField("Destination")
                            .withTextOffset(f)
                            .withTextSize(14.f)
                            .withIconSize(0.08f));
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
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate with a built LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING_GPS);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

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