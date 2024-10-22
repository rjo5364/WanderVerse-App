package edu.psu.sweng888.wanderverseapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
                            activityType = documentData["ActivityCategory"] as? String ?: "",

                            // Handling both Long and Double for "Target"
                            denominator = when (val targetValue = documentData["Target"]) {
                                is Long -> targetValue.toInt()
                                is Double -> targetValue.toInt()
                                else -> 0
                            },

                            // Handling both Long and Double for "Points"
                            points = when (val pointsValue = documentData["Points"]) {
                                is Long -> pointsValue.toInt()
                                is Double -> pointsValue.toInt()
                                else -> 0
                            },

                            // Handling both Long and Double for "Percentage"
                            percentage = when (val percentageValue = documentData["Percentage"]) {
                                is Long -> percentageValue.toFloat()
                                is Double -> percentageValue.toFloat()
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
                        adapter = RewardPaneAdapter(rewards)
                        recyclerView.adapter = adapter
                    }
                }
            }
        }
    }

}
