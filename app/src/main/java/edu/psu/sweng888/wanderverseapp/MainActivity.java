package edu.psu.sweng888.wanderverseapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth; // Handles Firebase Authentication
    FirebaseFirestore db; // Handles Firestore database operations
    Button buttonLogout, buttonViewRewards, buttonPreferences, buttonMap; // Buttons for user actions
    TextView textViewWelcome; // Displays welcome message
    FirebaseUser user; // Represents the logged-in user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find views by ID
        buttonLogout = findViewById(R.id.buttonLogout);
        textViewWelcome = findViewById(R.id.user_information);
        buttonViewRewards = findViewById(R.id.button_view_rewards);
        buttonPreferences = findViewById(R.id.button_preferences);
        buttonMap = findViewById(R.id.view_map);

        // Get the currently logged-in user
        user = auth.getCurrentUser();

        if (user == null) {
            // If no user is logged in, redirect to the Login activity
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            // Fetch user's first and last name from Firestore
            fetchUserDetails();
        }

        // Logout button click
        buttonLogout.setOnClickListener(view -> {
            // Sign out the user and navigate to Login activity
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        // View rewards button click
        buttonViewRewards.setOnClickListener(view -> {
            // Navigate to the RewardsList activity
            Intent intent = new Intent(MainActivity.this, RewardsList.class);
            startActivity(intent);
        });

        // Preferences button click
        buttonPreferences.setOnClickListener(view -> {
            // Navigate to the PreferencesActivity
            Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
            startActivity(intent);
        });

        // Map button click
        buttonMap.setOnClickListener(view -> {
            // Navigate to the MapActivity
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });
    }

    private void fetchUserDetails() {
        // Get the user ID from the logged-in user
        String userId = user.getUid();

        // Fetch the user's document from the "users" collection in Firestore
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // Retrieve the first and last name from Firestore
                DocumentSnapshot document = task.getResult();
                String fName = document.getString("fName");
                String lName = document.getString("lName");

                // Set the welcome message
                if (fName != null && lName != null) {
                    textViewWelcome.setText(String.format("Welcome %s %s!", fName, lName));
                } else {
                    textViewWelcome.setText("Welcome User!");
                }
            } else {
                // If the fetch fails, show a default message
                textViewWelcome.setText("Welcome User!");
            }
        });
    }
}