package com.example.testapp.model;

import java.util.Date;


public class AreaInformation {



    private int id;
    private double longitude;
    private double latitude;
    private String area;
    private String baranggay;
    private String city;
    private int activeCovidCases;
    private Date createdDate;
    private Date updatedDate;

   /* public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }*/

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getBaranggay() {
        return baranggay;
    }

    public void setBaranggay(String baranggay) {
        this.baranggay = baranggay;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getActiveCovidCases() {
        return activeCovidCases;
    }

    public void setActiveCovidCases(int activeCovidCases) {
        this.activeCovidCases = activeCovidCases;
    }

    public String toString(){
        return "longitude" + longitude;
    }

    public int getId() {
        return id;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public void setId(int id) {
        this.id = id;
    }
}
