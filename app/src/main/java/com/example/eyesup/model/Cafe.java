package com.example.eyesup.model;

public class Cafe {

    private double dist;
    private String name;
    private String phone;
    private double lat;
    private double lon;

    public Cafe(double dist, String name, String phone, double lat, double lon) {
        this.dist = dist;
        this.name = name;
        this.phone = phone;
        this.lat = lat;
        this.lon = lon;
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
}
