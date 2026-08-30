package com.cocobar.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        holder.tvQuantity.text = "${item.currentQuantity} ${item.unitType}"

        // Trigger update popup when clicked
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = inventoryList.size

    fun updateData(newList: List<RawMaterial>) {
        inventoryList = newList
        notifyDataSetChanged()
    }
}