package com.cocobar.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cocobar.app.network.Recipe

class RecipeAdapter(
    private var recipeList: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit // This line solves the "Too many arguments" error
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvRecipeName)
        val tvPrice: TextView = view.findViewById(R.id.tvRecipePrice)
        val tvCount: TextView = view.findViewById(R.id.tvIngredientCount)
        val ivImage: android.widget.ImageView = view.findViewById(R.id.ivRecipeImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_card, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        holder.tvName.text = recipe.name
        holder.tvPrice.text = "₹ ${recipe.sellingPrice}"
        holder.tvCount.text = "Contains ${recipe.ingredients.size} ingredients"

        // NEW: Load Image if available
        if (!recipe.imageUrl.isNullOrEmpty()) {
            try {
                holder.ivImage.setImageURI(android.net.Uri.parse(recipe.imageUrl))
            } catch (e: Exception) {
                holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery) // Fallback placeholder
        }

        holder.itemView.setOnClickListener {
            onItemClick(recipe)
        }
    }

    override fun getItemCount() = recipeList.size
}



//package com.cocobar.app
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.cocobar.app.network.Recipe
//
//class RecipeAdapter(
//    private var recipeList: List<Recipe>
//) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {
//
//    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val tvName: TextView = view.findViewById(R.id.tvRecipeName)
//        val tvPrice: TextView = view.findViewById(R.id.tvRecipePrice)
//        val tvCount: TextView = view.findViewById(R.id.tvIngredientCount)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_recipe_card, parent, false)
//        return RecipeViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
//        val recipe = recipeList[position]
//        holder.tvName.text = recipe.name
//        holder.tvPrice.text = "₹ ${recipe.sellingPrice}"
//
//        // Count how many raw materials make up this product
//        val count = recipe.ingredients.size
//        holder.tvCount.text = "Contains $count ingredients"
//    }
//
//    override fun getItemCount() = recipeList.size
//}