package com.cocobar.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.cocobar.app.network.ApiService
import com.cocobar.app.network.DashboardSummary
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tvTotalInventory: TextView
    private lateinit var tvTodaysSales: TextView
    private lateinit var tvTotalProducts: TextView // Replaced Profit
    private lateinit var tvLowStock: TextView
    private lateinit var tvCurrentDate: TextView
    private lateinit var tvWelcomeName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvTotalInventory = findViewById(R.id.tv_total_inventory)
        tvTodaysSales = findViewById(R.id.tv_todays_sales)
        tvTotalProducts = findViewById(R.id.tv_total_products) // Replaced Profit
        tvLowStock = findViewById(R.id.tv_low_stock)
        tvCurrentDate = findViewById(R.id.tv_current_date)
        tvWelcomeName = findViewById(R.id.tv_welcome_name)

        // 1. Inventory Card Click
        val cardInventory = findViewById<android.widget.LinearLayout>(R.id.card_inventory)
        cardInventory.setOnClickListener {
            val intent = Intent(this, InventoryActivity::class.java)
            startActivity(intent)
        }

        // 2. Low Stock Card Click
        val cardLowStock = findViewById<android.widget.LinearLayout>(R.id.card_low_stock)
        cardLowStock.setOnClickListener {
            val intent = Intent(this, InventoryActivity::class.java)
            intent.putExtra("FILTER_LOW_STOCK", true)
            startActivity(intent)
        }

        // 3. Today's Sales Card Click -> Opens DailySalesDetailActivity
        val cardSales = findViewById<android.widget.LinearLayout>(R.id.card_sales)
        cardSales.setOnClickListener {
            val intent = Intent(this, DailySalesDetailActivity::class.java)
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            intent.putExtra("SELECTED_DATE", todayStr)
            startActivity(intent)
        }

        // 4. Products Card Click -> Opens ProductsActivity
        val cardProduct = findViewById<android.widget.LinearLayout>(R.id.card_product)
        cardProduct.setOnClickListener {
            val intent = Intent(this, ProductsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        setupNavigationDrawer()
        setupBottomNavigation()

    }

    override fun onResume() {
        super.onResume()
        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setCheckedItem(R.id.nav_dashboard)

        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_bottom_dashboard

        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        tvCurrentDate.text = dateFormat.format(Date())

        val sharedPref = getSharedPreferences("CocobarPrefs", MODE_PRIVATE)
        val savedName = sharedPref.getString("USER_NAME", "Cocobar")

        val formattedName = savedName?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        tvWelcomeName.text = "Welcome back, $formattedName!"

        val savedBaseUrl = sharedPref.getString("BASE_URL", "http://10.62.109.69:8080")

        val retrofit = Retrofit.Builder()
            .baseUrl(savedBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        apiService.getDashboardSummary().enqueue(object : Callback<DashboardSummary> {
            override fun onResponse(call: Call<DashboardSummary>, response: Response<DashboardSummary>) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    tvTotalInventory.text = data.totalInventoryItems.toString()
                    tvTodaysSales.text = "₹ ${data.todaysSales}"
                    tvLowStock.text = data.lowStockItems.toString()
                    tvTotalProducts.text = data.totalProducts.toString() // Set Product Count

                    val chart = findViewById<LineChart>(R.id.sales_chart)
                    val entries = ArrayList<Entry>()
                    val daysOfWeek = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

                    data.salesGraphData?.let { graphData ->
                        var xIndex = 0f
                        for (point in graphData) {
                            val totalStr = point["total"].toString()
                            val yValue = totalStr.toFloatOrNull() ?: 0f
                            entries.add(Entry(xIndex, yValue))
                            xIndex += 1f
                        }
                    }

                    if (entries.isNotEmpty()) {
                        val goldColor = Color.parseColor("#D8B07D")
                        val dataSet = LineDataSet(entries, "Weekly Sales")

                        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                        dataSet.color = goldColor
                        dataSet.lineWidth = 3f
                        dataSet.setDrawFilled(true)
                        dataSet.fillColor = goldColor
                        dataSet.fillAlpha = 60
                        dataSet.setDrawValues(false)

                        dataSet.setDrawCircles(true)
                        dataSet.circleRadius = 5f
                        dataSet.setCircleColor(goldColor)
                        dataSet.setDrawCircleHole(true)
                        dataSet.circleHoleColor = Color.WHITE

                        chart.data = LineData(dataSet)

                        val darkBrown = Color.parseColor("#312018")
                        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                        chart.xAxis.textColor = darkBrown
                        chart.xAxis.valueFormatter = IndexAxisValueFormatter(daysOfWeek)
                        chart.xAxis.granularity = 1f
                        chart.xAxis.setDrawGridLines(false)
                        chart.xAxis.setDrawAxisLine(false)

                        chart.axisLeft.textColor = darkBrown
                        chart.axisLeft.granularity = 500f
                        chart.axisLeft.setDrawGridLines(true)
                        chart.axisLeft.gridColor = Color.parseColor("#E5D4BE")
                        chart.axisLeft.setDrawAxisLine(false)
                        chart.axisRight.isEnabled = false

                        chart.description.isEnabled = false
                        chart.legend.isEnabled = false
                        chart.setDrawGridBackground(false)
                        chart.setDrawBorders(false)

                        chart.animateX(1000)
                        chart.invalidate()
                    }
                } else {
                    Toast.makeText(this@DashboardActivity, "Server Error: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<DashboardSummary>, t: Throwable) {
                Toast.makeText(this@DashboardActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_bottom_dashboard

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_bottom_dashboard -> {
                    true
                }
                R.id.nav_bottom_product -> {
                    val intent = Intent(this, ProductsActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_bottom_sales -> {
                    val intent = Intent(this, SalesHistoryActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_bottom_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
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
        navView.setCheckedItem(R.id.nav_dashboard)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    Toast.makeText(this, "Already in Dashboard...", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_inventory -> {
                    val intent = Intent(this@DashboardActivity, InventoryActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
                R.id.nav_recepi -> {
                    val intent = Intent(this, RecipeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
                R.id.nav_product -> {
                    val intent = Intent(this, ProductsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
                R.id.nav_saleshistory -> {
                    val intent = Intent(this, SalesHistoryActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
                R.id.nav_reports -> {
                    val intent = Intent(this, ReportsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
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
//import android.content.Intent
//import android.graphics.Color
//import android.os.Bundle
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.ActionBarDrawerToggle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.drawerlayout.widget.DrawerLayout
//import com.cocobar.app.network.ApiService
//import com.cocobar.app.network.DashboardSummary
//import com.github.mikephil.charting.charts.LineChart
//import com.github.mikephil.charting.components.XAxis
//import com.github.mikephil.charting.data.Entry
//import com.github.mikephil.charting.data.LineData
//import com.github.mikephil.charting.data.LineDataSet
//import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
//import com.google.android.material.navigation.NavigationView
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//class DashboardActivity : AppCompatActivity() {
//    private lateinit var drawerLayout: DrawerLayout
//    private lateinit var tvTotalInventory: TextView
//    private lateinit var tvTodaysSales: TextView
//    private lateinit var tvTodaysProfit: TextView
//    private lateinit var tvLowStock: TextView
//    private lateinit var tvCurrentDate: TextView
//    private lateinit var tvWelcomeName: TextView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_dashboard)
//
//        tvTotalInventory = findViewById(R.id.tv_total_inventory)
//        tvTodaysSales = findViewById(R.id.tv_todays_sales)
//        tvTodaysProfit = findViewById(R.id.tv_todays_profit)
//        tvLowStock = findViewById(R.id.tv_low_stock)
//        tvCurrentDate = findViewById(R.id.tv_current_date)
//        tvWelcomeName = findViewById(R.id.tv_welcome_name)
//
//        val cardInventory = findViewById<android.widget.LinearLayout>(R.id.card_inventory)
//        val cardLowStock = findViewById<android.widget.LinearLayout>(R.id.card_low_stock)
//
//        cardInventory.setOnClickListener {
//            val intent = Intent(this, InventoryActivity::class.java)
//            startActivity(intent)
//        }
//
//        cardLowStock.setOnClickListener {
//            val intent = Intent(this, InventoryActivity::class.java)
//            intent.putExtra("FILTER_LOW_STOCK", true)
//            startActivity(intent)
//        }
//
//        setupNavigationDrawer()
//        setupBottomNavigation() // <-- NEW: Called here
//        fetchDashboardData()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        val navView = findViewById<NavigationView>(R.id.nav_view)
//        navView.setCheckedItem(R.id.nav_dashboard)
//
//        // Also highlight the bottom nav correctly on resume
//        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
//        bottomNav.selectedItemId = R.id.nav_bottom_dashboard
//    }
//
//    private fun fetchDashboardData() {
//        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
//        tvCurrentDate.text = dateFormat.format(Date())
//
//        val sharedPref = getSharedPreferences("CocobarPrefs", MODE_PRIVATE)
//        val savedName = sharedPref.getString("USER_NAME", "Cocobar")
//
//        val formattedName = savedName?.replaceFirstChar {
//            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
//        }
//
//        tvWelcomeName.text = "Welcome back, $formattedName!"
//
////        val sharedPref = getSharedPreferences("CocobarPrefs", MODE_PRIVATE)
//        val savedBaseUrl = sharedPref.getString("BASE_URL", "http://10.30.165.69:8080")
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl(savedBaseUrl)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
////        val retrofit = Retrofit.Builder()
////            .baseUrl("http://10.30.165.69:8080")
////            .addConverterFactory(GsonConverterFactory.create())
////            .build()
//
//        val apiService = retrofit.create(ApiService::class.java)
//
//        apiService.getDashboardSummary().enqueue(object : Callback<DashboardSummary> {
//            override fun onResponse(call: Call<DashboardSummary>, response: Response<DashboardSummary>) {
//                if (response.isSuccessful && response.body() != null) {
//                    val data = response.body()!!
//
//                    tvTotalInventory.text = data.totalInventoryItems.toString()
//                    tvTodaysSales.text = "₹ ${data.todaysSales}"
//                    tvTodaysProfit.text = "₹ ${data.todaysProfit}"
//                    tvLowStock.text = data.lowStockItems.toString()
//
//                    val chart = findViewById<LineChart>(R.id.sales_chart)
//                    val entries = ArrayList<Entry>()
//                    val daysOfWeek = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
//
//                    data.salesGraphData?.let { graphData ->
//                        var xIndex = 0f
//                        for (point in graphData) {
//                            val totalStr = point["total"].toString()
//                            val yValue = totalStr.toFloatOrNull() ?: 0f
//                            entries.add(Entry(xIndex, yValue))
//                            xIndex += 1f
//                        }
//                    }
//
//                    if (entries.isNotEmpty()) {
//                        val goldColor = Color.parseColor("#D8B07D")
//                        val dataSet = LineDataSet(entries, "Weekly Sales")
//
//                        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
//                        dataSet.color = goldColor
//                        dataSet.lineWidth = 3f
//                        dataSet.setDrawFilled(true)
//                        dataSet.fillColor = goldColor
//                        dataSet.fillAlpha = 60
//                        dataSet.setDrawValues(false)
//
//                        dataSet.setDrawCircles(true)
//                        dataSet.circleRadius = 5f
//                        dataSet.setCircleColor(goldColor)
//                        dataSet.setDrawCircleHole(true)
//                        dataSet.circleHoleColor = Color.WHITE
//
//                        chart.data = LineData(dataSet)
//
//                        val darkBrown = Color.parseColor("#312018")
//                        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
//                        chart.xAxis.textColor = darkBrown
//                        chart.xAxis.valueFormatter = IndexAxisValueFormatter(daysOfWeek)
//                        chart.xAxis.granularity = 1f
//                        chart.xAxis.setDrawGridLines(false)
//                        chart.xAxis.setDrawAxisLine(false)
//
//                        chart.axisLeft.textColor = darkBrown
//                        chart.axisLeft.granularity = 500f
//                        chart.axisLeft.setDrawGridLines(true)
//                        chart.axisLeft.gridColor = Color.parseColor("#E5D4BE")
//                        chart.axisLeft.setDrawAxisLine(false)
//                        chart.axisRight.isEnabled = false
//
//                        chart.description.isEnabled = false
//                        chart.legend.isEnabled = false
//                        chart.setDrawGridBackground(false)
//                        chart.setDrawBorders(false)
//
//                        chart.animateX(1000)
//                        chart.invalidate()
//                    }
//                } else {
//                    Toast.makeText(this@DashboardActivity, "Server Error: ${response.code()}", Toast.LENGTH_LONG).show()
//                }
//            }
//
//            override fun onFailure(call: Call<DashboardSummary>, t: Throwable) {
//                Toast.makeText(this@DashboardActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
//            }
//        })
//    }
//
//    // NEW: Bottom Navigation Logic
//    private fun setupBottomNavigation() {
//        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
//
//        // Highlight Dashboard
//        bottomNav.selectedItemId = R.id.nav_bottom_dashboard
//
//        bottomNav.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.nav_bottom_dashboard -> {
//                    // Already here
//                    true
//                }
//                R.id.nav_bottom_product -> {
//                    val intent = Intent(this, ProductsActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
//                    startActivity(intent)
//                    overridePendingTransition(0, 0)
//                    true
//                }
//                R.id.nav_bottom_sales -> {
//                    val intent = Intent(this, SalesHistoryActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
//                    startActivity(intent)
//                    overridePendingTransition(0, 0)
//                    true
//                }
//                R.id.nav_bottom_settings -> {
//                    val intent = Intent(this, SettingsActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
//                    startActivity(intent)
//                    overridePendingTransition(0, 0)
//                    true
//                }
//                else -> false
//            }
//        }
//    }
//
//    private fun setupNavigationDrawer() {
//        drawerLayout = findViewById(R.id.drawer_layout)
//        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
//        setSupportActionBar(toolbar)
//
//        val toggle = ActionBarDrawerToggle(
//            this, drawerLayout, toolbar,
//            R.string.nav_open, R.string.nav_close
//        )
//        drawerLayout.addDrawerListener(toggle)
//        toggle.syncState()
//
//        val navView = findViewById<NavigationView>(R.id.nav_view)
//        navView.setCheckedItem(R.id.nav_dashboard)
//
//        navView.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.nav_dashboard -> {
//                    Toast.makeText(this, "Already in Dashboard...", Toast.LENGTH_SHORT).show()
//                }
//                R.id.nav_inventory -> {
//                    val intent = Intent(this@DashboardActivity, InventoryActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    startActivity(intent)
//                    overridePendingTransition(0, 0)
//                }
//                R.id.nav_recepi -> {
//                    val intent = Intent(this, RecipeActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    startActivity(intent)
//                    overridePendingTransition(0, 0)
//                }
//                R.id.nav_product -> {
//                    val intent = Intent(this, ProductsActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    startActivity(intent)
//                    overridePendingTransition(0, 0)
//                }
//                R.id.nav_saleshistory -> {
//                    val intent = Intent(this, SalesHistoryActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    startActivity(intent)
//                    overridePendingTransition(0, 0)
//                }
//                R.id.nav_reports -> {
//                    val intent = Intent(this, ReportsActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    startActivity(intent)
//                    overridePendingTransition(0, 0)
//                }
//                R.id.nav_settings -> {
//                    val intent = Intent(this, SettingsActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    startActivity(intent)
//                    overridePendingTransition(0, 0)
//                    finish()
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