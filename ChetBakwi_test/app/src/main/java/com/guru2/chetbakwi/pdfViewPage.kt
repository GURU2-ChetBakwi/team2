package com.guru2.chetbakwi

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class pdfViewPage : AppCompatActivity() {

    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var imageView: ImageView
    private lateinit var pdfFile: File
    private lateinit var currentPage: PdfRenderer.Page

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfview)

        imageView = findViewById(R.id.imageView)

        // pdfUri 전달받기
        val pdfUri: Uri = intent.data!!
        pdfFile = File(cacheDir, "temp.pdf")
        pdfFile.createNewFile()

        contentResolver.openInputStream(pdfUri)?.use { inputStream ->
            FileOutputStream(pdfFile).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
        }

        // pdfUri 를 제대로 전달받았다면
        if (pdfUri != null) {
            // pdf 파일 열기
            openRenderer()

            // 첫 번째 페이지를 보여주기
            showPage(0)
            // for문으로 0 ~ pdfRenderer.pageCount -1 까지 showPage 반복해서 다음 페이지 버튼 누르면
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

    private fun openRenderer() {
        val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(fileDescriptor)
    }

    private fun showPage(index: Int) {
        if (index < 0 || index >= pdfRenderer.pageCount) {
            return
        }
        // 현재 페이지가 열려 있으면 닫기
        if (::currentPage.isInitialized) {
            currentPage.close()
        }
        // 새로운 페이지를 열기
        currentPage = pdfRenderer.openPage(index)
        // 비트맵을 만들어 이미지뷰에 보여주기
        val bitmap = Bitmap.createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        imageView.setImageBitmap(bitmap)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 액티비티가 종료될 때, 리소스를 정리
        currentPage.close()
        pdfRenderer.close()
    }
}