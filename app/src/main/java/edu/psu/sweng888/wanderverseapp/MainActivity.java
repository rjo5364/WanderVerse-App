package edu.psu.sweng888.wanderverseapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import edu.psu.sweng888.wanderverseapp.FirebaseManager;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    Button button, buttonSelectActivity, buttonActivityLogging, buttonViewRewards;
    TextView textView;
    FirebaseUser user;
    private FirebaseManager firebaseManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        firebaseManager = new FirebaseManager();

        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.buttonLogout);
        textView = findViewById(R.id.user_information);
        user = auth.getCurrentUser();
        buttonSelectActivity = findViewById(R.id.button_select_activity);
        buttonActivityLogging = findViewById(R.id.button_log_activity);
        buttonViewRewards = findViewById(R.id.button_view_rewards);
        if (user == null) {

            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            textView.setText(user.getEmail());
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        // Handles activity selection button click
        buttonSelectActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to the Activity Selection screen
                Intent intent = new Intent(MainActivity.this, ActivitySelectionActivity.class);
                startActivity(intent);
            }
        });

        // Handles activity selection button click
        buttonActivityLogging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigates to the Activity Selection screen
                Intent intent = new Intent(MainActivity.this, LogActivity.class);
                startActivity(intent);
            }
        });

        // Handle view rewards button click
        buttonViewRewards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to the View Rewards screen
                Intent intent = new Intent(MainActivity.this, RewardsList.class);
                startActivity(intent);
            }
        });
    }
}
