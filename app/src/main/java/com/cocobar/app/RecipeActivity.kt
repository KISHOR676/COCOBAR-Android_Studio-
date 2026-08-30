package com.cocobar.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocobar.app.databinding.ActivityRecipeBinding
import com.cocobar.app.network.CocobarApi
import com.cocobar.app.network.Recipe
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("DEPRECATION") // This removes the overridePendingTransition warnings
class RecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeBinding
    private lateinit var adapter: RecipeAdapter
    private var fullRecipeList: List<Recipe> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationDrawer()

        binding.rvRecipes.layoutManager = LinearLayoutManager(this)

        // Add New Recipe Button
        binding.fabAddRecipe.setOnClickListener {
            val intent = Intent(this, AddRecipeActivity::class.java)
            startActivity(intent)
        }

        // Search Bar Logic
        binding.etSearchRecipe.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val filtered = fullRecipeList.filter { it.name.lowercase().contains(query) }

                // Explicitly typing 'clickedRecipe: Recipe' removes the infer type error
                adapter = RecipeAdapter(filtered) { clickedRecipe: Recipe ->
                    showRecipeOptionsDialog(clickedRecipe)
                }
                binding.rvRecipes.adapter = adapter
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onResume() {
        super.onResume()
        fetchRecipes()
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
        navView.setCheckedItem(R.id.nav_recepi)

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
                R.id.nav_recepi -> { /* Already here */ }
            }
            binding.drawerLayout.closeDrawers()
            true
        }

        @Suppress("SpellCheckingInspection") // Suppresses the CocobarPrefs typo warning
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

    private fun fetchRecipes() {
        CocobarApi.create().getRecipes().enqueue(object : Callback<List<Recipe>> {
            override fun onResponse(call: Call<List<Recipe>>, response: Response<List<Recipe>>) {
                if (response.isSuccessful) {
                    fullRecipeList = response.body() ?: emptyList()

                    // Explicitly typing 'clickedRecipe: Recipe' removes the infer type error
                    adapter = RecipeAdapter(fullRecipeList) { clickedRecipe: Recipe ->
                        showRecipeOptionsDialog(clickedRecipe)
                    }
                    binding.rvRecipes.adapter = adapter
                }
            }

            override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                Toast.makeText(this@RecipeActivity, "Failed to load recipes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showRecipeOptionsDialog(recipe: Recipe) {
        val options = arrayOf("View/Edit Ingredients", "Delete Recipe")
        AlertDialog.Builder(this)
            .setTitle(recipe.name)
            .setItems(options) { _, which ->
                if (which == 0) {
                    // NEW: Launch AddRecipeActivity in Edit Mode
                    val intent = Intent(this, AddRecipeActivity::class.java)
                    intent.putExtra("EDIT_RECIPE", recipe) // Pass the recipe data
                    startActivity(intent)
                } else if (which == 1) {
                    AlertDialog.Builder(this)
                        .setTitle("Delete ${recipe.name}?")
                        .setMessage("Are you sure? This cannot be undone.")
                        .setPositiveButton("Delete") { _, _ ->
                            recipe.id?.let { deleteRecipe(it) }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }.show()
    }

    private fun deleteRecipe(id: Long) {
        CocobarApi.create().deleteRecipe(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RecipeActivity, "Recipe Deleted", Toast.LENGTH_SHORT).show()
                    binding.etSearchRecipe.text.clear()
                    fetchRecipes()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@RecipeActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}








//package com.cocobar.app
//
//import android.app.AlertDialog
//import android.content.Intent
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.widget.LinearLayout
//import android.widget.Toast
//import androidx.appcompat.app.ActionBarDrawerToggle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.cocobar.app.databinding.ActivityRecipeBinding
//import com.cocobar.app.network.CocobarApi
//import com.cocobar.app.network.Recipe
//import com.google.android.material.navigation.NavigationView
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class RecipeActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityRecipeBinding
//    private lateinit var adapter: RecipeAdapter
//    private var fullRecipeList: List<Recipe> = emptyList()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityRecipeBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        setupNavigationDrawer()
//
//        binding.rvRecipes.layoutManager = LinearLayoutManager(this)
//
//        // Add New Recipe Button
//        binding.fabAddRecipe.setOnClickListener {
//            val intent = Intent(this, AddRecipeActivity::class.java)
//            startActivity(intent)
//        }
//
//        // Search Bar Logic
//        binding.etSearchRecipe.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//                val query = s.toString().lowercase()
//                // Filter the list based on the search box
//                val filtered = fullRecipeList.filter { it.name.lowercase().contains(query) }
//
//                // Update the adapter with the filtered list
//                adapter = RecipeAdapter(filtered) { clickedRecipe ->
//                    showRecipeOptionsDialog(clickedRecipe)
//                }
//                binding.rvRecipes.adapter = adapter
//            }
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//        })
//    }
//
//    override fun onResume() {
//        super.onResume()
//        fetchRecipes()
//    }
//
//    private fun setupNavigationDrawer() {
//        val toolbar = binding.toolbar
//        setSupportActionBar(toolbar)
//
//        val toggle = ActionBarDrawerToggle(
//            this, binding.drawerLayout, toolbar,
//            R.string.nav_open, R.string.nav_close
//        )
//        binding.drawerLayout.addDrawerListener(toggle)
//        toggle.syncState()
//
//        val navView = findViewById<NavigationView>(R.id.nav_view)
//        navView.setCheckedItem(R.id.nav_recepi)
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
//                R.id.nav_recepi -> { /* Already here */ }
//            }
//            binding.drawerLayout.closeDrawers()
//            true
//        }
//
//        val btnLogout = findViewById<LinearLayout>(R.id.btn_custom_logout)
//        btnLogout.setOnClickListener {
//            val sharedPref = getSharedPreferences("CocobarPrefs", MODE_PRIVATE)
//            sharedPref.edit().clear().apply()
//            val intent = Intent(this, LoginActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//            finish()
//        }
//    }
//
//    private fun fetchRecipes() {
//        CocobarApi.create().getRecipes().enqueue(object : Callback<List<Recipe>> {
//            override fun onResponse(call: Call<List<Recipe>>, response: Response<List<Recipe>>) {
//                if (response.isSuccessful) {
//                    fullRecipeList = response.body() ?: emptyList()
//
//                    // Setup adapter with click listener
//                    adapter = RecipeAdapter(fullRecipeList) { clickedRecipe ->
//                        showRecipeOptionsDialog(clickedRecipe)
//                    }
//                    binding.rvRecipes.adapter = adapter
//                }
//            }
//
//            override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
//                Toast.makeText(this@RecipeActivity, "Failed to load recipes", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    // Popup menu for Edit / Delete
//    private fun showRecipeOptionsDialog(recipe: Recipe) {
//        val options = arrayOf("View/Edit Ingredients", "Delete Recipe")
//        AlertDialog.Builder(this)
//            .setTitle(recipe.name)
//            .setItems(options) { _, which ->
//                if (which == 0) {
//                    // Placeholder for Edit Screen
//                    Toast.makeText(this, "Edit feature coming in next step!", Toast.LENGTH_SHORT).show()
//                } else if (which == 1) {
//                    // Confirm Delete
//                    AlertDialog.Builder(this)
//                        .setTitle("Delete ${recipe.name}?")
//                        .setMessage("Are you sure? This cannot be undone.")
//                        .setPositiveButton("Delete") { _, _ ->
//                            recipe.id?.let { deleteRecipe(it) }
//                        }
//                        .setNegativeButton("Cancel", null)
//                        .show()
//                }
//            }.show()
//    }
//
//    private fun deleteRecipe(id: Long) {
//        CocobarApi.create().deleteRecipe(id).enqueue(object : Callback<Void> {
//            override fun onResponse(call: Call<Void>, response: Response<Void>) {
//                if (response.isSuccessful) {
//                    Toast.makeText(this@RecipeActivity, "Recipe Deleted", Toast.LENGTH_SHORT).show()
//                    // Clear search box and refresh list
//                    binding.etSearchRecipe.text.clear()
//                    fetchRecipes()
//                }
//            }
//            override fun onFailure(call: Call<Void>, t: Throwable) {
//                Toast.makeText(this@RecipeActivity, "Network Error", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//}