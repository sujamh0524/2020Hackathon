package com.example.testapp;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.location.Address;
import android.location.Geocoder;
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

import com.example.testapp.adapter.CustomTableDataAdapter;
import com.example.testapp.adapter.CustomTableHeaderAdapter;
import com.example.testapp.constant.AppConstants;
import com.example.testapp.model.AreaInformation;
import com.example.testapp.model.LocationRequestModel;
import com.example.testapp.model.SearchHistoryModel;
import com.example.testapp.model.ZoomAndDistanceModel;
import com.example.testapp.service.MapsService;
import com.example.testapp.util.DatabaseHelper;
import com.example.testapp.util.GoogleMapUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.codecrafters.tableview.TableView;


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
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(this);
        geocoder = new Geocoder(this, Locale.getDefault());
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
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(AppConstants.MAP_MENU).withName("Map");
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(AppConstants.SEARCH_HISTORY_MENU).withName("Search History");
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(AppConstants.RISK_TRAVEL_HISTORY_MENU).withName("Risk Travel History");

        //create the drawer
        navDrawer = new DrawerBuilder()
                .withActivity(this)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        new DividerDrawerItem(),
                        item3
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        setVisibileComponents((int) drawerItem.getIdentifier());
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
        Spinner spinner = findViewById(R.id.thresholdDropdown);
        ZoomAndDistanceModel zoomAndDistanceModel = getDistance(spinner.getSelectedItem().toString());

        scanButton.setEnabled(false);
        MapsService mapsService = new MapsService();
        List<AreaInformation> areaInformations = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        if(thisMarker != null) {
            LocationRequestModel locationRequestModel = new LocationRequestModel(thisMarker.getPosition(), zoomAndDistanceModel.getDistance());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(thisMarker.getPosition(), zoomAndDistanceModel.getZoom()));
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

    public void setVisibileComponents(int selectedMenu) {
        View mainContent = findViewById(R.id.mainContent);
        View searchHistoryContent = findViewById(R.id.searchHistoryContent);
        View riskTravelHistoryContent = findViewById(R.id.travelHistoryContent);

        searchHistoryContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db = new DatabaseHelper(getBaseContext());
                SQLiteDatabase database = db.getWritableDatabase();

                //show db data
                Cursor cursor = database.rawQuery("SELECT*FROM LOCATION_HISTORY WHERE CREATED_DATE > datetime('now','-15 day') ORDER BY CREATED_DATE DESC", null);
                List<SearchHistoryModel> searchHistoryModels = new ArrayList<>();
                while(cursor.moveToNext()){
                    List<Address> addresses = new ArrayList<>();
                    String address = "";

                    address = getAddress(cursor, addresses);
                    addLocationHistoryModels(cursor, searchHistoryModels, address);
                }

                final String[] columnHeaders = { "Location in the past 15 days", "Search Distance", "Number of Active Cases" };
                String[][] sampleData = new String[searchHistoryModels.size()][3];
                int counter = 0;
                for(SearchHistoryModel model : searchHistoryModels){

                    sampleData[counter][0] = model.getCreatedDate() + " - " + model.getLocation();
                    sampleData[counter][1] = model.getDistance();
                    int areaCounter = 0;
                    for (AreaInformation areaInformation : model.getResponse()) {
                        if(areaCounter > 0){
                            sampleData[counter][2] += ", ";
                            sampleData[counter][2] += areaInformation.getCity() + ", " + areaInformation.getBaranggay() + " - " + areaInformation.getActiveCovidCases();
                        }
                        else {
                            sampleData[counter][2] = areaInformation.getCity() + ", " + areaInformation.getBaranggay() + " - " + areaInformation.getActiveCovidCases();
                        }
                        areaCounter++;
                    }

                    Log.d("location model: ",model.toString());
                    counter++;
                }

                TableView<String[]> tableView = (TableView<String[]>) view.findViewById(R.id.table_view);
                tableView.setHeaderBackgroundColor(Color.parseColor("#2ecc71"));
                CustomTableHeaderAdapter tableHeaderAdapter = new CustomTableHeaderAdapter(getBaseContext(), columnHeaders);
                tableHeaderAdapter.setPaddingRight(0);
                tableHeaderAdapter.setTextSize(15);
                tableView.setHeaderAdapter(tableHeaderAdapter);
                tableView.setColumnCount(3);

                CustomTableDataAdapter tableDataAdapter = new CustomTableDataAdapter(getBaseContext(), sampleData);
                tableDataAdapter.setPaddingRight(0);
                tableDataAdapter.setTextSize(12);
                tableView.setDataAdapter(tableDataAdapter);
            }
        });

        riskTravelHistoryContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Get data within 500 meters and with cases

                db = new DatabaseHelper(getBaseContext());
                SQLiteDatabase database = db.getWritableDatabase();

                //show db data
                Cursor cursor = database.rawQuery("SELECT*FROM LOCATION_HISTORY WHERE CREATED_DATE > datetime('now','-15 day') AND DISTANCE <= 500 ORDER BY CREATED_DATE DESC", null);
                List<SearchHistoryModel> searchHistoryModels = new ArrayList<>();
                while(cursor.moveToNext()){
                    List<Address> addresses = new ArrayList<>();
                    String address = "";

                    address = getAddress(cursor, addresses);
                    addLocationHistoryModels(cursor, searchHistoryModels, address);
                }

                final String[] columnHeaders = { "Location in the past 15 days", "Number of Active Cases" };
                String[][] sampleData = new String[searchHistoryModels.size()][2];

                int counter = 0;
                for(SearchHistoryModel model : searchHistoryModels){

                    sampleData[counter][0] = model.getCreatedDate() + " - " + model.getLocation();
                    int areaCounter = 0;
                    for (AreaInformation areaInformation : model.getResponse()) {
                        if(areaInformation.getActiveCovidCases() > 0) {
                            if (areaCounter > 0) {
                                sampleData[counter][1] += ", ";
                                sampleData[counter][1] += areaInformation.getCity() + ", " + areaInformation.getBaranggay() + " - " + areaInformation.getActiveCovidCases();
                            } else {
                                sampleData[counter][1] = areaInformation.getCity() + ", " + areaInformation.getBaranggay() + " - " + areaInformation.getActiveCovidCases();
                            }
                            areaCounter++;
                        }else {
                            Log.d("WAAAAAA","WAAAA");
                        }

                    }

                    Log.d("location model: ", model.toString());
                    counter++;
                }

                TableView<String[]> travelHistTableView = (TableView<String[]>) view.findViewById(R.id.travel_history_table);
                travelHistTableView.setHeaderBackgroundColor(Color.parseColor("#2ecc71"));
                CustomTableHeaderAdapter tableHeaderAdapter = new CustomTableHeaderAdapter(getBaseContext(), columnHeaders);
                tableHeaderAdapter.setPaddingRight(0);
                tableHeaderAdapter.setTextSize(15);
                travelHistTableView.setHeaderAdapter(tableHeaderAdapter);
                travelHistTableView.setColumnCount(2);

                CustomTableDataAdapter tableDataAdapter = new CustomTableDataAdapter(getBaseContext(), sampleData);
                tableDataAdapter.setPaddingRight(0);
                tableDataAdapter.setTextSize(12);
                travelHistTableView.setDataAdapter(tableDataAdapter);
            }
        });

        switch (selectedMenu) {
            case AppConstants.MAP_MENU:
                mainContent.setVisibility(View.VISIBLE);
                searchHistoryContent.setVisibility(View.GONE);
                riskTravelHistoryContent.setVisibility(View.GONE);
                break;
            case AppConstants.SEARCH_HISTORY_MENU:
                searchHistoryContent.setVisibility(View.VISIBLE);
                searchHistoryContent.callOnClick();
                mainContent.setVisibility(View.GONE);
                riskTravelHistoryContent.setVisibility(View.GONE);
                break;
            case AppConstants.RISK_TRAVEL_HISTORY_MENU:
                riskTravelHistoryContent.setVisibility(View.VISIBLE);
                riskTravelHistoryContent.callOnClick();
                mainContent.setVisibility(View.GONE);
                searchHistoryContent.setVisibility(View.GONE);
                break;
        }
    }

    private String getAddress(Cursor cursor, List<Address> addresses) {
        String address;
        try {
            addresses = geocoder.getFromLocation(cursor.getDouble(2), cursor.getDouble(1), 1);
        } catch (IOException e) {
            Log.d("setVisibileComponents","ERROR");
        }
        if(addresses.size() > 0){
            address = addresses.get(0).getAddressLine(0);
            Log.d("onClick",address);
        } else {
            address = "Longitude: " + cursor.getDouble(1) + " Latitude: " + cursor.getDouble(2);
        }
        return address;
    }

    @SuppressLint("LongLogTag")
    private void addLocationHistoryModels(Cursor cursor, List<SearchHistoryModel> searchHistoryModels, String address) {
        try {
            Log.d("response: ", cursor.getString(5));
            SearchHistoryModel searchHistoryModel = new SearchHistoryModel(cursor.getString(0),
                    address,
                    cursor.getString(3)  + " meters",
                    cursor.getString(4),
                    new ObjectMapper().readValue(cursor.getString(5), new TypeReference<List<AreaInformation>>(){}));
            if(!searchHistoryModel.getResponse().isEmpty()) {
                searchHistoryModels.add(searchHistoryModel);
            }
        } catch (JsonProcessingException e) {
            Log.d("addLocationHistoryModels", "Error parsing Area Information");
        }
    }
}