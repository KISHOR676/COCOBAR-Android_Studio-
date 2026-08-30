package com.cocobar.app

import android.content.Intent // <--- ADD THIS IMPORT HERE
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cocobar.app.network.CocobarApi
import com.cocobar.app.network.RegistrationRequest
import com.cocobar.app.network.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etMobileNumber: EditText
    private lateinit var etBusinessEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvFooterText: TextView

    private val api: CocobarApi by lazy { CocobarApi.create() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etFullName = findViewById(R.id.etFullName)
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etBusinessEmail = findViewById(R.id.etBusinessEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvFooterText = findViewById(R.id.tvFooterText)

        btnRegister.setOnClickListener {
            handleRegistration()
        }

        // ---> ADD YOUR CODE RIGHT HERE <---
        // Handle the footer link to Login screen
        tvFooterText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        // -----------------------------------
    }

    private fun handleRegistration() {
        val fullName = etFullName.text.toString().trim()
        val mobile = etMobileNumber.text.toString().trim()
        val email = etBusinessEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        if (fullName.isEmpty() || mobile.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = RegistrationRequest(fullName, mobile, email, password)

        api.registerUser(request).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, "Registration Successful!", Toast.LENGTH_LONG).show()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Registration Failed"
                    Toast.makeText(this@RegisterActivity, errorBody, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                val realError = t.message ?: "Unknown error"
                Log.e("COCOBAR_API", "Error: $realError")
                Toast.makeText(this@RegisterActivity, "Failed: $realError", Toast.LENGTH_LONG).show()
            }
        })
    }
}