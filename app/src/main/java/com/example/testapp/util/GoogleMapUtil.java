package com.example.testapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;

import com.example.testapp.R;
import com.example.testapp.model.LocationObject;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class GoogleMapUtil {

    private static List<Marker> hotspotMarkerList = new ArrayList<>();
    private static Context ctx;

    public static void initialize(Context context) {
        ctx = context;
    }

    // TODO Add parameter for Area POJO parsed from json
    public static void populateLocationMarkers(GoogleMap mMap) {
        List<LocationObject> dummyLocations = new ArrayList<>();
        LocationObject loc1 = new LocationObject(14.5611721636, 121.021212863);
        LocationObject loc2 = new LocationObject(14.555267, 121.022352);
        dummyLocations.add(loc1);
        dummyLocations.add(loc2);

        clearExistingMarkers();
        Bitmap customIcon = getCustomIcon();
        for (LocationObject l : dummyLocations) {
            LatLng latLng = new LatLng(l.getLatitude(), l.getLongitude());
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(latLng);
            circleOptions.strokeWidth(4);
            circleOptions.strokeColor(Color.argb(255, 255, 0 , 0));
            circleOptions.fillColor(Color.argb(32, 255, 0 , 0));
            circleOptions.radius(30);
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Latitude: " + l.getLatitude() + " Longitude: " + l.getLongitude());
           /* MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Latitude: " + l.getLatitude() + " Longitude: " + l.getLongitude())
                    .icon(BitmapDescriptorFactory.fromBitmap(customIcon));*/
           mMap.addCircle(circleOptions);
            Marker marker = mMap.addMarker(markerOptions);
            hotspotMarkerList.add(marker);
        }
    }

    public static void clearExistingMarkers() {
        for (Marker marker : hotspotMarkerList) {
            marker.remove();
        }
        hotspotMarkerList.clear();
    }

    private static Bitmap getCustomIcon() {
        BitmapDrawable bitmapdraw = (BitmapDrawable) ctx.getResources().getDrawable(R.drawable.custom_marker);
        Bitmap b = bitmapdraw.getBitmap();
        return Bitmap.createScaledBitmap(b, 150, 150, false);
    }
}
