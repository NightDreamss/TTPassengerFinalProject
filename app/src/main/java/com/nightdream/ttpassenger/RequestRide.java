package com.nightdream.ttpassenger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
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
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.aviran.cookiebar2.CookieBar;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;
import static java.util.Objects.requireNonNull;


public class RequestRide extends Fragment implements OnMapReadyCallback, PermissionsListener {

    private static final int REQUEST_PERMISSION_LOCATION = 101;
    private View requestRideView;
    private Button requestbtn;
    private boolean locationValue;
    private Location location, cLocation;
    private String uID, dGeoLocation, cGeoLocation, decrypted_qrCode, sharedQrCode, qr_code_generated_text;
    private TextView name, user_type;
    private ResultReceiver receiver;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    //Mapbox
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    // variables for calculating and drawing a route
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private static final LatLng BOUND_CORNER_NW = new LatLng(10.854214, -60.880062);
    private static final LatLng BOUND_CORNER_SE = new LatLng(10.033263, -61.957491);
    private static final LatLngBounds RESTRICTED_BOUNDS_AREA = new LatLngBounds.Builder().include(BOUND_CORNER_NW).include(BOUND_CORNER_SE).build();
    private ImageView hoveringMarker;
    private static final String DROPPED_MARKER_LAYER_ID = "DROPPED_MARKER_LAYER_ID";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Mapbox.getInstance(getContext(), getString(R.string.mapboxToken));
        requestRideView = inflater.inflate(R.layout.fragment_request_ride, container, false);

        variables();
        databaseVariables();
        return requestRideView;
    }

    private void databaseVariables() {

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        uID = mAuth.getCurrentUser().getUid();

        reference.child("Users").child("Passenger").child(uID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String userName = requireNonNull(dataSnapshot.child("name").getValue()).toString();
                    name.setText(userName);
                    user_type.setText("Passenger");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void variables() {
        name = requestRideView.findViewById(R.id.account_name);
        user_type = requestRideView.findViewById(R.id.account_type);
        requestbtn = requestRideView.findViewById(R.id.requestRide_destination_button);
        mapView = requestRideView.findViewById(R.id.mapView);
        receiver = new AddressResults(new Handler());

        //mapbox
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                //Checking for connection to mapbox when title update
                HttpRequestUtil.setLogEnabled(false);

                // Find user's location on map
                enableLocationComponent(style);

                // Set up restriction for map to prevent user for viewing outside trinidad
                mapboxMap.setLatLngBoundsForCameraTarget(RESTRICTED_BOUNDS_AREA);


                addDestinationIconSymbolLayer(style);

                // Set up hovering marker for user to identify location of marker
                hoveringMarkerPlacement(style);
            }
        });
    }

    private void hoveringMarkerPlacement(Style style) {

        // Variables for initialize hovering marker
        hoveringMarker = new ImageView(getContext());
        hoveringMarker.setImageResource(R.drawable.mapbox_marker_icon_default);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        hoveringMarker.setLayoutParams(params);
        mapView.addView(hoveringMarker);

        // Initialize, but don't show, a SymbolLayer for the marker icon which will represent a selected location.
        initDroppedMarker(style);

        // Button to drop marker to set destination
        requestbtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseRequireInsteadOfGet")
            @Override
            public void onClick(View view) {

                if (hoveringMarker.getVisibility() == View.VISIBLE) {
                    // Use the map target's coordinates to make a reverse geocoding search
                    final LatLng mapTargetLatLng = mapboxMap.getCameraPosition().target;

                    // Hide the hovering red hovering ImageView marker
                    hoveringMarker.setVisibility(View.INVISIBLE);

                    // Transform the appearance of the button to become the cancel button
                    requestbtn.setText(getString(R.string.cancel_destination));

                    // Show the SymbolLayer icon to represent the selected map location
                    if (style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {
                        GeoJsonSource source = style.getSourceAs("dropped-marker-source-id");
                        if (source != null) {
                            source.setGeoJson(Feature.fromGeometry(Point.fromLngLat(
                                    mapTargetLatLng.getLongitude(), mapTargetLatLng.getLatitude())));
                        }
                        requireNonNull(style.getLayer(DROPPED_MARKER_LAYER_ID)).setProperties(visibility(VISIBLE));

                        if (ContextCompat.checkSelfPermission(requireNonNull(getContext()), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        location = new Location("providerA");
                        location.setLatitude(mapTargetLatLng.getLatitude());
                        location.setLongitude(mapTargetLatLng.getLongitude());

                        cLocation = new Location("providerN");
                        assert locationComponent.getLastKnownLocation() != null;
                        cLocation.setLatitude(locationComponent.getLastKnownLocation().getLatitude());
                        cLocation.setLongitude(locationComponent.getLastKnownLocation().getLongitude());

                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            String[] permissionLocation = {Manifest.permission.ACCESS_FINE_LOCATION};

                            requestPermissions(permissionLocation, REQUEST_PERMISSION_LOCATION);
                        } else {
                            locationValue = false;
                            getAddress(location);

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    locationValue = true;
                                    getAddress(cLocation);
                                }
                            }, 3000);
                        }
                    }

                } else {

                    // Switch the button appearance back to select a location.
                    requestbtn.setText(getString(R.string.select_destination));
                    requestbtn.setTextColor(getResources().getColor(R.color.whitePurple));

                    // Show the red hovering ImageView marker
                    hoveringMarker.setVisibility(View.VISIBLE);

                    // Hide the selected location SymbolLayer
                    if (style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {
                        style.getLayer(DROPPED_MARKER_LAYER_ID).setProperties(visibility(NONE));
                        removeRequest();
                    }
                }
            }
        });
    }

    private void removeRequest() {
        reference.child("taxiRequest").orderByChild("status").equalTo("waiting").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String key = child.getKey();
                    String id = child.child("id").getValue().toString();
                    if (id.equals(uID)) {
                        reference.child("taxiRequest").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    CookieBar.build(getActivity()).setCookiePosition(CookieBar.BOTTOM).setTitle("Ride Request").setMessage("Request Canceled").setDuration(3000).show();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // This is the function to call google play services to convert the latitude and longitude from the GPS to a geo location
    private void getAddress(Location location) {
        Intent intent = new Intent(getContext(), TheAddress.class);
        intent.putExtra(Constants.RECEIVER, receiver);
        intent.putExtra(Constants.LOCATION_NAME, location);
        requireActivity().startService(intent);
    }

    //This is the result handler, it will display the results from the function above
    private class AddressResults extends ResultReceiver {
        AddressResults(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == Constants.SUCCESS) {

                if (!locationValue) {
                    dGeoLocation = resultData.getString(Constants.DATA_KEY);
                } else {

                    reference.child("taxiRequest").orderByChild("id").equalTo(uID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {

                                cGeoLocation = resultData.getString(Constants.DATA_KEY);

                                final HashMap<String, Object> sessionMap = new HashMap<>();
                                sessionMap.put("id", uID);
                                sessionMap.put("dLocation", dGeoLocation);
                                sessionMap.put("dlat", String.valueOf(location.getLatitude()));
                                sessionMap.put("dlng", String.valueOf(location.getLongitude()));
                                sessionMap.put("cLocation", cGeoLocation);
                                sessionMap.put("clat", String.valueOf(cLocation.getLatitude()));
                                sessionMap.put("clng", String.valueOf(cLocation.getLongitude()));
                                sessionMap.put("status", "waiting");

                                qr_code_generated_text = generateText(10);

                                reference.child("taxiRequest").child(qr_code_generated_text).updateChildren(sessionMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            reference.child("rideTransaction").child(qr_code_generated_text).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {
                                                        scanQrCode();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    CookieBar.build(getActivity()).setCookiePosition(CookieBar.BOTTOM).setTitle("QR Code").setMessage("Error unable find QR Code").setDuration(3000).show();
                                                }
                                            });
                                        }
                                    }
                                });

                            } else {
                                for (DataSnapshot child : snapshot.getChildren()) {
                                    String statusValue = child.child("status").getValue().toString();
                                    if (statusValue.equals("waiting")) {
                                        CookieBar.build(getActivity()).setCookiePosition(CookieBar.BOTTOM).setTitle("Requesting a ride").setMessage("Ride request already in progress...").setDuration(3000).show();
                                        scanQrCode();
                                    } else {
                                        CookieBar.build(getActivity()).setCookiePosition(CookieBar.BOTTOM).setTitle("Requesting a ride").setMessage("Error unknown").setDuration(3000).show();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            } else {
                Toast.makeText(getContext(), "Please connect to the internet", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String generateText(int len) {
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int x = 0; x < len; x++) {
            char cha = chars[random.nextInt(chars.length)];
            stringBuilder.append(cha);
        }
        return stringBuilder.toString();
    }

    private void scanQrCode() {
        IntentIntegrator.forSupportFragment(RequestRide.this).initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent code) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, code);
        if (result != null) {
            if (result.getContents() == null) {
                CookieBar.build(getActivity()).setCookiePosition(CookieBar.BOTTOM).setTitle("QR Code").setMessage("Cancelled").setDuration(3000).show();
            } else {
                sharedQrCode = result.getContents();
                CookieBar.build(getActivity()).setCookiePosition(CookieBar.BOTTOM).setTitle("QR Code").setMessage("Scanned").setDuration(3000).show();
                compareQRCodes();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, code);
        }
    }

    private void compareQRCodes() {

        if (!sharedQrCode.isEmpty()) {
            String decrypted = "";
            decrypted_qrCode = "";
            try {
                decrypted = AESUtils.decrypt(sharedQrCode);
                decrypted_qrCode = decrypted;
            } catch (Exception e) {
                e.printStackTrace();
            }

            reference.child("rideTransaction").child(qr_code_generated_text).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String qrCodeDecrypted = snapshot.getKey();
                        if (qrCodeDecrypted.equals(decrypted_qrCode)) {
                            final HashMap<String, Object> rideMap = new HashMap<>();
                            rideMap.put("status", "riding");
                            reference.child("rideTransaction").child(qr_code_generated_text).updateChildren(rideMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(getContext(), Riding.class);
                                        startActivity(intent);
                                    }
                                }
                            });
                        } else {
                            CookieBar.build(getActivity()).setCookiePosition(CookieBar.BOTTOM).setTitle("QR Code").setMessage("Invalid QR Code please try again!").setDuration(3000).show();
                        }
                    } else {
                        CookieBar.build(getActivity()).setCookiePosition(CookieBar.BOTTOM).setTitle("Ride Request").setMessage("Ride does not exist!").setDuration(3000).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void initDroppedMarker(@NonNull Style loadedMapStyle) {
        // Add the marker image to map
        loadedMapStyle.addImage("dropped-icon-image", BitmapFactory.decodeResource(
                getResources(), R.drawable.mapbox_marker_icon_default));
        loadedMapStyle.addSource(new GeoJsonSource("dropped-marker-source-id"));
        loadedMapStyle.addLayer(new SymbolLayer(DROPPED_MARKER_LAYER_ID,
                "dropped-marker-source-id").withProperties(
                iconImage("dropped-icon-image"),
                visibility(NONE),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        ));
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {

        loadedMapStyle.addImage(geojsonSourceLayerId, BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));

        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");

        loadedMapStyle.addSource(geoJsonSource);

        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");

        destinationSymbolLayer.withProperties(iconImage("destination-icon-id"), iconAllowOverlap(true), iconIgnorePlacement(true));

        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    @SuppressLint("UseRequireInsteadOfGet")
    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(requireNonNull(getContext()))) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter

            // Get an instance of the component
            locationComponent = mapboxMap.getLocationComponent();

            // Activate with a built LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(getContext(), loadedMapStyle).build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING_GPS);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(getContext(), permissionsToExplain.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(getContext(), "Permissions Required!", Toast.LENGTH_LONG).show();
            requireActivity().finish();
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
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}