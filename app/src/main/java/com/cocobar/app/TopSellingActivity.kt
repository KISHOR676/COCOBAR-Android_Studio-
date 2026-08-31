package com.cocobar.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocobar.app.databinding.ActivityTopSellingBinding
import com.cocobar.app.network.CocobarApi
import com.cocobar.app.network.TopSellingDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class TopSellingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopSellingBinding
    private val calendar = Calendar.getInstance()
    private var startDateStr = getFormattedDate(-7)
    private var endDateStr = getFormattedDate(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopSellingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.rvTopSelling.layoutManager = LinearLayoutManager(this)

        updateDateUI()
        fetchTopSelling()

        binding.btnDateFilter.setOnClickListener {
            showDatePicker { selectedDate ->
                startDateStr = selectedDate
                endDateStr = selectedDate
                updateDateUI()
                fetchTopSelling()
            }
        }
    }

    private fun fetchTopSelling() {
        CocobarApi.create().getTopSelling(startDateStr, endDateStr).enqueue(object : Callback<List<TopSellingDTO>> {
            override fun onResponse(call: Call<List<TopSellingDTO>>, response: Response<List<TopSellingDTO>>) {
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    binding.rvTopSelling.adapter = TopSellingAdapter(data)
                }
            }
            override fun onFailure(call: Call<List<TopSellingDTO>>, t: Throwable) {
                Toast.makeText(this@TopSellingActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val datePicker = DatePickerDialog(this, { _, year, month, day ->
            val formatted = String.format("%04d-%02d-%02d", year, month + 1, day)
            onDateSelected(formatted)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    private fun getFormattedDate(offsetDays: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    private fun updateDateUI() {
        binding.tvDateRange.text = if (startDateStr == endDateStr) startDateStr else "$startDateStr to $endDateStr"
    }
}