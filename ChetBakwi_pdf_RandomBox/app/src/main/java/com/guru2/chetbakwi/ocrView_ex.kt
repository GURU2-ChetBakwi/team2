package com.guru2.chetbakwi

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class ocrView_ex : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the switch is ON or OFF
        val switchState = intent.getBooleanExtra("switchState", false)

        if (switchState) {
            // If the switch is ON, display the regular OCR view
            setContentView(R.layout.activity_ocr_ex)
            // Add code here to handle OCR view setup and display
            // You can customize this view to include OCR functionality, text recognition, or any other OCR-related features.
        } else {
            // If the switch is OFF, display a blank screen
            setContentView(R.layout.activity_ocr_ex)
            // This will display a blank screen as you requested. You can customize this blank screen layout as needed.
        }
    }
}