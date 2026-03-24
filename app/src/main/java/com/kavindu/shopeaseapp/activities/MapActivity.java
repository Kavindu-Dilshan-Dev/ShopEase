package com.kavindu.shopeaseapp.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.kavindu.shopeaseapp.R;
import com.kavindu.shopeaseapp.databinding.ActivityMapBinding;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private ActivityMapBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedClient;
    private Marker myLocationMarker;

    private static final int LOCATION_PERMISSION_CODE = 101;

    private static final List<StoreLocation> STORES =
            new ArrayList<StoreLocation>() {{
                add(new StoreLocation(
                        "ShopEase Colombo",
                        "No 1, Galle Road, Colombo 03",
                        6.9271, 79.8612));
                add(new StoreLocation(
                        "ShopEase Kandy",
                        "No 15, Peradeniya Road, Kandy",
                        7.2906, 80.6337));
                add(new StoreLocation(
                        "ShopEase Galle",
                        "No 8, Matara Road, Galle",
                        6.0535, 80.2210));
                add(new StoreLocation(
                        "ShopEase Negombo",
                        "No 22, Main Street, Negombo",
                        7.2008, 79.8738));
                add(new StoreLocation(
                        "ShopEase Jaffna",
                        "No 5, Hospital Road, Jaffna",
                        9.6615, 80.0255));
            }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(
                getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(true);
            getSupportActionBar()
                    .setTitle("Store Locator");
        }

        // Setup location client
        fusedClient = LocationServices
                .getFusedLocationProviderClient(this);

        // Initialize map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // FAB click go to my location
        binding.fabMyLocation.setOnClickListener(
                v -> moveToMyLocation());
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;


        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);


        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);


        addStoreMarkers();


        LatLng sriLanka = new LatLng(7.8731, 80.7718);
        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                        sriLanka, 7.5f));


        enableMyLocationLayer();


        mMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();


            for (StoreLocation store : STORES) {
                if (store.name.equals(marker.getTitle())) {
                    showNearestStoreCard(store, null);
                    break;
                }
            }
            return false;
        });


        mMap.setOnMapClickListener(latLng ->
                binding.cardNearestStore
                        .setVisibility(View.GONE));
    }


    private void addStoreMarkers() {
        for (StoreLocation store : STORES) {
            LatLng pos = new LatLng(
                    store.lat, store.lng);

            // Purple marker for store
            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(store.name)
                    .snippet(store.address)
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(
                                    BitmapDescriptorFactory.HUE_VIOLET)));

            // Purple circle around store (500m radius)
            mMap.addCircle(new CircleOptions()
                    .center(pos)
                    .radius(500)
                    .strokeColor(
                            Color.argb(180, 108, 32, 217))
                    .fillColor(
                            Color.argb(30, 108, 32, 217))
                    .strokeWidth(2f));
        }
    }


    private void enableMyLocationLayer() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_CODE);
        }
    }


    private void moveToMyLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
            return;
        }

        fusedClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng myPos = new LatLng(
                                location.getLatitude(),
                                location.getLongitude());

                        // Remove old marker
                        if (myLocationMarker != null)
                            myLocationMarker.remove();

                        // Add blue marker for user
                        myLocationMarker = mMap.addMarker(
                                new MarkerOptions()
                                        .position(myPos)
                                        .title("📍 You are here")
                                        .icon(BitmapDescriptorFactory
                                                .defaultMarker(
                                                        BitmapDescriptorFactory
                                                                .HUE_AZURE)));

                        // Animate to my location
                        mMap.animateCamera(
                                CameraUpdateFactory
                                        .newLatLngZoom(myPos, 13f));

                        // Find and show nearest store
                        findNearestStore(
                                location.getLatitude(),
                                location.getLongitude());

                    } else {
                        Toast.makeText(this,
                                "Could not get your location. " +
                                        "Make sure GPS is on.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Location error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }


    private void findNearestStore(
            double myLat, double myLng) {

        StoreLocation nearest = null;
        double minDist = Double.MAX_VALUE;

        for (StoreLocation store : STORES) {
            double dist = calculateDistance(
                    myLat, myLng, store.lat, store.lng);
            if (dist < minDist) {
                minDist = dist;
                nearest = store;
            }
        }

        if (nearest != null) {
            showNearestStoreCard(nearest, minDist);
            drawLineTo(myLat, myLng,
                    nearest.lat, nearest.lng);
        }
    }


    private void showNearestStoreCard(
            StoreLocation store, Double distKm) {

        binding.tvNearestName.setText(store.name);
        binding.tvNearestAddress.setText(store.address);

        if (distKm != null) {
            binding.tvNearestDistance.setText(
                    String.format("%.1f km", distKm));
        } else {
            binding.tvNearestDistance.setText("");
        }

        binding.cardNearestStore
                .setVisibility(View.VISIBLE);
    }


    private void drawLineTo(double myLat, double myLng,
                            double storeLat,
                            double storeLng) {
        mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(myLat, myLng))
                .add(new LatLng(storeLat, storeLng))
                .width(5f)
                .color(Color.argb(180, 108, 32, 217))
                .geodesic(true));
    }


    private double calculateDistance(
            double lat1, double lng1,
            double lat2, double lng2) {
        final double R = 6371; // Earth radius km
        double dLat    = Math.toRadians(lat2 - lat1);
        double dLng    = Math.toRadians(lng2 - lng1);
        double a       =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(Math.toRadians(lat1)) *
                                Math.cos(Math.toRadians(lat2)) *
                                Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(
                Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {
                enableMyLocationLayer();
                moveToMyLocation();
            } else {
                Toast.makeText(this,
                        "Location permission denied. " +
                                "Cannot show your location.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


    static class StoreLocation {
        String name, address;
        double lat, lng;

        StoreLocation(String name, String address,
                      double lat, double lng) {
            this.name    = name;
            this.address = address;
            this.lat     = lat;
            this.lng     = lng;
        }
    }
}