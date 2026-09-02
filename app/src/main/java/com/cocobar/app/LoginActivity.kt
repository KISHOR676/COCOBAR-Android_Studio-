package com.cocobar.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cocobar.app.network.CocobarApi
import com.cocobar.app.network.LoginRequest
import com.cocobar.app.network.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etLoginEmail: EditText
    private lateinit var etLoginPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvFooterRegister: TextView

    private val api: CocobarApi by lazy { CocobarApi.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etLoginEmail = findViewById(R.id.etLoginEmail)
        etLoginPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvFooterRegister = findViewById(R.id.tvFooterRegister)

        btnLogin.setOnClickListener {
            handleLogin()
        }

        // Navigate to Register page when clicked
        tvFooterRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish() // Close login screen so user doesn't return here on pressing 'back'
        }
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // --- AUTO-LOGIN CHECK ---
//        val sharedPref = getSharedPreferences("CocobarPrefs", MODE_PRIVATE)
//        val savedName = sharedPref.getString("USER_NAME", null)
//
//        // If a name is already saved, the user is logged in. Skip straight to Dashboard!
//        if (savedName != null) {
//            val intent = Intent(this, DashboardActivity::class.java)
//            startActivity(intent)
//            finish() // Close LoginActivity so they can't press 'back' to get here
//            return // Stop executing the rest of this screen's setup
//        }
//        // ------------------------
//
//        // If no name is saved, load the normal login screen
//        setContentView(R.layout.activity_login)
//
//        etLoginEmail = findViewById(R.id.etLoginEmail)
//        etLoginPassword = findViewById(R.id.etLoginPassword)
//        btnLogin = findViewById(R.id.btnLogin)
//        tvFooterRegister = findViewById(R.id.tvFooterRegister)
//
//        btnLogin.setOnClickListener {
//            handleLogin()
//        }
//
//        // Navigate to Register page when clicked
//        tvFooterRegister.setOnClickListener {
//            val intent = Intent(this, RegisterActivity::class.java)
//            startActivity(intent)
//            finish() // Close login screen so user doesn't return here on pressing 'back'
//        }
//    }

    private fun handleLogin() {
        val email = etLoginEmail.text.toString().trim()
        val password = etLoginPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = LoginRequest(email, password)

        api.loginUser(request).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    // --- ADD THESE TWO LINES TO SAVE THE NAME ---
                    val sharedPref = getSharedPreferences("CocobarPrefs", MODE_PRIVATE)
                    sharedPref.edit().putString("USER_NAME", user?.fullName).apply()
                    // --------------------------------------------
                    Toast.makeText(this@LoginActivity, "Welcome ${user?.fullName}!", Toast.LENGTH_LONG).show()

                    // Navigate to the Dashboard
                    val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                    startActivity(intent)

                    // Call finish() so the user cannot use the back button to return to the login screen
                    finish()

                } else {
                    val errorBody = response.errorBody()?.string() ?: "Login Failed"
                    Toast.makeText(this@LoginActivity, errorBody, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("COCOBAR_API", "Error: ${t.message}")
                Toast.makeText(this@LoginActivity, "Connection error. Is the server running?", Toast.LENGTH_LONG).show()
            }
        })
    }
}





