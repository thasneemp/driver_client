package com.launcher.mummu.cabclient.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by muhammed on 2/28/2017.
 */
@IgnoreExtraProperties
public class LocationModel {
    private double lat;
    private double longe;
    private float bearing;
    private boolean enable;
    private String message;

    public LocationModel() {
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLonge() {
        return longe;
    }

    public void setLonge(double longe) {
        this.longe = longe;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
