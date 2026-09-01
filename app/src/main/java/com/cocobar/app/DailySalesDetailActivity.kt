package com.cocobar.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocobar.app.databinding.ActivityDailySalesDetailBinding
import com.cocobar.app.network.CocobarApi
import com.cocobar.app.network.SaleDetailDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DailySalesDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDailySalesDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailySalesDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedDate = intent.getStringExtra("SELECTED_DATE") ?: return

        binding.toolbar.title = "Sales for $selectedDate"
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.rvDetailedSales.layoutManager = LinearLayoutManager(this)

        fetchDetailedSales(selectedDate)
    }

    private fun fetchDetailedSales(date: String) {
        CocobarApi.create().getDailyDetailedSales(date).enqueue(object : Callback<List<SaleDetailDTO>> {
            override fun onResponse(call: Call<List<SaleDetailDTO>>, response: Response<List<SaleDetailDTO>>) {
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()

                    // THIS IS THE FIX: Plug the data into the new adapter!
                    binding.rvDetailedSales.adapter = SaleDetailAdapter(data)
                    // You can reuse your existing Product/Sell adapter here or create a simple one
                    // binding.rvDetailedSales.adapter = SaleDetailAdapter(data)
                }
            }

            override fun onFailure(call: Call<List<SaleDetailDTO>>, t: Throwable) {
                Toast.makeText(this@DailySalesDetailActivity, "Failed to load details", Toast.LENGTH_SHORT).show()
            }
        })
    }
}