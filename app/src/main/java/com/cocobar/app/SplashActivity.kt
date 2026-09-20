package com.cocobar.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity

//import com.example.calculator.com.example.calculator.RegisterActivity


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        startSplashAnimation()
    }

    private fun startSplashAnimation() {

        val logoCircle = findViewById<View>(R.id.logoCircle)
        val goldRing = findViewById<View>(R.id.goldRing)
        val glowView = findViewById<View>(R.id.glowView)
        val splashTitle = findViewById<View>(R.id.tvSplashTitle)

        // -----------------------------------------
        // 1. COCOBAR LOGO APPEARS
        // -----------------------------------------

        logoCircle.animate()
            .alpha(1f)
            .scaleX(1.08f)
            .scaleY(1.08f)
            .setDuration(550)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {

                logoCircle.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(250)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            .start()


        // -----------------------------------------
        // 2. GOLD LIGHT / GLOW
        // -----------------------------------------

        glowView.animate()
            .alpha(0.9f)
            .scaleX(1.15f)
            .scaleY(1.15f)
            .setDuration(850)
            .setInterpolator(DecelerateInterpolator())
            .start()


        // -----------------------------------------
        // 3. GOLD RING
        // -----------------------------------------

        goldRing.animate()
            .alpha(1f)
            .rotation(360f)
            .setDuration(1100)
            .setInterpolator(DecelerateInterpolator())
            .start()


        // -----------------------------------------
        // 4. COCOBAR NAME
        // -----------------------------------------

        splashTitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(750)
            .setDuration(650)
            .setInterpolator(DecelerateInterpolator())
            .start()


        // -----------------------------------------
        // 5. OPEN REGISTER ACTIVITY
        // -----------------------------------------

        Handler(Looper.getMainLooper()).postDelayed({

            val intent = Intent(
                this@SplashActivity,
                DashboardActivity::class.java
            )

            startActivity(intent)

            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )

            finish()

        }, 2800)
    }
}