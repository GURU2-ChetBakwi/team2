package com.guru2.chetbakwi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView

class BoxDrawingView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {
    // 박스를 그리기 위한 Paint 객체
    private val boxPaint: Paint = Paint()

    //tvBoxCount를 멤버변수로 선언
    private lateinit var tvBoxCount: TextView

    // 페이지별로 생성된 박스들을 저장하는 리스트
    private val boxesByPage: MutableList<MutableList<RectF>> = mutableListOf()
    private var currentPageIndex: Int = 0

    // 추가!! 랜덤으로 생성된 상자를 저장할 목록
    private val randomBoxes: MutableList<RectF> = mutableListOf()

    // Setter method for tvBoxCount
    fun setTvBoxCount(tvBoxCount: TextView) {
        this.tvBoxCount = tvBoxCount
    }

    init {
        // 박스의 스타일과 두께, 색상 설정
        boxPaint.style = Paint.Style.FILL_AND_STROKE
        boxPaint.strokeWidth = 5f
        boxPaint.color = Color.RED
        boxesByPage.add(mutableListOf())
    }

    // View의 그리기 메서드를 오버라이드하여 박스 그리기
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 추가!! 랜덤 박스 그리기
        randomBoxes.forEach { box ->
            canvas.drawRect(box, boxPaint)
        }

        // 현재 페이지 인덱스에 해당하는 박스들만 그림
        val currentPageBoxes = boxesByPage.getOrNull(currentPageIndex)
        currentPageBoxes?.forEach { box ->
            canvas.drawRect(box, boxPaint)
        }

        // 추가!! 랜덤 박스 그리기
        randomBoxes.forEach { box ->
            canvas.drawRect(box, boxPaint)
        }

        // 추가!! 상자의 총 개수 계산(사용자가 그린 + 랜덤)
        val totalBoxCount = (currentPageBoxes?.size ?: 0) + randomBoxes.size
        tvBoxCount.text = "남은 빈칸 수: $totalBoxCount"

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 터치 좌표 얻기
                val x = event.x
                val y = event.y

                // 현재 페이지 인덱스에 해당하는 상자만 가져옴
                val currentPageBoxes = boxesByPage.getOrNull(currentPageIndex)

                if (currentPageBoxes != null) {
                    // 이미 생성된 박스 중에 선택된 박스가 있는지 확인
                    var selectedBoxIndex = -1

                    // 먼저 사용자가 그린 상자를 확인
                    for (i in currentPageBoxes.size - 1 downTo 0) {
                        if (currentPageBoxes[i].contains(x, y)) {
                            selectedBoxIndex = i
                            break // 최상위 상자를 찾고 break 문으로 종료
                        }
                    }

                    if (selectedBoxIndex != -1) {
                        // 선택된 박스가 있으면 해당 박스를 삭제
                        currentPageBoxes.removeAt(selectedBoxIndex)
                    } else {
                        // 터치 이벤트가 임의로 생성된 상자에 속하는지 확인
                        var selectedRandomBoxIndex = -1
                        for (i in randomBoxes.size - 1 downTo 0) {
                            if (randomBoxes[i].contains(x, y)) {
                                selectedRandomBoxIndex = i
                                break // 최상위 상자를 찾고 break 문으로 종료
                            }
                        }

                        if (selectedRandomBoxIndex != -1) {
                            // 선택된 임의의 상자가 있으면 해당 상자를 삭제
                            randomBoxes.removeAt(selectedRandomBoxIndex)
                        } else {
                            // 새로운 박스 생성
                            val box = RectF(x, y, x + 100, y + 100) // Create a box with width and height of 100
                            currentPageBoxes.add(box)
                        }
                    }
                } else {
                    // 현재 페이지에 박스 리스트가 없다면 새로 생성
                    val newBoxesList = mutableListOf<RectF>()
                    val box = RectF(x, y, x + 100, y + 100) // Create a box with width and height of 100
                    newBoxesList.add(box)
                    boxesByPage.add(newBoxesList)
                }
                // 화면을 다시 그리기 위해 호출
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                // 드래그할 때 상자 크기 조정
                val currentPageBoxes = boxesByPage.getOrNull(currentPageIndex)
                if (currentPageBoxes != null && currentPageBoxes.isNotEmpty()) {
                    val lastBox = currentPageBoxes.last()

                    // 드래그하는 위치로 상자의 끝점을 업데이트합니다.
                    lastBox.right = event.x
                    lastBox.bottom = event.y

                    // 화면을 다시 그리기 위해 호출
                    invalidate()
                }
            }
        }
        return true
    }

    // 현재 페이지 인덱스 설정
    fun setCurrentPageIndex(index: Int) {
        currentPageIndex = index
    }

    // 박스들을 모두 지우고 화면을 다시 그리도록 호출하는 함수
    fun clearBoxes() {
        val currentPageBoxes = boxesByPage.getOrNull(currentPageIndex)

        if (currentPageBoxes != null && currentPageBoxes.isEmpty() && randomBoxes.isEmpty()) {
            // 드래그로 생성된 박스가 없다면 토스트 메시지를 출력
            Toast.makeText(context, "지울 상자가 없습니다.", Toast.LENGTH_SHORT).show()
        } else {
            // 드래그하여 생성된 상자를 지웁니다.
            currentPageBoxes?.clear()

            // 이전에 무작위로 생성된 빈 상자를 제거합니다.
            randomBoxes.clear()

            // 화면을 다시 그리기 위해 호출
            invalidate()
        }
        updateBoxCount()
    }

    // 페이지를 넘길 때 랜덤 박스 초기화
    fun clearRandomBoxes() {
        randomBoxes.clear()
        invalidate()
    }

    // 페이지별 박스 리스트 초기화
    fun clearAllBoxes() {
        boxesByPage.clear()
        randomBoxes.clear()
        invalidate()
    }

    // 랜덤한 위치에 박스 생성하는 함수
// 랜덤한 위치에 박스 생성하는 함수
    fun addRandomBoxes(count: Int) {
        // 이전 박스들을 모두 제거
        randomBoxes.clear()

        // PDF 화면의 크기 가져오기
        val pdfViewWidth = width.toFloat()
        val pdfViewHeight = height.toFloat()

        // 입력된 개수만큼 랜덤한 박스 생성
        for (i in 1..count) {
            // 랜덤한 x, y 좌표를 생성
            val randomX = (0..((pdfViewWidth - 100).toInt())).random().toFloat()
            val randomY = (0..((pdfViewHeight - 620).toInt())).random().toFloat() + 300f

            // 빨간색 랜덤 박스 추가
            val box = RectF(randomX, randomY, randomX + 100, randomY + 20)
            boxPaint.color = Color.RED // Set box color to red
            randomBoxes.add(box)
            updateBoxCount()
        }

        // 화면을 다시 그리도록 호출
        invalidate()
    }
    private fun updateBoxCount() {
        // Calculate the total number of boxes (drawn by user + random)
        val currentPageBoxes = boxesByPage.getOrNull(currentPageIndex)
        val totalBoxCount = (currentPageBoxes?.size ?: 0) + randomBoxes.size
        tvBoxCount.text = "남은 빈칸 수 : $totalBoxCount"
    }
}