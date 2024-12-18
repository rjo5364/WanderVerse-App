package edu.psu.sweng888.wanderverseapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// Custom Adapter class
class RewardPaneAdapter(
    private var items: MutableList<RewardModel>,  // Mutable list of rewards
    private val userRewardMap: Map<String, UserRewardModel>, // Pass userRewardMap for states
    private val onItemClick: (RewardModel) -> Unit,  // Lambda function for item clicks
) : RecyclerView.Adapter<RewardPaneAdapter.ItemViewHolder>() {

    // ViewHolder class that holds references to each view in the item layout
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
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

        // Set border color based on userRewardMap
        val userReward = userRewardMap[reward.id]
        val borderColor = when {
            userReward?.completed == true -> R.color.green_border
            userReward?.tracked == true -> R.color.yellow_border
            else -> R.color.default_border
        }

        // Apply the border color dynamically
        holder.cardView.setCardBackgroundColor(
            holder.cardView.context.resources.getColor(borderColor, null)
        )

        // Choose the drawable resource dynamically based on the data class value
        val iconViewId = when (reward.activityType) {
            "bike" -> R.drawable.bike // Use the corresponding drawable resource
            "run" -> R.drawable.run
            else -> R.drawable.walk   // A default icon if no match is found
        }
        holder.iconView.setImageResource(iconViewId)

        Log.d("Reward", "reward: $reward")
        Log.d("ImageURLDebug", "Image URL: ${reward.imageUrl}")
        // Load reward image URL using Glide
        Glide.with(holder.imageView.context)
            .load(reward.imageUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
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

    // Add this method to dynamically update the list
    fun updateList(newItems: List<RewardModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
