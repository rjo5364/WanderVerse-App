package edu.psu.sweng888.wanderverseapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PreferencesActivity extends AppCompatActivity {

    private Spinner spinnerUpdatePoi;
    private Button btnSavePoi;

    private FirebaseAuth mAuth;
    private FirebaseFirestore fstore;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    // Has map to associate friendly names with Google Places API terms
    private final Map<String, String> poiMap = new HashMap<>();
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // Initializes Firebase instances
        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        spinnerUpdatePoi = findViewById(R.id.spinner_update_poi);
        btnSavePoi = findViewById(R.id.btn_save_poi);

        // Initialize the DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        // Set up ActionBarDrawerToggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set the navigation item listener
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        // Initializes the POI map with friendly names and API terms
        initializePoiMap();

        // Populates the spinner
        ArrayAdapter<String> poiAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, poiMap.keySet().toArray(new String[0]));
        poiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUpdatePoi.setAdapter(poiAdapter);

        loadCurrentPoiPreference();

        btnSavePoi.setOnClickListener(view -> savePoiPreference());
    }

    // Handles navigation item clicks
    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_main) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (id == R.id.nav_map) {
            startActivity(new Intent(this, MapActivity.class));
        } else if (id == R.id.nav_rewards) {
            startActivity(new Intent(this, RewardsList.class));
        } else if (id == R.id.nav_preferences) {
            Toast.makeText(this, "Already on Preferences", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawers(); // Close the navigation drawer
        return true;
    }

    // Loads the current POI preference from Firestore
    private void loadCurrentPoiPreference() {
        DocumentReference userDocRef = fstore.collection("users").document(userID);
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String currentPoi = documentSnapshot.getString("pointOfInterest");

                // Finds the friendly name for the current POI
                String friendlyPoi = getFriendlyPoiName(currentPoi);

                // Sets the spinner selection to the current POI
                if (friendlyPoi != null) {
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerUpdatePoi.getAdapter();
                    int position = adapter.getPosition(friendlyPoi);
                    spinnerUpdatePoi.setSelection(position);
                }
            } else {
                Toast.makeText(PreferencesActivity.this, "Failed to load current preference", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(PreferencesActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Saves the updated POI preference to Firestore
    private void savePoiPreference() {
        String selectedFriendlyPoi = spinnerUpdatePoi.getSelectedItem().toString();
        String selectedPoi = poiMap.get(selectedFriendlyPoi);

        if (selectedPoi != null) {
            DocumentReference userDocRef = fstore.collection("users").document(userID);

            userDocRef.update("pointOfInterest", selectedPoi)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(PreferencesActivity.this, "Preference updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PreferencesActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Invalid selection", Toast.LENGTH_SHORT).show();
        }
    }

    // Friendly names and place API mapping
    private void initializePoiMap() {
        poiMap.put("Bakery", "bakery");
        poiMap.put("Bar", "bar");
        poiMap.put("Book Store", "book_store");
        poiMap.put("Cafe", "cafe");
        poiMap.put("Gym", "gym");
        poiMap.put("Library", "library");
        poiMap.put("Movie Theater", "movie_theater");
        poiMap.put("Park", "park");
        poiMap.put("Pet Store", "pet_store");
        poiMap.put("Restaurant", "restaurant");
        poiMap.put("Shopping Mall", "shopping_mall");
        poiMap.put("Spa", "spa");
        poiMap.put("Stadium", "stadium");
        poiMap.put("Tourist Attraction", "tourist_attraction");
        poiMap.put("University", "university");
        poiMap.put("Zoo", "zoo");
    }

    // Gets the friendly name for the given API term
    private String getFriendlyPoiName(String apiPoi) {
        for (Map.Entry<String, String> entry : poiMap.entrySet()) {
            if (entry.getValue().equals(apiPoi)) {
                return entry.getKey();
            }
        }
        return null;
    }

    // Handles the Toggle clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}