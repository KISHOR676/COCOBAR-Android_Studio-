package com.cocobar.app.network

import retrofit2.Call
import retrofit2.http.GET

// Data class to match your backend DTO
data class DashboardSummary(
    val totalInventoryItems: Long,
    val todaysSales: Double,
    val todaysProfit: Double,
    val lowStockItems: Long,
    val salesGraphData: List<Map<String, Any>>?
)

interface ApiService {
    @GET("/api/dashboard/summary")
    fun getDashboardSummary(): Call<DashboardSummary>
}