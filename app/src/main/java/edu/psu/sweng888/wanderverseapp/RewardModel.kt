package edu.psu.sweng888.wanderverseapp

data class RewardModel(
    val id: String,
    val imageUrl: String,
    val title: String,
    val description: String,
    val activityType: String,
    val points: Int,
    val denominator: Int,
    val percentage: Float,
)


data class UserRewardModel(
    val id: String,
    val completed: Boolean,
    val progress: Int,
    val rewardID: String,
    val tracked: Boolean,
    val userID: String,
)