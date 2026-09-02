package com.cocobar.app

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.cocobar.app.databinding.ActivityReportsBinding
import com.cocobar.app.network.CocobarApi
import com.cocobar.app.network.ExportReportDTO
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private val calendar = Calendar.getInstance()

    // Default to today
    private var startDateStr = getFormattedDate(0)
    private var endDateStr = getFormattedDate(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationDrawer()
        updateDateUI()

        binding.btnChangeDates.setOnClickListener {
            showDatePicker("Select Start Date") { start ->
                startDateStr = start
                showDatePicker("Select End Date") { end ->
                    endDateStr = end
                    updateDateUI()
                }
            }
        }

        binding.btnGenerateReport.setOnClickListener {
            fetchAndShareReport()
        }
    }

    private fun getFormattedDate(offsetDays: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    private fun updateDateUI() {
        binding.tvDateRange.text = if (startDateStr == endDateStr) startDateStr else "$startDateStr  to  $endDateStr"
    }

    private fun showDatePicker(title: String, onDateSelected: (String) -> Unit) {
        val datePicker = DatePickerDialog(this, { _, year, month, day ->
            val formatted = String.format("%04d-%02d-%02d", year, month + 1, day)
            onDateSelected(formatted)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.setTitle(title)
        datePicker.show()
    }

    private fun fetchAndShareReport() {
        Toast.makeText(this, "Generating Report...", Toast.LENGTH_SHORT).show()

        CocobarApi.create(this).getExportReport(startDateStr, endDateStr).enqueue(object : Callback<ExportReportDTO> {
            override fun onResponse(call: Call<ExportReportDTO>, response: Response<ExportReportDTO>) {
                if (response.isSuccessful) {
                    val report = response.body()
                    if (report != null) {
                        showSuccessPopup(report)
                    }
                }
            }
            override fun onFailure(call: Call<ExportReportDTO>, t: Throwable) {
                Toast.makeText(this@ReportsActivity, "Failed to generate report", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showSuccessPopup(report: ExportReportDTO) {
        AlertDialog.Builder(this)
            .setTitle("Success!")
            .setMessage("Report has been generated successfully for the selected dates.")
            .setPositiveButton("Share Now") { _, _ ->
                formatAndShareText(report)
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(false)
            .show()
    }

    private fun formatAndShareText(report: ExportReportDTO) {
        val sb = StringBuilder()
        sb.append("📊 *COCOBAR PERFORMANCE REPORT* 📊\n")
        sb.append("📅 Date: ${report.date}\n\n")

        sb.append("💰 *Total Sales:* ₹${String.format("%.2f", report.totalSales)}\n")
        sb.append("📦 *Total Orders:* ${report.totalOrders}\n")
        sb.append("🛒 *Avg Order Value:* ₹${String.format("%.2f", report.averageOrderValue)}\n\n")

        sb.append("🏆 *Top Items Sold:*\n")
        if (report.topItems.isEmpty()) {
            sb.append("No items sold in this period.\n")
        } else {
            report.topItems.forEachIndexed { index, item ->
                sb.append("${index + 1}. ${item.itemName} (${item.quantitySold} units)\n")
            }
        }

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "Share Report via..."))
    }

    private fun setupNavigationDrawer() {
        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.nav_open, R.string.nav_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setCheckedItem(R.id.nav_reports)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0) // Warning suppressed at top of class
                    finish()
                }
                R.id.nav_inventory -> {
                    val intent = Intent(this, InventoryActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0) // Warning suppressed at top of class
                    finish()
                }
                R.id.nav_recepi -> {
                    val intent = Intent(this, RecipeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    // (Add finish() here if you are editing InventoryActivity, leave finish() out if you are editing DashboardActivity)
                }
                R.id.nav_product -> {
                    val intent = Intent(this, ProductsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                }
                R.id.nav_saleshistory -> {
                    val intent = Intent(this, SalesHistoryActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                }
                R.id.nav_reports -> {
                    Toast.makeText(this, "Already in Reportss...", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                }
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }
}