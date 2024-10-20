package edu.psu.sweng888.wanderverseapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide




// Custom Adapter class
class RewardPaneAdapter (private val items: List<RewardPaneModel>) : RecyclerView.Adapter<RewardPaneAdapter.ItemViewHolder>() {

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
        // Inflate the item_layout.xml to create the item view
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
        holder.numberView.text = reward.number.toString()


        // Load icon URL using Glide
        Glide.with(holder.iconView.context)
            .load(reward.iconUrl)
            .into(holder.iconView)

        // Load reward image URL using Glide
        Glide.with(holder.imageView.context)
            .load(reward.imageUrl)
            .into(holder.imageView)
    }

    // Returns the total number of items in the list
    override fun getItemCount(): Int {
        return items.size
    }
}
