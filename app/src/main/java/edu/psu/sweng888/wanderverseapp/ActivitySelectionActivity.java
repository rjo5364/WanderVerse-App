package edu.psu.sweng888.wanderverseapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ActivitySelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        Button cyclingButton = findViewById(R.id.button_cycling);
        Button runningButton = findViewById(R.id.button_running);
        Button swimmingButton = findViewById(R.id.button_swimming);
        Button walkingButton = findViewById(R.id.button_walking);

        cyclingButton.setOnClickListener(v -> openRewardsActivity("Cycling"));
        runningButton.setOnClickListener(v -> openRewardsActivity("Running"));
        swimmingButton.setOnClickListener(v -> openRewardsActivity("Swimming"));
        walkingButton.setOnClickListener(v -> openRewardsActivity("Walking"));
    }

    private void openRewardsActivity(String category) {
        Intent intent = new Intent(this, RewardsActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }
}