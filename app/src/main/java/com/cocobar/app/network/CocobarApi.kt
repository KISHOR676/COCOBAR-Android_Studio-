package com.cocobar.app.network

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

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
)

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



//package com.cocobar.app.network
//
//import com.google.gson.annotations.SerializedName
//import retrofit2.Call
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.http.Body
//import retrofit2.http.POST
//
//// Existing Registration DTO
//data class RegistrationRequest(
//    @SerializedName("fullName") val fullName: String,
//    @SerializedName("mobileNumber") val mobileNumber: String,
//    @SerializedName("businessEmail") val businessEmail: String,
//    @SerializedName("password") val password: String
//)
//
//// NEW: Login DTO
//data class LoginRequest(
//    @SerializedName("businessEmail") val businessEmail: String,
//    @SerializedName("password") val password: String
//)
//
//// Existing Response DTO (used for both Login and Registration)
//data class UserResponse(
//    val id: Long,
//    val fullName: String,
//    val businessEmail: String,
//    val mobileNumber: String
//)
//
//interface CocobarApi {
//    @POST("api/auth/register") // Removed the leading slash
//    fun registerUser(@Body request: RegistrationRequest): Call<UserResponse>
//
//    @POST("api/auth/login") // Removed the leading slash
//    fun loginUser(@Body request: LoginRequest): Call<UserResponse>
//    companion object {
//        private const val BASE_URL = "http://10.162.176.69:8080/"
//
//        fun create(): CocobarApi {
//            val retrofit = Retrofit.Builder()
//                .addConverterFactory(GsonConverterFactory.create())
//                .baseUrl(BASE_URL)
//                .build()
//            return retrofit.create(CocobarApi::class.java)
//        }
//    }
//}
//