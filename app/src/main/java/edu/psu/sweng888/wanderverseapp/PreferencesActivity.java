package edu.psu.sweng888.wanderverseapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PreferencesActivity extends AppCompatActivity {

    private ArrayList<CheckBox> interestCheckboxes;
    private ArrayList<CheckBox> distanceCheckboxes;
    private static final int MAX_INTEREST_SELECTIONS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // Points of Interest Checkboxes
        interestCheckboxes = new ArrayList<>();
        interestCheckboxes.add(findViewById(R.id.checkbox_parks));
        interestCheckboxes.add(findViewById(R.id.checkbox_coffee_shops));
        interestCheckboxes.add(findViewById(R.id.checkbox_museums));
        interestCheckboxes.add(findViewById(R.id.checkbox_bars));
        interestCheckboxes.add(findViewById(R.id.checkbox_restaurants));
        interestCheckboxes.add(findViewById(R.id.checkbox_theaters));
        interestCheckboxes.add(findViewById(R.id.checkbox_malls));
        interestCheckboxes.add(findViewById(R.id.checkbox_uni));
        interestCheckboxes.add(findViewById(R.id.checkbox_libraries));
        interestCheckboxes.add(findViewById(R.id.checkbox_historical_sites));

        // Distance Checkboxes
        distanceCheckboxes = new ArrayList<>();
        distanceCheckboxes.add(findViewById(R.id.checkbox_distance_short));
        distanceCheckboxes.add(findViewById(R.id.checkbox_distance_medium));
        distanceCheckboxes.add(findViewById(R.id.checkbox_distance_long));

        // Set listener to limit selections for Points of Interest
        for (CheckBox checkBox : interestCheckboxes) {
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && getSelectedInterestCount() > MAX_INTEREST_SELECTIONS) {
                    buttonView.setChecked(false);
                    Toast.makeText(this, "You can select up to " + MAX_INTEREST_SELECTIONS + " points of interest.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Set listener for Distance Preferences (only one selection allowed)
        for (CheckBox checkBox : distanceCheckboxes) {
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    for (CheckBox otherCheckbox : distanceCheckboxes) {
                        if (otherCheckbox != buttonView) {
                            otherCheckbox.setChecked(false);
                        }
                    }
                }
            });
        }

        Button saveButton = findViewById(R.id.button_save_preferences);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePreferences();
                startActivity(new Intent(PreferencesActivity.this, MapActivity.class));
                finish();
            }
        });
    }

    private int getSelectedInterestCount() {
        int count = 0;
        for (CheckBox checkBox : interestCheckboxes) {
            if (checkBox.isChecked()) count++;
        }
        return count;
    }

    private void savePreferences() {
        Set<String> selectedInterests = new HashSet<>();
        for (CheckBox checkBox : interestCheckboxes) {
            if (checkBox.isChecked()) {
                selectedInterests.add(checkBox.getText().toString());
            }
        }

        String selectedDistance = "";
        for (CheckBox checkBox : distanceCheckboxes) {
            if (checkBox.isChecked()) {
                selectedDistance = checkBox.getText().toString();
                break;
            }
        }

        // Save the preferences to shared preferences or another storage method
        getSharedPreferences("Preferences", MODE_PRIVATE).edit()
                .putStringSet("selectedInterests", selectedInterests)
                .putString("selectedDistance", selectedDistance)
                .apply();

        Toast.makeText(this, "Preferences saved!", Toast.LENGTH_SHORT).show();
    }
}