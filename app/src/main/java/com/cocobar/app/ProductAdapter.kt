package com.cocobar.app

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cocobar.app.network.Recipe

class ProductAdapter(
    private var productList: List<Recipe>,
    private val onItemClick: (Recipe, Int) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvProductName)
        val tvPrice: TextView = view.findViewById(R.id.tvProductPrice)
        val tvAvailable: TextView = view.findViewById(R.id.tvProductAvailable)
        val ivImage: ImageView = view.findViewById(R.id.ivProductImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_card, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.tvName.text = product.name
        holder.tvPrice.text = "₹${String.format("%.2f", product.sellingPrice)} per unit"

        // Calculate max available based on lowest inventory ingredient
        var maxAvailable = Int.MAX_VALUE
        if (product.ingredients.isEmpty()) maxAvailable = 0

        for (ingredient in product.ingredients) {
            if (ingredient.quantityRequired > 0) {
                val possible = (ingredient.rawMaterial.currentQuantity / ingredient.quantityRequired).toInt()
                if (possible < maxAvailable) maxAvailable = possible
            }
        }
        if (maxAvailable == Int.MAX_VALUE) maxAvailable = 0

        holder.tvAvailable.text = "Available: $maxAvailable"

        if (!product.imageUrl.isNullOrEmpty()) {
            try { holder.ivImage.setImageURI(Uri.parse(product.imageUrl)) }
            catch (e: Exception) { holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery) }
        }

        holder.itemView.setOnClickListener { onItemClick(product, maxAvailable) }
    }

    override fun getItemCount() = productList.size
}