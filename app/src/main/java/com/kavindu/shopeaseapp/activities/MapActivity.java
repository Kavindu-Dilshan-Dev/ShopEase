package com.kavindu.shopeaseapp.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.kavindu.shopeaseapp.R;
import com.kavindu.shopeaseapp.databinding.ActivityMapBinding;
import com.kavindu.shopeaseapp.utils.DirectionsParser;

import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_CODE = 101;
    private ActivityMapBinding binding;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedClient;
    private LatLng currentLocation;
    private Polyline currentRoute;

    // Predefined store locations
    private final List<StoreLocation> stores = Arrays.asList(
            new StoreLocation("ShopEase Colombo", new LatLng(6.9271, 79.8612)),
            new StoreLocation("ShopEase Kandy",   new LatLng(7.2906, 80.6337)),
            new StoreLocation("ShopEase Galle",   new LatLng(6.0535, 80.2210)),
            new StoreLocation("ShopEase Negombo", new LatLng(7.2083, 79.8358))
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        binding.btnMyLocation.setOnClickListener(v -> moveToCurrentLocation());
        binding.btnNearestStore.setOnClickListener(v -> findNearestStore());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Add store markers
        for (StoreLocation store : stores) {
            googleMap.addMarker(new MarkerOptions()
                    .position(store.latLng)
                    .title(store.name)
                    .snippet("Tap for directions")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
        }

        // Marker click → get directions
        googleMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            if (currentLocation != null)
                getDirections(currentLocation, marker.getPosition(), marker.getTitle());
            return true;
        });

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        googleMap.setMyLocationEnabled(true);
        moveToCurrentLocation();
    }

    private void moveToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14f));

                googleMap.addMarker(new MarkerOptions()
                        .position(currentLocation)
                        .title("You are here")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void findNearestStore() {
        if (currentLocation == null) {
            Toast.makeText(this, "Getting your location...", Toast.LENGTH_SHORT).show();
            moveToCurrentLocation(); return;
        }
        StoreLocation nearest = null;
        float minDist = Float.MAX_VALUE;
        for (StoreLocation store : stores) {
            float[] result = new float[1];
            Location.distanceBetween(
                    currentLocation.latitude, currentLocation.longitude,
                    store.latLng.latitude, store.latLng.longitude, result);
            if (result[0] < minDist) { minDist = result[0]; nearest = store; }
        }
        if (nearest != null) {
            getDirections(currentLocation, nearest.latLng, nearest.name);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nearest.latLng, 14f));
            Toast.makeText(this, "Nearest: " + nearest.name + " (" +
                    String.format("%.1f", minDist / 1000) + " km)", Toast.LENGTH_LONG).show();
        }
    }

    private void getDirections(LatLng origin, LatLng dest, String destName) {
        String apiKey = getString(R.string.google_maps_key);
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + dest.latitude + "," + dest.longitude +
                "&mode=driving&key=" + apiKey;

        new FetchDirectionsTask(destName).execute(url);
    }

    private class FetchDirectionsTask extends AsyncTask<String, Void, String> {
        private final String destName;
        FetchDirectionsTask(String destName) { this.destName = destName; }

        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                return sb.toString();
            } catch (Exception e) { return null; }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(MapActivity.this, "Directions unavailable", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject json = new JSONObject(result);
                List<LatLng> points = DirectionsParser.parse(json);
                if (currentRoute != null) currentRoute.remove();

                PolylineOptions opts = new PolylineOptions()
                        .addAll(points)
                        .width(8f)
                        .color(Color.parseColor("#6200EE"))
                        .geodesic(true);
                currentRoute = googleMap.addPolyline(opts);

                // Fit map to show full route
                if (!points.isEmpty()) {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (LatLng p : points) builder.include(p);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
                }

                binding.tvDirectionInfo.setText("Directions to: " + destName);
            } catch (Exception e) {
                Toast.makeText(MapActivity.this, "Error parsing route", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] perms,
                                           @NonNull int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code == LOCATION_PERMISSION_CODE && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }

    static class StoreLocation {
        String name; LatLng latLng;
        StoreLocation(String n, LatLng l) { name = n; latLng = l; }
    }
}