package com.cocobar.app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocobar.app.databinding.ActivityProductsBinding
import com.cocobar.app.network.CocobarApi
import com.cocobar.app.network.Recipe
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("DEPRECATION")
class ProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductsBinding
    private lateinit var adapter: ProductAdapter // Correctly set to ProductAdapter
    private var fullProductList: List<Recipe> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationDrawer()
        binding.rvProducts.layoutManager = LinearLayoutManager(this)

        binding.etSearchProduct.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val filtered = fullProductList.filter { it.name.lowercase().contains(query) }

                adapter = ProductAdapter(filtered) { clickedProduct, maxAvailable ->
                    val intent = Intent(this@ProductsActivity, SellProductActivity::class.java)
                    intent.putExtra("PRODUCT_DATA", clickedProduct)
                    intent.putExtra("MAX_AVAILABLE", maxAvailable)
                    startActivity(intent)
                }
                binding.rvProducts.adapter = adapter
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onResume() {
        super.onResume()
        fetchProducts()
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
                R.id.nav_product -> { /* Already here */ }
            }
            binding.drawerLayout.closeDrawers()
            true
        }

        @Suppress("SpellCheckingInspection")
        val btnLogout = findViewById<LinearLayout>(R.id.btn_custom_logout)
        btnLogout.setOnClickListener {
            getSharedPreferences("CocobarPrefs", MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
            finish()
        }
    }

    private fun fetchProducts() {
        CocobarApi.create().getRecipes().enqueue(object : Callback<List<Recipe>> {
            override fun onResponse(call: Call<List<Recipe>>, response: Response<List<Recipe>>) {
                if (response.isSuccessful) {
                    fullProductList = response.body() ?: emptyList()

                    adapter = ProductAdapter(fullProductList) { clickedProduct, maxAvailable ->
                        val intent = Intent(this@ProductsActivity, SellProductActivity::class.java)
                        intent.putExtra("PRODUCT_DATA", clickedProduct)
                        intent.putExtra("MAX_AVAILABLE", maxAvailable)
                        startActivity(intent)
                    }
                    binding.rvProducts.adapter = adapter
                }
            }
            override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                Toast.makeText(this@ProductsActivity, "Failed to load products", Toast.LENGTH_SHORT).show()
            }
        })
    }
}