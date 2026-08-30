package com.cocobar.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocobar.app.databinding.ActivityInventoryBinding
import com.cocobar.app.network.CocobarApi
import com.cocobar.app.network.RawMaterial
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private lateinit var adapter: InventoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup the Navigation Sidebar
        setupNavigationDrawer()

        // Setup RecyclerView for the list
        binding.rvInventory.layoutManager = LinearLayoutManager(this)

        // Floating '+' Button logic
        binding.fabAddItem.setOnClickListener {
            val intent = Intent(this, AddInventoryItemActivity::class.java)
            startActivity(intent)
        }

        fetchInventory()
    }

    override fun onResume() {
        super.onResume()
        // This runs every time the screen becomes visible again
        // It forces the app to fetch the latest data from the database
        fetchInventory()
    }

    private fun setupNavigationDrawer() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Connect the hamburger toggle icon using binding.drawerLayout
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, toolbar,
            R.string.nav_open, R.string.nav_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navView = findViewById<NavigationView>(R.id.nav_view)

        // Highlight Inventory as the current page
        navView.setCheckedItem(R.id.nav_inventory)

        // 1. Navigation Menu Item Clicks
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    val intent = Intent(this@InventoryActivity, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0) // Kills the laggy slide animation
                    finish() // Safely close the inventory menu
                }
                R.id.nav_inventory -> {
                    // Already here, do nothing
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Opening Settings...", Toast.LENGTH_SHORT).show()
                }
            }
            // Close the drawer safely using binding
            binding.drawerLayout.closeDrawers()
            true
        }

        // 2. CUSTOM LOGOUT BUTTON LOGIC
        val btnLogout = findViewById<LinearLayout>(R.id.btn_custom_logout)
        btnLogout.setOnClickListener {
            val sharedPref = getSharedPreferences("CocobarPrefs", MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun fetchInventory() {
        CocobarApi.create().getInventory().enqueue(object : Callback<List<RawMaterial>> {
            override fun onResponse(call: Call<List<RawMaterial>>, response: Response<List<RawMaterial>>) {
                if (response.isSuccessful) {
                    val inventoryList = response.body() ?: emptyList()

                    // Initialize the adapter and handle clicks
                    adapter = InventoryAdapter(inventoryList) { clickedItem ->
                        showUpdateDialog(clickedItem)
                    }
                    binding.rvInventory.adapter = adapter
                }
            }

            override fun onFailure(call: Call<List<RawMaterial>>, t: Throwable) {
                Toast.makeText(this@InventoryActivity, "Failed to load inventory", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showUpdateDialog(item: RawMaterial) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Restock ${item.itemName}")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(60, 40, 60, 20)

        // Current Status Header
        val statusLabel = android.widget.TextView(this)
        statusLabel.text = "Current: ${item.currentQuantity} ${item.unitType} | Total Value: ₹${item.unitPrice}"
        statusLabel.textSize = 12f
        statusLabel.setTextColor(android.graphics.Color.GRAY)
        statusLabel.setPadding(0, 0, 0, 32)
        layout.addView(statusLabel)

        // Add Label for Quantity Added
        val qtyLabel = android.widget.TextView(this)
        qtyLabel.text = "Quantity to Add (${item.unitType}):"
        qtyLabel.textSize = 14f
        qtyLabel.setTextColor(android.graphics.Color.parseColor("#312018"))
        qtyLabel.setPadding(0, 0, 0, 8)
        layout.addView(qtyLabel)

        val qtyInput = EditText(this)
        qtyInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        qtyInput.hint = "e.g. 5"
        layout.addView(qtyInput)

        // Add Label for Cost of New Stock
        val priceLabel = android.widget.TextView(this)
        priceLabel.text = "Cost of Added Stock (₹):"
        priceLabel.textSize = 14f
        priceLabel.setTextColor(android.graphics.Color.parseColor("#312018"))
        priceLabel.setPadding(0, 32, 0, 8)
        layout.addView(priceLabel)

        val priceInput = EditText(this)
        priceInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        priceInput.hint = "e.g. 150"
        layout.addView(priceInput)

        builder.setView(layout)

        builder.setPositiveButton("Add Stock") { _, _ ->
            // Get the inputted amounts (default to 0 if left blank)
            val addedQty = qtyInput.text.toString().toDoubleOrNull() ?: 0.0
            val addedCost = priceInput.text.toString().toDoubleOrNull() ?: 0.0

            // CALCULATE THE NEW TOTALS
            val newTotalQty = item.currentQuantity + addedQty
            val newTotalPrice = item.unitPrice + addedCost

            // Create the updated item object
            val updatedItem = item.copy(currentQuantity = newTotalQty, unitPrice = newTotalPrice)

            // Send to Database
            item.itemNo?.let { id ->
                CocobarApi.create().updateInventoryItem(id, updatedItem).enqueue(object : Callback<RawMaterial> {
                    override fun onResponse(call: Call<RawMaterial>, response: Response<RawMaterial>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@InventoryActivity, "Stock Added Successfully", Toast.LENGTH_SHORT).show()
                            fetchInventory() // Refresh the UI list
                        }
                    }
                    override fun onFailure(call: Call<RawMaterial>, t: Throwable) {
                        Toast.makeText(this@InventoryActivity, "Update failed", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        builder.setNegativeButton("Cancel", null)

        // ==========================================
        // PASTE THIS NEW DELETE BUTTON LOGIC HERE
        // ==========================================
        builder.setNeutralButton("Delete Item") { _, _ ->
            // Show a confirmation warning first
            AlertDialog.Builder(this)
                .setTitle("Delete ${item.itemName}?")
                .setMessage("Are you sure you want to completely remove this item? This action cannot be undone.")
                .setPositiveButton("Yes, Delete") { _, _ ->

                    // Call the API to delete
                    item.itemNo?.let { id ->
                        CocobarApi.create().deleteInventoryItem(id).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(this@InventoryActivity, "${item.itemName} deleted", Toast.LENGTH_SHORT).show()
                                    fetchInventory() // Refresh the list
                                } else {
                                    Toast.makeText(this@InventoryActivity, "Server Error", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(this@InventoryActivity, "Network Error", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        // ==========================================
        builder.show()
    }

}