package edu.psu.sweng888.wanderverseapp

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class RewardsDetail : AppCompatActivity() {

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

        // Load the image using Glide
        Glide.with(this)
            .load(rewardImageUrl)
            .into(imageView)
    }
}
