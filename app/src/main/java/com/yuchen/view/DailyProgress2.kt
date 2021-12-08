package com.yuchen.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class DailyProgress2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var pointCount: Int = 0
    private var progress: Float = 0f

    private var progressLinePointHeightRatio = 0.5f

    private var progressEndColor = Color.RED
    private var progressStartColor = Color.YELLOW
    private var progressBackgroundColor = Color.parseColor("#80000000")

    private val paint = Paint()

    private val srcMode = PorterDuffXfermode(PorterDuff.Mode.SRC)
    private val srcInMode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { canvas ->

            val progressLineHeight = height.toFloat() * progressLinePointHeightRatio
            val progressPointRadius = height.toFloat()/2.0f

            // -----Draw progress shape-----START
            val backLayer = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

            paint.reset()
            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            paint.xfermode = srcMode

            // use antiAlias draw bar corner round shape and point
            paint.isAntiAlias = true
            canvas.drawCircle(progressLineHeight / 2f, height.toFloat() / 2f, progressLineHeight / 2f, paint)
            canvas.drawCircle(width.toFloat() - progressLineHeight / 2f, height.toFloat() / 2f, progressLineHeight / 2f, paint)
            for (i in 1 .. pointCount) {
                val pointPosition = width.toFloat() * (i.toFloat() / (pointCount + 1).toFloat())
                canvas.drawCircle(pointPosition, height / 2.0f, progressPointRadius, paint)
            }
            // don't use antiAlias draw bar line
            paint.isAntiAlias = false
            canvas.drawRect(
                progressLineHeight / 2f,
                height.toFloat() / 2f - progressLineHeight / 2f,
                width.toFloat() - progressLineHeight / 2f,
                height.toFloat() / 2f + progressLineHeight / 2f,
                paint
            )
            // -----Draw progress shape-----End

            //Draw remain progress background color
            paint.reset()
            paint.style = Paint.Style.FILL
            paint.color = progressBackgroundColor
            paint.xfermode = srcInMode
            canvas.drawRect(width.toFloat() * progress, 0f, width.toFloat(), height.toFloat(), paint)

            //Draw fill progress color
            canvas.clipRect(0f, 0f, width.toFloat() * progress, height.toFloat())
            paint.reset()
            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(
                0f,
                0f,
                width.toFloat(),
                0f,
                progressStartColor,
                progressEndColor,
                Shader.TileMode.CLAMP
            )
            paint.xfermode = srcInMode
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            canvas.restoreToCount(backLayer)
        }
    }

    fun setPointCount(pointCount: Int) {
        this.pointCount = pointCount
        invalidate()
    }

    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }
}