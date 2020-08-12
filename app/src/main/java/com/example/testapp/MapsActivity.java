package com.example.testapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.testapp.model.AreaInformation;
import com.example.testapp.model.LocationRequestModel;
import com.example.testapp.model.ZoomAndDistanceModel;
import com.example.testapp.service.MapsService;
import com.example.testapp.util.DatabaseHelper;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getName();

    private GoogleMap mMap;
    private int LOCATION_REQUEST_CODE = 10001;
    FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private Marker thisMarker;
    Bitmap bitmap;
    private static final Map<String, Integer> DISTANCE_THRESHOLD_MAPPING = new HashMap<>();
    private DatabaseHelper db;
    private Drawer navDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(this);

        SQLiteDatabase database = db.getWritableDatabase();
        db.onUpgrade(database,0,0);
        //show db data
        Cursor cursor = database.rawQuery("SELECT*FROM LOCATION_HISTORY WHERE CREATED_DATE > datetime('now','-15 day')", null);
        while(cursor.moveToNext()){
            Log.d("ID", cursor.getString(0));
            Log.d("Longitude", ""+cursor.getDouble(1));
            Log.d("Latitude", ""+cursor.getDouble(2));
            Log.d("DISTANCE", cursor.getString(3));
            Log.d("CREATED_DATE", cursor.getString(4));
            Log.d("RESPONSE", cursor.getString(5));
        }



        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.usericon);
        Bitmap b = bitmapdraw.getBitmap();
        bitmap = Bitmap.createScaledBitmap(b, 60, 100, false);

        new DrawerBuilder().withActivity(this).build();
        //if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Map");
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName("Location History");

        //create the drawer
        navDrawer = new DrawerBuilder()
                .withActivity(this)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
//                        setContentView(R.layout.location_history);
                        return false;
                    }
                })
                .build();
    }

    private void addData(double longitude, double latitude, int distance, Date date, String response){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        boolean isInserted = db.insertData(longitude, latitude, distance, df.format(date), response);
        if(isInserted){
            Log.d("addData", "DATA INSERTED");
        } else {
            Log.d("addData", "DATA NOT INSERTED");
        }
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
        initializeThresholdDropdown();
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
                    thisMarker.setTitle("You are here");
                }else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                    thisMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("You are here").icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
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
        Spinner spinner = findViewById(R.id.thresholdDropdown);
        ZoomAndDistanceModel zoomAndDistanceModel = getDistance(spinner.getSelectedItem().toString());

        loadingText.setVisibility(View.VISIBLE);
        scanButton.setEnabled(false);
        MapsService mapsService = new MapsService();
        List<AreaInformation> areaInformations = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        if(thisMarker != null) {
            LocationRequestModel locationRequestModel = new LocationRequestModel(thisMarker.getPosition(), zoomAndDistanceModel.getDistance());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(thisMarker.getPosition(), zoomAndDistanceModel.getZoom()));
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(thisMarker.getPosition());
            circleOptions.strokeWidth(4);
            circleOptions.strokeColor(Color.argb(255, 0, 255 , 0));
            circleOptions.fillColor(Color.argb(32, 0, 255 , 0));
            circleOptions.radius(zoomAndDistanceModel.getDistance());
            mMap.addCircle(circleOptions);
            mapsService.execute(locationRequestModel);
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



        int numAreas = GoogleMapUtil.populateLocationMarkers(mMap, areaInformations);

        AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        if(numAreas > 0) {
            //alert
            alertDialog.setIcon(R.drawable.custom_marker);
            alertDialog.setTitle("Warning");
            alertDialog.setMessage("There are " + numAreas + " hotspot(s) within " + zoomAndDistanceModel.getDistance() + " meters. ");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            //notifications
            createNotificationChannel();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "my_channel")
                    .setSmallIcon(R.drawable.warning_icon)
                    .setContentTitle("Warning")
                    .setContentText("You are near disease hotspot areas.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            int notificationId = 12345;
            notificationManager.notify(notificationId, builder.build());
        } else {
            alertDialog.setMessage("You are safe within "+ zoomAndDistanceModel.getDistance() +" meters.");
            alertDialog.show();
        }


        try {
            addData(thisMarker.getPosition().longitude, thisMarker.getPosition().latitude, zoomAndDistanceModel.getDistance(), new Date(), objectMapper.writeValueAsString(areaInformations));
        } catch (JsonProcessingException e) {
            Log.d("onScanButtonClick", "PARSING OF CLOB FAILED");
        }
        loadingText.setVisibility(View.INVISIBLE);
        scanButton.setEnabled(true);
    }

    private ZoomAndDistanceModel getDistance(String dropDownText){
        ZoomAndDistanceModel zoomAndDistanceModel = null;
        if(dropDownText.equalsIgnoreCase("500 m")){
            zoomAndDistanceModel = new ZoomAndDistanceModel(500, 16);
        } else if (dropDownText.equalsIgnoreCase("1 km")){
            zoomAndDistanceModel = new ZoomAndDistanceModel(1000, 15f);
        } else if (dropDownText.equalsIgnoreCase("2 km")){
            zoomAndDistanceModel = new ZoomAndDistanceModel(2000, 14f);
        } else if (dropDownText.equalsIgnoreCase("3 km")){
            zoomAndDistanceModel = new ZoomAndDistanceModel(3000, 13.3f);
        } else if (dropDownText.equalsIgnoreCase("4 km")){
            zoomAndDistanceModel = new ZoomAndDistanceModel(4000, 13f);
        } else if (dropDownText.equalsIgnoreCase("5 km")){
            zoomAndDistanceModel = new ZoomAndDistanceModel(5000, 12.6f);
        }
        return zoomAndDistanceModel;
    }



    /**
     * Exit button click listener
     */
    /*public void onExitButtonClick(View view) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }*/


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

    private void initializeDropdownValueMapping() {
        String[] dropdownValues = getResources().getStringArray(R.array.threshold_options);
        for (String displayValue : dropdownValues) {
            int valueInMeters = 0;
            String cleanString = null;
            final String KM = "km";
            final String M = "m";
            if (displayValue.contains(KM)) {
                cleanString = displayValue.replace(KM, "").trim();
                valueInMeters = Integer.parseInt(cleanString) * 1000;
            } else if (displayValue.contains(M)) {
                cleanString = displayValue.replace(M, "").trim();
                valueInMeters = Integer.parseInt(cleanString);
            }
            DISTANCE_THRESHOLD_MAPPING.put(displayValue, valueInMeters);
        }
    }

    private void initializeThresholdDropdown() {
        Spinner spinner = (Spinner) findViewById(R.id.thresholdDropdown);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.threshold_options, R.layout.custom_dropdown);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        initializeDropdownValueMapping();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String selectedThreshold = (String) adapterView.getItemAtPosition(pos);
                Log.d(TAG, "selectedThreshold = " + selectedThreshold);
                Log.d(TAG, "value in meters = " + DISTANCE_THRESHOLD_MAPPING.get(selectedThreshold));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
    }

    public void closeHelpContent(View view) {
        ConstraintLayout helpContentPanel = findViewById(R.id.helpContent);
        helpContentPanel.setVisibility(View.GONE);
    }

    public void openDrawer(View view) {
        navDrawer.openDrawer();
    }
}