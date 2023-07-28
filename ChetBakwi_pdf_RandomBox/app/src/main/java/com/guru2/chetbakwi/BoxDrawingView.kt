package com.guru2.chetbakwi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView

class BoxDrawingView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {

    // 박스를 그리기 위한 Paint 객체
    private val boxPaint: Paint = Paint()

    // 페이지별로 생성된 박스들을 저장하는 리스트
    private val boxesByPage: MutableList<MutableList<RectF>> = mutableListOf()

    // 랜덤으로 생성된 상자를 저장할 목록
    private val randomBoxes: MutableList<RectF> = mutableListOf()

    private var currentPageIndex: Int = 0

    init {
        // 박스의 스타일과 두께, 색상 설정
        boxPaint.style = Paint.Style.FILL_AND_STROKE
        boxPaint.strokeWidth = 5f
        boxPaint.color = Color.RED
        boxesByPage.add(mutableListOf()) // 초기에 페이지별로 빈 박스 리스트를 추가
    }

    // View의 그리기 메서드를 오버라이드하여 박스 그리기
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 랜덤 박스 그리기
        randomBoxes.forEach { box ->
            canvas.drawRect(box, boxPaint) // randomBoxes 리스트에 있는 각각의 박스를 그림
        }

        // 현재 페이지 인덱스에 해당하는 박스들만 그림
        val currentPageBoxes = boxesByPage.getOrNull(currentPageIndex)
        currentPageBoxes?.forEach { box ->
            canvas.drawRect(box, boxPaint)  // currentPageBoxes 리스트에 있는 각각의 박스를 그림
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 터치한 좌표 얻기
                val x = event.x
                val y = event.y

                // 현재 페이지 인덱스에 해당하는 박스들만 가져옴
                val currentPageBoxes = boxesByPage.getOrNull(currentPageIndex)

                if (currentPageBoxes != null) {
                    // 이미 생성된 박스 중에서 선택한 박스가 있는지 확인
                    var selectedBoxIndex = -1

                    // 상자를 역순으로 반복(마지막 추가에서 처음으로)
                    for (i in currentPageBoxes.size - 1 downTo 0) {
                        if (currentPageBoxes[i].contains(x, y)) {
                            selectedBoxIndex = i
                            break // 최상위 상자를 찾은 후 break문으로 나가기
                        }
                    }

                    if (selectedBoxIndex != -1) {
                        // 선택한 박스가 있을 경우 해당 박스를 삭제
                        currentPageBoxes.removeAt(selectedBoxIndex)
                    } else {
                        // 새로운 박스 추가
                        val box = RectF(x, y, x + 100, y + 100) // Create a box with width and height of 100
                        currentPageBoxes.add(box)
                    }
                } else {
                    // 현재 페이지에 대한 박스 리스트가 없을 경우 새로 생성
                    val newBoxesList = mutableListOf<RectF>()
                    val box = RectF(x, y, x + 100, y + 100) // Create a box with width and height of 100
                    newBoxesList.add(box)
                    boxesByPage.add(newBoxesList)
                }
                // 화면을 다시 그리도록 호출
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                // 드래그 시 박스 크기 조정
                val currentPageBoxes = boxesByPage.getOrNull(currentPageIndex)
                if (currentPageBoxes != null && currentPageBoxes.isNotEmpty()) {
                    val lastBox = currentPageBoxes.last()

                    // 드래그하는 위치로 박스의 끝점을 업데이트
                    lastBox.right = event.x
                    lastBox.bottom = event.y

                    // 화면을 다시 그리도록 호출
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

        if (currentPageBoxes != null && currentPageBoxes.isEmpty()) {
            Toast.makeText(context, "빈칸이 모두 초기화 되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            currentPageBoxes?.clear()

            randomBoxes.clear()

            invalidate()
        }
    }

    // 페이지별 박스 리스트 초기화
    fun clearAllBoxes() {
        boxesByPage.clear()
        randomBoxes.clear()
        invalidate()
    }

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
            val randomY = (0..((pdfViewHeight - 20).toInt())).random().toFloat()

            // 빨간색 랜덤 박스 추가
            val box = RectF(randomX, randomY, randomX + 100, randomY + 20)
            boxPaint.color = Color.RED // Set box color to red
            randomBoxes.add(box)
        }

        // 화면을 다시 그리도록 호출
        invalidate()
    }
}
