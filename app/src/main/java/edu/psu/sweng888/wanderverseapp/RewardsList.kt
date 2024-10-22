package edu.psu.sweng888.wanderverseapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.typeOf

class RewardsList : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RewardPaneAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rewards_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Add a vertical divider between items
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)

        // Fetch data and bind the adapter
        fetchItems()
    }

    private fun fetchItems() {
        val fb = FirebaseManager()

        fb.setCollection("rewards")
        fb.readDocuments { documents ->
            // Mutable list to store RewardPaneModel objects
            val rewards = mutableListOf<RewardModel>()

            // Variable to keep track of how many documents have been processed
            var processedDocuments = 0

            // Loop through document references
            documents.forEach { documentRef ->
                fb.setDocument(documentRef.id)  // Set the document reference

                // Fetch the document's fields
                fb.readAllFields { documentData ->
                    // Check if the data exists
                    if (documentData != null) {

                        // Convert document data to RewardPaneModel
                        val reward = RewardModel(
                            imageUrl = documentData["url"] as? String ?: "",
                            title = documentData["Title"] as? String ?: "",
                            description = documentData["Description"] as? String ?: "",
                            activityType = documentData["ActivityType"] as? String ?: "",
                            // Handling both Int, Long, and String for Denominator
                            denominator = when (val denomValue = documentData["Target"]) {
                                is Int -> denomValue
                                is Long -> denomValue.toInt()
                                is String -> denomValue.toIntOrNull() ?: 0  // Convert String to Int safely
                                else -> 0
                            },

                            // Handling both Int, Long, and String for Points
                            points = when (val pointsValue = documentData["Points"]) {
                                is Int -> pointsValue
                                is Long -> pointsValue.toInt()
                                is String -> pointsValue.toIntOrNull() ?: 0  // Convert String to Int safely
                                else -> 0
                            },

                            // Handling percentage
                            percentage = when (val percentageValue = documentData["Percentage"]) {
                                is Double -> percentageValue.toFloat()
                                is Long -> percentageValue.toFloat()
                                is String -> percentageValue.toFloatOrNull() ?: 0.00f  // Handle String to Float
                                else -> 0.00f
                            }
                        )

                        // Add the reward to the list
                        rewards.add(reward)
                    }

                    // Increment the counter for processed documents
                    processedDocuments++

                    // Once all documents have been processed, update the adapter
                    if (processedDocuments == documents.size) {
                        Log.d("RewardAdapter", "Rewards: $rewards")
                        adapter = RewardPaneAdapter(rewards) { reward ->
                            // Start RewardsDetailActivity when an item is clicked
                            val intent = Intent(this, RewardsDetail::class.java)
                            intent.putExtra("rewardTitle", reward.title)
                            intent.putExtra("rewardDescription", reward.description)
                            intent.putExtra("rewardActivityType", reward.activityType)
                            intent.putExtra("rewardImageUrl", reward.imageUrl)
                            intent.putExtra("rewardPoints", reward.points)
                            intent.putExtra("rewardDenominator", reward.denominator)
                            intent.putExtra("rewardPercentage", reward.percentage)
                            startActivity(intent)
                        }
                        recyclerView.adapter = adapter
                    }
                }
            }
        }
    }

}
