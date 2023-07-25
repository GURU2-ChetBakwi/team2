package com.guru2.chetbakwi

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

class pdfViewPage : AppCompatActivity() {

    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var imageView: ImageView
    private lateinit var pdfFile: File
    private lateinit var currentPage: PdfRenderer.Page

    // 추가
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private var currentPageIndex: Int = 0
    private lateinit var tesseract: TessBaseAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfview)

        imageView = findViewById(R.id.imageView)

        //추가
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)

        // tesseract 초기화
        initTesseract()

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

            extractTextFromPDF(pdfUri)
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

        //추가
        // 이전 페이지 버튼 클릭 리스너
        btnPrev.setOnClickListener {
            showPreviousPage()
        }

        // 다음 페이지 버튼 클릭 리스너
        btnNext.setOnClickListener {
            showNextPage()
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

    //추가
    // 이전 페이지 보이기
    private fun showPreviousPage() {
        if (currentPageIndex > 0) {
            currentPageIndex--
            showPage(currentPageIndex)
        } else {
            // 첫 페이지이므로 이전 페이지 없음을 알림
            Toast.makeText(this, "첫 페이지입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 다음 페이지 보이기
    private fun showNextPage() {
        if (currentPageIndex < pdfRenderer.pageCount - 1) {
            currentPageIndex++
            showPage(currentPageIndex)
        } else {
            // 마지막 페이지이므로 다음 페이지 없음을 알림
            Toast.makeText(this, "마지막 페이지입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 액티비티가 종료될 때, 리소스를 정리
        currentPage.close()
        pdfRenderer.close()
        tesseract.end()
    }

    private fun initTesseract() {
        tesseract = TessBaseAPI()
        // cacheDir.absolutePath : /data/data/com.guru2.chetbakwi/cache/tessdata
        // 한국어와 영어를 동시에 인식 -> 추출
        tesseract.init(cacheDir.absolutePath, "kor+eng")
    }

    private fun extractTextFromPDF(pdfUri: Uri) {
        GlobalScope.launch(Dispatchers.IO) {
            val pdfText = withContext(Dispatchers.IO) {
                try {
                    // 읽기로 pdfuri 열기
                    val pdfFileDescriptor: ParcelFileDescriptor? = contentResolver.openFileDescriptor(pdfUri, "r")
                    if (pdfFileDescriptor != null) {
                        // PDF에서 텍스트 추출
                        val pdfTextBuilder = StringBuilder()
                        val pdfRenderer = PdfRenderer(pdfFileDescriptor)
                        // 첫번째 페이지부터 마지막 페이지까지
                        for (pageIndex in 0 until pdfRenderer.pageCount) {
                            // 페이지 열기
                            val page = pdfRenderer.openPage(pageIndex)
                            // 텍스트 추출
                            val text = tesseractOCR(page)
                            // textBulder에 append
                            pdfTextBuilder.append(text)
                            // 열어둔 페이지 닫기
                            page.close()
                        }
                        // pdfRenderer 닫기
                        pdfRenderer.close()
                        pdfTextBuilder.toString()
                    } else {
                        "Error: Unable to open PDF file."
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    "Error: File not found."
                }
            }
            // 추출된 pdfText print
            println(pdfText)
        }
    }

    private fun tesseractOCR(page: PdfRenderer.Page): String {
        // pdf 페이지 너비, 높이 기반 비트맵 생성 (pdf -> img)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        // pdf 페이지를 전체 영역으로 렌더링 -> 비트맵에 그리기
        val rect = Rect(0, 0, page.width, page.height)
        page.render(bitmap, rect, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        // tesseract OCR 에 비트맵 설정 -> 이미지 인식
        tesseract.setImage(bitmap)
        // 인식된 텍스트 utf8로 변환 후 return
        return tesseract.utF8Text
    }
}