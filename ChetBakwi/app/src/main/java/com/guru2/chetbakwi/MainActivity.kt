package com.guru2.chetbakwi

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val REQUEST_READ_EXTERNAL_STORAGE = 1000

    // 파일 선택 액티비티의 결과 처리
    private val pdfPicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // 사용자가 파일을 선택하고 결과가 OK인 경우, 선택한 파일의 URI를 가져오기
                val uri = result.data?.data
                // 선택한 파일의 URI를 사용하여 처리 함수 호출
                uri?.let {
                    handleSelectedFile(it)
                }
            }
        }
    // pdf를 uri 형태로 저장할 변수
    private var selectedPdfUri: Uri? = null

    // pdf 파일 로컬에서 찾아오는 버튼
    lateinit var btnUpload: Button
    // 저장된 pdf 파일을 열어보는 버튼
    lateinit var btnOpen: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 권한 허용 여부 확인
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        )

        // 권한이 허용되지 않음
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                //이전에 거부한 적이 있으면 설명 (경고)
                var dlg = AlertDialog.Builder(this)
                dlg.setTitle("권한이 필요한 이유")
                dlg.setMessage("파일 정보를 얻기 위해서는 외부 저장소 권한이 필수로 필요합니다")
                dlg.setPositiveButton("확인") { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_READ_EXTERNAL_STORAGE
                    )
                }
                dlg.setNegativeButton("취소", null)
                dlg.show()
            } else {
                // 처음 권한 요청
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_EXTERNAL_STORAGE
                )
            }

        // pdf 파일 로컬에서 찾아오는 버튼
        btnUpload = findViewById<Button>(R.id.btnUpload)
        // uri 로 저장된 pdf 파일을 열어보는 버튼
        btnOpen = findViewById<Button>(R.id.btnOpen)

        // pdf 파일 찾아오는 버튼 클릭 시
        btnUpload.setOnClickListener {
            // 로컬 파일 내에서 원하는 pdf 파일 선택하면 파일의 경로 반환
            openFilePicker()
        }

        // pdf 파일을 여는 버튼 클릭 시
        btnOpen.setOnClickListener {
            selectedPdfUri?.let {
                openPdfFile(it)
            }
        }
    }

    // pdf 파일을 선택하는 함수
    private fun openFilePicker() {
        // 파일 선택기를 열기 위해 ACTION_OPEN_DOCUMENT 인텐트 사용
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            // 파일 형식을 pdf로 제한
            type = "application/pdf"
        }
        pdfPicker.launch(intent)
    }

    private fun handleSelectedFile(uri: Uri) {
        // 선택된 파일 URI를 사용하여 파일의 이름 가져오기
        val displayName = getFileName(uri)
        // 선택된 파일 URI를 사용하여 실제 경로 가져오기
        val pdfPath = uri.toString()

        // 제대로 pdf 선택되었는지 확인용 print문
        //println(displayName)
        //println(pdfPath)

        // pdf를 Uri 타입 변수에 저장
        selectedPdfUri = uri
    }

    // 파일의 경로와 이름을 가져오기 위한 함수
    private fun getFileName(uri: Uri): String? {
        // contentResolver를 사용하여 파일 이름을 쿼리
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            // 쿼리 결과로부터 "DISPLAY_NAME" 컬럼의 인덱스를 가져오기
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            // 커서를 첫 번째 레코드로 이동
            cursor.moveToFirst()
            // 파일 이름을 가져와서 반환
            return cursor.getString(nameIndex)
        }
        // 파일 이름에 null을 반환할 수 있으므로 null 처리를 고려하여 null 반환하기
        return null
    }

    // pdf 파일을 열어보는 함수
    private fun openPdfFile(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        // PDF 뷰어 앱을 호출하여 선택한 PDF 파일 열기
        startActivity(intent)
    }
}
