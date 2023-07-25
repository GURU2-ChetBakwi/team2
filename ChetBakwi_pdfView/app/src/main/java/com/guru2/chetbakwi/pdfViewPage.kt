package com.guru2.chetbakwi

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import java.io.File

class pdfViewPage : AppCompatActivity() {

    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var pdfView: PDFView
    private lateinit var pdfFile: File
    private lateinit var currentPage: PdfRenderer.Page

    // 추가
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private var currentPageIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfview)

        pdfView = findViewById(R.id.pdfView)

        // pdfUri 전달받기
        val pdfUri: Uri = intent.data!!

        // pdfUri 를 제대로 전달받았다면
        if (pdfUri != null) {

            // pdf 열기
            pdfView.fromUri(pdfUri).load()

        } else {
            // pdfUri 를 제대로 전달받지 못했다면 파일을 다시 선택하라는 alertDialog 띄우기
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("PDF 파일 선택 오류")
            alertDialogBuilder.setMessage("PDF 파일을 다시 선택해주세요.")
            alertDialogBuilder.setPositiveButton("예") { dialogInterface: DialogInterface, i: Int ->
                // 메인 화면으로 돌아가기
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }
}