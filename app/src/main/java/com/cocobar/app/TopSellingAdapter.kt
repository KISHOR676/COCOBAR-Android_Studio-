package com.cocobar.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cocobar.app.network.TopSellingDTO

class TopSellingAdapter(private val items: List<TopSellingDTO>) : RecyclerView.Adapter<TopSellingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView = view.findViewById(R.id.tvRank)
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvQtySold: TextView = view.findViewById(R.id.tvQtySold)
        val tvRevenue: TextView = view.findViewById(R.id.tvRevenue)
        val tvOrders: TextView = view.findViewById(R.id.tvOrders)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_top_selling, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvRank.text = (position + 1).toString()
        holder.tvProductName.text = item.productName
        holder.tvQtySold.text = "${item.totalQuantity} Items Sold"
        holder.tvRevenue.text = "₹${String.format("%.2f", item.totalRevenue)}"
        holder.tvOrders.text = "${item.totalOrders} Orders"
    }

    override fun getItemCount() = items.size
}