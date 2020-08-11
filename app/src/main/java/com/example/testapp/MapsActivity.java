package com.example.testapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.testapp.model.AreaInformation;
import com.example.testapp.service.MapsService;
import com.example.testapp.util.GoogleMapUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getName();

    private GoogleMap mMap;
    private int LOCATION_REQUEST_CODE = 10001;
    FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private Marker thisMarker;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.topviewman);
        Bitmap b = bitmapdraw.getBitmap();
        bitmap = Bitmap.createScaledBitmap(b, 110, 60, false);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
            startLocationUpdates();
        } else {
            askLocationPermission();
        }
        GoogleMapUtil.initialize(this);
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();

        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    Log.d(TAG, "onSuccess: " + location.toString());
                    Log.d(TAG, "Longitude: " + location.getLongitude());
                    Log.d(TAG, "Latitude: " + location.getLatitude());
                }
            }
        });

        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //fail
            }
        });


    }

    private void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d(TAG, "askLocationPermission: you should show an alert dialog....");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET}, LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET}, LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (LOCATION_REQUEST_CODE == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                askLocationPermission();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.d(TAG, "on Location Result: " + locationResult.getLastLocation());
            LatLng latLng = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
            if(mMap != null){

                if(thisMarker != null) {
                    thisMarker.setPosition(latLng);
                    thisMarker.setTitle("Latitude: " + locationResult.getLastLocation().getLatitude() + " Longitude: " + locationResult.getLastLocation().getLongitude());
                }else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                    thisMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Latitude: " + locationResult.getLastLocation().getLatitude() + " Longitude: " + locationResult.getLastLocation().getLongitude()).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                }
            }
        }
    };

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }else {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    /**
     * Scan button click listener
     */
    public void onScanButtonClick(View view) {
        Log.d(TAG, "TODO call web service...");
        // TODO Call web service and parse json
        Button scanButton = findViewById(R.id.scanBtn);
        TextView loadingText = findViewById(R.id.loadingText);
        loadingText.setVisibility(View.VISIBLE);
        scanButton.setEnabled(false);
        MapsService mapsService = new MapsService();
        List<AreaInformation> areaInformations = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        if(thisMarker != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(thisMarker.getPosition(), 18));
            mapsService.execute(thisMarker.getPosition());
            try {
                areaInformations.addAll(mapsService.get());
                Log.d("thisTest", objectMapper.writeValueAsString(areaInformations));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }



        GoogleMapUtil.populateLocationMarkers(mMap, areaInformations);

        AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
        alertDialog.setTitle("Warning");
        alertDialog.setMessage("You are near disease hotspot areas.");
        alertDialog.setIcon(R.drawable.custom_marker);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "my_channel")
                .setSmallIcon(R.drawable.warning_icon)
                .setContentTitle("Warning")
                .setContentText("You are near disease hotspot areas.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId = 12345;
        notificationManager.notify(notificationId, builder.build());

        loadingText.setVisibility(View.INVISIBLE);
        scanButton.setEnabled(true);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "For creating sample notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("my_channel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}