package com.yuchen.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CircleProgressBarPercentage @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var max = 100
    private var progress: Int = 0
    private var startAngle: Float = 270f

    private val progressPaint = Paint()
    private var strokeWidth = 20f
    private var strokeColor = Color.BLACK

    private val progressRectF = RectF().apply {
        progressPaint.isAntiAlias = true
    }

    fun setProgressValue(progress: Int){
        this.progress = progress
        invalidate()
    }

    init {
        progressPaint.color = strokeColor
        progressPaint.strokeWidth = strokeWidth
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.apply {
            progressRectF.apply {
                left = strokeWidth
                top = strokeWidth
                right = width.toFloat() - strokeWidth
                bottom = height.toFloat() - strokeWidth
            }
            val sweepAngle = 360f * progress.toFloat()/max.toFloat()
            val strokeWidthOver = strokeWidth >= width/2 || strokeWidth >= height/2
            if (strokeWidthOver) {
                progressPaint.style = Paint.Style.FILL
            } else {
                progressPaint.style = Paint.Style.STROKE
            }
            drawArc(progressRectF, startAngle, sweepAngle, strokeWidthOver, progressPaint)
        }
    }
}