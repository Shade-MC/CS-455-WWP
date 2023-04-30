package com.example.cs_455_wwp;


public class LocationPing {
    private double latitude;
    private double longitude;
    private String userId;
    private String time;
    private String team;

    LocationPing(double latitude, double longitude, String userId, String time, String team){

        this.latitude = latitude;
        this.longitude = longitude;
        this.userId = userId;
        this.time = time;
        this.team = team;
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

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }
}
