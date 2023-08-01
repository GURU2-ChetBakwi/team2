package com.guru2.chetbakwi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class Splash : AppCompatActivity() {

    lateinit var splash : ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)

        splash = findViewById(R.id.SplashLayout)

        splash.setOnClickListener {
            val intent = Intent(this@Splash, MainActivity::class.java)

            // pdfViewPage로 넘어가기
            startActivity(intent)
            finish()
        }
    }
}