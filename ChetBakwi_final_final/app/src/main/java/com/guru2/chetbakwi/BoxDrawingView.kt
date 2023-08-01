package com.guru2.chetbakwi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView

class BoxDrawingView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {
    // 박스를 그리기 위한 Paint 객체
    private val boxPaint: Paint = Paint()

    // 빈칸 수를 표시하기 위한 TextView
    private lateinit var tvBoxCount: TextView

    // 현재 페이지 인덱스
    private var currentPageIndex: Int = 0

    // 페이지별로 생성된 드래그 박스들을 저장하는 리스트
    private val boxesByPage: MutableList<MutableList<RectF>> = mutableListOf()

    // 페이지별로 생성된 랜덤 상자를 저장하는 리스트
    private val randomBoxesByPage: MutableList<MutableList<RectF>> = mutableListOf()

    // tvBoxCount를 설정하는 Setter 메서드
    fun setTvBoxCount(tvBoxCount: TextView) {
        this.tvBoxCount = tvBoxCount
    }

    init {
        // 박스의 스타일과 두께, 색상 설정
        boxPaint.style = Paint.Style.FILL
        boxPaint.strokeWidth = 5f
        boxPaint.color = Color.parseColor("#FF6B6B")

        // 초기 페이지별 리스트들을 빈 리스트로 초기화
        boxesByPage.add(mutableListOf())
        randomBoxesByPage.add(mutableListOf())
    }

    // View의 그리기 메서드를 오버라이드하여 박스 그리기
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 현재 페이지 인덱스에 해당하는 박스들만 그림
        val currentPageBoxes = boxesByPage.getOrNull(currentPageIndex)
        currentPageBoxes?.forEach { box ->
            canvas.drawRoundRect(box, 20f, 20f, boxPaint) // 둥근 테두리로 그리기
        }

        // 현재 페이지 인덱스에 해당하는 랜덤 박스들만 그림
        val currentPageRandomBoxes = randomBoxesByPage.getOrNull(currentPageIndex)
        currentPageRandomBoxes?.forEach { box ->
            canvas.drawRoundRect(box, 20f, 20f, boxPaint) // 둥근 테두리로 그리기
        }

        // 상자의 총 개수 계산(사용자가 그린 + 랜덤)
        val totalBoxCount = (currentPageBoxes?.size ?: 0) + (currentPageRandomBoxes?.size ?: 0)
        tvBoxCount.text = "남은 빈칸 수: $totalBoxCount"
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 터치 좌표 얻기
                val x = event.x
                val y = event.y

                // 현재 페이지 인덱스에 해당하는 상자 리스트들 가져오기
                val currentPageBoxes = boxesByPage.getOrNull(currentPageIndex)
                val currentPageRandomBoxes = randomBoxesByPage.getOrNull(currentPageIndex)

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
                        val currentPageRandomBoxes = randomBoxesByPage.getOrNull(currentPageIndex)

                        // currentPageRandomBoxes가 null인 경우 빈 리스트로 대체
                        val currentPageBoxesList = currentPageRandomBoxes ?: emptyList()

                        var selectedRandomBoxIndex = -1
                        for (i in currentPageBoxesList.size - 1 downTo 0) {
                            if (currentPageBoxesList[i].contains(x, y)) {
                                selectedRandomBoxIndex = i
                                break // 최상위 상자를 찾고 break 문으로 종료
                            }
                        }


                        if (selectedRandomBoxIndex != -1) {
                            // 선택된 임의의 상자가 있으면 해당 상자를 삭제
                            currentPageRandomBoxes?.removeAt(selectedRandomBoxIndex)
                        } else {
                            // 새로운 박스 생성
                            val box = RectF(x, y, x + 100, y + 100)
                            currentPageBoxes.add(box)
                        }
                    }
                } else {
                    // 현재 페이지에 박스 리스트가 없다면 새로 생성
                    val newBoxesList = mutableListOf<RectF>()
                    val box = RectF(x, y, x + 100, y + 100)
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
        // 페이지 인덱스에 해당하는 랜덤 박스 리스트가 없으면 빈 리스트를 추가
        while (randomBoxesByPage.size <= index) {
            randomBoxesByPage.add(mutableListOf())
        }
        updateBoxCount()
        invalidate()
    }

    // 랜덤한 위치에 박스 생성하는 함수
    fun addRandomBoxes(count: Int) {
        // 현재 페이지에 해당하는 랜덤 박스 리스트를 가져오기
        val currentPageRandomBoxes = randomBoxesByPage.getOrNull(currentPageIndex)

        // 이전 박스들을 모두 제거
        currentPageRandomBoxes?.clear()

        // PDF 화면의 크기 가져오기
        val pdfViewWidth = width.toFloat()
        val pdfViewHeight = height.toFloat()

        // 최대 시도 횟수를 설정합니다.
        val maxAttempts = 100

        // 입력된 개수만큼 랜덤한 박스 생성
        for (i in 1..count) {
            // 랜덤한 x, y 좌표를 생성
            var randomX = (0..((pdfViewWidth - 240).toInt())).random().toFloat() + 70f
            var randomY = (0..((pdfViewHeight - 620).toInt())).random().toFloat() + 300f

            // 새로운 박스 생성
            val newBox = RectF(randomX, randomY, randomX + 100, randomY + 35)
            boxPaint.color = Color.parseColor("#FF6B6B") // 빨간색

            // 기존의 박스들과 겹치는지 확인
            var isOverlapping = false
            for (box in currentPageRandomBoxes ?: emptyList()) {
                if (RectF.intersects(box, newBox)) {
                    isOverlapping = true
                    break
                }
            }

            // 겹치지 않을 때까지 다시 랜덤한 위치에 생성
            while (isOverlapping) {
                var randomX = (0..((pdfViewWidth - 240).toInt())).random().toFloat() + 70f
                var randomY = (0..((pdfViewHeight - 620).toInt())).random().toFloat() + 300f
                val newBox = RectF(randomX, randomY, randomX + 100, randomY + 35)

                isOverlapping = false
                for (box in currentPageRandomBoxes ?: emptyList()) {
                    if (RectF.intersects(box, newBox)) {
                        isOverlapping = true
                        break
                    }
                }
            }

            // 생성된 박스를 추가
            currentPageRandomBoxes?.add(newBox)
        }

        // 상자 개수 업데이트 및 화면 다시 그리기
        updateBoxCount()
        invalidate()
    }

    // 모든 상자들을 초기화하는 함수
    fun clearAllBoxes() {
        val currentPageBoxes = boxesByPage.getOrNull(currentPageIndex)
        val currentPageRandomBoxes = randomBoxesByPage.getOrNull(currentPageIndex)

        // 현재 페이지의 박스 리스트가 비어있는지 확인
        val isBoxesEmpty = currentPageBoxes?.isEmpty() ?: true
        val isRandomBoxesEmpty = currentPageRandomBoxes?.isEmpty() ?: true

        // 만약 박스 리스트가 비어있다면 토스트 메시지
        if (isBoxesEmpty && isRandomBoxesEmpty) {
            Toast.makeText(context, "박스가 모두 초기화 되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            // 비어있지 않다면 박스 리스트를 초기화
            currentPageBoxes?.clear()
            currentPageRandomBoxes?.clear()
            invalidate()

            // 상자 개수 업데이트
            updateBoxCount()
        }
    }

    // 남은 빈칸 수를 업데이트하는 함수
    private fun updateBoxCount() {
        // 사용자 그림 상자와 랜덤 상자의 총 개수 계산
        val currentPageBoxes = boxesByPage.getOrNull(currentPageIndex)
        val currentPageRandomBoxes = randomBoxesByPage.getOrNull(currentPageIndex)
        val totalBoxCount = (currentPageBoxes?.size ?: 0) + (currentPageRandomBoxes?.size ?: 0)
        tvBoxCount.text = "남은 빈칸 수 : $totalBoxCount"
    }
}