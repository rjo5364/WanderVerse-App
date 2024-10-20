package edu.psu.sweng888.wanderverseapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView



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
        val item = items[position]

        // Bind data to views
        holder.titleView.text = item.title
        holder.descriptionView.text = item.description
        holder.numberView.text = item.number.toString()
        holder.imageView.setImageURI(item.imageUrl.toUri())
        holder.iconView.setImageURI(item.iconUrl.toUri())

    }

    // Returns the total number of items in the list
    override fun getItemCount(): Int {
        return items.size
    }
}
