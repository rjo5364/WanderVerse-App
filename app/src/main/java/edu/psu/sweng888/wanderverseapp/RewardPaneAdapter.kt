package edu.psu.sweng888.wanderverseapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// Custom Adapter class
class RewardPaneAdapter(
    private val items: List<RewardModel>,  // List of rewards
    private val onItemClick: (RewardModel) -> Unit  // Lambda function for item clicks
) : RecyclerView.Adapter<RewardPaneAdapter.ItemViewHolder>() {

    // ViewHolder class that holds references to each view in the item layout
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_image)
        val titleView: TextView = itemView.findViewById(R.id.item_title)
        val descriptionView: TextView = itemView.findViewById(R.id.item_description)
        val iconView: ImageView = itemView.findViewById(R.id.item_icon)
        val numberView: TextView = itemView.findViewById(R.id.item_number)
    }

    // Called when RecyclerView needs a new ViewHolder to represent an item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // Inflate the item layout
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rewards_reward_pane, parent, false)
        return ItemViewHolder(view)
    }

    // Called by RecyclerView to display data at the specified position
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        // Get the current item from the list
        val reward = items[position]

        // Bind data to views
        holder.titleView.text = reward.title
        holder.descriptionView.text = reward.description
        holder.numberView.text = reward.denominator.toString()

        // Choose the drawable resource dynamically based on the data class value
        val iconViewId = when (reward.activityType) {
            "bike" -> R.drawable.bike // Use the corresponding drawable resource
            "run" -> R.drawable.run
            else -> R.drawable.walk   // A default icon if no match is found
        }
        holder.iconView.setImageResource(iconViewId)

        // Load reward image URL using Glide
        Glide.with(holder.imageView.context)
            .load(reward.imageUrl)
            .into(holder.imageView)

        // Set the click listener to handle the item click
        holder.itemView.setOnClickListener {
            onItemClick(reward)  // Pass the clicked reward to the lambda function
        }
    }

    // Returns the total number of items in the list
    override fun getItemCount(): Int {
        return items.size
    }
}
