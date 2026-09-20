package com.cocobar.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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

    // ADDED: Missing variable for the sidebar
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Bind the list option cards
        val optionProfile = findViewById<LinearLayout>(R.id.ll_option_profile)
        val optionIp = findViewById<LinearLayout>(R.id.ll_option_ip)
        val optionPassword = findViewById<LinearLayout>(R.id.ll_option_password)

        optionProfile.setOnClickListener { showUpdateNameDialog() }
        optionIp.setOnClickListener { showUpdateIpDialog() }
        optionPassword.setOnClickListener { showChangePasswordDialog() }

        // ADDED: Actually call the navigation setup functions
        setupBottomNavigation()
        setupNavigationDrawer()
    }

    // FIXED: Moved this outside of onCreate
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

    // FIXED: Moved this outside of onCreate
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

    private fun showUpdateNameDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_name, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etName = dialogView.findViewById<EditText>(R.id.etDialogName)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnDialogCancel)
        val btnUpdate = dialogView.findViewById<Button>(R.id.btnDialogUpdate)

        val prefs = getSharedPreferences("CocobarPrefs", Context.MODE_PRIVATE)
        etName.setText(prefs.getString("USER_NAME", "Cocobar"))

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnUpdate.setOnClickListener {
            val newName = etName.text.toString().trim()
            if (newName.isNotEmpty()) {
                prefs.edit().putString("USER_NAME", newName).apply()
                Toast.makeText(this, "Profile Name Updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun showUpdateIpDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_ip, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etIp = dialogView.findViewById<EditText>(R.id.etDialogIp)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnDialogCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnDialogSave)

        val prefs = getSharedPreferences("CocobarPrefs", Context.MODE_PRIVATE)
        etIp.setText(prefs.getString("BASE_URL", "http://10.30.165.69:8080/"))

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val newIp = etIp.text.toString().trim()
            if (newIp.isNotEmpty()) {
                prefs.edit().putString("BASE_URL", newIp).apply()
                Toast.makeText(this, "Server IP Saved. Please restart app to apply.", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "IP cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etCurrent = dialogView.findViewById<EditText>(R.id.etDialogCurrentPass)
        val etNew = dialogView.findViewById<EditText>(R.id.etDialogNewPass)
        val etConfirm = dialogView.findViewById<EditText>(R.id.etDialogConfirmPass)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnDialogCancel)
        val btnUpdate = dialogView.findViewById<Button>(R.id.btnDialogUpdate)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnUpdate.setOnClickListener {
            val oldPass = etCurrent.text.toString()
            val newPass = etNew.text.toString()
            val confirmPass = etConfirm.text.toString()

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirmPass) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("CocobarPrefs", Context.MODE_PRIVATE)
            val savedEmail = prefs.getString("USER_EMAIL", "admin@cocobar.com") ?: "admin@cocobar.com"

            val request = ChangePasswordRequest(savedEmail, oldPass, newPass)

            CocobarApi.create(this).changePassword(request).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@SettingsActivity, "Password Changed Successfully", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this@SettingsActivity, "Incorrect Current Password", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(this@SettingsActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            })
        }
        dialog.show()
    }
}