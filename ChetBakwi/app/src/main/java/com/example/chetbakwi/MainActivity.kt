package com.example.chetbakwi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    // pdf 파일 로컬에서 찾아오는 버튼
    lateinit var btnUpload: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // pdf 파일 로컬에서 찾아오는 버튼
        btnUpload = findViewById<Button>(R.id.btnUpload)

        // pdf 파일 찾아오는 버튼 클릭 시
        btnUpload.setOnClickListener {
            // 로컬 파일 내에서 원하는 pdf 파일 선택하면 파일의 경로 반환
        }
    }
}