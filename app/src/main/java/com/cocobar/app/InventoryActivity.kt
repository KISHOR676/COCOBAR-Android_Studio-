package com.cocobar.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    private var fullInventoryList: List<RawMaterial> = emptyList() // NEW
    private var isLowStockFilterActive = false // Add this at the top of the class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Read the intent flag from the Dashboard
        isLowStockFilterActive = intent.getBooleanExtra("FILTER_LOW_STOCK", false)

        setupNavigationDrawer()

        binding.rvInventory.layoutManager = LinearLayoutManager(this)

        binding.fabAddItem.setOnClickListener {
            val intent = Intent(this, AddInventoryItemActivity::class.java)
            startActivity(intent)
        }

        // NEW: Search Bar Logic
        binding.etSearchInventory.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Prevent crash if user types before internet data arrives
                if (fullInventoryList.isEmpty()) return

                // If the user starts typing, disable the low stock filter to search everything
                isLowStockFilterActive = false

                val query = s.toString().lowercase()
                val filtered = fullInventoryList.filter { it.itemName.lowercase().contains(query) }

                adapter = InventoryAdapter(filtered) { clickedItem ->
                    showUpdateDialog(clickedItem)
                }
                binding.rvInventory.adapter = adapter
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onResume() {
        super.onResume()
        fetchInventory()
    }

    private fun setupNavigationDrawer() {
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, toolbar,
            R.string.nav_open, R.string.nav_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setCheckedItem(R.id.nav_inventory)

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
                    Toast.makeText(this, "Already in Inventory...", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_recepi -> {
                    val intent = Intent(this@InventoryActivity, RecipeActivity::class.java)
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
        CocobarApi.create(this).getInventory().enqueue(object : Callback<List<RawMaterial>> {
            override fun onResponse(call: Call<List<RawMaterial>>, response: Response<List<RawMaterial>>) {
                if (response.isSuccessful) {
                    fullInventoryList = response.body() ?: emptyList() // Updated

                    // Determine which list to show based on the dashboard click
                    val displayList = if (isLowStockFilterActive) {
                        // Show only items where current stock is less than or equal to the limit
                        fullInventoryList.filter { it.currentQuantity <= it.lowStockLimit }
                    } else {
                        fullInventoryList
                    }

                    adapter = InventoryAdapter(displayList) { clickedItem ->
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

        val statusLabel = android.widget.TextView(this)
        statusLabel.text = "Current: ${item.currentQuantity} ${item.unitType} | Total Value: ₹${item.unitPrice}"
        statusLabel.textSize = 12f
        statusLabel.setTextColor(android.graphics.Color.GRAY)
        statusLabel.setPadding(0, 0, 0, 32)
        layout.addView(statusLabel)

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
            val addedQty = qtyInput.text.toString().toDoubleOrNull() ?: 0.0
            val addedCost = priceInput.text.toString().toDoubleOrNull() ?: 0.0

            val newTotalQty = item.currentQuantity + addedQty
            val newTotalPrice = item.unitPrice + addedCost

            val updatedItem = item.copy(currentQuantity = newTotalQty, unitPrice = newTotalPrice)

            item.itemNo?.let { id ->
                CocobarApi.create(this).updateInventoryItem(id, updatedItem).enqueue(object : Callback<RawMaterial> {
                    override fun onResponse(call: Call<RawMaterial>, response: Response<RawMaterial>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@InventoryActivity, "Stock Added Successfully", Toast.LENGTH_SHORT).show()
                            binding.etSearchInventory.text.clear() // Clear search on success
                            fetchInventory()
                        }
                    }
                    override fun onFailure(call: Call<RawMaterial>, t: Throwable) {
                        Toast.makeText(this@InventoryActivity, "Update failed", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        builder.setNegativeButton("Cancel", null)

        builder.setNeutralButton("Delete Item") { _, _ ->
            AlertDialog.Builder(this)
                .setTitle("Delete ${item.itemName}?")
                .setMessage("Are you sure you want to completely remove this item? This action cannot be undone.")
                .setPositiveButton("Yes, Delete") { _, _ ->
                    item.itemNo?.let { id ->
                        CocobarApi.create(this).deleteInventoryItem(id).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(this@InventoryActivity, "${item.itemName} deleted", Toast.LENGTH_SHORT).show()
                                    binding.etSearchInventory.text.clear() // Clear search on success
                                    fetchInventory()
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

        builder.show()
    }
}