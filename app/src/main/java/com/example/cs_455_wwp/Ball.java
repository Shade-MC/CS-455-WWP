package com.example.cs_455_wwp;

import androidx.annotation.NonNull;

import com.google.ar.sceneform.math.Vector3;


public class Ball {
    private Vector3 position;
    private Vector3 speed;
    private Vector3 acceleration;
    private static final double EARTH_RADIUS = 6378137.0; // in meters

    public Ball(){
        this.position =  new Vector3(45, 45, 0);
        this.speed = new Vector3(100, 100, 1000);
        this.acceleration = new Vector3(0,0,(float) -9.8);
    }

    public Ball(Vector3 position, Vector3 speed, Vector3 acceleration){
        this.position = position;
        this.speed = speed;
        this.acceleration = acceleration;
    }

    public void updatePosition(double deltaTime){

        Vector3 horizontalSpeed = new Vector3(this.speed);
        horizontalSpeed.z = 0;

        // Calculate the distance traveled in meters
        double distanceTraveled = horizontalSpeed.length()  * deltaTime;

        // Calculate the angle of travel in radians
        double angleOfTravel = Math.atan2(this.speed.y, this.speed.x);

        // Calculate the change in latitude and longitude
        double deltaLatitude = Math.toDegrees(distanceTraveled * Math.cos(angleOfTravel) /
                EARTH_RADIUS);
        double deltaLongitude = Math.toDegrees(distanceTraveled * Math.sin(angleOfTravel) /
                (EARTH_RADIUS * Math.cos(Math.toRadians(0))));

        // Calculate the change in altitude
        double deltaAltitude = this.speed.z * deltaTime;

        this.position = Vector3.add(this.position, new Vector3((float) deltaLatitude,
                (float) deltaLongitude, (float) deltaAltitude));

        if (Math.abs(this.position.x) > 180){
            float over = this.position.x - (180 * Math.signum(this.position.x));
            this.position.x *= -1;
            this.position.x += over;
        }
        if (Math.abs(this.position.y) > 90){
            float over = this.position.y - (90 * Math.signum(this.position.y));
            this.speed.y *= -1;
            this.position.y = (90 * Math.signum(this.position.y)) - over;
        }

        if (this.position.z < 0) this.position.z = 0;

        this.speed = Vector3.add(this.speed, this.acceleration.scaled((float) deltaTime));

    }

    public void setVectors(String vectorsString){

        String[] ballStringStats = vectorsString.split(",");
        double[] ballStats = new double[ballStringStats.length];
        for (int i = 0; i < ballStringStats.length; ++i){
            ballStats[i] = Float.parseFloat(ballStringStats[i]);
        }

        this.setPosition(new Vector3((float) ballStats[0], (float) ballStats[1], (float) ballStats[2]));
        this.setSpeed(new Vector3((float) ballStats[3], (float) ballStats[4], (float) ballStats[5]));
        this.setAcceleration(new Vector3((float) ballStats[6], (float) ballStats[7], (float) ballStats[8]));
    }

    public Vector3 getSpeed() {
        return speed;
    }

    public void setSpeed(Vector3 speed) {
        this.speed = speed;
    }

    public Vector3 getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector3 acceleration) {
        this.acceleration = acceleration;
    }

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
    }

    @NonNull
    public String toString(){
        return  "Position: " + this.position.toString() + "\n" +
                "Speed: " + this.speed.toString() + "\n" +
                "Acceleration: " + this.acceleration.toString();
    }
}
