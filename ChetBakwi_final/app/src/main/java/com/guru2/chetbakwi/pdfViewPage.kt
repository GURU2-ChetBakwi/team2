package com.guru2.chetbakwi

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class pdfViewPage : AppCompatActivity() {

    // 변수 선언
    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var pdfFile: File
    private lateinit var currentPage: PdfRenderer.Page

    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnRandom: Button
    private lateinit var btnReset: Button
    private lateinit var pdfPageNumber: TextView
    private lateinit var etRandomBoxCount: EditText
    private lateinit var tvBoxCount: TextView

    // 메모 editText, 메모 초기화 버튼
    private lateinit var editTextMemo: EditText
    private lateinit var btnMemoReset: Button

    private lateinit var boxDrawingView: BoxDrawingView

    private var currentPageIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfview)

        // 초기화 및 연결
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        btnRandom = findViewById(R.id.btnRandom)
        btnReset = findViewById(R.id.btnReset)
        etRandomBoxCount = findViewById(R.id.etRandomBoxCount)
        pdfPageNumber = findViewById(R.id.pdfPageNumber)
        tvBoxCount = findViewById(R.id.tvBoxCount)

        editTextMemo = findViewById(R.id.editTextMemo)
        btnMemoReset = findViewById(R.id.btnMemoReset2)

        // 메모 리셋 버튼 클릭 리스너 설정
        btnMemoReset.setOnClickListener {
            editTextMemo.setText("")
        }

        // BoxDrawingView 초기화 및 연결
        boxDrawingView = findViewById(R.id.imageView)
        boxDrawingView.setTvBoxCount(tvBoxCount)

        // pdfUri를 인텐트로 전달받기
        val pdfUri: Uri = intent.data!!
        pdfFile = File(cacheDir, "temp.pdf")
        pdfFile.createNewFile()

        // 선택된 PDF 파일을 임시 위치로 복사
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

        // 이전 페이지 버튼 클릭 리스너
        btnPrev.setOnClickListener {
            showPreviousPage()
        }

        // 다음 페이지 버튼 클릭 리스너
        btnNext.setOnClickListener {
            showNextPage()
        }

        // 빈칸 랜덤 생성 버튼 클릭 리스너
        btnRandom.setOnClickListener {
            val countInput = etRandomBoxCount.text.toString()
            if (countInput.isNotBlank()) {
                val count = countInput.toIntOrNull()
                if (count != null && count > 0) {
                    // 사용자 입력에 기반하여 지정된 개수의 랜덤 박스 생성하는 함수 호출
                    boxDrawingView.addRandomBoxes(count)
                } else {
                    Toast.makeText(this, "유효하지 않은 입력입니다. 유효한 숫자를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "숫자를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 빈칸 초기화 버튼 클릭 리스너
        btnReset.setOnClickListener {
            boxDrawingView.clearAllBoxes()
        }
    }

    // PDF Renderer 열기
    private fun openRenderer() {
        val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(fileDescriptor)
    }

    // 특정 페이지를 표시
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
        boxDrawingView.setImageBitmap(bitmap)

        // 현재 페이지 번호 업데이트
        pdfPageNumber.text = "${index + 1}/${pdfRenderer.pageCount}"
    }

    // 이전 페이지 보이기
    private fun showPreviousPage() {
        if (currentPageIndex > 0) {
            currentPageIndex--
            boxDrawingView.setCurrentPageIndex(currentPageIndex)
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
            boxDrawingView.setCurrentPageIndex(currentPageIndex)
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
    }

    //이전버튼 누르면 창 없어지기
    override fun onBackPressed() {
        finish()
    }
}