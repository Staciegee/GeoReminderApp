package com.example.georeminderapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;

public class MainActivity extends AppCompatActivity {

    private TextView latitudeText, longitudeText;
    private EditText nameInput, radiusInput;
    private Button saveButton;

    private FusedLocationProviderClient fusedLocationClient;
    private GeofencingClient geofencingClient;

    private AppDatabase db;

    private double currentLat = 0.0;
    private double currentLng = 0.0;

    private static final int LOCATION_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔧 Initialize UI
        latitudeText = findViewById(R.id.latitudeText);
        longitudeText = findViewById(R.id.longitudeText);
        nameInput = findViewById(R.id.nameInput);
        radiusInput = findViewById(R.id.radiusInput);
        saveButton = findViewById(R.id.saveButton);

        // 🔧 Initialize services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);

        // 🔧 Database
        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "locations_db")
                .fallbackToDestructiveMigration() // 🔥 IMPORTANT
                .allowMainThreadQueries()
                .build();

        // 🔐 Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    100);
        }

        // 🔐 Location permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            getLocation();
        }

        // ✅ SAVE BUTTON
        saveButton.setOnClickListener(v -> {

            try {
                String name = nameInput.getText().toString().trim();
                String radiusStr = radiusInput.getText().toString();

                if (name.isEmpty() || radiusStr.isEmpty()) {
                    Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                String latText = latitudeText.getText().toString();
                String lngText = longitudeText.getText().toString();

                if (!latText.contains(":") || !lngText.contains(":")) {
                    Toast.makeText(this, "Location not ready yet!", Toast.LENGTH_SHORT).show();
                    return;
                }

                double lat = Double.parseDouble(latText.replace("Latitude: ", ""));
                double lng = Double.parseDouble(lngText.replace("Longitude: ", ""));
                float radius = Float.parseFloat(radiusStr);

                // 🔥 CHECK DUPLICATES

                LocationEntity sameName = db.locationDao().getLocationByName(name);
                LocationEntity sameCoords = db.locationDao().getLocationByCoordinates(lat, lng);

                if (sameName != null || sameCoords != null) {
                    Toast.makeText(this, "Location already saved!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ✅ SAVE
                LocationEntity location = new LocationEntity(lat, lng, name, radius);
                db.locationDao().insert(location);

                addGeofence(location);

                Toast.makeText(this, "Location Saved!", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
    }

    // 📍 GET LOCATION
    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {

                        currentLat = location.getLatitude();
                        currentLng = location.getLongitude();

                        latitudeText.setText("Latitude: " + currentLat);
                        longitudeText.setText("Longitude: " + currentLng);

                    } else {
                        Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 📍 CREATE GEOFENCE
    private Geofence createGeofence(LocationEntity location) {
        return new Geofence.Builder()
                .setRequestId(String.valueOf(location.id))
                .setCircularRegion(
                        location.latitude,
                        location.longitude,
                        location.radius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_DWELL
                )
                .setLoiteringDelay(5000)
                .build();
    }

    // 📍 ADD GEOFENCE
    private void addGeofence(LocationEntity location) {

        Geofence geofence = createGeofence(location);

        GeofencingRequest request = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        geofencingClient.addGeofences(request, getGeofencePendingIntent())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Geofence Added!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Geofence Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // 📍 PENDING INTENT
    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceReceiver.class);

        return PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );
    }

    // 🔐 PERMISSION RESULT
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getLocation();
            }
        }
    }
}