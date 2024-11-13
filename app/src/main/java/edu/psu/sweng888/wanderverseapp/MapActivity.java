package edu.psu.sweng888.wanderverseapp;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationClient;
    private RecyclerView recyclerViewSuggestions;
    private SuggestionAdapter suggestionAdapter;
    private List<PlaceSuggestion> suggestionsList = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize Places API
        Places.initialize(getApplicationContext(), getString(R.string.google_api_key));
        placesClient = Places.createClient(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set up RecyclerView for suggestions
        recyclerViewSuggestions = findViewById(R.id.recyclerView_suggestions);
        recyclerViewSuggestions.setLayoutManager(new LinearLayoutManager(this));

        // Load user preferences and fetch real data suggestions
        loadPreferencesAndFetchSuggestions();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        enableUserLocation();
        getUserLocation();
    }

    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));

                    // Add a marker for the user's current location
                    map.addMarker(new MarkerOptions().position(userLatLng).title("You are here"));
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void loadPreferencesAndFetchSuggestions() {
        SharedPreferences prefs = getSharedPreferences("Preferences", MODE_PRIVATE);
        Set<String> selectedInterests = prefs.getStringSet("selectedInterests", new HashSet<>());
        String selectedDistance = prefs.getString("selectedDistance", ""); // Default to empty if not set

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    int radius = convertDistanceToMeters(selectedDistance);

                    // Prepare a request to find places based on current location
                    List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.TYPES);
                    FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

                    placesClient.findCurrentPlace(request).addOnSuccessListener(response -> {
                        suggestionsList.clear();
                        for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                            Place place = placeLikelihood.getPlace();

                            // Check if the place matches one of the user-selected interests
                            if (matchesSelectedInterests(place, selectedInterests)) {
                                LatLng latLng = place.getLatLng();
                                if (place.getTypes() != null && !place.getTypes().isEmpty()) {
                                    PlaceSuggestion suggestion = new PlaceSuggestion(
                                            place.getName(),
                                            getPlaceTypeString(place.getTypes().get(0)),
                                            latLng.latitude,
                                            latLng.longitude
                                    );
                                    suggestionsList.add(suggestion);
                                }
                            }
                        }

                        // Notify adapter and update RecyclerView with real suggestions
                        suggestionAdapter = new SuggestionAdapter(suggestionsList, suggestion -> startNavigation(suggestion));
                        recyclerViewSuggestions.setAdapter(suggestionAdapter);
                        suggestionAdapter.notifyDataSetChanged();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(MapActivity.this, "Failed to retrieve places: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private int convertDistanceToMeters(String selectedDistance) {
        switch (selectedDistance) {
            case "0.5-2 miles":
                return 3200; // approx 2 miles in meters
            case "3-6 miles":
                return 9600; // approx 6 miles in meters
            case "10-15 miles":
                return 24000; // approx 15 miles in meters
            default:
                return 1600; // default to 1 mile if distance preference is unknown
        }
    }

    private boolean matchesSelectedInterests(Place place, Set<String> selectedInterests) {
        if (place.getTypes() == null) return false;
        for (Place.Type type : place.getTypes()) {
            if (selectedInterests.contains(getPlaceTypeString(type))) {
                return true;
            }
        }
        return false;
    }

    private String getPlaceTypeString(Place.Type type) {
        switch (type) {
            case PARK: return "Parks";
            case CAFE: return "Coffee Shops";
            case MUSEUM: return "Museums";
            case BAR: return "Bars";
            case RESTAURANT: return "Restaurants";
            case MOVIE_THEATER: return "Theaters";
            case SHOPPING_MALL: return "Malls";
            case TOURIST_ATTRACTION: return "Historical Sites";
            case LIBRARY: return "Libraries";
            case UNIVERSITY: return "Universities";
            default: return "Other";
        }
    }

    private void startNavigation(PlaceSuggestion suggestion) {
        LatLng destination = new LatLng(suggestion.getLatitude(), suggestion.getLongitude());
        map.addMarker(new MarkerOptions().position(destination).title(suggestion.getName()));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 15f));
        Toast.makeText(this, "Navigating to " + suggestion.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "Location permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }
}