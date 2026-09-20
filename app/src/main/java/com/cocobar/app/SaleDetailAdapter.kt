package com.cocobar.app

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cocobar.app.network.SaleDetailDTO
import java.text.SimpleDateFormat
import java.util.Locale

class SaleDetailAdapter(private val items: List<SaleDetailDTO>) : RecyclerView.Adapter<SaleDetailAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProductImage: ImageView = view.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvSaleTime: TextView = view.findViewById(R.id.tvSaleTime)
        val tvTotalPrice: TextView = view.findViewById(R.id.tvTotalPrice)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sale_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvProductName.text = item.productName
        holder.tvTotalPrice.text = "₹${String.format("%.2f", item.totalPrice)}"
        holder.tvQuantity.text = "Qty: ${item.quantity}"

        // Format the time (e.g., "02:30 PM")
        try {
            val formatIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatOut = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val dateObj = formatIn.parse(item.saleDate)
            if (dateObj != null) {
                holder.tvSaleTime.text = formatOut.format(dateObj)
            }
        } catch (e: Exception) {
            holder.tvSaleTime.text = item.saleDate
        }

        // Load Image securely using Glide
        if (!item.imageUrl.isNullOrBlank()) {
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(holder.ivProductImage)
        } else {
            holder.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

//        // Safe Image Loading
//        holder.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
//        item.imageUrl?.let { urlString ->
//            if (urlString.isNotBlank()) {
//                try {
//                    holder.ivProductImage.setImageURI(Uri.parse(urlString))
//                } catch (e: Exception) {
//                    holder.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
//                }
//            }
//        }
    }

    override fun getItemCount() = items.size
}