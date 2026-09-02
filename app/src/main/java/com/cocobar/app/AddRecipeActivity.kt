package com.cocobar.app

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cocobar.app.databinding.ActivityAddRecipeBinding
import com.cocobar.app.network.CocobarApi
import com.cocobar.app.network.RawMaterial
import com.cocobar.app.network.Recipe
import com.cocobar.app.network.RecipeIngredient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecipeBinding
    private var fullInventoryList: List<RawMaterial> = emptyList()
    private var selectedImageUri: Uri? = null
    private var recipeToEdit: Recipe? = null // NEW: Tracks if we are editing

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            // Persist permission so the image works after app restart
            contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedImageUri = uri
            binding.ivRecipeImage.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.ivRecipeImage.setPadding(0, 0, 0, 0)
            binding.ivRecipeImage.setImageURI(null)
            binding.ivRecipeImage.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // NEW: Fetch and set current date
        val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        binding.tvDate.text = "Date: $currentDate"

        // Check if we are in Edit Mode
        recipeToEdit = intent.getSerializableExtra("EDIT_RECIPE") as? Recipe

        if (recipeToEdit != null) {
            binding.toolbar.title = "Edit Recipe"
            binding.btnSaveRecipe.text = "Update Recipe"
            binding.etRecipeName.setText(recipeToEdit!!.name)
            binding.etSellingPrice.setText(recipeToEdit!!.sellingPrice.toString())

            if (!recipeToEdit!!.imageUrl.isNullOrEmpty()) {
                selectedImageUri = Uri.parse(recipeToEdit!!.imageUrl)
                binding.ivRecipeImage.setImageURI(selectedImageUri)
                binding.ivRecipeImage.scaleType = ImageView.ScaleType.CENTER_CROP
                binding.ivRecipeImage.setPadding(0, 0, 0, 0)
            }
        }

        fetchInventoryForSpinners()

        binding.ivRecipeImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnAddIngredient.setOnClickListener {
            addIngredientRow(null)
        }

        binding.btnSaveRecipe.setOnClickListener {
            saveRecipe()
        }
    }

    private fun fetchInventoryForSpinners() {
        CocobarApi.create(this).getInventory().enqueue(object : Callback<List<RawMaterial>> {
            override fun onResponse(call: Call<List<RawMaterial>>, response: Response<List<RawMaterial>>) {
                if (response.isSuccessful) {
                    fullInventoryList = response.body() ?: emptyList()

                    if (recipeToEdit != null) {
                        // Pre-fill existing ingredients
                        recipeToEdit!!.ingredients.forEach { existingIngredient ->
                            addIngredientRow(existingIngredient)
                        }
                    } else {
                        // Empty row for new recipe
                        addIngredientRow(null)
                    }
                }
            }
            override fun onFailure(call: Call<List<RawMaterial>>, t: Throwable) {
                Toast.makeText(this@AddRecipeActivity, "Failed to load inventory", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Updated to accept an existing ingredient for pre-filling
    private fun addIngredientRow(existingIngredient: RecipeIngredient?) {
        if (fullInventoryList.isEmpty()) {
            Toast.makeText(this, "No inventory items available", Toast.LENGTH_SHORT).show()
            return
        }

        val rowView = LayoutInflater.from(this).inflate(R.layout.item_recipe_ingredient_row, null)
        val spinner = rowView.findViewById<Spinner>(R.id.spinnerInventory)
        val tvUnit = rowView.findViewById<TextView>(R.id.tvUnit)
        val etQty = rowView.findViewById<EditText>(R.id.etQuantityRequired)

        val itemNames = fullInventoryList.map { it.itemName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, itemNames)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                tvUnit.text = fullInventoryList[position].unitType
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // If editing, auto-select the right item and type the quantity
        if (existingIngredient != null) {
            val index = fullInventoryList.indexOfFirst { it.itemNo == existingIngredient.rawMaterial.itemNo }
            if (index >= 0) spinner.setSelection(index)
            etQty.setText(existingIngredient.quantityRequired.toString())
        }

        binding.llIngredientsContainer.addView(rowView)
    }

    private fun saveRecipe() {
        val recipeName = binding.etRecipeName.text.toString().trim()
        val sellingPriceStr = binding.etSellingPrice.text.toString().trim()

        if (recipeName.isEmpty() || sellingPriceStr.isEmpty()) {
            Toast.makeText(this, "Enter name and price", Toast.LENGTH_SHORT).show()
            return
        }

        val ingredientsList = mutableListOf<RecipeIngredient>()

        for (i in 0 until binding.llIngredientsContainer.childCount) {
            val rowView = binding.llIngredientsContainer.getChildAt(i)
            val spinner = rowView.findViewById<Spinner>(R.id.spinnerInventory)
            val etQty = rowView.findViewById<EditText>(R.id.etQuantityRequired)

            val qtyStr = etQty.text.toString().trim()

            if (qtyStr.isEmpty()) {
                Toast.makeText(this, "Error: Please enter a quantity for all selected ingredients.", Toast.LENGTH_LONG).show()
                return
            }

            val qty = qtyStr.toDoubleOrNull() ?: 0.0
            if (qty <= 0) {
                Toast.makeText(this, "Error: Quantity must be greater than 0.", Toast.LENGTH_LONG).show()
                return
            }

            val selectedPosition = spinner.selectedItemPosition
            val rawMaterial = fullInventoryList[selectedPosition]

            ingredientsList.add(RecipeIngredient(rawMaterial, qty))
        }

        if (ingredientsList.isEmpty()) {
            Toast.makeText(this, "Add at least one ingredient", Toast.LENGTH_SHORT).show()
            return
        }

        val imageUrlString = selectedImageUri?.toString()

        val newRecipe = Recipe(
            name = recipeName,
            sellingPrice = sellingPriceStr.toDouble(),
            imageUrl = imageUrlString,
            ingredients = ingredientsList
        )

        // Decide whether to POST (Add) or PUT (Update)
        if (recipeToEdit != null) {
            recipeToEdit!!.id?.let { id ->
                CocobarApi.create(this).updateRecipe(id, newRecipe).enqueue(object : Callback<Recipe> {
                    override fun onResponse(call: Call<Recipe>, response: Response<Recipe>) {
                        Toast.makeText(this@AddRecipeActivity, "Recipe Updated!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    override fun onFailure(call: Call<Recipe>, t: Throwable) {
                        Toast.makeText(this@AddRecipeActivity, "Error updating recipe", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        } else {
            CocobarApi.create(this).addRecipe(newRecipe).enqueue(object : Callback<Recipe> {
                override fun onResponse(call: Call<Recipe>, response: Response<Recipe>) {
                    Toast.makeText(this@AddRecipeActivity, "Recipe Saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                override fun onFailure(call: Call<Recipe>, t: Throwable) {
                    Toast.makeText(this@AddRecipeActivity, "Error saving recipe", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}








//package com.cocobar.app
//
//import android.net.Uri
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.widget.*
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import com.cocobar.app.databinding.ActivityAddRecipeBinding
//import com.cocobar.app.network.CocobarApi
//import com.cocobar.app.network.RawMaterial
//import com.cocobar.app.network.Recipe
//import com.cocobar.app.network.RecipeIngredient
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class AddRecipeActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityAddRecipeBinding
//    private var fullInventoryList: List<RawMaterial> = emptyList()
//    private var selectedImageUri: Uri? = null
//
//    // Image Picker Launcher
//    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//        if (uri != null) {
//            selectedImageUri = uri
//            // Assuming you add an ImageView with ID ivRecipeImage to activity_add_recipe.xml
//            binding.ivRecipeImage.scaleType = ImageView.ScaleType.CENTER_CROP
//            binding.ivRecipeImage.setImageURI(null)
//            binding.ivRecipeImage.setImageURI(uri)
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityAddRecipeBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        fetchInventoryForSpinners()
//
//        // Handle Image Click
//        binding.ivRecipeImage.setOnClickListener {
//            pickImageLauncher.launch("image/*")
//        }
//
//        binding.btnAddIngredient.setOnClickListener {
//            addIngredientRow()
//        }
//
//        binding.btnSaveRecipe.setOnClickListener {
//            saveRecipe()
//        }
//    }
//
//    private fun fetchInventoryForSpinners() {
//        CocobarApi.create().getInventory().enqueue(object : Callback<List<RawMaterial>> {
//            override fun onResponse(call: Call<List<RawMaterial>>, response: Response<List<RawMaterial>>) {
//                if (response.isSuccessful) {
//                    fullInventoryList = response.body() ?: emptyList()
//                    addIngredientRow()
//                }
//            }
//            override fun onFailure(call: Call<List<RawMaterial>>, t: Throwable) {
//                Toast.makeText(this@AddRecipeActivity, "Failed to load inventory", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private fun addIngredientRow() {
//        if (fullInventoryList.isEmpty()) {
//            Toast.makeText(this, "No inventory items available", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val rowView = LayoutInflater.from(this).inflate(R.layout.item_recipe_ingredient_row, null)
//        val spinner = rowView.findViewById<Spinner>(R.id.spinnerInventory)
//        val tvUnit = rowView.findViewById<TextView>(R.id.tvUnit)
//
//        val itemNames = fullInventoryList.map { it.itemName }
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, itemNames)
//        spinner.adapter = adapter
//
//        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                tvUnit.text = fullInventoryList[position].unitType
//            }
//            override fun onNothingSelected(parent: AdapterView<*>?) {}
//        }
//
//        binding.llIngredientsContainer.addView(rowView)
//    }
//
//    private fun saveRecipe() {
//        val recipeName = binding.etRecipeName.text.toString().trim()
//        val sellingPriceStr = binding.etSellingPrice.text.toString().trim()
//
//        if (recipeName.isEmpty() || sellingPriceStr.isEmpty()) {
//            Toast.makeText(this, "Enter name and price", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val ingredientsList = mutableListOf<RecipeIngredient>()
//
//        // Validation Loop
//        for (i in 0 until binding.llIngredientsContainer.childCount) {
//            val rowView = binding.llIngredientsContainer.getChildAt(i)
//            val spinner = rowView.findViewById<Spinner>(R.id.spinnerInventory)
//            val etQty = rowView.findViewById<EditText>(R.id.etQuantityRequired)
//
//            val qtyStr = etQty.text.toString().trim()
//
//            // Strict Validation: Block empty or 0 quantities
//            if (qtyStr.isEmpty()) {
//                Toast.makeText(this, "Error: Please enter a quantity for all selected ingredients.", Toast.LENGTH_LONG).show()
//                return
//            }
//
//            val qty = qtyStr.toDoubleOrNull() ?: 0.0
//            if (qty <= 0) {
//                Toast.makeText(this, "Error: Quantity must be greater than 0.", Toast.LENGTH_LONG).show()
//                return
//            }
//
//            val selectedPosition = spinner.selectedItemPosition
//            val rawMaterial = fullInventoryList[selectedPosition]
//
//            ingredientsList.add(RecipeIngredient(rawMaterial, qty))
//        }
//
//        if (ingredientsList.isEmpty()) {
//            Toast.makeText(this, "Add at least one ingredient", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val imageUrlString = selectedImageUri?.toString()
//
//        val newRecipe = Recipe(
//            name = recipeName,
//            sellingPrice = sellingPriceStr.toDouble(),
//            imageUrl = imageUrlString,
//            ingredients = ingredientsList
//        )
//
//        CocobarApi.create().addRecipe(newRecipe).enqueue(object : Callback<Recipe> {
//            override fun onResponse(call: Call<Recipe>, response: Response<Recipe>) {
//                if (response.isSuccessful) {
//                    Toast.makeText(this@AddRecipeActivity, "Recipe Saved!", Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//            }
//            override fun onFailure(call: Call<Recipe>, t: Throwable) {
//                Toast.makeText(this@AddRecipeActivity, "Error saving recipe", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//}