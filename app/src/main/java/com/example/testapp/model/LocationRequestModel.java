package com.example.testapp.model;

import com.google.android.gms.maps.model.LatLng;

public class LocationRequestModel {
    private LatLng latLng;
    private int distance;

    public LocationRequestModel(LatLng latLng, int distance) {
        this.latLng = latLng;
        this.distance = distance;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}
