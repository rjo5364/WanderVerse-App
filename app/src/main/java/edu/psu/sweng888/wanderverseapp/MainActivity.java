package edu.psu.sweng888.wanderverseapp;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;
    TextView textViewWelcome;
    FirebaseUser user;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

    // Buttons for user actions
    Button buttonMap, buttonViewRewards, buttonPreferences, buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializes Firebase and UI components
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        textViewWelcome = findViewById(R.id.user_information);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        buttonMap = findViewById(R.id.view_map);
        buttonViewRewards = findViewById(R.id.button_view_rewards);
        buttonPreferences = findViewById(R.id.button_preferences);
        buttonLogout = findViewById(R.id.buttonLogout);

        // Configures the ActionBarDrawerToggle for the navigation drawer
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Enables the ActionBar to display the navigation drawer toggle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Sets up navigation menu item click handling
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        // Checks if the user is logged in, otherwise navigates to Login
        user = auth.getCurrentUser();
        if (user == null) {
            navigateToLogin();
        } else {
            fetchUserDetails();
        }

        // Configures button click listeners
        buttonMap.setOnClickListener(view -> startActivity(new Intent(this, MapActivity.class)));
        buttonViewRewards.setOnClickListener(view -> startActivity(new Intent(this, RewardsList.class)));
        buttonPreferences.setOnClickListener(view -> startActivity(new Intent(this, PreferencesActivity.class)));
        buttonLogout.setOnClickListener(view -> {
            auth.signOut();
            navigateToLogin();
        });
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handles navigation menu item selections
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            startActivity(new Intent(this, MapActivity.class));
        } else if (id == R.id.nav_rewards) {
            startActivity(new Intent(this, RewardsList.class));
        } else if (id == R.id.nav_preferences) {
            startActivity(new Intent(this, PreferencesActivity.class));
        } else if (id == R.id.nav_logout) {
            auth.signOut();
            navigateToLogin();
        }

        drawerLayout.closeDrawers(); // Closes the navigation drawer
        return true;
    }

    private void fetchUserDetails() {
        // Fetches user details from Firestore
        String userId = user.getUid();
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                String fName = document.getString("fName");
                String lName = document.getString("lName");

                // Updates the welcome message
                textViewWelcome.setText(fName != null && lName != null
                        ? String.format("Welcome %s %s!", fName, lName)
                        : "Welcome User!");
            } else {
                // Displays a default welcome message if fetching fails
                textViewWelcome.setText("Welcome User!");
            }
        });
    }

    private void navigateToLogin() {
        // Navigates to the Login activity
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handles the navigation drawer toggle
        return toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
}