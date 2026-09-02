package com.cocobar.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.cocobar.app.network.ChangePasswordRequest
import com.cocobar.app.network.CocobarApi
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupNavigationDrawer()
        setupBottomNavigation()

        val prefs = getSharedPreferences("CocobarPrefs", Context.MODE_PRIVATE)

        // 1. Account Info Logic
        val etDisplayName = findViewById<EditText>(R.id.etDisplayName)
        etDisplayName.setText(prefs.getString("USER_NAME", "Cocobar"))

        findViewById<Button>(R.id.btnSaveName).setOnClickListener {
            val newName = etDisplayName.text.toString().trim()
            if (newName.isNotEmpty()) {
                prefs.edit().putString("USER_NAME", newName).apply()
                Toast.makeText(this, "Name updated! Will reflect on Dashboard.", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Network Config Logic
        val etServerIp = findViewById<EditText>(R.id.etServerIp)
        // Default to your hardcoded IP if nothing is saved yet
        etServerIp.setText(prefs.getString("BASE_URL", "http://10.30.165.69:8080"))

        findViewById<Button>(R.id.btnSaveIp).setOnClickListener {
            var newUrl = etServerIp.text.toString().trim()
            if (newUrl.isNotEmpty()) {
                // Ensure it doesn't end with a slash, to prevent Retrofit crash
                if (newUrl.endsWith("/")) newUrl = newUrl.dropLast(1)

                prefs.edit().putString("BASE_URL", newUrl).apply()
                Toast.makeText(this, "Server IP Saved. Restart app to apply.", Toast.LENGTH_LONG).show()
            }
        }

        // 3. Security (Change Password)
        findViewById<Button>(R.id.btnChangePassword).setOnClickListener {
            val oldPass = findViewById<EditText>(R.id.etOldPassword).text.toString()
            val newPass = findViewById<EditText>(R.id.etNewPassword).text.toString()
            // Make sure you saved "USER_EMAIL" in SharedPreferences during LoginActivity!
            val savedEmail = prefs.getString("USER_EMAIL", "admin@cocobar.com") ?: "admin@cocobar.com"
//            val savedUsername = prefs.getString("USER_NAME", "admin") ?: "admin"

            if (oldPass.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "Please enter both passwords", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

//            val request = ChangePasswordRequest(savedUsername, oldPass, newPass)
            val request = ChangePasswordRequest(savedEmail, oldPass, newPass)
            CocobarApi.create(this).changePassword(request).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@SettingsActivity, "Password Changed!", Toast.LENGTH_SHORT).show()
                        findViewById<EditText>(R.id.etOldPassword).text.clear()
                        findViewById<EditText>(R.id.etNewPassword).text.clear()
                    } else {
                        Toast.makeText(this@SettingsActivity, "Failed to change password", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(this@SettingsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // 4. Sync Status (Display current time when opened)
        val tvSyncStatus = findViewById<TextView>(R.id.tvSyncStatus)
        val currentTime = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        tvSyncStatus.text = "Last Synced: $currentTime (Live)"
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_bottom_settings

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_bottom_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP })
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_bottom_product -> {
                    startActivity(Intent(this, ProductsActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP })
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_bottom_sales -> {
                    startActivity(Intent(this, SalesHistoryActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP })
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_bottom_settings -> true
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
        navView.setCheckedItem(R.id.nav_settings)
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
                    val intent = Intent(this, ReportsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Already in Settings...", Toast.LENGTH_SHORT).show()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }
}