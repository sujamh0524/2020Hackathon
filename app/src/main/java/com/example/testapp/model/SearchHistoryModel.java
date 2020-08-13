package com.example.testapp.model;

import java.util.List;

public class SearchHistoryModel {


    private String id;
    private String location;
    private String distance;
    private String createdDate;
    private List<AreaInformation> response;

    public SearchHistoryModel(String id, String location, String distance, String createdDate, List<AreaInformation> response) {
        this.id = id;
        this.location = location;
        this.distance = distance;
        this.createdDate = createdDate;
        this.response = response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public List<AreaInformation> getResponse() {
        return response;
    }

    public void setResponse(List<AreaInformation> response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "LocationHistoryModel{" +
                "id='" + id + '\'' +
                ", location='" + location + '\'' +
                ", distance='" + distance + '\'' +
                ", createdDate='" + createdDate + '\'' +
                '}';
    }
}
