package com.example.testapp.util;

import com.example.testapp.model.Location;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class GoogleMapUtil {

    private static List<Marker> hotspotMarkerList = new ArrayList<>();

    // TODO Add parameter for Area POJO parsed from json
    public static void populateLocationMarkers(GoogleMap mMap) {
        List<Location> dummyLocations = new ArrayList<>();
        Location loc1 = new Location(14.555727, 121.021749);
        Location loc2 = new Location(14.555267, 121.022352);
        dummyLocations.add(loc1);
        dummyLocations.add(loc2);

        clearExistingMarkers();

        for (Location l : dummyLocations) {
            LatLng latLng = new LatLng(l.getLatitude(), l.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions().position(latLng);
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
}
