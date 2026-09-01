package com.cocobar.app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocobar.app.databinding.ActivitySalesHistoryBinding
import com.cocobar.app.network.CocobarApi
import com.cocobar.app.network.DailySaleDTO
import com.cocobar.app.network.SalesOverviewDTO
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class SalesHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalesHistoryBinding
    private lateinit var drawerLayout: DrawerLayout
    private val calendar = Calendar.getInstance()

    private var startDateStr = getFormattedDate(-7)
    private var endDateStr = getFormattedDate(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalesHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Setup the Sidebar
        setupNavigationDrawer()

        // 2. Setup the rest of the UI
        binding.btnOpenTopSelling.setOnClickListener {
            startActivity(Intent(this, TopSellingActivity::class.java))
        }

        binding.rvDailySales.layoutManager = LinearLayoutManager(this)

        updateDateUI()
        fetchAllData()

        binding.btnDateFilter.setOnClickListener {
            showDatePicker { selectedDate ->
                startDateStr = selectedDate
                endDateStr = selectedDate
                updateDateUI()
                fetchAllData()
            }
        }
    }

    private fun fetchAllData() {
        fetchOverview()
        fetchDailySalesHistory()
    }

    private fun fetchOverview() {
        CocobarApi.create().getSalesOverview(startDateStr, endDateStr).enqueue(object : Callback<SalesOverviewDTO> {
            override fun onResponse(call: Call<SalesOverviewDTO>, response: Response<SalesOverviewDTO>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        binding.tvTotalSales.text = "₹${String.format("%.2f", data.totalSales)}"
                        binding.tvTotalOrders.text = data.totalOrders.toString()
                        binding.tvItemsSold.text = data.itemsSold.toString()
                        binding.tvAvgOrderValue.text = "₹${String.format("%.2f", data.avgOrderValue)}"
                    }
                }
            }
            override fun onFailure(call: Call<SalesOverviewDTO>, t: Throwable) {
                Toast.makeText(this@SalesHistoryActivity, "Failed to load overview", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchDailySalesHistory() {
        CocobarApi.create().getDailySales(startDateStr, endDateStr).enqueue(object : Callback<List<DailySaleDTO>> {
            override fun onResponse(call: Call<List<DailySaleDTO>>, response: Response<List<DailySaleDTO>>) {
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()

                    // UPDATED: Pass the click listener into the adapter to open the detail page
                    binding.rvDailySales.adapter = DailySaleAdapter(data) { clickedDate ->
                        val intent = Intent(this@SalesHistoryActivity, DailySalesDetailActivity::class.java)
                        intent.putExtra("SELECTED_DATE", clickedDate)
                        startActivity(intent)
                    }
                }
            }
            override fun onFailure(call: Call<List<DailySaleDTO>>, t: Throwable) {
                Toast.makeText(this@SalesHistoryActivity, "Failed to load daily sales", Toast.LENGTH_SHORT).show()
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

    private fun setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.nav_open, R.string.nav_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navView = findViewById<NavigationView>(R.id.nav_view)

        navView.setCheckedItem(R.id.nav_saleshistory)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                }
                R.id.nav_inventory -> {
                    val intent = Intent(this, InventoryActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                }
                R.id.nav_recepi -> {
                    val intent = Intent(this, RecipeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                }
                R.id.nav_product -> {
                    val intent = Intent(this, ProductsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                }
                R.id.nav_saleshistory -> {
                    // Already here, do nothing
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Opening Settings...", Toast.LENGTH_SHORT).show()
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        val btnLogout = findViewById<android.widget.LinearLayout>(R.id.btn_custom_logout)
        btnLogout.setOnClickListener {
            val sharedPref = getSharedPreferences("CocobarPrefs", MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}








//package com.cocobar.app
//
//import android.app.DatePickerDialog
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Toast
//import androidx.appcompat.app.ActionBarDrawerToggle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.drawerlayout.widget.DrawerLayout
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.cocobar.app.databinding.ActivitySalesHistoryBinding
//import com.cocobar.app.network.CocobarApi
//import com.cocobar.app.network.DailySaleDTO
//import com.cocobar.app.network.SalesOverviewDTO
//import com.google.android.material.navigation.NavigationView
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import java.text.SimpleDateFormat
//import java.util.*
//
//class SalesHistoryActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivitySalesHistoryBinding
//    private lateinit var drawerLayout: DrawerLayout
//    private val calendar = Calendar.getInstance()
//
//    private var startDateStr = getFormattedDate(-7)
//    private var endDateStr = getFormattedDate(0)
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivitySalesHistoryBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // 1. Setup the Sidebar (This takes control of the hamburger menu)
//        setupNavigationDrawer()
//
//        // 2. Setup the rest of the UI
//        binding.btnOpenTopSelling.setOnClickListener {
//            startActivity(Intent(this, TopSellingActivity::class.java))
//        }
//
//        binding.rvDailySales.layoutManager = LinearLayoutManager(this)
//
//        updateDateUI()
//        fetchAllData()
//
//        binding.btnDateFilter.setOnClickListener {
//            showDatePicker { selectedDate ->
//                startDateStr = selectedDate
//                endDateStr = selectedDate
//                updateDateUI()
//                fetchAllData()
//            }
//        }
//    }
//
//    private fun fetchAllData() {
//        fetchOverview()
//        fetchDailySalesHistory()
//    }
//
//    private fun fetchOverview() {
//        CocobarApi.create().getSalesOverview(startDateStr, endDateStr).enqueue(object : Callback<SalesOverviewDTO> {
//            override fun onResponse(call: Call<SalesOverviewDTO>, response: Response<SalesOverviewDTO>) {
//                if (response.isSuccessful) {
//                    val data = response.body()
//                    if (data != null) {
//                        binding.tvTotalSales.text = "₹${String.format("%.2f", data.totalSales)}"
//                        binding.tvTotalOrders.text = data.totalOrders.toString()
//                        binding.tvItemsSold.text = data.itemsSold.toString()
//                        binding.tvAvgOrderValue.text = "₹${String.format("%.2f", data.avgOrderValue)}"
//                    }
//                }
//            }
//            override fun onFailure(call: Call<SalesOverviewDTO>, t: Throwable) {
//                Toast.makeText(this@SalesHistoryActivity, "Failed to load overview", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private fun fetchDailySalesHistory() {
//        CocobarApi.create().getDailySales(startDateStr, endDateStr).enqueue(object : Callback<List<DailySaleDTO>> {
//            override fun onResponse(call: Call<List<DailySaleDTO>>, response: Response<List<DailySaleDTO>>) {
//                if (response.isSuccessful) {
//                    val data = response.body() ?: emptyList()
//                    binding.rvDailySales.adapter = DailySaleAdapter(data)
//                }
//            }
//            override fun onFailure(call: Call<List<DailySaleDTO>>, t: Throwable) {
//                Toast.makeText(this@SalesHistoryActivity, "Failed to load daily sales", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private fun showDatePicker(onDateSelected: (String) -> Unit) {
//        val datePicker = DatePickerDialog(this, { _, year, month, day ->
//            val formatted = String.format("%04d-%02d-%02d", year, month + 1, day)
//            onDateSelected(formatted)
//        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
//        datePicker.show()
//    }
//
//    private fun getFormattedDate(offsetDays: Int): String {
//        val cal = Calendar.getInstance()
//        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
//        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
//    }
//
//    private fun updateDateUI() {
//        binding.tvDateRange.text = if (startDateStr == endDateStr) startDateStr else "$startDateStr to $endDateStr"
//    }
//
//    private fun setupNavigationDrawer() {
//        drawerLayout = findViewById(R.id.drawer_layout)
//        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
//
//        // This tells Android to use your Toolbar as the main ActionBar
//        setSupportActionBar(toolbar)
//
//        // This connects the hamburger icon to the sliding drawer
//        val toggle = ActionBarDrawerToggle(
//            this, drawerLayout, toolbar,
//            R.string.nav_open, R.string.nav_close
//        )
//        drawerLayout.addDrawerListener(toggle)
//        toggle.syncState()
//
//        val navView = findViewById<NavigationView>(R.id.nav_view)
//
//        // Highlight Sales History as the active page
//        navView.setCheckedItem(R.id.nav_saleshistory)
//
//        navView.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.nav_dashboard -> {
//                    val intent = Intent(this, DashboardActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    startActivity(intent)
//                    overridePendingTransition(0, 0)
//                    finish()
//                }
//                R.id.nav_inventory -> {
//                    val intent = Intent(this, InventoryActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    startActivity(intent)
//                    overridePendingTransition(0, 0)
//                    finish()
//                }
//                R.id.nav_recepi -> {
//                    val intent = Intent(this, RecipeActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    startActivity(intent)
//                    overridePendingTransition(0, 0)
//                    finish()
//                }
//                R.id.nav_product -> {
//                    val intent = Intent(this, ProductsActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    startActivity(intent)
//                    overridePendingTransition(0, 0)
//                    finish()
//                }
//                R.id.nav_saleshistory -> {
//                    // Already here, do nothing
//                }
//                R.id.nav_settings -> {
//                    Toast.makeText(this, "Opening Settings...", Toast.LENGTH_SHORT).show()
//                }
//            }
//            drawerLayout.closeDrawers()
//            true
//        }
//
//        val btnLogout = findViewById<android.widget.LinearLayout>(R.id.btn_custom_logout)
//        btnLogout.setOnClickListener {
//            val sharedPref = getSharedPreferences("CocobarPrefs", MODE_PRIVATE)
//            sharedPref.edit().clear().apply()
//
//            val intent = Intent(this, LoginActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//            finish()
//        }
//    }
//}