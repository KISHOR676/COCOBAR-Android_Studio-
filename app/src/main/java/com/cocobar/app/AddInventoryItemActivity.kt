package com.cocobar.app

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cocobar.app.databinding.ActivityAddInventoryBinding
import com.cocobar.app.network.CocobarApi
import com.cocobar.app.network.RawMaterial
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddInventoryItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddInventoryBinding
    private var selectedImageUri: Uri? = null
    private lateinit var currentDateString: String

    // Standard Android Image Picker logic
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            // Fix: Use setImageURI directly, but ensure the scale type is correct
            binding.ivItemImage.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            binding.ivItemImage.setImageURI(null) // Reset first
            binding.ivItemImage.setImageURI(uri) // Show selected image in the UI
            binding.ivItemImage.setPadding(0, 0, 0, 0) // Remove padding to fit the image nicely
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Setup Header Back Button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 2. Set Current Date automatically
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val today = Date()

        currentDateString = dateFormat.format(today) // Format for database (e.g., 2026-08-30)
        binding.tvDateAdded.text = "Date Added: ${displayFormat.format(today)}" // Format for UI

        // 3. Setup Image Click Listener
        binding.cardImagePicker.setOnClickListener {
            pickImageLauncher.launch("image/*") // Opens phone gallery
        }

        // 4. Setup Save Button
        binding.btnSaveItem.setOnClickListener {
            saveItemToDatabase()
        }
    }

    private fun saveItemToDatabase() {
        // Gather all text inputs
        val name = binding.etItemName.text.toString().trim()
//        val unit = binding.etUnitType.text.toString().trim()
        val quantityStr = binding.etQuantity.text.toString().trim()
        val lowLimitStr = binding.etLowLimit.text.toString().trim()
        val priceStr = binding.etPrice.text.toString().trim()
        // Read the selected Radio Button
        val unit = when (binding.rgUnitType.checkedRadioButtonId) {
            R.id.rbKg -> "kg"
            R.id.rbGrams -> "grams"
            R.id.rbMl -> "ml"
            R.id.rbPcs -> "pcs"
            else -> "units"
        }

        // Validate required fields
        if (name.isEmpty() || unit.isEmpty() || quantityStr.isEmpty() || lowLimitStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val initialQty = quantityStr.toDoubleOrNull() ?: 0.0
        val lowLimit = lowLimitStr.toDoubleOrNull() ?: 0.0

        // LOGIC CHECK: Ensure low limit is strictly less than initial quantity
        if (lowLimit >= initialQty) {
            Toast.makeText(this, "Low Stock Limit must be less than Initial Quantity", Toast.LENGTH_LONG).show()
            return // Stop the save process
        }

        // Convert the optional Image URI to a string if it exists
        val imageUrlString = selectedImageUri?.toString()

        // Create the data object with all 8 requirements
        val newItem = RawMaterial(
            imageUrl = imageUrlString, // 2. Optional Image
            itemName = name,           // 3. Item Name
            unitType = unit,           // 4. Quantity (kg/grams)
            dateAdded = currentDateString, // 5. Date added (Auto)
            currentQuantity = quantityStr.toDoubleOrNull() ?: 0.0, // 6. Current Quantity
            lowStockLimit = lowLimitStr.toDoubleOrNull() ?: 0.0,   // 7. Low Limit
            unitPrice = priceStr.toDoubleOrNull() ?: 0.0           // 8. Price
        )

        // Send to Spring Boot Backend
        CocobarApi.create().addInventoryItem(newItem).enqueue(object : Callback<RawMaterial> {
            override fun onResponse(call: Call<RawMaterial>, response: Response<RawMaterial>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddInventoryItemActivity, "Item Saved Successfully!", Toast.LENGTH_SHORT).show()
                    finish() // Close the form and return to the list
                } else {
                    Toast.makeText(this@AddInventoryItemActivity, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RawMaterial>, t: Throwable) {
                Toast.makeText(this@AddInventoryItemActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}








//package com.cocobar.app
//
//import android.os.Bundle
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.cocobar.app.databinding.ActivityAddInventoryBinding
//import com.cocobar.app.network.CocobarApi
//import com.cocobar.app.network.RawMaterial
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class AddInventoryItemActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityAddInventoryBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityAddInventoryBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.btnSaveItem.setOnClickListener {
//            saveItemToDatabase()
//        }
//    }
//
//    private fun saveItemToDatabase() {
//        // 1. Gather all inputs
//        val name = binding.etItemName.text.toString().trim()
//        val unit = binding.etUnitType.text.toString().trim()
//        val quantityStr = binding.etQuantity.text.toString().trim()
//        val lowLimitStr = binding.etLowLimit.text.toString().trim()
//        val priceStr = binding.etPrice.text.toString().trim()
//
//        // 2. Validate empty fields
//        if (name.isEmpty() || unit.isEmpty() || quantityStr.isEmpty() || lowLimitStr.isEmpty() || priceStr.isEmpty()) {
//            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // 3. Create the data object
//        val newItem = RawMaterial(
//            itemName = name,
//            unitType = unit,
//            currentQuantity = quantityStr.toDoubleOrNull() ?: 0.0,
//            lowStockLimit = lowLimitStr.toDoubleOrNull() ?: 0.0,
//            unitPrice = priceStr.toDoubleOrNull() ?: 0.0
//        )
//
//        // 4. Send to Spring Boot Backend
//        CocobarApi.create().addInventoryItem(newItem).enqueue(object : Callback<RawMaterial> {
//            override fun onResponse(call: Call<RawMaterial>, response: Response<RawMaterial>) {
//                if (response.isSuccessful) {
//                    Toast.makeText(this@AddInventoryItemActivity, "Item Saved Successfully!", Toast.LENGTH_SHORT).show()
//                    finish() // Close the form and return to the list
//                } else {
//                    Toast.makeText(this@AddInventoryItemActivity, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<RawMaterial>, t: Throwable) {
//                Toast.makeText(this@AddInventoryItemActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
//            }
//        })
//    }
//}