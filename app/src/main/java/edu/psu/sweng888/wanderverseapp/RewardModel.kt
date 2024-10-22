package edu.psu.sweng888.wanderverseapp

data class RewardModel(
    val imageUrl: String,
    val title: String,
    val description: String,
    val activityType: String,
    val points: Int,
    val denominator: Int,
    val percentage: Float,
)
