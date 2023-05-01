package com.example.cs_455_wwp;

import androidx.annotation.NonNull;

import com.google.ar.sceneform.math.Vector3;


public class Ball {
    private Vector3 position;  //lat/long for x/y, meters for altitude
    private Vector3 speed;  //meters/second
    private Vector3 acceleration;  //meters/second^2
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

    public double getTimeRemaining() {
        //returns the time remaining, as a double representing seconds
        //time is just how long till the balls altitude is 0, we should be able to calculate that with kinematics
        //returns -1 on an error
        double discriminant = (this.speed.z * this.speed.z) + (2 * this.acceleration.z * (-this.position.z));
        if (discriminant < 0) {
            //using quadratic formula for d = vt + .5at^2 would lead to an imaginary number, which shouldn't happen
            return -1;
        }
        //accounting for both the plus and minus in the quadratic formula
        double plusSpeed = ((-this.speed.z) + Math.sqrt(discriminant)) / this.acceleration.z;
        double minusSpeed = ((-this.speed.z) - Math.sqrt(discriminant)) / this.acceleration.z;

        //return which ever one is positive, the negative one was how long ago the ball last landed
        if (plusSpeed >= 0) {
            return plusSpeed;
        }
        return minusSpeed;
    }

    public Vector3 getFinalPosition()
    {
        //returns the balls final position, as a Vector3 representing Lat. and Long and altitude.
        //altitude should just be 0 meters
        //code might be buggy over long distances, esp when going around the Earth
        //get some values from elsewhere
        Vector3 finalPosition = this.position;
        double time = getTimeRemaining();
        double metersPerDegree = 2.0 * Math.PI * EARTH_RADIUS / 360.0;
        //calculate distance traveled
        double deltaX = this.speed.x * time + 0.5 * this.acceleration.x * time * time;  //might need to account form longitude
        double deltaY = this.speed.y * time + 0.5 * this.acceleration.y * time * time;
        //convert from meters to degrees, and add to the current position (stored in final position)
        finalPosition.x += deltaX / metersPerDegree;
        finalPosition.y += deltaY / metersPerDegree;
        finalPosition.z = 0; //should be landing, we can assume
        //account for overflow
        //code borrowed and modified from Eric
        if (Math.abs(finalPosition.x) > 180){
            float over = finalPosition.x - (180 * Math.signum(finalPosition.x));
            finalPosition.x *= -1;
            finalPosition.x += over;
        }
        if (Math.abs(finalPosition.y) > 90){
            float over = finalPosition.y - (90 * Math.signum(finalPosition.y));
            finalPosition.y *= -1;
            finalPosition.y = (90 * Math.signum(finalPosition.y)) - over;
        }
        return finalPosition;
    }

    public String finalPositionToString() {
        //converts final position to a string
        Vector3 finalPosition = getFinalPosition();
        return(finalPosition.x + "," + finalPosition.y);
    }

    @NonNull
    public String toString(){
        return  "Position: " + this.position.toString() + "\n" +
                "Speed: " + this.speed.toString() + "\n" +
                "Acceleration: " + this.acceleration.toString() + "\n";
    }
}
