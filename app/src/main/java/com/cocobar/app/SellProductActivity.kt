package com.cocobar.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cocobar.app.databinding.ActivitySellProductBinding
import com.cocobar.app.network.CocobarApi
import com.cocobar.app.network.Recipe
import com.cocobar.app.network.Sale
import com.cocobar.app.network.SaleRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SellProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySellProductBinding
    private lateinit var product: Recipe
    private var maxAvailable: Int = 0
    private var currentQty = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        product = intent.getSerializableExtra("PRODUCT_DATA") as Recipe
        maxAvailable = intent.getIntExtra("MAX_AVAILABLE", 0)

        // Setup UI
        binding.tvProductName.text = product.name
        binding.tvProductPrice.text = "₹${String.format("%.2f", product.sellingPrice)} per unit"
        binding.tvProductAvailable.text = "Available: $maxAvailable"

        // 1. Set a default placeholder first
        binding.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)

        // 2. Safely attempt to load the real image
        val imageUrl = intent.getStringExtra("PRODUCT_IMAGE_URL") // Or however you get your image string
        if (!imageUrl.isNullOrBlank()) {
            try {
                binding.ivProductImage.setImageURI(Uri.parse(imageUrl))
            } catch (e: SecurityException) {
                // If Android blocks the image, just keep the placeholder instead of crashing!
                binding.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
            } catch (e: Exception) {
                binding.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }

//        if (!product.imageUrl.isNullOrEmpty()) {
//            try { binding.ivProductImage.setImageURI(Uri.parse(product.imageUrl)) }
//            catch (e: Exception) {}
//        }

        updatePriceUI()

        binding.btnMinus.setOnClickListener {
            if (currentQty > 1) {
                currentQty--
                updatePriceUI()
            }
        }

        binding.btnPlus.setOnClickListener {
            if (currentQty < maxAvailable) {
                currentQty++
                updatePriceUI()
            } else {
                Toast.makeText(this, "Not enough inventory stock!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSellProduct.setOnClickListener {
            if (currentQty > 0 && currentQty <= maxAvailable) {
                processSale()
            } else {
                Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show()
            }
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun updatePriceUI() {
        binding.tvQuantity.text = currentQty.toString()
        val total = currentQty * product.sellingPrice
        binding.tvTotalPrice.text = "₹${String.format("%.2f", total)}"
    }

    private fun processSale() {
        // Add the selected item to our global order manager
        com.cocobar.app.network.CartManager.addItem(product, currentQty)

        Toast.makeText(this, "${currentQty}x ${product.name} added to order!", Toast.LENGTH_SHORT).show()
        finish() // Return to the product list so the owner can add more items
    }

//    private fun processSale() {
//        binding.btnSellProduct.isEnabled = false // Prevent double clicks
//        val request = SaleRequest(recipeId = product.id!!, quantity = currentQty)
//
//        CocobarApi.create().recordSale(request).enqueue(object : Callback<Sale> {
//            override fun onResponse(call: Call<Sale>, response: Response<Sale>) {
//                try {
//                    if (response.isSuccessful) {
//                        val saleRecord = response.body()
//                        val intent = Intent(this@SellProductActivity, SaleSuccessActivity::class.java)
//                        intent.putExtra("SALE_RECORD", saleRecord)
//                        startActivity(intent)
//                        finish()
//                    } else {
//                        binding.btnSellProduct.isEnabled = true
//                        Toast.makeText(this@SellProductActivity, "Server Error Code: ${response.code()}", Toast.LENGTH_LONG).show()
//                    }
//                } catch (e: Exception) {
//                    binding.btnSellProduct.isEnabled = true
//                    Toast.makeText(this@SellProductActivity, "App Error: ${e.message}", Toast.LENGTH_LONG).show()
//                }
//            }
//
//            override fun onFailure(call: Call<Sale>, t: Throwable) {
//                binding.btnSellProduct.isEnabled = true
//                Toast.makeText(this@SellProductActivity, "Network Error: Cannot reach server", Toast.LENGTH_LONG).show()
//            }
//        })
//    }


}