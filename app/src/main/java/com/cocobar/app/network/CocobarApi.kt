package com.cocobar.app.network

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import java.io.Serializable

// Existing Registration DTO
data class RegistrationRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("mobileNumber") val mobileNumber: String,
    @SerializedName("businessEmail") val businessEmail: String,
    @SerializedName("password") val password: String
)

// Login DTO
data class LoginRequest(
    @SerializedName("businessEmail") val businessEmail: String,
    @SerializedName("password") val password: String
)

// Response DTO (used for both Login and Registration)
data class UserResponse(
    val id: Long,
    val fullName: String,
    val businessEmail: String,
    val mobileNumber: String
)

// NEW: Raw Material DTO
data class RawMaterial(
    val itemNo: Long? = null,
    val imageUrl: String? = null, // 2 - Item image (Optional)
    val itemName: String,         // 3 - Item Name
    val unitType: String,         // 4 - Quantity unit (kg/grams)
    val dateAdded: String,        // 5 - Date added
    val currentQuantity: Double,  // 6 - Initial quantity
    val lowStockLimit: Double,    // 7 - Low limit alert
    val unitPrice: Double         // 8 - Price
) : Serializable

// Merged Interface
interface CocobarApi {
    @POST("api/auth/register")
    fun registerUser(@Body request: RegistrationRequest): Call<UserResponse>

    @POST("api/auth/login")
    fun loginUser(@Body request: LoginRequest): Call<UserResponse>

    @GET("api/inventory")
    fun getInventory(): Call<List<RawMaterial>>

    @POST("api/inventory")
    fun addInventoryItem(@Body item: RawMaterial): Call<RawMaterial>

    // NEW: Update existing item
    @PUT("api/inventory/{id}")
    fun updateInventoryItem(@retrofit2.http.Path("id") id: Long, @Body item: RawMaterial): Call<RawMaterial>

    // NEW: Delete existing item
    @retrofit2.http.DELETE("api/inventory/{id}")
    fun deleteInventoryItem(@retrofit2.http.Path("id") id: Long): Call<Void>

    // RECIPE ENDPOINTS
    @retrofit2.http.GET("api/recipes")
    fun getRecipes(): Call<List<Recipe>>

    @retrofit2.http.POST("api/recipes")
    fun addRecipe(@retrofit2.http.Body recipe: Recipe): Call<Recipe>

    @PUT("api/recipes/{id}")
    fun updateRecipe(@retrofit2.http.Path("id") id: Long, @Body recipe: Recipe): Call<Recipe>

    @DELETE("api/recipes/{id}")
    fun deleteRecipe(@retrofit2.http.Path("id") id: Long): Call<Void>

    @retrofit2.http.POST("api/sales")
    fun recordSale(@retrofit2.http.Body request: SaleRequest): Call<Sale>

    @retrofit2.http.POST("api/sales/bulk")
    fun recordBulkSale(@retrofit2.http.Body requests: List<SaleRequest>): Call<List<Sale>>

    companion object {
        private const val BASE_URL = "http://10.30.165.69:8080/"

        fun create(): CocobarApi {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(CocobarApi::class.java)
        }
    }
}

// 1. Add Data Classes at the bottom of CocobarApi.kt
data class RecipeIngredient(
    val rawMaterial: RawMaterial,
    val quantityRequired: Double
) : Serializable

data class Recipe(
    val id: Long? = null,
    val name: String,
    val sellingPrice: Double,
    val imageUrl: String? = null, // Added image URL
    val ingredients: List<RecipeIngredient>
) : Serializable

// Add this to your data classes at the bottom of CocobarApi.kt
data class Sale(
    val id: Long? = null,
    val recipe: Recipe,
    val quantity: Int,
    val totalPrice: Double,
    val saleDate: String
) : Serializable

data class SaleRequest(
    val recipeId: Long,
    val quantity: Int
)

data class CartItem(
    val product: Recipe,
    var quantity: Int
)

object CartManager {
    val cartItems = mutableListOf<CartItem>()

    fun addItem(product: Recipe, quantity: Int) {
        val existing = cartItems.find { it.product.id == product.id }
        if (existing != null) {
            existing.quantity += quantity
        } else {
            cartItems.add(CartItem(product, quantity))
        }
    }

    fun getTotalPrice(): Double {
        return cartItems.sumOf { it.product.sellingPrice * it.quantity }
    }

    fun clearCart() {
        cartItems.clear()
    }
}