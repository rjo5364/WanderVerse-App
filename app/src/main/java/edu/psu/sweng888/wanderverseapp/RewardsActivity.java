package edu.psu.sweng888.wanderverseapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RewardsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rewards_activity);

        db = FirebaseFirestore.getInstance();

        // Gets the current authenticated user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Checks if the user is logged in and retrieve the userId (UID)
        if (user != null) {
            userId = user.getUid();
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity and return to the login screen
            return;
        }

        TextView categoryTitle = findViewById(R.id.category_title);
        CheckBox rewardCheckbox = findViewById(R.id.reward_checkbox);
        Button enrollButton = findViewById(R.id.enroll_button);

        String category = getIntent().getStringExtra("category");
        categoryTitle.setText("Select your " + category + " reward");

        // rewards based on category using a single XML layout
        switch (category) {
            case "Cycling":
                rewardCheckbox.setText("Reward: Cycling 100 miles");
                break;
            case "Running":
                rewardCheckbox.setText("Reward: Running 50 miles");
                break;
            case "Swimming":
                rewardCheckbox.setText("Reward: Swimming 50 miles");
                break;
            case "Walking":
                rewardCheckbox.setText("Reward: Walking 30 miles");
                break;
        }

        enrollButton.setOnClickListener(v -> {
            if (rewardCheckbox.isChecked()) {
                enrollInReward(category);
            }
        });
    }

    private void enrollInReward(String category) {
        Map<String, Object> rewardData = new HashMap<>();
        rewardData.put("UserId", userId);  // Set the userId dynamically
        rewardData.put("Tracked", true);
        rewardData.put("Completed", false);
        rewardData.put("ProgressNumerator", 0);  // Start at 0
        rewardData.put("ActivityCategory", category);

        // Sets targets based on the category
        switch (category) {
            case "Cycling":
                rewardData.put("Target", 100);
                rewardData.put("RewardId", "cycling_100_miles");
                break;
            case "Running":
                rewardData.put("Target", 50);
                rewardData.put("RewardId", "running_50_miles");
                break;
            case "Swimming":
                rewardData.put("Target", 50);
                rewardData.put("RewardId", "swimming_50_miles");
                break;
            case "Walking":
                rewardData.put("Target", 30);
                rewardData.put("RewardId", "walking_30_miles");
                break;
        }

        db.collection("rewards").add(rewardData)
                .addOnSuccessListener(documentReference -> Toast.makeText(this, "Enrolled in Reward!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}