package edu.psu.sweng888.wanderverseapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlin.reflect.typeOf

class RewardsList : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RewardPaneAdapter

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rewards_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)

        // Initialize the DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        // Sets up ActionBarDrawerToggle to handle drawer opening/closing
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Handles navi item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_main -> startActivity(Intent(this, MainActivity::class.java))
                R.id.nav_map -> startActivity(Intent(this, MapActivity::class.java))
                R.id.nav_rewards -> {} // Already in RewardsList, do nothing
                R.id.nav_preferences -> startActivity(Intent(this, PreferencesActivity::class.java))
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, Login::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawers() // Close drawer after selecting an item
            true
        }

        // Initialize the spinner
        Log.d("RewardsList", "Spinner Initialized.")
        val filterSpinner: Spinner = findViewById(R.id.filterSpinner)
        val filterOptions = listOf("All","Run", "Walk", "Bike", "Tracked", "Completed")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = spinnerAdapter

        // Fetch user rewards first, then fetch main rewards
        fetchUserRewards {
            Log.d("RewardsList", "User rewards fetched.")
            fetchItems { rewards ->
                adapter = RewardPaneAdapter(rewards.toMutableList(), userRewardMap) { reward ->
                    val intent = Intent(this, RewardsDetail::class.java)
                    intent.putExtra("rewardTitle", reward.title)
                    intent.putExtra("rewardDescription", reward.description)
                    intent.putExtra("rewardActivityType", reward.activityType)
                    intent.putExtra("rewardImageUrl", reward.imageUrl)
                    intent.putExtra("rewardPoints", reward.points)
                    intent.putExtra("rewardDenominator", reward.denominator)
                    intent.putExtra("rewardPercentage", reward.percentage)
                    // Helpful incase userReward fails to load
                    val userRewardID = userRewardMap[reward.id]?.id // Get userRewardID from the map
                    if (userRewardID != null) {
                        intent.putExtra("userRewardID", userRewardID)
                        Log.d("RewardNavigation", "Passing userRewardID: $userRewardID")
                    } else {
                        Log.e("RewardNavigation", "No userRewardID found for reward ID: ${reward.id}")
                    }
                    startActivity(intent)
                }
                recyclerView.adapter = adapter

                // Set up filtering logic
                filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val selectedFilter = filterOptions[position]
                        val filteredRewards = when (selectedFilter) {
                            "All" -> rewards.filter { reward ->
                                userRewardMap[reward.id]?.completed == false
                            }
                            "Tracked" -> rewards.filter { reward ->
                                userRewardMap[reward.id]?.tracked == true
                            }
                            "Completed" -> rewards.filter { reward ->
                                userRewardMap[reward.id]?.completed == true
                            }
                            "Run" -> rewards.filter { reward ->
                                reward.activityType == "run"
                            }
                            "Walk" -> rewards.filter { reward ->
                                reward.activityType == "walk"
                            }
                            "Bike" -> rewards.filter { reward ->
                                reward.activityType == "bike"
                            }
                            else -> rewards.filter { reward -> reward.activityType == selectedFilter }
                        }
                        Log.d("RewardsList", "Filtered rewards for '$selectedFilter': $filteredRewards")
                        adapter.updateList(filteredRewards)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }
    }
    //toggles nav menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    private fun fetchItems(callback: (List<RewardModel>) -> Unit) {
        val fb = FirebaseManager()

        fb.setCollection("rewards")
        fb.readDocuments { documents ->
            if (documents.isEmpty()) {
                Log.e("fetchItems", "No documents found in rewards collection.")
            } else {
                Log.d("fetchItems", "Documents fetched: ${documents.size}")
            }

            val rewards = mutableListOf<RewardModel>()
            var processedDocuments = 0

            documents.forEach { documentRef ->
                fb.setDocument(documentRef.id)

                fb.readAllFields { documentData ->
                    if (documentData != null) {
                        Log.d("fetchItems", "Document ID: ${documentRef.id}, Data: $documentData")
                        val reward = RewardModel(
                            id = documentRef.id,
                            imageUrl = documentData["url"] as? String ?: "",
                            title = documentData["Title"] as? String ?: "",
                            description = documentData["Description"] as? String ?: "",
                            activityType = documentData["ActivityType"] as? String ?: "",
                            denominator = when (val denomValue = documentData["Target"]) {
                                is Int -> denomValue
                                is Long -> denomValue.toInt()
                                is String -> denomValue.toIntOrNull() ?: 0
                                else -> 0
                            },
                            points = when (val pointsValue = documentData["Points"]) {
                                is Int -> pointsValue
                                is Long -> pointsValue.toInt()
                                is String -> pointsValue.toIntOrNull() ?: 0
                                else -> 0
                            },
                            percentage = when (val percentageValue = documentData["Percentage"]) {
                                is Double -> percentageValue.toFloat()
                                is Long -> percentageValue.toFloat()
                                is String -> percentageValue.toFloatOrNull() ?: 0.00f
                                else -> 0.00f
                            }
                        )
                        rewards.add(reward)
                    } else {
                        Log.e("fetchItems", "Document data is null for ID: ${documentRef.id}")
                    }

                    processedDocuments++

                    if (processedDocuments == documents.size) {
                        callback(rewards)
                    }
                }
            }
        }
    }



    private lateinit var userRewardMap: Map<String, UserRewardModel>

    private fun fetchUserRewards(callback: () -> Unit) {
        val fb = FirebaseManager()
        var processedDocuments = 0

        Log.d("fetchUserRewards", "Fetching user rewards...")

        fb.setCollection("user_rewards") // Secondary collection for UserRewardModel
        fb.readDocuments { documents ->
            val userRewardTempMap = mutableMapOf<String, UserRewardModel>()

            documents.forEach { documentRef ->
                fb.setDocument(documentRef.id)
                fb.readAllFields { documentData ->
                    if (documentData != null) {
                        val userReward = UserRewardModel(
                            completed = documentData["completed"] as? Boolean ?: false,
                            progress = documentData["progress"] as? Int ?: 0,
                            rewardID = documentData["rewardID"] as? String ?: "",
                            tracked = documentData["tracked"] as? Boolean ?: false,
                            userID = documentData["userID"] as? String ?: "",
                            id = documentRef.id
                        )
                        Log.d("fetchUserRewards", "User reward fetched: $userReward")
                        userRewardTempMap[userReward.rewardID] = userReward
                    }
                    processedDocuments++

                    // Invoke callback once all documents are processed
                    if (processedDocuments == documents.size) {
                        userRewardMap = userRewardTempMap
                        Log.d("fetchUserRewards", "All user rewards processed. Invoking callback.")
                        callback()
                    }
                }
            }
        }
    }



}
