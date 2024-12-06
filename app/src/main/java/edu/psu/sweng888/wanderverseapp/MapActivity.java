package edu.psu.sweng888.wanderverseapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private GoogleMap mMap; // Holds the GoogleMap instance
    private FusedLocationProviderClient fusedLocationClient; // Retrieves the user's location
    private RecyclerView recyclerView; // Displays the list of places
    private Spinner distanceFilterSpinner; // Filters results by distance
    private ArrayList<PlaceInfo> placesList = new ArrayList<>(); // Stores all fetched places
    private ArrayList<PlaceInfo> filteredPlacesList = new ArrayList<>(); // Stores filtered places
    private LatLng userLatLng; // Stores the user's location
    private String userPreference = "park"; // Default user preference
    private boolean locationPermissionGranted = false; // Tracks location permission status

    private FirebaseAuth mAuth; // Handles Firebase Authentication
    private FirebaseFirestore db; // Handles Firestore database operations

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize Firebase and location services
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up UI components and load data
        setupUI();
        fetchUserPreference();
        checkLocationPermission();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);


        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }



    private void setupUI() {
        // Sets the title for the activity
        getSupportActionBar().setTitle("Nearby Places");

        // Finds the RecyclerView and sets its layout manager
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Finds and sets up the distance filter spinner
        distanceFilterSpinner = findViewById(R.id.distanceFilterSpinner);
        setupDistanceFilterSpinner();

        // Sets up the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Finds and sets up the search button
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> {
            if (locationPermissionGranted) {
                fetchUserLocationAndSearch(); // Fetches user's location and performs a search
            } else {
                Toast.makeText(this, "Location permission not granted!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDistanceFilterSpinner() {
        // Sets up the spinner with distance options
        String[] distanceOptions = {"No Filter", "1 mile or less", "5 miles or less", "10 miles or less"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, distanceOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceFilterSpinner.setAdapter(spinnerAdapter);

        // Handles spinner item selection
        distanceFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                switch (position) {
                    case 0:
                        showAllPlaces(); // Shows all places without filtering
                        break;
                    case 1:
                        filterPlacesByDistance(1.0); // Filters places within 1 mile
                        break;
                    case 2:
                        filterPlacesByDistance(5.0); // Filters places within 5 miles
                        break;
                    case 3:
                        filterPlacesByDistance(10.0); // Filters places within 10 miles
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
    }

    private void showAllPlaces() {
        // Clears the filtered list and adds all places
        filteredPlacesList.clear();
        filteredPlacesList.addAll(placesList);
        updateRecyclerView(); // Updates the RecyclerView with all places
        addMarkersToMap(filteredPlacesList); // Adds markers for all places
    }

    private void fetchUserPreference() {
        // Fetches the user's preference from Firestore
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                userPreference = task.getResult().getString("pointOfInterest");
                if (userPreference == null || userPreference.isEmpty()) {
                    userPreference = "park";
                }
            }
        });
    }

    private void checkLocationPermission() {
        // Checks if location permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Handles location permission result
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        }
    }

    private void fetchUserLocationAndSearch() {
        // Fetches the user's location and performs a nearby search
        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Location location = task.getResult();
                userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(userLatLng).title("You are here"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 13));
                searchNearbyPlaces(userLatLng); // Searches for nearby places
            }
        });
    }

    private void searchNearbyPlaces(LatLng userLatLng) {
        // Constructs the URL for the Places API request
        String url = String.format(
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=40233&type=%s&key=%s",
                userLatLng.latitude,
                userLatLng.longitude,
                userPreference,
                BuildConfig.PLACES_API_KEY
        );

        // Sends a request to the Places API
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                JSONArray results = response.getJSONArray("results");
                placesList.clear();

                // Parses the API response and adds places to the list
                for (int i = 0; i < results.length(); i++) {
                    JSONObject place = results.getJSONObject(i);
                    String name = place.getString("name");
                    String address = place.optString("vicinity", "No address available");
                    JSONObject location = place.getJSONObject("geometry").getJSONObject("location");
                    LatLng latLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));

                    placesList.add(new PlaceInfo(name, address, latLng));
                }

                showAllPlaces(); // Shows all places by default
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing places response", e);
            }
        }, error -> Log.e(TAG, "Error fetching places", error));

        requestQueue.add(request);
    }

    private void filterPlacesByDistance(double maxDistanceMiles) {
        // Filters places based on the selected distance
        filteredPlacesList.clear();

        for (PlaceInfo place : placesList) {
            float[] results = new float[1];
            Location.distanceBetween(
                    userLatLng.latitude,
                    userLatLng.longitude,
                    place.getLatLng().latitude,
                    place.getLatLng().longitude,
                    results
            );
            float distanceInMiles = results[0] / 1609.34f;

            if (distanceInMiles <= maxDistanceMiles) {
                filteredPlacesList.add(place);
            }
        }

        updateRecyclerView(); // Updates the RecyclerView with filtered results
        addMarkersToMap(filteredPlacesList); // Updates the map markers
    }

    private void updateRecyclerView() {
        // Updates the RecyclerView with the current list
        PlaceAdapter adapter = new PlaceAdapter(filteredPlacesList, userLatLng);
        recyclerView.setAdapter(adapter);
    }

    private void addMarkersToMap(ArrayList<PlaceInfo> places) {
        // Clears all markers and adds the user's location marker
        mMap.clear();
        if (userLatLng != null) {
            mMap.addMarker(new MarkerOptions().position(userLatLng).title("You are here"));
        }

        // Adds markers for each place in the list
        for (PlaceInfo place : places) {
            mMap.addMarker(new MarkerOptions()
                    .position(place.getLatLng())
                    .title(place.getName()));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Sets up the map when it is ready
        mMap = googleMap;
        if (locationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_main) {
            // Navigate to MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish(); // Optional: Finish current activity to prevent stacking
        } else if (id == R.id.nav_map) {
            Toast.makeText(this, "Already on Map", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_rewards) {
            startActivity(new Intent(this, RewardsList.class));
        } else if (id == R.id.nav_preferences) {
            startActivity(new Intent(this, PreferencesActivity.class));
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawers(); // Close the drawer
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
}