package com.cocobar.app

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cocobar.app.network.RawMaterial

class InventoryAdapter(
    private var inventoryList: List<RawMaterial>,
    private val onItemClick: (RawMaterial) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    class InventoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvItemName)
        val tvPrice: TextView = view.findViewById(R.id.tvItemPrice)
        val tvQuantity: TextView = view.findViewById(R.id.tvItemQuantity)
        val ivImage: ImageView = view.findViewById(R.id.ivItemImage) // NEW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventory_card, parent, false)
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val item = inventoryList[position]
        holder.tvName.text = item.itemName
        holder.tvPrice.text = "₹ ${item.unitPrice}"
        holder.tvQuantity.text = "Stock: ${item.currentQuantity} ${item.unitType}"

        // SAFE IMAGE LOADING TO PREVENT ContentProvider CRASHES
        holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery) // Default fallback

        item.imageUrl?.let { urlString ->
            if (urlString.isNotBlank()) {
                try {
                    val uri = Uri.parse(urlString)
                    // Only try loading content URI if accessible; catch permission errors
                    holder.ivImage.setImageURI(uri)
                } catch (e: Exception) {
                    // Fallback to placeholder if the URI belongs to another device
                    holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
        }

//        // NEW: Load Image if available
//        if (!item.imageUrl.isNullOrEmpty()) {
//            holder.ivImage.setImageURI(Uri.parse(item.imageUrl))
//        } else {
//            holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery) // Fallback placeholder
//        }

        // Trigger update popup when clicked
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = inventoryList.size
}