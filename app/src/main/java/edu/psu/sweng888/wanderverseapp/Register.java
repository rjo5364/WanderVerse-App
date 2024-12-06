package edu.psu.sweng888.wanderverseapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    // Declare UI elements and Firebase variables
    TextInputEditText editTextEmail, editTextPassword, editTextFirstName, editTextLastName;
    Spinner spinnerAge, spinnerActivityCategory, spinnerPointOfInterest;
    Button buttonRegistration;
    TextView textView;
    ProgressBar progressBar;
    FirebaseAuth mAuth; // This handles Firebase Authentication
    FirebaseFirestore fstore; // This handles Firestore database operations
    String userID; // This stores the current user's ID

    // Map to associate friendly names with Google Places API terms
    private final Map<String, String> poiMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth and Firestore instances
        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        // Find views by their IDs
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextFirstName = findViewById(R.id.f_name);
        editTextLastName = findViewById(R.id.l_name);
        spinnerAge = findViewById(R.id.spinner_age);
        spinnerActivityCategory = findViewById(R.id.spinner_activity_category);
        spinnerPointOfInterest = findViewById(R.id.spinner_point_of_interest);
        buttonRegistration = findViewById(R.id.button_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);

        // Initialize the friendly names map
        initializePoiMap();

        // Set up spinners with data from string arrays or map keys
        ArrayAdapter<CharSequence> ageAdapter = ArrayAdapter.createFromResource(this, R.array.age_range, android.R.layout.simple_spinner_item);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAge.setAdapter(ageAdapter);

        ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(this, R.array.activity_categories, android.R.layout.simple_spinner_item);
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivityCategory.setAdapter(activityAdapter);

        // Populate the spinner with friendly names
        ArrayAdapter<String> poiAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, poiMap.keySet().toArray(new String[0]));
        poiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPointOfInterest.setAdapter(poiAdapter);

        // Set up a click listener for the "Login Now" text view
        textView.setOnClickListener(view -> {
            // This starts the Login activity and finishes the current activity
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        // Set up a click listener for the registration button
        buttonRegistration.setOnClickListener(view -> {
            // Show progress bar during registration
            progressBar.setVisibility(View.VISIBLE);

            // Get input values from the form
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            String fName = editTextFirstName.getText().toString();
            String lName = editTextLastName.getText().toString();
            String age = spinnerAge.getSelectedItem().toString();
            String activityCategory = spinnerActivityCategory.getSelectedItem().toString();
            String displayPointOfInterest = spinnerPointOfInterest.getSelectedItem().toString();

            // Map the friendly display name to the API term
            String pointOfInterest = poiMap.get(displayPointOfInterest);

            // Validate inputs
            if (TextUtils.isEmpty(fName) || TextUtils.isEmpty(lName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(pointOfInterest)) {
                Toast.makeText(Register.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(Register.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            // Create a new user with Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // Hide progress bar when registration is complete
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        // Get the current user ID
                        userID = mAuth.getCurrentUser().getUid();

                        // Create a Firestore document for the new user
                        DocumentReference documentReference = fstore.collection("users").document(userID);

                        // Prepare user data to save in Firestore
                        Map<String, Object> user = new HashMap<>();
                        user.put("fName", fName);
                        user.put("lName", lName);
                        user.put("email", email);
                        user.put("age", age);
                        user.put("activityCategory", activityCategory);
                        user.put("pointOfInterest", pointOfInterest);

                        // Save user data in Firestore
                        documentReference.set(user).addOnSuccessListener(aVoid -> {
                            Toast.makeText(Register.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                            // Initialize user rewards after registration
                            createUserRewards(userID);
                        }).addOnFailureListener(e ->
                                Toast.makeText(Register.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );

                        // Navigate to the Login activity
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Handle registration errors
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthUserCollisionException e) {
                            Toast.makeText(Register.this, "Email already registered.", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(Register.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        });
    }

    // This initializes the POI map with friendly names and Google Places API terms
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

    // This initializes user rewards for the new user in Firestore
    public void createUserRewards(final String userId) {
        // Fetch all reward documents from the "rewards" collection
        fstore.collection("rewards").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Iterate through each reward document
                for (QueryDocumentSnapshot rewardDoc : task.getResult()) {
                    String rewardId = rewardDoc.getId();

                    // Prepare user reward data
                    Map<String, Object> userReward = new HashMap<>();
                    userReward.put("userID", userId);
                    userReward.put("rewardID", rewardId);
                    userReward.put("progress", 0); // Set initial progress to 0
                    userReward.put("completed", false); // Mark as incomplete
                    userReward.put("tracked", false); // Mark as not tracked

                    // Save user reward data in the "user_rewards" collection
                    fstore.collection("user_rewards")
                            .add(userReward)
                            .addOnSuccessListener(docRef -> Log.d("Firestore", "User reward created for rewardID: " + rewardId))
                            .addOnFailureListener(e -> Log.e("Firestore", "Error creating user reward: " + e.getMessage()));
                }
            } else {
                // Log an error if fetching rewards fails
                Log.e("Firestore", "Error fetching rewards: " + task.getException().getMessage());
            }
        });
    }
}