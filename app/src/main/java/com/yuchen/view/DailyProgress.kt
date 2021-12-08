package com.yuchen.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class DailyProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progressTotalCount: Int = 1
    private var progressActualCount: Int = 0

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

            // -----Draw bottom shape-----START
            val backLayer = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

            paint.reset()
            paint.style = Paint.Style.FILL
            paint.color = progressBackgroundColor
            paint.xfermode = srcMode

            // use antiAlias draw bar corner round shape and point
            paint.isAntiAlias = true
            canvas.drawCircle(progressLineHeight / 2f, height.toFloat() / 2f, progressLineHeight / 2f, paint)
            for (i in 1..progressTotalCount) {
                val pointPosition = (width.toFloat() / progressTotalCount.toFloat()) * i - progressPointRadius
                canvas.drawCircle(pointPosition, height / 2.0f, progressPointRadius, paint)
            }
            // don't use antiAlias draw bar line
            paint.isAntiAlias = false
            canvas.drawRect(
                progressLineHeight / 2f,
                height.toFloat() / 2f - progressLineHeight / 2f,
                width.toFloat() - progressPointRadius,
                height.toFloat() / 2f + progressLineHeight / 2f,
                paint
            )
            canvas.restoreToCount(backLayer)
            // -----Draw bottom shape-----END

            // -----Draw fill shape-----START
            if (progressTotalCount > 0 && progressActualCount > 0 && progressTotalCount >= progressActualCount) {
                val fillLayer = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

                paint.reset()
                paint.style = Paint.Style.FILL
                paint.color = Color.WHITE
                paint.isAntiAlias = true

                for (i in 1..progressActualCount) {
                    val pointPosition = (width.toFloat() / progressTotalCount.toFloat()) * i - progressPointRadius
                    canvas.drawCircle(pointPosition, height / 2.0f, progressPointRadius, paint)
                }
                canvas.drawCircle(progressLineHeight / 2f, height.toFloat() / 2f, progressLineHeight / 2f, paint)

                paint.isAntiAlias = false
                canvas.drawRect(
                    progressLineHeight / 2f,
                    height.toFloat() / 2f - progressLineHeight / 2f,
                    width.toFloat() * progressActualCount / progressTotalCount - progressPointRadius,
                    height.toFloat() / 2f + progressLineHeight / 2f,
                    paint
                )

                paint.reset()
                paint.style = Paint.Style.FILL
                paint.shader = LinearGradient(0f, 0f, width.toFloat(), 0f, progressStartColor, progressEndColor, Shader.TileMode.CLAMP)
                paint.xfermode = srcInMode

                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                canvas.restoreToCount(fillLayer)
            }
            // -----Draw fill shape-----END
        }
    }

    fun setCount(actualCount: Int, totalCount: Int) {
        this.progressActualCount = actualCount
        this.progressTotalCount = totalCount
        invalidate()
    }
}