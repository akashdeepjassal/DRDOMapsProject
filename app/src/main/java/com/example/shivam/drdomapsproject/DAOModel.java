package com.example.shivam.drdomapsproject;

/**
 * Created by shivam on 11/7/18.
 */

public class DAOModel {
    public double lat = 0.00;
    public double lon = 0.00;

    public DAOModel() {
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public DAOModel(double lat, double lon, int result) {
        this.lat = lat;
        this.lon = lon;
        this.result = result;
    }

    public int result = 0;

}
