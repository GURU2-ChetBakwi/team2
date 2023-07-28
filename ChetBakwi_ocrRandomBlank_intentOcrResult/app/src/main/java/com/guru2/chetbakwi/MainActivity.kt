package com.guru2.chetbakwi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private val REQUEST_READ_EXTERNAL_STORAGE = 1000

    // Firebase Storage 인스턴스 가져오기
    private val storage = Firebase.storage
    private val storageReference = storage.reference

    //pdf 파일이름과 Uri 리스트, pdf 개수 !!!
    var pdfList = arrayListOf<String>()
    var pdfUriList = arrayListOf<Uri>()
    var pdfCount = 0
    var tempName = ""

    // pdf를 uri 형태로 저장할 변수
    private var selectedPdfUri: Uri? = null
    // 파일 선택 액티비티의 결과 처리
    private val pdfPicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // 사용자가 파일을 선택하고 결과가 OK인 경우, 선택한 파일의 URI를 가져오기
                val uri = result.data?.data
                // 선택한 파일의 URI를 사용하여 처리 함수 호출
                selectedPdfUri = uri

                //pdf 버튼 이름변경 !!!
                updatePdfFile(uri!!)
            }
        }

    // pdf 파일 로컬에서 찾아오는 버튼
    lateinit var btnUpload: Button
    // 저장된 pdf 파일을 열어보는 버튼
    lateinit var btnOpen: Button

    //pdf 파일 firebase에서 찾아오는 버튼 !!!
    lateinit var btnPdf1: Button
    lateinit var btnPdf2: Button
    lateinit var btnPdf3: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Pdf List 설정 !!!
        for(i in 1 until 4){
            pdfList.add("1")
            pdfUriList.add("1".toUri())
        }

        // firebase 초기화
        FirebaseApp.initializeApp(this)

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

        //pdf 파일이름 버튼 !!!
        btnPdf1 = findViewById(R.id.pdfBtn1)
        btnPdf2 = findViewById(R.id.pdfBtn2)
        btnPdf3 = findViewById(R.id.pdfBtn3)

        // pdf 파일 찾아오는 버튼 클릭 시
        btnUpload.setOnClickListener {
            // 로컬 파일 내에서 원하는 pdf 파일 선택하면 파일의 경로 반환
            openFilePicker()
        }

        // pdf 파일을 여는 버튼 클릭 시
        btnOpen.setOnClickListener {
            //리스트 확인 코드
            Log.e("!!!pdf",pdfList.toString())
            Log.e("!!!pdfUri",pdfUriList.toString())

            pdfViewOpen(selectedPdfUri!!)

            // firebase 에 저장 --------------------------------------------------------------->> 기능상 btnUpload.setOnClickListener로 옮겨야 할 함수
            uploadPDFToFirebaseStorage(selectedPdfUri!!)
        }

        //버튼누르면 파일이름에 해당하는 열기 !!!
        btnPdf1.setOnClickListener {
            val pdfs = 0
            if(pdfList[pdfs]!="1"){
                downloadPDFToFirebaseStorage(pdfUriList[pdfs]!!)
                pdfViewOpen(pdfUriList[pdfs])
            }
        }
        btnPdf2.setOnClickListener {
            val pdfs = 1
            if(pdfList[pdfs]!="1"){
                downloadPDFToFirebaseStorage(pdfUriList[pdfs]!!)
                pdfViewOpen(pdfUriList[pdfs])
            }
        }
        btnPdf3.setOnClickListener {
            val pdfs = 2
            if(pdfList[pdfs]!="1"){
                downloadPDFToFirebaseStorage(pdfUriList[pdfs]!!)
                pdfViewOpen(pdfUriList[pdfs])
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

    // firebase 에 pdf upload 하는 함수
    private fun uploadPDFToFirebaseStorage (uri: Uri) {
        // firebase storage upload path setting
        // 선택된 파일 URI를 사용하여 파일의 이름 가져오기
        val fileName = getFileName(uri)
        val pdfRef: StorageReference = storageReference.child("uploadedPDF/$fileName")

        // firebase storage에 저장
        pdfRef.putFile(uri)
            .addOnSuccessListener {
                Log.d("uploadPDFToFirebaseStorage", "PDF 업로드 성공")
            }
            .addOnFailureListener { exception ->
                Log.d("uploadPDFToFirebaseStorage", "PDF 업로드 실패 : $exception")
            }
    }

    // firebase에서 pdf download 하는 함수
    private fun downloadPDFToFirebaseStorage (uri: Uri) {
        val fileName = getFileName(uri)
        val pdfRef: StorageReference = storageReference.child("uploadedPDF/$fileName")
        // firebase storage에 저장
        pdfRef.getFile(uri)
            .addOnSuccessListener {
                Log.d("downloadPDFToFirebaseStorage", "PDF 다운로드 성공")
            }
            .addOnFailureListener { exception ->
                Log.d("downloadPDFToFirebaseStorage", "PDF 다운로드 실패 : $exception")
            }

        val intent = Intent(this, pdfViewPage::class.java)
        intent.data = uri

        // pdfViewPage로 넘어가기
        startActivity(intent)
    }

    //pdf 버튼 파일이름 바꾸는 함수 !!!
    fun updatePdfFile(uri : Uri){
        pdfUriList.set(pdfCount, uri!!) //Uri저장 set(0,"")
        tempName = getFileName(pdfUriList[pdfCount]!!).toString()
        pdfList.set(pdfCount, tempName) //파일이름저장
        if(pdfCount==0){
            btnPdf1.setText(pdfList[pdfCount]) //파일이름으로 바꾸기
            pdfCount ++
        }else if(pdfCount==1){
            btnPdf2.setText(pdfList[pdfCount]) //파일이름으로 바꾸기
            pdfCount ++
        }else if(pdfCount==2){
            btnPdf3.setText(pdfList[pdfCount]) //파일이름으로 바꾸기
            pdfCount = 0
        }
    }

    //해당 Uri를 pdfViewPage로 화면에 띄우는 함수 !!!
    fun pdfViewOpen(uri: Uri)  {
        val intent = Intent(this, pdfViewPage::class.java)
        intent.data = uri


        print("")
        // pdfViewPage로 넘어가기
        startActivity(intent)
    }

}
