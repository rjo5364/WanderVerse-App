package edu.psu.sweng888.wanderverseapp;

public class PlaceSuggestion {
    private String name;
    private String type;
    private double latitude;
    private double longitude;

    public PlaceSuggestion(String name, String type, double latitude, double longitude) {
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}