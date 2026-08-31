package com.cocobar.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cocobar.app.databinding.ActivitySaleSuccessBinding
import com.cocobar.app.network.Sale

class SaleSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySaleSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaleSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Receiving the complete Sale object
        val sale = intent.getSerializableExtra("SALE_RECORD") as? Sale

        if (sale != null) {
            binding.tvSummaryProduct.text = sale.recipe.name
            binding.tvSummaryQty.text = sale.quantity.toString()
            binding.tvSummaryTotal.text = "₹${String.format("%.2f", sale.totalPrice)}"

            // You now have access to sale.saleDate here for future use!
        }

        binding.btnGoToProducts.setOnClickListener {
            val intent = Intent(this, ProductsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        binding.btnViewHistory.setOnClickListener {
            Toast.makeText(this, "Sales History coming in next update!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        binding.btnGoToProducts.performClick()
    }
}