package edu.psu.sweng888.wanderverseapp

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
        percentageTextView.text = "Completion: $rewardPercentage%"

        Log.d("RewardsDetailActivity", "Reward Image URL: $rewardImageUrl")
        // Load the image using Glide
        Glide.with(this)
            .load(rewardImageUrl)
            .into(imageView)


        // get the Switch
        val rewardSwitch: SwitchCompat = findViewById(R.id.track_switch)

        // Set the listener for the Switch
        rewardSwitch.setOnCheckedChangeListener { _, isChecked ->
            onSwitchToggled(isChecked)
        }
    }

    // Handle the switch toggle event
    private fun onSwitchToggled(isChecked: Boolean) {
        if (isChecked) {
            enableRewardFeature()
        } else {
            disableRewardFeature()
        }
    }

    // Created a user_reward document
    private fun enableRewardFeature() {
        fb.setCollection("user_rewards")

    }

    // Deleted the user_reward document
    private fun disableRewardFeature() {
        // Implement what happens when the switch is OFF
        Log.d("RewardsDetailActivity", "Reward feature disabled")
        // Add any additional functionality you want here
    }
}
