package com.example.cs_455_wwp;

public class LocationPing {
    private double latitude;
    private double longitude;
    private String userId;
    private String time;

    LocationPing(double latitude, double longitude, String userId, String time){

        this.latitude = latitude;
        this.longitude = longitude;
        this.userId = userId;
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
