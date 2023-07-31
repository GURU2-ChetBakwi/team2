package com.guru2.chetbakwi

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class PdfViewOcr : AppCompatActivity() {
    private lateinit var updatedSpannableString: SpannableString
    private lateinit var originalSpannableString: SpannableString
    private lateinit var text : String
    // 메모 editText, 메모 초기화 버튼
    private lateinit var editTextMemo: EditText
    private lateinit var btnMemoReset: Button
    // 남은 빈칸 수 세는 텍스트뷰
    private lateinit var remainBlankTextView : TextView
    // 빈칸 수
    private var replacedWordCount = 0
    // 빈칸 전부 없애기 버튼
    private lateinit var btnRefresh: Button
    // pdf 보기 버튼 생성
    private lateinit var btnToPdf : Button
    // 랜덤 빈칸 생성 버튼
    private lateinit var btnRandomBlank: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfocr)

        //intent ocr결과 넘겨받기
        text = intent.getStringExtra("textOCR")!!

        // 레이아웃에서 TextView와 Button을 찾기
        val textView = findViewById<TextView>(R.id.ocrTextView)

        btnRandomBlank = findViewById(R.id.btnRandomBlank)
        btnToPdf = findViewById(R.id.btnToPdf)
        remainBlankTextView = findViewById(R.id.remainBlankTextView)
        btnRefresh = findViewById(R.id.btnRefresh)
        editTextMemo = findViewById(R.id.editTextMemo)
        btnMemoReset = findViewById(R.id.btnMemoReset)

        // 메모 리셋 버튼 클릭 리스너 설정
        btnMemoReset.setOnClickListener {
            editTextMemo.setText("")
        }

        // 초기 텍스트 설정
        originalSpannableString = SpannableString(text)

        // 클릭 가능한 링크를 생성하는 SpannableString 생성
        updatedSpannableString = SpannableString(text)

        // 한 개 이상의 문자로 이루어진 단어 찾기 (정규식 패턴)
        val regex = "\\w+".toRegex()
        val matches = regex.findAll(text)

        // 각 단어를 클릭 가능한 링크로 설정하기
        for (match in matches) {
            val word = match.value
            val startIndex = match.range.first
            val endIndex = match.range.last + 1

            // 클릭 가능한 링크를 만들기 위한 ClickableSpan 생성
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // 단어를 클릭하면 토글 함수 호출하여 단어를 바꾸기
                    toggleWord(widget as TextView, word, startIndex, endIndex)
                }
                override fun updateDrawState(ds: TextPaint) {
                    // 클릭 가능한 링크의 스타일을 설정합니다.
                    ds.isUnderlineText = false // 밑줄 제거
                    ds.color = Color.BLACK // 클릭 가능한 링크의 색상을 파란색으로 설정
                }
            }
            // 클릭 가능한 링크를 해당 단어에 적용
            updatedSpannableString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        // 텍스트뷰에 클릭 가능한 링크가 포함된 SpannableString 설정
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.text = updatedSpannableString

        // pdf 페이지 보기 버튼 클릭 리스너 설정
        btnToPdf.setOnClickListener {
            finish()
        }

        // 빈칸 전부 없애기 버튼 클릭 리스너 설정
        btnRefresh.setOnClickListener {
            // 기존 빈칸 제거 후 업데이트된 SpannableString 생성
            updatedSpannableString = SpannableString(originalSpannableString)

            // 한 개 이상의 문자로 이루어진 단어 찾기 (정규식 패턴)
            val regex = "\\w+".toRegex()
            val matches = regex.findAll(text)

            // 각 단어를 클릭 가능한 링크로 설정하기
            for (match in matches) {
                val word = match.value
                val startIndex = match.range.first
                val endIndex = match.range.last + 1

                // 클릭 가능한 링크를 만들기 위한 ClickableSpan 생성
                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        // 단어를 클릭하면 토글 함수 호출하여 단어를 바꾸기
                        toggleWord(widget as TextView, word, startIndex, endIndex)
                    }
                    override fun updateDrawState(ds: TextPaint) {
                        // 클릭 가능한 링크의 스타일을 설정합니다.
                        ds.isUnderlineText = false // 밑줄 제거
                        ds.color = Color.BLACK // 클릭 가능한 링크의 색상을 파란색으로 설정
                    }
                }
                // 클릭 가능한 링크를 해당 단어에 적용
                updatedSpannableString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            // 텍스트뷰에 클릭 가능한 링크가 포함된 SpannableString 설정
            textView.movementMethod = LinkMovementMethod.getInstance()
            textView.text = updatedSpannableString

            // 랜덤 빈칸 버튼 실행 후 만들어진 빈칸 수 세기
            replacedWordCount = 0
            remainBlankTextView.text = "$replacedWordCount"
        }

        // '랜덤 빈칸 만들기' 버튼 클릭 리스너 설정
        btnRandomBlank.setOnClickListener {
            // 기존 빈칸 제거 후 업데이트된 SpannableString 생성
            updatedSpannableString = SpannableString(originalSpannableString)

            // 랜덤으로 단어를 _으로 바꿀 개수 계산 (전체 단어 개수의 30%)
            val totalWords = countWords(text)
            val wordsToReplace = (totalWords * 0.3).toInt()

            // 랜덤 단어 인덱스 선택
            val wordsIndices = getRandomWordsIndices(totalWords, wordsToReplace)

            // 선택된 단어들을 _로 대체하여 업데이트된 SpannableString 생성
            updatedSpannableString = replaceWordsWithUnderscore(updatedSpannableString, wordsIndices)

            // 한 개 이상의 문자로 이루어진 단어 찾기 (정규식 패턴)
            val regex = "\\w+".toRegex()
            val matches = regex.findAll(text)

            // 각 단어를 클릭 가능한 링크로 설정하기
            for (match in matches) {
                val word = match.value
                val startIndex = match.range.first
                val endIndex = match.range.last + 1

                // 클릭 가능한 링크를 만들기 위한 ClickableSpan 생성
                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        // 단어를 클릭하면 토글 함수 호출하여 단어를 바꾸기
                        toggleWord(widget as TextView, word, startIndex, endIndex)
                    }
                    override fun updateDrawState(ds: TextPaint) {
                        // 클릭 가능한 링크의 스타일을 설정합니다.
                        ds.isUnderlineText = false // 밑줄 제거
                        ds.color = Color.BLACK // 클릭 가능한 링크의 색상을 파란색으로 설정
                    }
                }
                // 클릭 가능한 링크를 해당 단어에 적용
                updatedSpannableString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            // 텍스트뷰에 클릭 가능한 링크가 포함된 SpannableString 설정
            textView.movementMethod = LinkMovementMethod.getInstance()
            textView.text = updatedSpannableString

            // 랜덤 빈칸 버튼 실행 후 만들어진 빈칸 수 세기
            replacedWordCount = countReplacedWords(updatedSpannableString)
            remainBlankTextView.text = "$replacedWordCount"
        }
    }

    // 단어를 클릭하여 토글하는 함수
    private fun toggleWord(textView: TextView, word: String, startIndex: Int, endIndex: Int) {
        val newText =
            // 현재 텍스트와 클릭한 단어를 비교하여 해당 단어를 _로 대체
            if (text.substring(startIndex, endIndex) == word) {
                updatedSpannableString.replaceRange(startIndex, endIndex, "_".repeat(word.length))

            } else {    // 하거나 원래 단어로 복원
                updatedSpannableString.replaceRange(startIndex, endIndex, text.substring(startIndex, endIndex))
            }

        // 새로운 클릭 가능한 링크 적용한 SpannableString 생성
        updatedSpannableString = SpannableString(newText)

        // 업데이트된 텍스트에서 새로운 단어들을 찾아서 클릭 가능한 링크 설정
        val regex = "\\w+".toRegex()
        val matches = regex.findAll(newText.toString())

        for (match in matches) {
            val newWord = match.value
            val newStartIndex = match.range.first
            val newEndIndex = match.range.last + 1

            // 클릭 가능한 링크 만들기 위한 ClickableSpan 생성
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    toggleWord(widget as TextView, newWord, newStartIndex, newEndIndex)
                }
                override fun updateDrawState(ds: TextPaint) {
                    // 클릭 가능한 링크의 스타일을 설정합니다.
                    ds.isUnderlineText = false // 밑줄 제거
                    ds.color = Color.BLACK // 클릭 가능한 링크의 색상을 파란색으로 설정
                }
            }
            // 클릭 가능한 링크를 해당 단어에 적용
            updatedSpannableString.setSpan(clickableSpan, newStartIndex, newEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        // 텍스트뷰에 업데이트된 SpannableString 설정
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.text = updatedSpannableString

        // 랜덤 빈칸 버튼 실행 후 만들어진 빈칸 수 세기
        replacedWordCount = countReplacedWords(updatedSpannableString)
        remainBlankTextView.text = "$replacedWordCount"
    }

    // 주어진 텍스트에서 단어 개수 세는 함수
    private fun countWords(text: String): Int {
        return "\\w+".toRegex().findAll(text).count()
    }

    // 주어진 범위 내에서 랜덤한 단어 인덱스 반환 함수
    private fun getRandomWordsIndices(totalWords: Int, wordsToReplace: Int): List<Int> {
        val indices = mutableListOf<Int>()
        while (indices.size < wordsToReplace) {
            val index = Random.nextInt(totalWords) // Use Random.nextInt function to get a random integer
            if (index !in indices) {
                indices.add(index)
            }
        }
        return indices
    }

    // SpannableString 내에서 주어진 인덱스 리스트에 해당하는 단어들을 _로 대체하는 함수
    private fun replaceWordsWithUnderscore(spannableString: SpannableString, indices: List<Int>): SpannableString {
        var updatedText = spannableString.toString()
        val regex = "\\w+".toRegex()
        val matches = regex.findAll(updatedText)

        for ((i, match) in matches.withIndex()) {
            if (i in indices) {
                val startIndex = match.range.first
                val endIndex = match.range.last + 1
                updatedText = updatedText.replaceRange(startIndex, endIndex, "_".repeat(match.value.length))
            }
        }

        // 새로운 단어들에 대해 클릭 가능한 링크 설정한 SpannableString 생성
        val updatedSpannableString = SpannableString(updatedText)
        val newMatches = regex.findAll(updatedText)

        for ((i, match) in newMatches.withIndex()) {
            if (i in indices) {
                val newWord = match.value
                val newStartIndex = match.range.first
                val newEndIndex = match.range.last + 1

                // 클릭 가능한 링크 만들기 위한 ClickableSpan 생성
                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        // 단어 클릭하면 토글 함수 호출하여 단어 바꿈
                        toggleWord(widget as TextView, newWord, newStartIndex, newEndIndex)
                    }
                }
                // 클릭 가능한 링크를 해당 단어에 적용
                updatedSpannableString.setSpan(clickableSpan, newStartIndex, newEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        return updatedSpannableString
    }

    //이전버튼 누르면 창 없어지기
    override fun onBackPressed() {
        finish()
    }

    // 빈칸 수 세기 함수
    private fun countReplacedWords(text: CharSequence): Int {
        val regex = "_+".toRegex()
        val matches = regex.findAll(text)
        return matches.count()
    }
}