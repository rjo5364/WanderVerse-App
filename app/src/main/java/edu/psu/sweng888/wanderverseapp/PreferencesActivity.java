package edu.psu.sweng888.wanderverseapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
                        finish(); // Close the activity
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PreferencesActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Invalid selection", Toast.LENGTH_SHORT).show();
        }
    }

    // friendly names and place api mapping
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
}