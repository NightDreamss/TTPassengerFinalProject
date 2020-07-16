package com.nightdream.ttpassenger.Notification;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.module.http.HttpRequestUtil;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.nightdream.ttpassenger.R;

import java.util.List;
import java.util.Objects;

public class ShareRideScreen extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    private MapView mapView;
    private String keyId;
    private Button backbtn;

    private PermissionsManager permissionsManager;
    private static final String ID_ICON_PASSENGER_LOCATION = "passengerLocation";
    private static final String ID_ICON_PASSENGER_DESTINATION = "passengerDestination";

    //firebase
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_ride_screen);

        if (getIntent().hasExtra("data")) {
            keyId = getIntent().getStringExtra("data");
        }

        variables();
        back();
    }

    private void back() {
        backbtn.setOnClickListener(v -> {
            onBackPressed();
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {

            //Checking for connection to mapbox when title update
            HttpRequestUtil.setLogEnabled(false);

            // create symbol manager object
            addPassengerLocation(style);
            addPassengerDestination(style);
            SymbolManager passengerLocation = new SymbolManager(mapView, mapboxMap, style);
            SymbolManager passengerDestination = new SymbolManager(mapView, mapboxMap, style);

            passengerLocation.setIconAllowOverlap(true);
            passengerLocation.setIconIgnorePlacement(true);
            passengerDestination.setIconAllowOverlap(true);
            passengerDestination.setIconIgnorePlacement(true);

            Float[] f = {0.f, 1.5f};

            Symbol passengerLocationSymbol = passengerLocation.create(new SymbolOptions()
                    .withLatLng(new LatLng(11.00, -11.00))
                    .withIconImage(ID_ICON_PASSENGER_LOCATION)
                    .withTextField("Ride Location")
                    .withTextOffset(f)
                    .withTextSize(14.f)
                    .withIconSize(0.6f));

            Symbol symbolDestination = passengerDestination.create(new SymbolOptions()
                    .withLatLng(new LatLng(11.00, -11.00))
                    .withIconImage(ID_ICON_PASSENGER_DESTINATION)
                    .withTextField("Destination")
                    .withTextOffset(f)
                    .withTextSize(14.f)
                    .withIconSize(0.08f));

            ShareTracking shareTracking = new ShareTracking(passengerDestination, passengerLocation, reference, keyId, mapboxMap, passengerLocationSymbol, symbolDestination);
            new Thread(shareTracking).start();
        });
    }

    static class ShareTracking extends Thread {
        SymbolManager passengerDestination;
        SymbolManager passengerLocation;
        Symbol passengerLocationSymbol, symbolDestination;
        DatabaseReference reference;
        String keyId;
        MapboxMap mapboxMap;

        public ShareTracking(SymbolManager passengerDestination, SymbolManager passengerLocation, DatabaseReference reference, String keyId, MapboxMap mapboxMap, Symbol passengerLocationSymbol, Symbol symbolDestination) {
            this.passengerDestination = passengerDestination;
            this.passengerLocation = passengerLocation;
            this.reference = reference;
            this.keyId = keyId;
            this.mapboxMap = mapboxMap;
            this.passengerLocationSymbol = passengerLocationSymbol;
            this.symbolDestination = symbolDestination;
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

                            CameraPosition position = new CameraPosition.Builder()
                                    .target(new LatLng(Double.parseDouble(sLat), Double.parseDouble(sLng))) // Sets the new camera position
                                    .zoom(14) // Sets the zoom
                                    .build(); // Creates a CameraPosition from the builder

                            mapboxMap.moveCamera(CameraUpdateFactory
                                    .newCameraPosition(position));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            reference.child("taxiRequest").child(keyId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Object lat = snapshot.child("dlat").getValue();
                        String sLat = String.valueOf(lat);
                        Object lng = snapshot.child("dlng").getValue();
                        String sLng = String.valueOf(lng);

                        symbolDestination.setLatLng(new LatLng(Double.parseDouble(sLat), Double.parseDouble(sLng)));
                        passengerDestination.update(symbolDestination);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void addPassengerDestination(Style style) {
        style.addImage(ID_ICON_PASSENGER_DESTINATION,
                Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_marker, null)));
    }

    private void addPassengerLocation(Style style) {
        style.addImage(ID_ICON_PASSENGER_LOCATION,
                Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_man, null)));

    }

    private void variables() {
        reference = FirebaseDatabase.getInstance().getReference();
        mapView = findViewById(R.id.mapShare);
        mapView.getMapAsync(this);
        backbtn = findViewById(R.id.back_to_main_app_share);
        permissionsManager = new PermissionsManager(this);
        permissionsManager.requestLocationPermissions(this);
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
            Toast.makeText(this, "Shared ride view", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Permissions Required!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

}