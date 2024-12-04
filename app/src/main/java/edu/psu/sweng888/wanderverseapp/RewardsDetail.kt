package edu.psu.sweng888.wanderverseapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide

class RewardsDetail : AppCompatActivity() {

    val fb: FirebaseManager = FirebaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rewards_pane_details)

        val rewardTitle = intent.getStringExtra("rewardTitle")
        val rewardDescription = intent.getStringExtra("rewardDescription")
        val rewardImageUrl = intent.getStringExtra("rewardImageUrl")
        val rewardPoints = intent.getIntExtra("rewardPoints", 0)
        val rewardPercentage = intent.getFloatExtra("rewardPercentage", 0f)
        val userRewardID = intent.getStringExtra("userRewardID")
        val rewardPercentageString = String.format("%.2f", rewardPercentage)

        if (userRewardID != null) {
            setInitialSwitchState(userRewardID)
        } else {
            Log.e("RewardsDetail", "userRewardID is null. Cannot initialize switch.")
        }

        // Choose the drawable resource dynamically based on the data class value
        val activityType = intent.getStringExtra("rewardActivityType")
        val iconViewId = when (activityType) {
            "bike" -> R.drawable.bike // Use the corresponding drawable resource
            "run" -> R.drawable.run
            else -> R.drawable.walk   // A default icon if no match is found
        }
        val activity_icon: ImageView = findViewById(R.id.activity_icon)
        activity_icon.setImageResource(iconViewId)

        // Find views and bind data
        val titleTextView: TextView = findViewById(R.id.title)
        val descriptionTextView: TextView = findViewById(R.id.description)
        val imageView: ImageView = findViewById(R.id.imageView)
        val pointsTextView: TextView = findViewById(R.id.points_value)
        val percentageTextView: TextView = findViewById(R.id.percentage_of_completions_value)

        titleTextView.text = rewardTitle
        descriptionTextView.text = rewardDescription
        pointsTextView.text = "Points: $rewardPoints"
        percentageTextView.text = "Completion: $rewardPercentageString%"

        Log.d("RewardsDetailActivity", "Reward Image URL: $rewardImageUrl")
        // Load the image using Glide
        Glide.with(this)
            .load(rewardImageUrl)
            .into(imageView)


        // get the Switch
        val rewardSwitch: SwitchCompat = findViewById(R.id.track_switch)

        // Set the listener for the Switch
        rewardSwitch.setOnCheckedChangeListener { _, isChecked ->
            userRewardID?.let { documentId ->
                onSwitchToggled(isChecked, documentId)
            } ?: Log.e("RewardsDetailActivity", "userRewardID is null, cannot toggle switch.")
        }
    }

    // Handle the switch toggle event
    private fun onSwitchToggled(isChecked: Boolean, documentId: String) {

        if (isChecked) {
            enableRewardFeature(documentId)
        } else {
            disableRewardFeature(documentId)
        }
    }

    // Created a user_reward document
    private fun enableRewardFeature(documentId: String) {
        val fb = FirebaseManager()
        fb.setCollection("user_rewards")
        fb.setDocument(documentId)
        fb.updateField("tracked", true) { isSuccess ->
            if (isSuccess) {
                Log.d("enableRewardFeature", "Successfully enabled reward feature for document ID: $documentId")
            } else {
                Log.e("enableRewardFeature", "Failed to enable reward feature for document ID: $documentId")
            }
        }
    }

    // Deleted the user_reward document
    private fun disableRewardFeature(documentId: String) {
        val fb = FirebaseManager()
        fb.setCollection("user_rewards")
        fb.setDocument(documentId)
        fb.updateField("tracked", false) { isSuccess ->
            if (isSuccess) {
                Log.d("disableRewardFeature", "Successfully disabled reward feature for document ID: $documentId")
            } else {
                Log.e("disableRewardFeature", "Failed to disable reward feature for document ID: $documentId")
            }
        }
    }

    // Is used to get the value of the user_reward to determine if switch should be switched on or off
    private fun setInitialSwitchState(documentId: String) {
        val fb = FirebaseManager()
        fb.setCollection("user_rewards") // Set the correct collection
        fb.setDocument(documentId) // Use the document ID to fetch data

        fb.readAllFields { documentData ->
            if (documentData != null) {
                val isTracked = documentData["tracked"] as? Boolean ?: false // Default to false if null
                val rewardSwitch: SwitchCompat = findViewById(R.id.track_switch)
                rewardSwitch.isChecked = isTracked // Set the switch position
                Log.d("setInitialSwitchState", "Switch set to $isTracked for document ID: $documentId")
            } else {
                Log.e("setInitialSwitchState", "Document data is null for document ID: $documentId")
            }
        }
    }


}
