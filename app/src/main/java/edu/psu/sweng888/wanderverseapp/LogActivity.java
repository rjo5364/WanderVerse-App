package edu.psu.sweng888.wanderverseapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LogActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;

    private Spinner activityCategorySpinner;
    private EditText activityDistance, startLocation, endLocation, dateCompleted;
    private Button startActivityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logger);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        //checks auth server
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        activityCategorySpinner = findViewById(R.id.activity_category_spinner);
        activityDistance = findViewById(R.id.activity_distance);
        startLocation = findViewById(R.id.start_location);
        endLocation = findViewById(R.id.end_location);
        dateCompleted = findViewById(R.id.date_completed);
        startActivityButton = findViewById(R.id.button_start_activity);

        startActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logActivity();
            }
        });
    }

    private void logActivity() {
        String category = activityCategorySpinner.getSelectedItem().toString();
        String distanceString = activityDistance.getText().toString();
        String startLocationString = startLocation.getText().toString();
        String endLocationString = endLocation.getText().toString();
        String dateCompletedString = dateCompleted.getText().toString();

        //null checker
        if (distanceString.isEmpty() || startLocationString.isEmpty() || endLocationString.isEmpty() || dateCompletedString.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        double distance = Double.parseDouble(distanceString);

        // Parses date completed from string
        final Date completedDate; // had to make final for use in onRewardIdRecieved
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            completedDate = sdf.parse(dateCompletedString);
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format. Use YYYY-MM-DD.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieves the RewardActivityId before logging the activity
        getRewardActivityId(category, new OnRewardIdReceivedListener() {
            @Override
            public void onRewardIdReceived(String rewardActivityId) {
                if (rewardActivityId != null) {
                    // Create activity data to log in Firestore
                    Map<String, Object> activityData = new HashMap<>();
                    activityData.put("UserId", user.getUid());
                    activityData.put("ActivityID", db.collection("activities").document().getId());
                    activityData.put("RewardActivityId", rewardActivityId);  // Set the correct RewardActivityId
                    activityData.put("ActivityCategory", category);
                    activityData.put("ActivityDistance", distance);
                    activityData.put("DateStarted", Timestamp.now());  // You could add a start date field if needed
                    activityData.put("DateCompleted", new Timestamp(completedDate));
                    activityData.put("StartLocation", startLocationString);
                    activityData.put("EndLocation", endLocationString);

                    db.collection("activities").add(activityData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(LogActivity.this, "Activity logged successfully!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(LogActivity.this, "Failed to log activity: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // Handles case where no reward is found for the category
                    Toast.makeText(LogActivity.this, "No active reward found for this category.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Fetches the RewardActivityId asynchronously from Firestore
    private void getRewardActivityId(String category, OnRewardIdReceivedListener listener) {
        db.collection("rewards")
                .whereEqualTo("UserId", user.getUid())
                .whereEqualTo("ActivityCategory", category)
                .whereEqualTo("Tracked", true)  // Find the reward the user is currently tracking
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Gets the first reward the user is currently woring on for the activity category
                        String rewardActivityId = task.getResult().getDocuments().get(0).getString("RewardId");
                        listener.onRewardIdReceived(rewardActivityId);
                    } else {
                        // Handles the case where no active reward is found
                        listener.onRewardIdReceived(null);  // Return null when no reward is found
                    }
                });
    }

    // Receiving the reward ID on Async
    public interface OnRewardIdReceivedListener {
        void onRewardIdReceived(String rewardActivityId);
    }

    private void updateProgressForReward(String rewardActivityId, double distance) {
        if (rewardActivityId == null || rewardActivityId.isEmpty()) {
            Toast.makeText(LogActivity.this, "Invalid RewardActivityId!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch the document asynchronously
        db.collection("rewards").document(rewardActivityId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            Long currentProgress = documentSnapshot.getLong("ProgressNumerator");
                            Long target = documentSnapshot.getLong("Target");

                            if (currentProgress == null || target == null) {
                                Toast.makeText(LogActivity.this, "Missing ProgressNumerator or Target!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            long newProgress = currentProgress + (long) distance;

                            // Prepare the data to update
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("ProgressNumerator", newProgress);

                            if (newProgress >= target) {
                                updates.put("Completed", true);
                            }

                            // Now asynchronously update the document with new progress
                            db.collection("rewards").document(rewardActivityId)
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(LogActivity.this, "Progress updated successfully!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(LogActivity.this, "Failed to update progress: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(LogActivity.this, "Reward document does not exist!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LogActivity.this, "Error fetching reward: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}

