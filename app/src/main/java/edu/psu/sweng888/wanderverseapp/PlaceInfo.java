package edu.psu.sweng888.wanderverseapp;

import com.google.android.gms.maps.model.LatLng;

public class PlaceInfo {
    private String name; // Name of the place
    private String address; // Address of the place
    private LatLng latLng; // Latitude and longitude of the place

    // initializes the PlaceInfo object
    public PlaceInfo(String name, String address, LatLng latLng) {
        this.name = name;
        this.address = address;
        this.latLng = latLng;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Getter for address
    public String getAddress() {
        return address;
    }

    // Getter for LatLng
    public LatLng getLatLng() {
        return latLng;
    }
}