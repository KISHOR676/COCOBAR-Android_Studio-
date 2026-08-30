package com.cocobar.app

import androidx.core.view.GravityCompat
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
    private lateinit var tvTodaysProfit: TextView
    private lateinit var tvLowStock: TextView
    private lateinit var tvCurrentDate: TextView
    private lateinit var tvWelcomeName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize UI Elements
        tvTotalInventory = findViewById(R.id.tv_total_inventory)
        tvTodaysSales = findViewById(R.id.tv_todays_sales)
        tvTodaysProfit = findViewById(R.id.tv_todays_profit)
        tvLowStock = findViewById(R.id.tv_low_stock)
        tvCurrentDate = findViewById(R.id.tv_current_date)
        tvWelcomeName = findViewById(R.id.tv_welcome_name)

        setupNavigationDrawer()
        fetchDashboardData()
    }

    override fun onResume() {
        super.onResume()
        // Force the sidebar to highlight "Dashboard" whenever this screen appears
        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setCheckedItem(R.id.nav_dashboard)
    }

    private fun fetchDashboardData() {
        // Set dynamic date in header
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        tvCurrentDate.text = dateFormat.format(Date())

        // --- READ THE SAVED NAME FROM LOGIN ---
        val sharedPref = getSharedPreferences("CocobarPrefs", MODE_PRIVATE)
        val savedName = sharedPref.getString("USER_NAME", "Cocobar") // "Cocobar" is the fallback if name is missing

        // Capitalize the first letter for a clean UI (e.g., "abc" becomes "Abc")
        val formattedName = savedName?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        tvWelcomeName.text = "Welcome back, $formattedName!"
        // --------------------------------------

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.30.165.69:8080") // Ensure your IP is still correct
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        apiService.getDashboardSummary().enqueue(object : Callback<DashboardSummary> {
            override fun onResponse(call: Call<DashboardSummary>, response: Response<DashboardSummary>) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    tvTotalInventory.text = data.totalInventoryItems.toString()
                    tvTodaysSales.text = "₹ ${data.todaysSales}"
                    tvTodaysProfit.text = "₹ ${data.todaysProfit}"
                    tvLowStock.text = data.lowStockItems.toString()

                    // --- CHART RENDERING LOGIC ---
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

                        // Line Styling (Curved and Filled)
                        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Makes it curvy
                        dataSet.color = goldColor
                        dataSet.lineWidth = 3f
                        dataSet.setDrawFilled(true)
                        dataSet.fillColor = goldColor
                        dataSet.fillAlpha = 60 // Transparent fill
                        dataSet.setDrawValues(false) // Hides numbers floating above dots

                        // Dot Styling (Hollow circles)
                        dataSet.setDrawCircles(true)
                        dataSet.circleRadius = 5f
                        dataSet.setCircleColor(goldColor)
                        dataSet.setDrawCircleHole(true)
                        dataSet.circleHoleColor = Color.WHITE

                        chart.data = LineData(dataSet)

                        // --- AXIS STYLING ---
                        val darkBrown = Color.parseColor("#312018")

                        // X-Axis (Days of week)
                        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                        chart.xAxis.textColor = darkBrown
                        chart.xAxis.valueFormatter = IndexAxisValueFormatter(daysOfWeek)
                        chart.xAxis.granularity = 1f
                        chart.xAxis.setDrawGridLines(false) // Removes vertical bars
                        chart.xAxis.setDrawAxisLine(false)

                        // Y-Axis (Intervals of 500)
                        chart.axisLeft.textColor = darkBrown
                        chart.axisLeft.granularity = 500f // Forces 500 steps
                        chart.axisLeft.setDrawGridLines(true)
                        chart.axisLeft.gridColor = Color.parseColor("#E5D4BE") // Soft grid
                        chart.axisLeft.setDrawAxisLine(false)
                        chart.axisRight.isEnabled = false // Hides right axis

                        // Chart Background cleanup
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

        // Highlight Dashboard by default when opened
        navView.setCheckedItem(R.id.nav_dashboard)

        // 1. Navigation Menu Item Clicks
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    // Already here, do nothing
                }
                R.id.nav_inventory -> {
                    Toast.makeText(this, "Opening Inventory...", Toast.LENGTH_SHORT).show()
//                    // Launch the Inventory Activity
//                    val intent = Intent(this, InventoryActivity::class.java)
//                    startActivity(intent)

                    val intent = Intent(this@DashboardActivity, InventoryActivity::class.java)
                    // Safe approach: Creates it if dead, reuses it if alive
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0) // Kills the laggy slide animation
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Opening Settings...", Toast.LENGTH_SHORT).show()
                }
            }
            drawerLayout.closeDrawers()
            true
        } // <-- The listener safely closes here

        // 2. CUSTOM LOGOUT BUTTON LOGIC (Safely outside the listener above)
        val btnLogout = findViewById<android.widget.LinearLayout>(R.id.btn_custom_logout)
        btnLogout.setOnClickListener {
            // Wipe the saved user session from the phone
            val sharedPref = getSharedPreferences("CocobarPrefs", MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            // Go back to Login Activity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

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
//        navView.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.nav_dashboard -> { /* Already on this screen */ }
//            }
//            drawerLayout.closeDrawers()
//            true
//        }
//    }

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
//
//        // Highlight Dashboard by default when opened
//        navView.setCheckedItem(R.id.nav_dashboard)
//
//        navView.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.nav_dashboard -> {
//                    // Already here, do nothing
//                }
//                R.id.nav_inventory -> {
//                    // startActivity(Intent(this, InventoryActivity::class.java))
//                    Toast.makeText(this, "Opening Inventory...", Toast.LENGTH_SHORT).show()
//                }
//                R.id.nav_settings -> {
//                    // startActivity(Intent(this, SettingsActivity::class.java))
//                    Toast.makeText(this, "Opening Settings...", Toast.LENGTH_SHORT).show()
//                }
//                R.id.nav_logout -> {
//                    // 1. Wipe the saved user session from the phone
//                    val sharedPref = getSharedPreferences("CocobarPrefs", MODE_PRIVATE)
//                    sharedPref.edit().clear().apply()
//
//                    // 2. Go back to Login Activity
//                    val intent = Intent(this, LoginActivity::class.java)
//                    // This flag ensures the user can't press 'back' to return to the dashboard
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    startActivity(intent)
//                    finish()
//                }
//            }
//            drawerLayout.closeDrawers()
//            true
//        }
//    }
}