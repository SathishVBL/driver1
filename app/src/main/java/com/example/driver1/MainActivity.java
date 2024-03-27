package com.example.driver1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import android.os.Handler;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.driver1.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    Handler handler;
    public String actual_route_name;
    public String actual_busID;
    private Location previousLocation;
    public float MIN_DISTANCE_THRESHOLD=10;

    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) AutoCompleteTextView Eroute_name=findViewById(R.id.enterRouteID);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) AutoCompleteTextView Ebus_ID=findViewById(R.id.enterBusID);

        String[] route_names={

                "Vellore to Amrithi", "Vellore to Anaicut","Vellore to Arni","Vellore to Adukkamparai","Vellore to Arcot","Vellore to Gudiyatham","Katpadi to Bagayam"
        };
        String[] bus_names={"bus1","bus2"};


//        adapter for bus and route names
        ArrayAdapter<String> adap_route_names=new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,route_names);
        ArrayAdapter<String> adap_busID=new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,bus_names);
        Eroute_name.setAdapter(adap_route_names);
        Ebus_ID.setAdapter(adap_busID);





//        btn to load
        View btn=findViewById(R.id.BtnStart);
        btn.setOnClickListener(View->{

            actual_route_name=Eroute_name.getText().toString();
            actual_busID=Ebus_ID.getText().toString();

            firebaseAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference(actual_route_name);
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


            // Check for location permissions
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                // Start location updates
                handler= new Handler();
                fetchLocationPeriodically();
            }
        });

    }

    private void fetchLocationPeriodically() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getLocation();
                handler.postDelayed(this, 10 * 1000); // 10 seconds
            }
        }, 0); // Immediately fetch location on start
    }


    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            locationRequest.setInterval(15 * 1000); // Update location every 10 seconds
            locationRequest.setFastestInterval(10 * 1000);

            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if (locationResult != null) {
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            saveLocationToFirebase(latitude, longitude);
                            Toast.makeText(MainActivity.this, "Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            };

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    private void saveLocationToFirebase(double latitude, double longitude) {
        // Save location data to Firebase Realtime Database under the "drivers" node
        DatabaseReference mydata= databaseReference.child(actual_busID);

//        this is to set the current loction lat and lan
        mydata.child("lat").setValue(latitude);
        mydata.child("lan").setValue(longitude);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}