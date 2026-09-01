package com.cocobar.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cocobar.app.network.DailySaleDTO
import java.text.SimpleDateFormat
import java.util.Locale

class DailySaleAdapter(
    private val items: List<DailySaleDTO>,
    private val onDateClick: (String) -> Unit // ADDED: Click listener parameter
) : RecyclerView.Adapter<DailySaleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDateDayNum: TextView = view.findViewById(R.id.tvDateDayNum)
        val tvDateMonthYear: TextView = view.findViewById(R.id.tvDateMonthYear)
        val tvDayOfWeek: TextView = view.findViewById(R.id.tvDayOfWeek)
        val tvDailyRevenue: TextView = view.findViewById(R.id.tvDailyRevenue)
        val tvDailyStats: TextView = view.findViewById(R.id.tvDailyStats)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_daily_sale, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        try {
            val formatIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateObj = formatIn.parse(item.date)
            if (dateObj != null) {
                holder.tvDateDayNum.text = SimpleDateFormat("dd", Locale.getDefault()).format(dateObj)
                holder.tvDateMonthYear.text = SimpleDateFormat("MMM\nyyyy", Locale.getDefault()).format(dateObj)
                holder.tvDayOfWeek.text = SimpleDateFormat("EEEE", Locale.getDefault()).format(dateObj)
            }
        } catch (e: Exception) {
            holder.tvDayOfWeek.text = item.date
        }

        holder.tvDailyRevenue.text = "₹${String.format("%.2f", item.dailyTotal)}"
        holder.tvDailyStats.text = "${item.totalOrders} Orders • ${item.itemsSold} Items"

        // ADDED: Make the entire row clickable and pass the date back to the Activity
        holder.itemView.setOnClickListener {
            onDateClick(item.date)
        }
    }

    override fun getItemCount() = items.size
}




//
//
//
//package com.cocobar.app
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.cocobar.app.network.DailySaleDTO
//import java.text.SimpleDateFormat
//import java.util.Locale
//
//class DailySaleAdapter(private val sales: List<DailySaleDTO>, private val onDateClick: (String) -> Unit) : RecyclerView.Adapter<DailySaleAdapter.ViewHolder>() {
//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val tvDateDayNum: TextView = view.findViewById(R.id.tvDateDayNum)
//        val tvDateMonthYear: TextView = view.findViewById(R.id.tvDateMonthYear)
//        val tvDayOfWeek: TextView = view.findViewById(R.id.tvDayOfWeek)
//        val tvDailyRevenue: TextView = view.findViewById(R.id.tvDailyRevenue)
//        val tvDailyStats: TextView = view.findViewById(R.id.tvDailyStats)
//    }
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_daily_sale, parent, false)
//        return ViewHolder(view)
//    }
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val item = items[position]
//        try {
//            val formatIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            val dateObj = formatIn.parse(item.date)
//            if (dateObj != null) {
//                holder.tvDateDayNum.text = SimpleDateFormat("dd", Locale.getDefault()).format(dateObj)
//                holder.tvDateMonthYear.text = SimpleDateFormat("MMM\nyyyy", Locale.getDefault()).format(dateObj)
//                holder.tvDayOfWeek.text = SimpleDateFormat("EEEE", Locale.getDefault()).format(dateObj)
//            }
//        } catch (e: Exception) {
//            holder.tvDayOfWeek.text = item.date
//        }
//        holder.tvDailyRevenue.text = "₹${String.format("%.2f", item.dailyTotal)}"
//        holder.tvDailyStats.text = "${item.totalOrders} Orders • ${item.itemsSold} Items"
//
//        holder.itemView.setOnClickListener {
//            onDateClick(item.date) //
//    }
//    override fun getItemCount() = items.size
//}