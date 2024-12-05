package edu.psu.sweng888.wanderverseapp;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {
    private final ArrayList<PlaceInfo> placesList; // Holds the list of places to display
    private final LatLng userLocation; // Stores the user's current location

    // Constructor that initializes  places list and user's location
    public PlaceAdapter(ArrayList<PlaceInfo> placesList, LatLng userLocation) {
        this.placesList = placesList;
        this.userLocation = userLocation;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflates layout for an individual item in the RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Gets the place information at the current position
        PlaceInfo place = placesList.get(position);

        // Sets the place name and address in the corresponding TextViews
        holder.name.setText(place.getName());
        holder.address.setText(place.getAddress());

        // Calculates the distance between the user's location and the place
        float[] results = new float[1];
        Location.distanceBetween(
                userLocation.latitude,
                userLocation.longitude,
                place.getLatLng().latitude,
                place.getLatLng().longitude,
                results
        );

        // converts  distance to miles and sets it in the distance TextView
        float distanceInMiles = results[0] / 1609.34f; // Convert meters to miles
        holder.distance.setText(String.format("Distance: %.2f miles", distanceInMiles));
    }

    @Override
    public int getItemCount() {
        // Retuns the total number of items in the places list
        return placesList.size();
    }

    //  class that holds the views for an individual item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, address, distance; // TextViews for name, address, and distance

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Finds the TextViews by their IDs
            name = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.address);
            distance = itemView.findViewById(R.id.distance);
        }
    }
}