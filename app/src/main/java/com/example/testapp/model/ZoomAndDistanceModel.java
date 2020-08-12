package com.example.testapp.model;

public class ZoomAndDistanceModel {
    private int distance;
    private float zoom;

    public ZoomAndDistanceModel(int distance, float zoom) {
        this.distance = distance;
        this.zoom = zoom;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }
}
