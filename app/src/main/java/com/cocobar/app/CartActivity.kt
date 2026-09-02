package com.cocobar.app

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cocobar.app.databinding.ActivityCartBinding
import com.cocobar.app.network.CartManager
import com.cocobar.app.network.CocobarApi
import com.cocobar.app.network.Sale
import com.cocobar.app.network.SaleRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        loadCartItems()

        binding.btnCheckout.setOnClickListener {
            if (CartManager.cartItems.isEmpty()) return@setOnClickListener
            processBulkSale()
        }
    }

    private fun loadCartItems() {
        binding.llCartItems.removeAllViews()
        val total = CartManager.getTotalPrice()
        binding.tvGrandTotal.text = "₹${String.format("%.2f", total)}"

        for (item in CartManager.cartItems) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 16, 0, 16)
                gravity = Gravity.CENTER_VERTICAL
            }

            val tvName = TextView(this).apply {
                text = "${item.quantity}x ${item.product.name}"
                textSize = 18f
                setTextColor(Color.parseColor("#312018"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvPrice = TextView(this).apply {
                text = "₹${String.format("%.2f", item.product.sellingPrice * item.quantity)}"
                textSize = 16f
                setTextColor(Color.parseColor("#388E3C"))
            }

            row.addView(tvName)
            row.addView(tvPrice)
            binding.llCartItems.addView(row)
        }
    }

    private fun processBulkSale() {
        binding.btnCheckout.isEnabled = false
        binding.btnCheckout.text = "Processing..."

        // Convert the order into a list of requests for the backend
        val requests = CartManager.cartItems.map {
            SaleRequest(it.product.id!!, it.quantity)
        }

        CocobarApi.create(this).recordBulkSale(requests).enqueue(object : Callback<List<Sale>> {
            override fun onResponse(call: Call<List<Sale>>, response: Response<List<Sale>>) {
                if (response.isSuccessful) {
                    val finalTotal = CartManager.getTotalPrice()
                    val finalCount = CartManager.cartItems.sumOf { it.quantity }
                    CartManager.clearCart() // Empty the order manager

                    val intent = Intent(this@CartActivity, SaleSuccessActivity::class.java)
                    intent.putExtra("TOTAL_PRICE", finalTotal)
                    intent.putExtra("ITEM_COUNT", finalCount)
                    startActivity(intent)
                    finish()
                } else {
                    binding.btnCheckout.isEnabled = true
                    binding.btnCheckout.text = "Complete Sale"
                    val errorMsg = response.errorBody()?.string() ?: "Unknown Server Error"
                    AlertDialog.Builder(this@CartActivity)
                        .setTitle("Sale Rejected")
                        .setMessage(errorMsg)
                        .setPositiveButton("OK", null)
                        .show()
                }
            }

            override fun onFailure(call: Call<List<Sale>>, t: Throwable) {
                binding.btnCheckout.isEnabled = true
                binding.btnCheckout.text = "Complete Sale"
                Toast.makeText(this@CartActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}